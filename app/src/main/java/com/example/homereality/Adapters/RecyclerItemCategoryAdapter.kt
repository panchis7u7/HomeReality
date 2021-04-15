package com.example.homereality.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.homereality.Models.FurnitureCategory
import com.example.homereality.R

class RecyclerItemCategoryAdapter (private var context: Context,
                                   private var items: MutableList<FurnitureCategory>) :
RecyclerView.Adapter<RecyclerItemCategoryAdapter.ItemHolder>(){

    inner class ItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        val imageViewIconCategory: ImageView = itemView.findViewById(R.id.imageViewIconCategory)
        val textViewCategoria: TextView = itemView.findViewById(R.id.textViewCategory)

        init {

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        return ItemHolder(LayoutInflater.from(parent.context).inflate(
                R.layout.item_furniture_category_layout,
                parent,
                false
        ))
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        var item: FurnitureCategory = items.get(position)
        holder.textViewCategoria.text = item.category
        Glide.with(holder.itemView)
                .load(item.icon)
                .into(holder.imageViewIconCategory)

    }

    override fun getItemCount(): Int {
        return items.size
    }

}