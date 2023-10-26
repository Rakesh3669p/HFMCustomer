package com.hfm.customer.ui.fragments.blogs.adapter

import android.content.Context
import android.os.Build
import android.text.Html
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.hfm.customer.R
import com.hfm.customer.databinding.ItemBlogsBinding
import com.hfm.customer.databinding.ItemChatUsersBinding
import com.hfm.customer.ui.fragments.blogs.model.Blogs
import com.hfm.customer.utils.replaceBaseUrl
import javax.inject.Inject
import kotlin.concurrent.timerTask


class BlogsAdapter @Inject constructor() : RecyclerView.Adapter<BlogsAdapter.ViewHolder>() {
    private lateinit var context: Context

    inner class ViewHolder(private val bind: ItemBlogsBinding) :
        RecyclerView.ViewHolder(bind.root) {
        fun bind(data:Blogs) {
            with(bind) {
                blogImage.load(replaceBaseUrl(data.image)){
                    placeholder(R.drawable.logo)
                    
                }
                blogAuthor.text = data.author
                blogTitle.text = data.blog_title

                root.setOnClickListener {
                    onBlogClick?.let {
                        it(data.blog_id)
                    }
                }
            }
        }
    }

      private val diffUtil = object : DiffUtil.ItemCallback<Blogs>(){
          override fun areItemsTheSame(oldItem: Blogs, newItem: Blogs): Boolean {
              return oldItem == newItem
          }

          override fun areContentsTheSame(oldItem: Blogs, newItem: Blogs): Boolean {
              return oldItem == newItem
          }

      }
      val differ = AsyncListDiffer(this,diffUtil)


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BlogsAdapter.ViewHolder {
        context =    parent.context
        return ViewHolder(
            ItemBlogsBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: BlogsAdapter.ViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int = differ.currentList.size

    private var onBlogClick: ((id: Int) -> Unit)? = null

    fun setOnBlogClickListener(listener: (id: Int) -> Unit) {
        onBlogClick = listener
    }

}