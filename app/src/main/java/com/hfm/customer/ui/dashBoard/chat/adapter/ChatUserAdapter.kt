package com.hfm.customer.ui.dashBoard.chat.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.hfm.customer.R
import com.hfm.customer.databinding.ItemChatUsersBinding
import com.hfm.customer.ui.dashBoard.chat.model.ChatList
import com.hfm.customer.utils.replaceBaseUrl
import javax.inject.Inject


class ChatUserAdapter @Inject constructor() : RecyclerView.Adapter<ChatUserAdapter.ViewHolder>() {
    private lateinit var context: Context

    inner class ViewHolder(private val bind: ItemChatUsersBinding) :
        RecyclerView.ViewHolder(bind.root) {
        fun bind(data: ChatList) {
            with(bind) {
                userImage.load(replaceBaseUrl(data.logo)){
                    placeholder(R.drawable.logo)
                    
                }
                userName.text = data.store_name
                lastMessage.text = data.last_message
                date.text = data.last_chat_date
                messageCounts.text = data.unread_msg.toString()

                root.setOnClickListener {

                    onChatClick?.let {
                        it(data)
                    }
                }
            }
        }
    }

      private val diffUtil = object : DiffUtil.ItemCallback<ChatList>(){
          override fun areItemsTheSame(oldItem: ChatList, newItem: ChatList): Boolean {
              return oldItem == newItem
          }

          override fun areContentsTheSame(oldItem: ChatList, newItem: ChatList): Boolean {
              return oldItem == newItem
          }

      }
      val differ = AsyncListDiffer(this,diffUtil)


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        context =    parent.context
        return ViewHolder(
            ItemChatUsersBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int = differ.currentList.size

    private var onChatClick: ((data: ChatList) -> Unit)? = null

    fun setOnChatClickListener(listener: (data: ChatList) -> Unit) {
        onChatClick = listener
    }

}