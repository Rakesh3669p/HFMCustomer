package com.hfm.customer.ui.fragments.products.productDetails.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.hfm.customer.databinding.ItemProductImageBinding
import com.hfm.customer.ui.fragments.products.productDetails.model.Image
import javax.inject.Inject


class ProductImagesAdapter @Inject constructor() :
    RecyclerView.Adapter<ProductImagesAdapter.ViewHolder>() {
    private lateinit var context: Context

    inner class ViewHolder(private val bind: ItemProductImageBinding) :
        RecyclerView.ViewHolder(bind.root) {
        fun bind(data:Image) {
            with(bind) {
                val imageOriginal = data.image
                val imageReplaced = imageOriginal.replace("https://uat.hfm.synuos.com", "http://4.194.191.242")
                productImage.load(imageReplaced)

                root.setOnClickListener {
                    onImageClick?.let {
                        val imageOriginal = data.image
                        val imageReplaced = imageOriginal.replace("https://uat.hfm.synuos.com", "http://4.194.191.242")
                        it(imageReplaced)
                    }
                }
            }
        }
    }

      private val diffUtil = object : DiffUtil.ItemCallback<Image>(){
          override fun areItemsTheSame(oldItem: Image, newItem: Image): Boolean {
              return oldItem == newItem
          }

          override fun areContentsTheSame(oldItem: Image, newItem: Image): Boolean {
              return oldItem == newItem
          }

      }
      val differ = AsyncListDiffer(this,diffUtil)


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ProductImagesAdapter.ViewHolder {
        context = parent.context
        return ViewHolder(
            ItemProductImageBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ProductImagesAdapter.ViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int = differ.currentList.size

    private var onImageClick: ((image: String) -> Unit)? = null

    fun setOnImageClickListener(listener: (image: String) -> Unit) {
        onImageClick = listener
    }

}