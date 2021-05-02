package com.example.homereality

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.homereality.databinding.ActivityARSceneBinding
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.assets.RenderableSource
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.firebase.FirebaseApp
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import java.lang.Exception

class ARSceneActivity : AppCompatActivity() {
    private var _binding: ActivityARSceneBinding? = null
    private val binding get() = _binding!!
    private var storage: FirebaseStorage? = null
    private var renderable: ModelRenderable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityARSceneBinding.inflate(layoutInflater)
        //setContentView(R.layout.activity_a_r_scene)
        setContentView(binding.root)

        var model: String? = ""
        intent?.let {
            model = it.extras?.getString("model")
        }

        /*FirebaseApp.initializeApp(this)
        storage = FirebaseStorage.getInstance()
        storage?.let {
            Toast.makeText(this, "Resource: ${model}", Toast.LENGTH_LONG)
            var storageRef: StorageReference = it.getReference().child(model!!)
            var file: File = File.createTempFile("model", "glb")
            try {
                storageRef!!.getFile(file).addOnSuccessListener {
                    buildModel(file)
                    var arFragment: ArFragment =
                        supportFragmentManager.findFragmentById(R.id.fragmentARScene) as ArFragment
                    arFragment.setOnTapArPlaneListener { hitResult, plane, motionEvent ->
                        var anchorNode: AnchorNode = AnchorNode(hitResult.createAnchor())
                        anchorNode.renderable = renderable
                        arFragment.arSceneView.scene.addChild(anchorNode)
                    }
                }
            } catch (e: Exception) {
                e.stackTrace
            }
        }*/

        storage = FirebaseStorage.getInstance()
        storage?.let {
            var storageRef: StorageReference = it.reference.child(model!!)
            var file: File = File.createTempFile("model", "glb")
            storageRef.getFile(file).addOnSuccessListener {

                var renderableSource: RenderableSource = RenderableSource.builder()
                        .setSource(this, Uri.parse(file.path), RenderableSource.SourceType.GLB)
                        .setRecenterMode(RenderableSource.RecenterMode.ROOT)
                        .build()

                ModelRenderable.builder()
                        .setSource(this, renderableSource)
                        .setRegistryId(file.path)
                        .build()
                        .thenAccept { modelRenderable ->
                            Toast.makeText(this, "Model Built", Toast.LENGTH_LONG)
                            renderable = modelRenderable
                        }
                Log.d("Model", "Model Built ---------------------------------------")
                    var arFragment: ArFragment =
                            supportFragmentManager.findFragmentById(R.id.fragmentARScene) as ArFragment
                    arFragment.setOnTapArPlaneListener { hitResult, plane, motionEvent ->
                        var anchorNode: AnchorNode = AnchorNode(hitResult.createAnchor())
                        anchorNode.renderable = renderable
                        arFragment.arSceneView.scene.addChild(anchorNode)

                }
            }
        }
    }

    private fun buildModel(file: File){

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}