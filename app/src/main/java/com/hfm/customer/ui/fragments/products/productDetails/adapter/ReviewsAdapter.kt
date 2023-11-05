package com.hfm.customer.ui.fragments.products.productDetails.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.hfm.customer.R
import com.hfm.customer.commonModel.Review
import com.hfm.customer.databinding.ItemReviewCommentsBinding
import com.hfm.customer.databinding.ItemVoucherBinding
import com.hfm.customer.utils.initRecyclerView
import com.hfm.customer.utils.replaceBaseUrl
import javax.inject.Inject


class ReviewsAdapter @Inject constructor() :
    RecyclerView.Adapter<ReviewsAdapter.ViewHolder>() {
    private lateinit var context: Context

    inner class ViewHolder(private val bind: ItemReviewCommentsBinding) :
        RecyclerView.ViewHolder(bind.root) {
        fun bind(data:Review) {
            with(bind) {

                userImage.load(replaceBaseUrl(data.customer_image)){
                    placeholder(R.drawable.logo)
                    error(R.drawable.logo)
                }
                val mediaAdapter = StoreProductsReviewsMediaAdapter()
                userName.text= data.customer_name
                date.text = data.review_date
                ratingBar.rating= data.rating.toFloat()
                reviewText.text = data.comment
                initRecyclerView(context,reviewMedia,mediaAdapter,true)
                mediaAdapter.differ.submitList(data.image)
                mediaAdapter.setOnItemClickListener {

                    onImageClick?.invoke(it)
                }
            }
        }
    }

      private val diffUtil = object : DiffUtil.ItemCallback<Review>(){
          override fun areItemsTheSame(oldItem: Review, newItem: Review): Boolean {
              return oldItem == newItem
          }

          override fun areContentsTheSame(oldItem: Review, newItem: Review): Boolean {
              return oldItem == newItem
          }

      }
      val differ = AsyncListDiffer(this,diffUtil)


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ReviewsAdapter.ViewHolder {
        context = parent.context
        return ViewHolder(
            ItemReviewCommentsBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ReviewsAdapter.ViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int = differ.currentList.size

    private var onImageClick: ((imageLink: String) -> Unit)? = null

    fun setOnImageClickListener(listener: (imageLink: String) -> Unit) {
        onImageClick = listener
    }

}