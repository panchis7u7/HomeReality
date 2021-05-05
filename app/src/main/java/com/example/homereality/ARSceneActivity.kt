package com.example.homereality

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.KeyCharacterMap
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import com.example.homereality.Features.BoxMeasurements
import com.example.homereality.Features.MeasurementBox
import com.example.homereality.databinding.ActivityARSceneBinding
import com.google.ar.core.Anchor
import com.google.ar.core.ArCoreApk
import com.google.ar.core.ArCoreApk.Availability
import com.google.ar.core.ArCoreApk.InstallStatus
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Camera
import com.google.ar.sceneform.Sun
import com.google.ar.sceneform.assets.RenderableSource
import com.google.ar.sceneform.rendering.Color
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File


class ARSceneActivity : AppCompatActivity() {
    private var _binding: ActivityARSceneBinding? = null
    private val binding get() = _binding!!
    private var storage: FirebaseStorage? = null
    private var arFragment: ArFragment? = null
    private val TAG: String = ARSceneActivity::class.java.simpleName
    private val MIN_OPENGL_VERSION = 3.0

    /** Renderable constants. **/
    val CUBE_RENDABLE_RADIUS = 0.01f
    val CUBE_RENDABLE_COLOR = Color(0F, 255F, 0F, 0F)
    val CUBE_RENDABLE_SQUARE_COLOR = Color(0F, 0.05F, 0F, 0.9F)

    /** Measurement related. **/
    private lateinit var box: MeasurementBox
    private var userMeasurements: BoxMeasurements? = null
    private var measuredSelected: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityARSceneBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bottomAppBarNavigation.setOnApplyWindowInsetsListener(null)

        arFragment = supportFragmentManager.findFragmentById(R.id.fragmentARScene) as ArFragment

        var modelLocation: String? = ""
        intent?.let {
            modelLocation = it.extras?.getString("model")
        }

        storage = FirebaseStorage.getInstance()
        //Check if firebase storgage is up and ARCore is supported and up to date.
        if (storage != null && modelLocation != "") {

            var model: File = File.createTempFile("model", "glb")
            var storageRef: StorageReference = storage!!.reference.child(modelLocation!!)
            storageRef.getFile(model).addOnSuccessListener {

                Log.d("Model Recieved", "-----------------------> Model Downloaded!")
                Toast.makeText(arFragment!!.context, "Model Downloaded!", Toast.LENGTH_SHORT).show()
                arFragment!!.setOnTapArPlaneListener { hitResult, plane, motionEvent ->
                    Log.d("AR init.", "-----------------------> Setting up AR!")
                    var anchor: Anchor = hitResult.createAnchor()
                    buildModel(model, arFragment!!, anchor)
                }

            }

        } else {
            finish()
        }

    }

    private fun buildModel(model: File, arFragment: ArFragment, anchor: Anchor) {
        var renderableSource = RenderableSource
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
        Log.d("NodeGen", "-----------------------> Adding node to scene!")
        var anchorNode = AnchorNode(anchor)
        var node = TransformableNode(arFragment.transformationSystem)
        node.renderable = renderable
        node.setParent(anchorNode)
        arFragment.arSceneView.scene.addChild(anchorNode)
        node.select()
    }

    private fun setupRulerButton(){
        binding.bottomAppBarNavigation.menu.getItem(2).setOnMenuItemClickListener {
            Toast.makeText(this, "Ruler button pressed!", Toast.LENGTH_LONG).show()
            if(!measuredSelected){
                measuredSelected = true

            } else {

            }
            return@setOnMenuItemClickListener true
        }
    }

    /*private fun onClear() {
        val children = ArrayList(arFragment!!.arSceneView.scene.children)
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
        findViewById<IndicatorStayLayout>(R.id.indicator_container).visibility = View.GONE
        seekBar.setProgress(0f)

        //
        changeInfoStageToYellow()

        //
        seekBar.visibility = View.GONE
        minusButton.visibility = View.GONE
        plusButton.visibility = View.GONE
        userMeasurements = null
    }*/

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}