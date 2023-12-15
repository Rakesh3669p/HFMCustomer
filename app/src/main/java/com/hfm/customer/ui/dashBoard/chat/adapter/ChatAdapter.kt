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
import com.hfm.customer.databinding.ItemChatBinding
import com.hfm.customer.ui.dashBoard.chat.model.Message
import com.hfm.customer.utils.loadImage
import com.hfm.customer.utils.replaceBaseUrl
import com.hfm.customer.utils.toFormattedDateTimeChat
import com.hfm.customer.utils.toTimeAgo
import com.hfm.customer.utils.toUnixTimestamp
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject


class ChatAdapter @Inject constructor() : RecyclerView.Adapter<ChatAdapter.ViewHolder>() {
    private lateinit var context: Context
    private var sellerName = ""

    inner class ViewHolder(private val bind: ItemChatBinding) :
        RecyclerView.ViewHolder(bind.root) {
        fun bind(data: Message) {
            with(bind) {
                if (absoluteAdapterPosition == 0) {
                    chatDate.isVisible = true
                    chatDate.text = data.chat_date.toFormattedDate()
                } else {
                    if (data.chat_date != differ.currentList[absoluteAdapterPosition - 1].chat_date) {
                        chatDate.isVisible = true
                        chatDate.text = data.chat_date.toFormattedDate()
                    }
                }

                if (data.align == "right") {
                    showLeftChat(bind, false)
                    showRightChat(bind, true)
                    val chatTime = data.created_at.toUnixTimestamp()
                    rightTimeAgo.text = chatTime.toFormattedDateTimeChat()
                    warning.isVisible = data.warningMessage
                    warningMessage.root.isVisible = data.warning
                    rightChat.isVisible = !data.message.isNullOrEmpty()
                    rightChat.text = data.message
                    if (data.image.isNotEmpty()) {
                        rightChatImage.isVisible = true
                        rightChatImage.loadImage(replaceBaseUrl(data.image))
                    } else {
                        rightChatImage.isVisible = false
                    }

                } else {
                    showLeftChat(bind, true)
                    showRightChat(bind, false)

                    leftChat.isVisible = !data.message.isNullOrEmpty()
                    leftChat.text = data.message
                    if (data.image.isNotEmpty()) {
                        leftChatImage.isVisible = true
                        leftChatImage.loadImage(replaceBaseUrl(data.image))
                    } else {
                        leftChatImage.isVisible = false
                    }
                    val chatTime = data.created_at.toUnixTimestamp()
                    leftTimeAgo.text = chatTime.toFormattedDateTimeChat()
                    leftChat.text = data.message
                    agentName.text = sellerName
                }

                leftChatImage.setOnClickListener { onChatImageClick?.invoke(data.image) }
                rightChatImage.setOnClickListener { onChatImageClick?.invoke(data.image) }
            }
        }
    }

    fun String.toFormattedDate(): String {
        val inputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)
        val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.US)

        val date = inputFormat.parse(this)
        return outputFormat.format(date)
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

    private var onChatImageClick: ((imageURl: String) -> Unit)? = null

    fun setOnChatImageClickListener(listener: (imageURl: String) -> Unit) {
        onChatImageClick = listener
    }

    fun setSellerName(name: String) {
        sellerName = name
    }

}