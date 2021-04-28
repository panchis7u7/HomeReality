package com.example.homereality

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.homereality.Adapters.RecyclerItemDepartmentAdapter
import com.example.homereality.Models.Furniture
import com.example.homereality.Models.FurnitureCategory
import com.example.homereality.databinding.ActivityDepartmentBinding
import com.google.firebase.firestore.FirebaseFirestore

class DepartmentActivity : AppCompatActivity() {
    private var _binding: ActivityDepartmentBinding? = null
    private val binding get() = _binding!!
    private var db: FirebaseFirestore? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityDepartmentBinding.inflate(layoutInflater)
        //setContentView(R.layout.activity_department)
        setContentView(binding.root)
        setFullScreen()

        var category: String? = ""
        intent?.let {
            category = it.extras?.getString("furniture")
        }

        binding.recyclerDepartment.layoutManager = LinearLayoutManager(
            this, RecyclerView.VERTICAL, false)

        var items: MutableList<Furniture> = mutableListOf()
        db = FirebaseFirestore.getInstance()
        db?.let {
            it.collection("Furniture").whereEqualTo("category", category?.toUpperCase()).get()
                .addOnSuccessListener {
                    it.documents.map { document ->
                        println("${document}")
                        items.add(
                            Furniture(
                                (document.get("category") as String),
                                (document.get("color") as String),
                                (document.get("details") as Map<String?, String?>),
                                (document.get("images") as List<String?>),
                                (document.get("model") as String),
                                (document.get("price") as Long),
                                (document.get("rendable") as String),
                                (document.get("sizes") as List<Long?>),
                                (document.get("source") as String)
                            )
                        )
                    }
                    binding.recyclerDepartment.adapter = RecyclerItemDepartmentAdapter(this, items)
                }
        }
    }

    private fun setFullScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(true)
            val controller = window.insetsController
            if (controller != null) {
                controller.hide(
                    WindowInsets.Type.navigationBars()
                        or WindowInsets.Type.captionBar())
                supportActionBar?.hide()
                controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
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
        }
    }
}