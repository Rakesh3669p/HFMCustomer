package com.hfm.customer.ui.fragments.chat.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hfm.customer.databinding.ItemChatBinding
import com.hfm.customer.databinding.ItemChatUsersBinding
import javax.inject.Inject


class ChatAdapter @Inject constructor() : RecyclerView.Adapter<ChatAdapter.ViewHolder>() {
    private lateinit var context: Context

    inner class ViewHolder(private val bind: ItemChatBinding) :
        RecyclerView.ViewHolder(bind.root) {
        fun bind(data: String) {
            with(bind) {
                if (absoluteAdapterPosition % 2 == 0) {
                    rightChatGroup.isVisible = true
                } else {
                    leftChatGroup.isVisible = true
                }

                warningMessage.root.isVisible = absoluteAdapterPosition == 2
            }
        }
    }

    private val diffUtil = object : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

    }
    val differ = AsyncListDiffer(this, diffUtil)


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ChatAdapter.ViewHolder {
        context = parent.context
        return ViewHolder(
            ItemChatBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ChatAdapter.ViewHolder, position: Int) {
        holder.bind("")
//        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int = 3

    private var onChatClick: ((id: Int) -> Unit)? = null

    fun setOnChatClickListener(listener: (id: Int) -> Unit) {
        onChatClick = listener
    }

}