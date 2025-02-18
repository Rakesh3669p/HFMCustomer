package com.hfm.customer.ui.dashBoard.chat.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.hfm.customer.R
import com.hfm.customer.databinding.ItemChatUsersBinding
import com.hfm.customer.ui.dashBoard.chat.model.ChatList
import com.hfm.customer.utils.loadImage
import com.hfm.customer.utils.makeInvisible
import com.hfm.customer.utils.makeVisible
import com.hfm.customer.utils.replaceBaseUrl
import javax.inject.Inject


class ChatUserAdapter @Inject constructor() : RecyclerView.Adapter<ChatUserAdapter.ViewHolder>() {
    private lateinit var context: Context

    inner class ViewHolder(private val bind: ItemChatUsersBinding) :
        RecyclerView.ViewHolder(bind.root) {
        fun bind(data: ChatList) {
            with(bind) {
                userImage.loadImage(replaceBaseUrl(data.logo))
                userName.text = data.store_name
                if(!data.order_id.isNullOrEmpty()){
                    lastMessage.text = "Order #:${data.order_id}"
                }else {
                    lastMessage.text = data.last_message
                }
                date.text = data.last_chat_date
                if(data.unread_msg>0){
                    messageCounts.makeVisible()
                }else{
                    messageCounts.makeInvisible()
                }
                messageCounts.text = data.unread_msg.toString()

                root.setOnClickListener {
                    data.unread_msg = 0
                    notifyItemChanged(absoluteAdapterPosition)
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