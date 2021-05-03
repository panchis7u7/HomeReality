package com.example.homereality

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.KeyCharacterMap
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.homereality.databinding.ActivityARSceneBinding
import com.google.ar.core.Anchor
import com.google.ar.core.ArCoreApk
import com.google.ar.core.ArCoreApk.Availability
import com.google.ar.core.ArCoreApk.InstallStatus
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.assets.RenderableSource
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
    private val TAG: String = ARSceneActivity::class.java.simpleName
    private val MIN_OPENGL_VERSION = 3.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityARSceneBinding.inflate(layoutInflater)
        //setContentView(R.layout.activity_a_r_scene)
        setContentView(binding.root)

        var arFragment: ArFragment = supportFragmentManager.findFragmentById(R.id.fragmentARScene) as ArFragment

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
                Toast.makeText(arFragment.context, "Model Downloaded!", Toast.LENGTH_SHORT).show()
                arFragment.setOnTapArPlaneListener { hitResult, plane, motionEvent ->
                    Log.d("AR init.", "-----------------------> Setting up AR!")
                    var anchor: Anchor = hitResult.createAnchor()
                    buildModel(model, arFragment, anchor)
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

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}