package com.voxel.homereality.Adapters

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.voxel.homereality.DepartmentActivity
import com.voxel.homereality.Models.FurnitureCategory
import com.voxel.homereality.R


class RecyclerItemCategoryAdapter (private val context: Context,
                                   private val items: List<FurnitureCategory>) :
RecyclerView.Adapter<RecyclerItemCategoryAdapter.ItemHolder>(){

    inner class ItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val imageViewIconCategory: ImageView = itemView.findViewById(R.id.imageViewIconCategory)
        val textViewCategoria: TextView = itemView.findViewById(R.id.textViewCategory)
        val lottieAnimationView: LottieAnimationView = itemView.findViewById(R.id.animationView)

        init {
            itemView.setOnClickListener {
                val position: Int = adapterPosition
                context.startActivity(Intent(context, DepartmentActivity::class.java)
                    .putExtra("furniture", items.get(position).category))
            }
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
        val item: FurnitureCategory = items.get(position)
        holder.textViewCategoria.text = item.category

        Glide.with(holder.itemView)
            .load(item.iconBlack)
            .listener(imageLoadingListener(holder.lottieAnimationView))
            .into(holder.imageViewIconCategory)
    }

    private fun imageLoadingListener(pendingImage: LottieAnimationView): RequestListener<Drawable?> {
        return object : RequestListener<Drawable?> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?, target:
                Target<Drawable?>?,
                isFirstResource: Boolean): Boolean {
                return false
            }

            override fun onResourceReady(
                resource: Drawable?,
                model: Any?,
                target: Target<Drawable?>?,
                dataSource: DataSource?,
                isFirstResource: Boolean
            ): Boolean {
                pendingImage.pauseAnimation()
                pendingImage.visibility = View.GONE
                return false
            }
        }
    }

    override fun getItemCount(): Int = items.size
}