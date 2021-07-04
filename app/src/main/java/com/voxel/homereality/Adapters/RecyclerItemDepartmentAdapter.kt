package com.voxel.homereality.Adapters

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.voxel.homereality.ARSceneActivity
import com.voxel.homereality.Fragments.LoadingDialogFragment
import com.voxel.homereality.Interfaces.IOnClick
import com.voxel.homereality.Models.Furniture
import com.voxel.homereality.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import me.relex.circleindicator.CircleIndicator3
import java.io.File

class RecyclerItemDepartmentAdapter(private val context: Context,
                                    private val items: List<Furniture>,
                                    private val onClick: IOnClick
) :
RecyclerView.Adapter<RecyclerItemDepartmentAdapter.ItemHolder>() {
    private val loadingDialogFragment by lazy { LoadingDialogFragment() }
    private var clicked: Boolean = false

    inner class ItemHolder(itemView: View): RecyclerView.ViewHolder(itemView){

        val rotateOpen: Animation by lazy { AnimationUtils.loadAnimation(context, R.anim.rotate_open_animation)}
        val rotateClose: Animation by lazy { AnimationUtils.loadAnimation(context, R.anim.rotate_close_animation)}
        val fromBottom: Animation by lazy { AnimationUtils.loadAnimation(context, R.anim.from_bottom_animation)}
        val toBottom: Animation by lazy { AnimationUtils.loadAnimation(context, R.anim.to_bottom_animation)}

        val floatActionOptions: FloatingActionButton = itemView.findViewById(R.id.floatingButtonOptions)
        val floatActionOptionAr: FloatingActionButton = itemView.findViewById(R.id.floatingButtonOption1)
        val floatActionOptionPurchase: FloatingActionButton = itemView.findViewById(R.id.floatingButtonOption2)
        val viewPagerFurniture: ViewPager2 = itemView.findViewById(R.id.viewPagerFurniture)
        val circleIndicator: CircleIndicator3 = itemView.findViewById(R.id.circleIndicatorViewPager)
        val textViewModel: TextView = itemView.findViewById(R.id.textViewFurnitureModel)
        val textViewMaterial: TextView = itemView.findViewById(R.id.textViewFurnitureMaterial)
        val textViewCost: TextView = itemView.findViewById(R.id.textViewFurnitureCost)
        val textViewDimensions: TextView = itemView.findViewById(R.id.textViewFurnitureDimensions)
        val textViewFeatures: TextView = itemView.findViewById(R.id.textViewFeatures)

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
        val item: Furniture = items.get(position)
        holder.textViewModel.text = item.model
        holder.textViewMaterial.text = item.color
        holder.textViewCost.text = "$" + item.price.toString() + ".00 MXN"
        holder.textViewDimensions.text = item.sizes[0].toString() + " x " +
                item.sizes[1].toString() + " x " +
                item.sizes[2].toString()

        holder.textViewFeatures.text = item.details.get("description")
        holder.floatActionOptions.setOnClickListener {
            if(!clicked){
                holder.floatActionOptionAr.visibility = View.VISIBLE
                holder.floatActionOptionPurchase.visibility = View.VISIBLE
                holder.floatActionOptionAr.startAnimation(holder.fromBottom)
                holder.floatActionOptionPurchase.startAnimation(holder.fromBottom)
                holder.floatActionOptions.startAnimation(holder.rotateOpen)
            } else {
                holder.floatActionOptionAr.visibility = View.INVISIBLE
                holder.floatActionOptionPurchase.visibility = View.INVISIBLE
                holder.floatActionOptionAr.startAnimation(holder.toBottom)
                holder.floatActionOptionPurchase.startAnimation(holder.toBottom)
                holder.floatActionOptions.startAnimation(holder.rotateClose)
            }
            clicked = !clicked
        }

        holder.floatActionOptionAr.setOnClickListener { downloadModel(item) }

        holder.floatActionOptionPurchase.setOnClickListener {
            try {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(item.source)))
            } catch (e: Exception){
                Toast.makeText(context, "Product is not available!", Toast.LENGTH_LONG).show()
            }
        }

        holder.viewPagerFurniture.adapter = FurniturePager(context, item.images)
        holder.viewPagerFurniture.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        holder.circleIndicator.setViewPager(holder.viewPagerFurniture)


            /* *************************** Alternative using Files!. *******************************

            val localFile: File = File.createTempFile("Furniture", "jpg")
            storage.getReference("images").child(item.images[0]!!.split("/")[1]).getFile(localFile)
                    .addOnSuccessListener {
                        var bitmap: Bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
                        holder.imageViewFurniture.setImageBitmap(bitmap)

            }**************************************************************************************/
    }

    override fun getItemCount(): Int = items.size

    /** Download the model. **/
    private fun downloadModel(item: Furniture){

        val storage = FirebaseStorage.getInstance()
        val model: File = File.createTempFile("model", "glb")
        /*val loading = (holder.itemView.context as DepartmentActivity)
            .findViewById<ConstraintLayout>(R.id.lottieAnimationView)*/

        if (item.rendable != "") {
            onClick.resumeAnimation()
            val storageRef: StorageReference = storage.reference.child(item.rendable!!)
            storageRef.getFile(model).addOnSuccessListener {
                val bundle = Bundle()
                bundle.putSerializable("model", model)
                    bundle.putDouble("length", item.sizes[0]!!)
                    bundle.putDouble("width", item.sizes[1]!!)
                    bundle.putDouble("height", item.sizes[2]!!)
                context.startActivity(Intent(context, ARSceneActivity::class.java).putExtras(bundle))
            }
        } else {
            Toast.makeText(context, "Error downloading the model!!", Toast.LENGTH_LONG).show()
        }
    }
}