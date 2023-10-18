package com.hfm.customer.ui.dashBoard.chat.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.hfm.customer.databinding.ItemChatBinding
import com.hfm.customer.ui.dashBoard.chat.model.Message
import com.hfm.customer.utils.replaceBaseUrl
import javax.inject.Inject


class ChatAdapter @Inject constructor() : RecyclerView.Adapter<ChatAdapter.ViewHolder>() {
    private lateinit var context: Context

    inner class ViewHolder(private val bind: ItemChatBinding) :
        RecyclerView.ViewHolder(bind.root) {
        fun bind(data: Message) {
            with(bind) {
                if(data.align=="right"){
                    showLeftChat(bind,false)
                    showRightChat(bind,true)
                    rightTimeAgo.text = data.chat_time
                    warning.isVisible = data.warningMessage
                    warningMessage.root.isVisible = data.warning
                    if(data.image.isNotEmpty()){
                        rightChatImage.isVisible = true
                        rightChat.isVisible = false
                        rightChatImage.load(replaceBaseUrl(data.image))
                    }else{
                        rightChatImage.isVisible = false
                        rightChat.text = data.message
                    }

                }else{
                    showLeftChat(bind,true)
                    showRightChat(bind,false)

                    if(data.image.isNotEmpty()){
                        leftChatImage.isVisible = true
                        leftChat.isVisible = false
                        leftChatImage.load(replaceBaseUrl(data.image))
                    }else{
                        leftChatImage.isVisible = false
                        leftChat.text = data.message
                    }

                    leftTimeAgo.text = data.chat_time
                    leftChat.text = data.message
                }

            }
        }
    }

    private fun showLeftChat(bind: ItemChatBinding, status: Boolean) {
        bind.leftChatImage.isVisible = status
        bind.leftChat.isVisible = status
        bind.leftTimeAgo.isVisible = status
        bind.agentImage.isVisible = status
        bind.agentName.isVisible = status
    }
    private fun showRightChat(bind: ItemChatBinding, status: Boolean) {
        bind.rightChat.isVisible = status
        bind.rightTimeAgo.isVisible = status
        bind.rightChatImage.isVisible = status
    }

    private val diffUtil = object : DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem == newItem
        }

    }
    val differ = AsyncListDiffer(this, diffUtil)


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        context = parent.context
        return ViewHolder(
            ItemChatBinding.inflate(
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

    private var onChatClick: ((id: Int) -> Unit)? = null

    fun setOnChatClickListener(listener: (id: Int) -> Unit) {
        onChatClick = listener
    }

}