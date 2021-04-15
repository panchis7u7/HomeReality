package com.example.homereality.Adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.homereality.Models.FurnitureCategory

class RecyclerItemCategoryAdapter (private var context: Context,
                                   private var items: MutableList<FurnitureCategory>) :
RecyclerView.Adapter<RecyclerItemCategoryAdapter.ItemHolder>(){

    inner class ItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        init {

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        TODO("Not yet implemented")
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        TODO("Not yet implemented")
    }

    override fun getItemCount(): Int {
        return items.size
    }

}