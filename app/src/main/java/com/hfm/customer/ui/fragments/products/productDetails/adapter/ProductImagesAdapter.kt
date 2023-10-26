package com.hfm.customer.ui.fragments.products.productDetails.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.hfm.customer.R
import com.hfm.customer.databinding.ItemProductImageBinding
import com.hfm.customer.ui.fragments.products.productDetails.model.Image
import com.hfm.customer.utils.replaceBaseUrl
import javax.inject.Inject


class ProductImagesAdapter @Inject constructor() :
    RecyclerView.Adapter<ProductImagesAdapter.ViewHolder>() {
    private lateinit var context: Context
    var selectedPosition = 0

    inner class ViewHolder(private val bind: ItemProductImageBinding) :
        RecyclerView.ViewHolder(bind.root) {
        fun bind(data: Image) {
            with(bind) {
                val imageOriginal = data.image
                val imageReplaced =
                    imageOriginal.replace("https://uat.hfm.synuos.com", "http://4.194.191.242")
                productImage.load(imageReplaced) {
                    placeholder(R.drawable.logo)
                }
                if (selectedPosition == absoluteAdapterPosition) {
                    mainLayout.background = ContextCompat.getDrawable(context, R.drawable.outline_line_box_red)

                } else {
                    mainLayout.background = ContextCompat.getDrawable(context, R.drawable.edt_box_smaa_radius)
                }

                root.setOnClickListener {
                    selectedPosition = absoluteAdapterPosition
                    onImageClick?.let {
                        it(replaceBaseUrl(data.image))
                    }
                    notifyDataSetChanged()
                }
            }
        }
    }

    private val diffUtil = object : DiffUtil.ItemCallback<Image>() {
        override fun areItemsTheSame(oldItem: Image, newItem: Image): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Image, newItem: Image): Boolean {
            return oldItem == newItem
        }

    }
    val differ = AsyncListDiffer(this, diffUtil)


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