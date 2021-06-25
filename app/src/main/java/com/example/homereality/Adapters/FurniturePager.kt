package com.example.homereality.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.homereality.Modules.GlideApp
import com.example.homereality.R
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class FurniturePager(val context: Context, val images: List<String?>) :
    RecyclerView.Adapter<FurniturePager.Pager2ViewHolder>() {

        inner class Pager2ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
            val imageViewFurniture: ImageView = itemView.findViewById(R.id.imageViewFurniture)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Pager2ViewHolder {
        return Pager2ViewHolder(LayoutInflater.from(parent.context).inflate(
            R.layout.viewpager_furniture_layout, parent, false))
    }

    override fun onBindViewHolder(holder: Pager2ViewHolder, position: Int) {
        val storage: FirebaseStorage = FirebaseStorage.getInstance()
        storage.let {
            val imageRef: StorageReference = it.reference.child(images[position]!!)
            GlideApp.with(context)
                .load(imageRef)
                .into(holder.imageViewFurniture)
        }
    }

    override fun getItemCount(): Int = images.size
}