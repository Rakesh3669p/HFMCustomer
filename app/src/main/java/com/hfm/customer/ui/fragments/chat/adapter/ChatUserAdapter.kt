package com.hfm.customer.ui.fragments.chat.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hfm.customer.databinding.ItemChatUsersBinding
import javax.inject.Inject


class ChatUserAdapter @Inject constructor() : RecyclerView.Adapter<ChatUserAdapter.ViewHolder>() {
    private lateinit var context: Context

    inner class ViewHolder(private val bind: ItemChatUsersBinding) :
        RecyclerView.ViewHolder(bind.root) {
        fun bind(data:String) {
            with(bind) {
                root.setOnClickListener {
                    onChatClick?.let {
                        it(adapterPosition)
                    }
                }
            }
        }
    }

      private val diffUtil = object : DiffUtil.ItemCallback<String>(){
          override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
              return oldItem == newItem
          }

          override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
              return oldItem == newItem
          }

      }
      val differ = AsyncListDiffer(this,diffUtil)


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ChatUserAdapter.ViewHolder {
        context =    parent.context
        return ViewHolder(
            ItemChatUsersBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ChatUserAdapter.ViewHolder, position: Int) {
        holder.bind("")
//        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int = 3

    private var onChatClick: ((id: Int) -> Unit)? = null

    fun setOnChatClickListener(listener: (id: Int) -> Unit) {
        onChatClick = listener
    }

}