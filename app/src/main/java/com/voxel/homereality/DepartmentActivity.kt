package com.voxel.homereality

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.voxel.homereality.Adapters.RecyclerItemDepartmentAdapter
import com.voxel.homereality.Interfaces.IOnClick
import com.voxel.homereality.Models.Furniture
import com.voxel.homereality.databinding.ActivityDepartmentBinding
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class DepartmentActivity : AppCompatActivity(), IOnClick {
    private var _binding: ActivityDepartmentBinding? = null
    private val binding get() = _binding!!
    private var db: FirebaseFirestore? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityDepartmentBinding.inflate(layoutInflater)
        //setContentView(R.layout.activity_department)
        setContentView(binding.root)

        var category: String? = ""
        intent?.let {
            category = it.extras?.getString("furniture")
        }

        supportActionBar!!.title = category
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        setFullScreen()
        binding.recyclerDepartment.layoutManager = LinearLayoutManager(
            this@DepartmentActivity, RecyclerView.VERTICAL, false)


        ////////////////////////////////////////////////////////////////////////////////////////////////

        ////////////////////////////////////////////////////////////////////////////////////////////////
        /** Retrive all furniture based on the users category choice. **/

        val items: MutableList<Furniture> = mutableListOf()
        db = FirebaseFirestore.getInstance()
        db?.let {
            it.collection("Furniture").whereEqualTo("category",
                category?.uppercase(Locale.getDefault())
            ).get()
                .addOnSuccessListener {
                    it.documents.map { document ->
                        items.add(
                            Furniture(
                                (document.get("category") as String),
                                (document.get("color") as String),
                                (document.get("details") as Map<String?, String?>),
                                (document.get("images") as List<String?>),
                                (document.get("model") as String),
                                (document.get("price") as Long),
                                (document.get("rendable") as String),
                                (document.get("sizes") as List<Double?>),
                                (document.get("source") as String)
                            )
                        )
                    }
                    binding.recyclerDepartment.adapter = RecyclerItemDepartmentAdapter(this, items, this)
                }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////
    /** Enable back button in the navigation bar. **/

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////
    /** Enable fullscreen activity. **/

    private fun setFullScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(true)
            val controller = window.insetsController
            if (controller != null) {
                /*controller.hide(
                    WindowInsets.Type.navigationBars()
                        or WindowInsets.Type.captionBar())*/
                controller.hide(WindowInsets.Type.navigationBars())
                //supportActionBar?.hide()
                controller.systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {/*
            // All below using to hide navigation bar
            val currentApiVersion = Build.VERSION.SDK_INT
            val flags = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)

            // This work only for android 4.4+
            if (currentApiVersion >= Build.VERSION_CODES.KITKAT) {
                window.decorView.systemUiVisibility = flags
                // Code below is to handle presses of Volume up or Volume down.
                // Without this, after pressing volume buttons, the navigation bar will
                // show up and won't hide
                val decorView = window.decorView
                decorView.setOnSystemUiVisibilityChangeListener { visibility: Int ->
                    if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                        decorView.systemUiVisibility = flags
                    }
                }
            }
        */
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////

    override fun onResume() {
        super.onResume()
        pauseAnimation()
    }

    override fun resumeAnimation() {
        binding.lottieAnimationView.loadAnimation.visibility = View.VISIBLE
        binding.lottieAnimationView.animationView.resumeAnimation()
        binding.lottieAnimationView.animationView.visibility = View.VISIBLE
    }

    override fun pauseAnimation() {
        binding.lottieAnimationView.loadAnimation.visibility = View.GONE
        binding.lottieAnimationView.animationView.pauseAnimation()
        binding.lottieAnimationView.animationView.visibility = View.GONE
    }
}