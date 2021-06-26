package com.example.homereality

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.homereality.Features.*
import com.example.homereality.databinding.ActivityARSceneBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.ar.core.Anchor
import com.google.ar.core.HitResult
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Camera
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.Sun
import com.google.ar.sceneform.assets.RenderableSource
import com.google.ar.sceneform.collision.Box
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.Color
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import com.google.firebase.storage.FirebaseStorage
import com.warkiz.widget.IndicatorSeekBar
import com.warkiz.widget.IndicatorStayLayout
import com.warkiz.widget.OnSeekChangeListener
import com.warkiz.widget.SeekParams
import es.dmoral.toasty.Toasty
import java.io.File

class ARSceneActivity : AppCompatActivity() {
    private var _binding: ActivityARSceneBinding? = null
    private val binding get() = _binding!!
    private var storage: FirebaseStorage? = null
    private var arFragment: ArFragment? = null
    private var model: File? = null
    private val TAG: String = ARSceneActivity::class.java.simpleName
    private val MIN_OPENGL_VERSION = 3.0

    /** Renderable constants. **/
    val CUBE_RENDERABLE_RADIUS = 0.01f
    val CUBE_RENDERABLE_COLOR = Color(0F, 255F, 0F, 0F)
    val CUBE_RENDERABLE_SQUARE_COLOR = Color(0F, 0.05F, 0F, 0.9F)

    /** Measurement related. **/
    private lateinit var box: MeasurementBox
    private var userMeasurements: BoxMeasurements? = null
    private var measuredSelected: Boolean = false

    /** seekbar related. **/
    private var isSeeking = false
    lateinit var navigationView: NavigationView
    lateinit var seekBar: IndicatorSeekBar
    lateinit var minusButton: ImageView
    lateinit var plusButton: ImageView

    /** Furniture module related. **/
    private var furnitureRenderable: Renderable? = null
    var furnitureAnchor: Anchor? = null
    var modelLength: Double = 0.0
    var modelWidth: Double = 0.0
    var modelHeight: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityARSceneBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bottomAppBarNavigation.setOnApplyWindowInsetsListener(null)
        arFragment = supportFragmentManager.findFragmentById(R.id.fragmentARScene) as ArFragment

        intent?.let {
            model = it.extras?.get("model") as File
            modelLength = it.extras?.getDouble("length")!!
            modelWidth = it.extras?.getDouble("width")!!
            modelHeight = it.extras?.getDouble("height")!!

            modelLength /= 100.0
            modelWidth /= 100.0
            modelHeight /= 100.0
        }

        setArFragmentAction(model!!)

        setupToasty()
        setupBox()
        setSeekBar()
        onClear()
        setupClearButton()
        setupRulerButton()
    }

    private fun setArFragmentAction(model: File){
        arFragment!!.setOnTapArPlaneListener { hitResult, plane, motionEvent ->
            if (measuredSelected && box.getMeasurementStage() < MeasurementStage.LENGTH){
                val anchorNode = createAnchorNode(hitResult)

                if (box.getAnchorListSize() == 0)
                    furnitureAnchor = hitResult.createAnchor()

                box.addAnchorNode(anchorNode)

                if(box.getAnchorListSize() < 3)
                    box.addVertex(anchorNode)

                if (box.getMeasurementStage() == MeasurementStage.WIDTH){
                    box.drawLine(
                        box.getAnchorNode(MeasurementBox.PT_1),
                        box.getAnchorNode(MeasurementBox.PT_2)
                    )
                }

                if(box.getAnchorListSize() == 3){
                    box.drawSquare()
                    navigationView.visibility = View.VISIBLE
                    seekBar.visibility = View.VISIBLE
                    minusButton.visibility = View.VISIBLE
                    plusButton.visibility = View.VISIBLE
                    findViewById<IndicatorStayLayout>(R.id.indicatorStayLayout).visibility =  View.VISIBLE
                }
            } else {
                val anchor: Anchor = hitResult.createAnchor()
                buildModel(model, arFragment!!, anchor)
            }
        }
    }

    private fun buildModel(model: File, arFragment: ArFragment, anchor: Anchor) {
        val renderableSource = RenderableSource
                .builder()
                .setSource(arFragment.context, Uri.parse(model.path), RenderableSource.SourceType.GLB)
                .setRecenterMode(RenderableSource.RecenterMode.ROOT)
                .build()

        ModelRenderable.builder()
                .setSource(arFragment.context, renderableSource)
                .setRegistryId(model.path)
                .build()
                .thenAccept { modelRenderable -> addNodeToScene(arFragment, anchor, modelRenderable) }
                .exceptionally { throwable -> Toast.makeText(arFragment.context, "Error: ${throwable.message}",
                        Toast.LENGTH_LONG).show();  null}
    }

    private fun addNodeToScene(arFragment: ArFragment, anchor: Anchor, renderable: Renderable) {
        val anchorNode = AnchorNode(anchor)
        val node = TransformableNode(arFragment.transformationSystem)

        node.scaleController.isEnabled = false
        node.translationController.isEnabled = true
        node.rotationController.isEnabled = true
        node.renderable = renderable

        val boundingBox: Box = renderable.collisionShape as Box
        val renderableSize: Vector3 = boundingBox.size

        /** Update world scale. **/
        node.worldScale = Vector3(
            (modelWidth * 1.0 / renderableSize.x).toFloat(),
            (modelHeight * 1.0 / renderableSize.y).toFloat(),
            (modelLength * 1.0 / renderableSize.z).toFloat()
        )

        node.setParent(anchorNode)
        arFragment.arSceneView.scene.addChild(anchorNode)
        findViewById<FloatingActionButton>(R.id.floatingActionClear).visibility = View.VISIBLE
        node.select()
        box.arFragment = arFragment
    }

    /** Setup toasty. **/
    private fun setupToasty() {
        Toasty.Config.getInstance()
            .allowQueue(false) // optional (prevents several Toastys from queuing)
            .apply();
    }

    private fun setupBox(){
        box = MeasurementBox(
            boxRenderData = BoxRenderData(
                pointRenderRadius = CUBE_RENDERABLE_RADIUS,
                pointRenderColor = CUBE_RENDERABLE_COLOR,
                lineRenderColor = CUBE_RENDERABLE_COLOR,
                areaRenderColor = CUBE_RENDERABLE_SQUARE_COLOR
            ),
            boxInfoCardLayout = BoxInfoCardLayout(
                cardLayout = R.layout.distance_info_card_layout,
                heightCardLayout = R.layout.height_distance_info_card_layout
            ),
            context = this,
            arFragment = arFragment!!
        )
        box.setUI()
    }

    private fun setSeekBar(){
        navigationView = findViewById(R.id.navigationViewSliderSize)
        seekBar = findViewById(R.id.slider)
        seekBar.setIndicatorTextFormat("\${PROGRESS} cm")
        navigationView.visibility = View.GONE
        seekBar.visibility = View.GONE

        seekBar.onSeekChangeListener = (object : OnSeekChangeListener{
            override fun onSeeking(seekParams: SeekParams?) {
                if (isSeeking) {
                    box.setBoxHeight(seekParams!!.progress.toFloat())
                }
            }

            override fun onStartTrackingTouch(seekBar: IndicatorSeekBar?) {
                isSeeking = true
            }

            override fun onStopTrackingTouch(seekBar: IndicatorSeekBar?) {
                isSeeking = false
            }
        })

        minusButton = findViewById(R.id.imageViewDecrementHeight)
        plusButton = findViewById(R.id.imageViewIncrementHeight)
        minusButton.visibility = View.GONE
        plusButton.visibility = View.GONE

    }

    private fun setupRulerButton(){
        val clear: FloatingActionButton = findViewById(R.id.floatingActionClear)
        binding.bottomAppBarNavigation.menu.getItem(2).setOnMenuItemClickListener {
            Toast.makeText(this, "Ruler button pressed!", Toast.LENGTH_LONG).show()
            if(!measuredSelected){
                measuredSelected = true
                onClear()
                if(box.getMeasurementStage() == MeasurementStage.HEIGHT){

                }
                clear.visibility = View.VISIBLE
            } else if(measuredSelected && box.getMeasurementStage() == MeasurementStage.HEIGHT){
                userMeasurements = box.getBoxMeasurements()
                navigationView.visibility = View.GONE
                seekBar.visibility = View.GONE
                minusButton.visibility = View.GONE
                plusButton.visibility = View.GONE
                findViewById<IndicatorStayLayout>(R.id.indicatorStayLayout).visibility = View.GONE
            }
            return@setOnMenuItemClickListener true
        }
    }

    private fun setupClearButton(){
        val clearBtn: FloatingActionButton = findViewById(R.id.floatingActionClear)
        clearBtn.setOnClickListener {
            onClear()
        }
    }

    private fun onClear() {
        val children: List<Node> = ArrayList(arFragment!!.arSceneView.scene.children)
        for (node in children) {
            if (node is AnchorNode) {
                if (node.anchor != null) {
                    node.anchor!!.detach()
                }
            }
            if (node !is Camera && node !is Sun) {
                node.setParent(null)
            }
        }
        box.clear()
        findViewById<IndicatorStayLayout>(R.id.indicatorStayLayout).visibility = View.GONE
        seekBar.setProgress(0f)

        navigationView.visibility = View.GONE
        seekBar.visibility = View.GONE
        minusButton.visibility = View.GONE
        plusButton.visibility = View.GONE
        userMeasurements = null
    }

    private fun createAnchorNode(hitResult: HitResult): AnchorNode {
        val anchor = hitResult.createAnchor()
        val anchorNode = AnchorNode(anchor)
        anchorNode.setParent(arFragment!!.arSceneView.scene)
        return anchorNode
    }

    /**  Displays a message when no shape was measured. **/
    private fun showShapedMeasuredDialog() {
        val toast = Toasty.success(this, "Shape was measured!", Toast.LENGTH_SHORT, true)
        toast.setGravity(toast.gravity, toast.xOffset, toast.yOffset + 70)
        toast.show();
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}