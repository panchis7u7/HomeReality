package com.example.homereality.Adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.homereality.ARSceneActivity
import com.example.homereality.DepartmentActivity
import com.example.homereality.Fragments.LoadingDialogFragment
import com.example.homereality.Models.Furniture
import com.example.homereality.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.relex.circleindicator.CircleIndicator3
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class RecyclerItemDepartmentAdapter(private var context: Context,
                                    private var items: MutableList<Furniture>) :
RecyclerView.Adapter<RecyclerItemDepartmentAdapter.ItemHolder>()
{
    private var isLoading = false
    private val loadingDialogFragment by lazy { LoadingDialogFragment() }
    private var clicked: Boolean = false

    inner class ItemHolder(itemView: View): RecyclerView.ViewHolder(itemView){

        val rotateOpen: Animation by lazy { AnimationUtils.loadAnimation(context, R.anim.rotate_open_animation)}
        val rotateClose: Animation by lazy { AnimationUtils.loadAnimation(context, R.anim.rotate_close_animation)}
        val fromBottom: Animation by lazy { AnimationUtils.loadAnimation(context, R.anim.from_bottom_animation)}
        val toBottom: Animation by lazy { AnimationUtils.loadAnimation(context, R.anim.to_bottom_animation)}

        val floatActionOptions: FloatingActionButton = itemView.findViewById(R.id.floatingButtonOptions)
        val floatActionOptionAr: FloatingActionButton = itemView.findViewById(R.id.floatingButtonOption1)
        val floatActionOptionHelp: FloatingActionButton = itemView.findViewById(R.id.floatingButtonOption2)
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
        var item: Furniture = items.get(position)
        GlobalScope.launch {
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
                    holder.floatActionOptionHelp.visibility = View.VISIBLE
                    holder.floatActionOptionAr.startAnimation(holder.fromBottom)
                    holder.floatActionOptionHelp.startAnimation(holder.fromBottom)
                    holder.floatActionOptions.startAnimation(holder.rotateOpen)
                } else {
                    holder.floatActionOptionAr.visibility = View.INVISIBLE
                    holder.floatActionOptionHelp.visibility = View.INVISIBLE
                    holder.floatActionOptionAr.startAnimation(holder.toBottom)
                    holder.floatActionOptionHelp.startAnimation(holder.toBottom)
                    holder.floatActionOptions.startAnimation(holder.rotateClose)
                }
                clicked = !clicked
            }

            holder.floatActionOptionAr.setOnClickListener {
                downloadModel(holder, item)
            }

            holder.floatActionOptionHelp.setOnClickListener {

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
    private fun downloadModel(holder: ItemHolder, item: Furniture){

        val storage = FirebaseStorage.getInstance()
        val model: File = File.createTempFile("model", "glb")
        val loading = (holder.itemView.context as DepartmentActivity)
            .findViewById<ConstraintLayout>(R.id.load_animation)

        if (item.rendable != "") {

            val storageRef: StorageReference = storage.reference.child(item.rendable!!)
            storageRef.getFile(model).addOnSuccessListener {
                isLoading = false
                context.startActivity(Intent(context ,ARSceneActivity::class.java)
                    .putExtra("model", model)
                    .putExtra("length", item.sizes[0])
                    .putExtra("width", item.sizes[1])
                    .putExtra("height", item.sizes[2]))
            }
            isLoading = true
            loading.visibility = View.VISIBLE
        } else {
            Toast.makeText(context, "Error downloading the model!!", Toast.LENGTH_LONG).show()
        }
    }

}