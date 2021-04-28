package com.example.homereality

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.homereality.Models.FurnitureCategory
import com.example.homereality.databinding.ActivityDepartmentBinding

class DepartmentActivity : AppCompatActivity() {
    private var _binding: ActivityDepartmentBinding? = null
    private val binding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityDepartmentBinding.inflate(layoutInflater)
        //setContentView(R.layout.activity_department)
        intent?.let {
            var furniture: FurnitureCategory? = it.extras?.getParcelable("furniture") as FurnitureCategory?
            binding.textViewMSG.text = furniture?.category
        }
        setContentView(binding.root)
    }
}