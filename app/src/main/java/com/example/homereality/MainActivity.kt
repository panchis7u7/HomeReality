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
                "https://img.icons8.com/ios/452/test-passed.png",
                "Beds & Mattresses"))
        items.add(FurnitureCategory(
                "https://img.icons8.com/ios/452/test-passed.png",
                "Desks"))
        items.add(FurnitureCategory(
                "https://img.icons8.com/ios/452/test-passed.png",
                "Kitchen cabinets & appliences"))
        items.add(FurnitureCategory(
                "https://img.icons8.com/ios/452/test-passed.png",
                "Chairs"))
        items.add(FurnitureCategory(
                "https://img.icons8.com/ios/452/test-passed.png",
                "Bathroom Storage"))
        items.add(FurnitureCategory(
                "https://img.icons8.com/ios/452/test-passed.png",
                "Cloth Storage"))
        items.add(FurnitureCategory(
                "https://www.flaticon.com/svg/vstatic/svg/3081/3081993.svg?token=exp=1618596070~hmac=b609000bdf1ee0384e20579220165eaf",
                "Baby and children products"))
        items.add(FurnitureCategory(
                "https://img.icons8.com/ios/452/test-passed.png",
                "Mirrors"))
        items.add(FurnitureCategory(
                "https://img.icons8.com/ios/452/test-passed.png",
                "Outdoor Furniture"))
        items.add(FurnitureCategory(
                "https://img.icons8.com/ios/452/test-passed.png",
                "Small Storage"))
        items.add(FurnitureCategory(
                "https://img.icons8.com/ios/452/test-passed.png",
                "Sofas & armchairs"))
        items.add(FurnitureCategory(
                "https://img.icons8.com/ios/452/test-passed.png",
                "Tables"))
        items.add(FurnitureCategory(
                "https://img.icons8.com/ios/452/test-passed.png",
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