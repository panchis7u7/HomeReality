package com.example.homereality.Adapters

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.homereality.Models.Furniture
import com.example.homereality.Modules.GlideApp
import com.example.homereality.Modules.GlideModule
import com.example.homereality.R
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.util.regex.Pattern

class RecyclerItemDepartmentAdapter(private var context: Context,
                                    private var items: MutableList<Furniture>) :
RecyclerView.Adapter<RecyclerItemDepartmentAdapter.ItemHolder>()
{
    inner class ItemHolder(itemView: View): RecyclerView.ViewHolder(itemView){

        val imageViewFurniture: ImageView = itemView.findViewById(R.id.imageViewFurniture)
        val textViewModel: TextView = itemView.findViewById(R.id.textViewFurnitureModel)
        val textViewMaterial: TextView = itemView.findViewById(R.id.textViewFurnitureMaterial)
        val textViewCost: TextView = itemView.findViewById(R.id.textViewFurnitureCost)
        val textViewDimensions: TextView = itemView.findViewById(R.id.textViewFurnitureDimensions)

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
        GlobalScope.launch {
            holder.textViewModel.text = item.model
            holder.textViewMaterial.text = item.color
            holder.textViewCost.text = "$" + item.price.toString() + "MXN"
            holder.textViewDimensions.text = item.sizes[0].toString() + " x " +
                    item.sizes[1].toString() + " x " +
                    item.sizes[2].toString()
        }

        var storage: FirebaseStorage = FirebaseStorage.getInstance()
        storage?.let {
            var imageRef: StorageReference = it.reference.child(item.images[0]!!)
            GlideApp.with(context)
                .load(imageRef)
                .into(holder.imageViewFurniture)
        }

            /* *************************** Alternative using Files!. *******************************

            val localFile: File = File.createTempFile("Furniture", "jpg")
            storage.getReference("images").child(item.images[0]!!.split("/")[1]).getFile(localFile)
                    .addOnSuccessListener {
                        var bitmap: Bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
                        holder.imageViewFurniture.setImageBitmap(bitmap)

            }**************************************************************************************/
    }

    override fun getItemCount(): Int {
        return items.size
    }

}