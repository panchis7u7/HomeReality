package com.example.homereality.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.homereality.Models.Furniture
import com.example.homereality.R

class RecyclerItemDepartmentAdapter(private var context: Context,
                                    private var items: MutableList<Furniture>) :
RecyclerView.Adapter<RecyclerItemDepartmentAdapter.ItemHolder>()
{
    inner class ItemHolder(itemView: View): RecyclerView.ViewHolder(itemView){

        //val imageViewFurniture: ImageView = itemView.findViewById(R.id.imageViewFurniture)
        val textViewModel: TextView = itemView.findViewById(R.id.textViewFurnitureModel)

        init {

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        return ItemHolder(LayoutInflater.from(parent.context).inflate(
            R.layout.item_furniture_department_layout,
            parent,
            false
        ))
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        var item: Furniture = items.get(position)
        holder.textViewModel.text = item.model
        /*Glide.with(holder.itemView)
            .load(item.images[0])
            .into(holder.imageViewFurniture)*/
    }

    override fun getItemCount(): Int {
        return items.size
    }

}