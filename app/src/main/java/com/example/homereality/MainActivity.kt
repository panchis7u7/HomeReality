package com.example.homereality

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.homereality.Adapters.RecyclerItemCategoryAdapter
import com.example.homereality.Models.FurnitureCategory
import com.example.homereality.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        //setContentView(R.layout.activity_main)
        setContentView(binding.root)

        binding.recyclerCategory.layoutManager = LinearLayoutManager(this,
                RecyclerView.VERTICAL, false)
        binding.recyclerCategory.adapter = RecyclerItemCategoryAdapter(this, populateList())

    }

    private fun populateList(): MutableList<FurnitureCategory>{
        var items: MutableList<FurnitureCategory> = mutableListOf()
        items.add(FurnitureCategory("https://img.icons8.com/ios/452/test-passed.png",
        "Prueba1"))
        items.add(FurnitureCategory("https://img.icons8.com/ios/452/test-passed.png",
                "Prueba2"))
        items.add(FurnitureCategory("https://img.icons8.com/ios/452/test-passed.png",
                "Prueba3"))
        return items
    }

}