package com.hfm.customer.ui.fragments.notifications.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hfm.customer.R
import com.hfm.customer.databinding.ItemNotificationBinding
import com.hfm.customer.ui.fragments.notifications.model.Notification
import com.hfm.customer.utils.toCustomTimeAgo
import com.hfm.customer.utils.toTimeAgo
import com.hfm.customer.utils.toUnixTimestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class NotificationAdapter @Inject constructor() : RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {
    private lateinit var context: Context

    inner class ViewHolder(private val binding: ItemNotificationBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: Notification) {

            with(binding) {
                notificationTitle.text = "${data.title}: ${data.description}"
                val timestamp = data.created_at.toUnixTimestamp()
                val timeAgo = timestamp.toCustomTimeAgo()
                time.text = timeAgo.toString()

                if(data.viewed==1){
                    notificationTitle.setTextColor(ContextCompat.getColor(context, R.color.grey))
                    time.setTextColor(ContextCompat.getColor(context, R.color.grey))
                }else{
                    notificationTitle.setTextColor(ContextCompat.getColor(context, R.color.black))
                    time.setTextColor(ContextCompat.getColor(context, R.color.textGreyDark))
                }

                root.setOnClickListener {
                    data.viewed=1
                    notifyItemChanged(absoluteAdapterPosition)
                    onItemClick?.invoke(absoluteAdapterPosition)
                }
            }
        }
    }

    private val diffUtil = object : DiffUtil.ItemCallback<Notification>() {
        override fun areItemsTheSame(oldItem: Notification, newItem: Notification): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Notification, newItem: Notification): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }

    }
    val differ = AsyncListDiffer(this, diffUtil)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        return ViewHolder(
            ItemNotificationBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(differ.currentList[position])
    override fun getItemCount(): Int = differ.currentList.size

    private var onItemClick: ((id: Int) -> Unit)? = null

    fun setOnItemClickListener(listener: (id: Int) -> Unit) {
        onItemClick = listener
    }
}