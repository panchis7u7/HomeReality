package com.example.homereality

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.homereality.Adapters.RecyclerItemCategoryAdapter
import com.example.homereality.Models.FurnitureCategory
import com.example.homereality.databinding.ActivityMainBinding
import java.lang.Exception


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        //setContentView(R.layout.activity_main)
        setContentView(binding.root)

        setFullScreen()

        binding.recyclerCategory.layoutManager = GridLayoutManager(applicationContext, 2,
        LinearLayoutManager.VERTICAL, false)
        binding.recyclerCategory.setHasFixedSize(false)
        binding.recyclerCategory.adapter = RecyclerItemCategoryAdapter(this, populateList())

    }

    private fun populateList(): MutableList<FurnitureCategory>{
        var items: MutableList<FurnitureCategory> = mutableListOf()
        items.add(FurnitureCategory(
                "https://drive.google.com/uc?export=view&id=1XO6o6cBh8iSX9H2X021hJX2qFfcLXfzJ",
                "Beds & Mattresses"))
        items.add(FurnitureCategory(
                "https://drive.google.com/uc?export=view&id=1Hz5Xok6tmNnV5mh9papSGsjSk-l02P6o",
                "Desks"))
        items.add(FurnitureCategory(
                "https://drive.google.com/uc?export=view&id=19l_G9g1pdcwDS5ZuKCqw5exWkB468NCi",
                "Kitchen cabinets & appliences"))
        items.add(FurnitureCategory(
                "https://drive.google.com/uc?export=view&id=17UlvxclVYq9XfJF3vFmJt88Vb3dbOFdx",
                "Chairs"))
        items.add(FurnitureCategory(
                "https://drive.google.com/uc?export=view&id=1GYy_BYSqyTM5a5yQrUk3xIaa6X9rXP2_",
                "Bathroom Storage"))
        items.add(FurnitureCategory(
                "https://drive.google.com/uc?export=view&id=1bTxvfK1cbY7fv4vZWmseOwCK3-x0yr5P",
                "Cloth Storage"))
        items.add(FurnitureCategory(
                "https://drive.google.com/uc?export=view&id=1pNFeJUKVAdKZI7m52RQrhzOr_7SYrAMV",
                "Baby and children products"))
        items.add(FurnitureCategory(
                "https://drive.google.com/uc?export=view&id=1fLxmp9GpmhCc464j0sJDZsZp23azIW76",
                "Mirrors"))
        items.add(FurnitureCategory(
                "https://drive.google.com/uc?export=view&id=1dsywUdgMQLS8vvMVuw7AmILnnj7a3E4a",
                "Outdoor Furniture"))
        items.add(FurnitureCategory(
                "https://drive.google.com/uc?export=view&id=1iT12HAL1Hg93u34CCuKsTPI1CC9iLPC1",
                "Small Storage"))
        items.add(FurnitureCategory(
                "https://drive.google.com/uc?export=view&id=1885zEnC_2XsQ5QoCHP0UtcLvg_rQhVUz",
                "Sofas & armchairs"))
        items.add(FurnitureCategory(
                "https://drive.google.com/uc?export=view&id=1hNPMXWUBI9-uvfHigkxv6FXycKcRzlqq",
                "Tables"))
        items.add(FurnitureCategory(
                "https://drive.google.com/uc?export=view&id=1BV0njjOQ8LRiDTlUGDyhN9JRO4x83emu",
                "Lightning"))
        return items
    }

    private fun setFullScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(true)
            val controller = window.insetsController
            if (controller != null) {
                controller.hide(WindowInsets.Type.navigationBars()
                        or WindowInsets.Type.captionBar())
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