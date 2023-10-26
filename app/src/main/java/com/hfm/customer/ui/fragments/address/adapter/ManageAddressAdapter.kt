package com.hfm.customer.ui.fragments.address.adapter

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hfm.customer.R
import com.hfm.customer.databinding.ItemAddressBinding
import com.hfm.customer.databinding.ItemChatUsersBinding
import com.hfm.customer.ui.fragments.address.model.Address
import com.hfm.customer.ui.fragments.address.model.AddressData
import javax.inject.Inject


class ManageAddressAdapter @Inject constructor() :
    RecyclerView.Adapter<ManageAddressAdapter.ViewHolder>() {
    private lateinit var context: Context

    inner class ViewHolder(private val bind: ItemAddressBinding) :
        RecyclerView.ViewHolder(bind.root) {
        fun bind(data: Address) {
            with(bind) {
                customerName.text = data.name.toString()
                customerAddress.text =
                    "${data.house} ${data.street},\n${data.city}, ${data.state}, ${data.country}, ${data.pincode}"
                customerMobile.text = data.phone.toString()
                selectedAddressSwitch.isChecked = data.is_default == 1

                if (selectedAddressSwitch.isChecked) {
                    val colorFilter = PorterDuffColorFilter(
                        ContextCompat.getColor(context, R.color.red),
                        PorterDuff.Mode.SRC_IN
                    )
                    selectedAddressSwitch.trackDrawable.colorFilter = colorFilter
                } else {
                    val colorFilter = PorterDuffColorFilter(
                        ContextCompat.getColor(context, R.color.grey_lite),
                        PorterDuff.Mode.SRC_IN
                    )
                    selectedAddressSwitch.trackDrawable.colorFilter = colorFilter
                }

                selectedAddressSwitch.setOnClickListener {
                    onDefaultAddressClick?.let {
                        it(data.id)
                    }
                }
                delete.setOnClickListener {
                    onDeleteClick?.let {
                        it(data.id)
                    }
                }

                edit.setOnClickListener {
                    onEditAddressClick?.let {
                        it(adapterPosition)
                    }
                }

                root.setOnClickListener {
                    onAddressClick?.invoke(data.id)
                }
            }
        }
    }

    private val diffUtil = object : DiffUtil.ItemCallback<Address>() {
        override fun areItemsTheSame(oldItem: Address, newItem: Address): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Address, newItem: Address): Boolean {
            return oldItem == newItem
        }

    }
    val differ = AsyncListDiffer(this, diffUtil)


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
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int = differ.currentList.size

    private var onDeleteClick: ((id: Int) -> Unit)? = null
    private var onEditAddressClick: ((id: Int) -> Unit)? = null
    private var onAddressClick: ((id: Int) -> Unit)? = null

    private var onDefaultAddressClick: ((id: Int) -> Unit)? = null

    fun setOnEditAddressClickListener(listener: (id: Int) -> Unit) {
        onEditAddressClick = listener
    }
    fun setOnAddressClickListener(listener: (id: Int) -> Unit) {
        onAddressClick = listener
    }

    fun setOnDefaultClickListener(listener: (id: Int) -> Unit) {
        onDefaultAddressClick = listener
    }

    fun setOnDeleteClickListener(listener: (id: Int) -> Unit) {
        onDeleteClick = listener
    }

}