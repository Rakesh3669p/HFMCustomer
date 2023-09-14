package com.hfm.customer.ui.fragments.address.adapter

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hfm.customer.R
import com.hfm.customer.databinding.ItemAddressBinding
import com.hfm.customer.databinding.ItemChatUsersBinding
import javax.inject.Inject


class ManageAddressAdapter @Inject constructor() : RecyclerView.Adapter<ManageAddressAdapter.ViewHolder>() {
    private lateinit var context: Context

    inner class ViewHolder(private val bind: ItemAddressBinding) :
        RecyclerView.ViewHolder(bind.root) {
        fun bind(data:String) {
            with(bind) {

                selectedAddressSwitch.isChecked = adapterPosition==0
                if (selectedAddressSwitch.isChecked) {
                    val colorFilter = PorterDuffColorFilter(ContextCompat.getColor(context, R.color.red), PorterDuff.Mode.SRC_IN);
                    selectedAddressSwitch.trackDrawable.colorFilter = colorFilter
                } else {
                    val colorFilter = PorterDuffColorFilter(ContextCompat.getColor(context, R.color.grey_lite), PorterDuff.Mode.SRC_IN);
                    selectedAddressSwitch.trackDrawable.colorFilter = colorFilter
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
    ): ManageAddressAdapter.ViewHolder {
        context = parent.context
        return ViewHolder(
            ItemAddressBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ManageAddressAdapter.ViewHolder, position: Int) {
        holder.bind("")
//        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int = 3

    private var onChatClick: ((id: Int) -> Unit)? = null

    fun setOnChatClickListener(listener: (id: Int) -> Unit) {
        onChatClick = listener
    }

}