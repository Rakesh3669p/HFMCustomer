package com.hfm.customer.ui.fragments.products.productDetails.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hfm.customer.commonModel.Review
import com.hfm.customer.databinding.ItemReviewCommentsBinding
import com.hfm.customer.utils.initRecyclerView
import com.hfm.customer.utils.loadImage
import com.hfm.customer.utils.replaceBaseUrl
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject


class ReviewsAdapter @Inject constructor() :
    RecyclerView.Adapter<ReviewsAdapter.ViewHolder>() {
    private lateinit var context: Context

    inner class ViewHolder(private val bind: ItemReviewCommentsBinding) :
        RecyclerView.ViewHolder(bind.root) {
        fun bind(data:Review) {
            with(bind) {

                userImage.loadImage(replaceBaseUrl(data.customer_image))
                val mediaAdapter = StoreProductsReviewsMediaAdapter()
                userName.text= data.customer_name
                date.text = if(data.date!=null) convertDateFormat(data.date) else convertReviewDateFormat(data.review_date)
                ratingBar.rating= data.rating.toFloat()
                reviewTitle.text = data.title
                reviewText.text = data.comment

                initRecyclerView(context,reviewMedia,mediaAdapter,true)
                mediaAdapter.differ.submitList(data.image)

                reviewVideo.isVisible = !data.video_link.isNullOrEmpty()

                reviewVideo.setOnClickListener {
                    onVideoClick?.invoke(data.video_link.toString())
                }

                mediaAdapter.setOnItemClickListener {

                    onImageClick?.invoke(data.image,absoluteAdapterPosition)
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
    fun convertDateFormat(inputDate: String): String {
        val inputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        val date = inputFormat.parse(inputDate)
        return outputFormat.format(date!!)
    }

    fun convertReviewDateFormat(inputDate: String): String {
        val inputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        val date = inputFormat.parse(inputDate)
        return outputFormat.format(date!!)
    }

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
    override fun getItemViewType(position: Int): Int = position

    private var onImageClick: ((imageLink: List<String>,index:Int) -> Unit)? = null

    fun setOnImageClickListener(listener: (imageLink: List<String>,index:Int) -> Unit) {
        onImageClick = listener
    }

    private var onVideoClick: ((videoLink: String) -> Unit)? = null

    fun setOnVideoClickListener(listener: (videoLink: String) -> Unit) {
        onVideoClick = listener
    }

}