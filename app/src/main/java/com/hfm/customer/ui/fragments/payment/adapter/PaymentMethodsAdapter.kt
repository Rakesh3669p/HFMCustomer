package com.hfm.customer.ui.fragments.payment.adapter

import android.content.Context
import android.os.Build
import android.text.Html
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hfm.customer.databinding.ItemFaqBinding
import com.hfm.customer.databinding.ItemMyOrdersBinding
import com.hfm.customer.databinding.ItemPaymentMethodsBinding
import com.hfm.customer.ui.fragments.checkOut.model.PaymentMethod
import com.hfm.customer.ui.fragments.myOrders.model.Purchase
import com.hfm.customer.ui.fragments.payment.model.Faq
import com.hfm.customer.utils.formatToTwoDecimalPlaces
import com.hfm.customer.utils.initRecyclerView
import javax.inject.Inject


class PaymentMethodsAdapter @Inject constructor() :
    RecyclerView.Adapter<PaymentMethodsAdapter.ViewHolder>() {

    private lateinit var context: Context
    var selectedPosition: Int = -1

    inner class ViewHolder(private val bind: ItemPaymentMethodsBinding) :
        RecyclerView.ViewHolder(bind.root) {
        fun bind(data: PaymentMethod) {

            with(bind) {
                paymentTitle.text = data.title



                if (selectedPosition == absoluteAdapterPosition) {
                    onlinePaymentRadioBtn.isChecked = false
                    bankTransferLayout.radioBtn.isChecked = false
                    duitLayout.radioBtn.isChecked = false

                    onlinePaymentLayout.isVisible = false
                    duitLayout.root.isVisible = false
                    bankTransferLayout.root.isVisible = false

                    titleArrow.rotation = 90F
                    when (differ.currentList[selectedPosition].is_online) {
                        0 -> {
                            onlinePaymentLayout.isVisible = false
                            if (data.payment_type_id == 1) {
                                bankTransferLayout.root.isVisible = true
                            } else if (data.payment_type_id == 2) {
                                duitLayout.root.isVisible = true
                            }
                        }

                        1 -> {
                            onlinePaymentLayout.isVisible = true
                        }
                    }
                } else {
                    titleArrow.rotation = 0F
                    onlinePaymentLayout.isVisible = false
                    duitLayout.root.isVisible = false
                    bankTransferLayout.root.isVisible = false
                }

                onlinePaymentRadioBtn.setOnClickListener {
                    if (onlinePaymentRadioBtn.isChecked) {
                        onPaymentMethodSelected?.invoke(data.payment_type_id,data.is_online==1)
                    }
                }

                bankTransferLayout.radioBtn.setOnClickListener {
                    if (bankTransferLayout.radioBtn.isChecked) {
                        onPaymentMethodSelected?.invoke(data.payment_type_id,data.is_online==1)
                    }
                }

                duitLayout.radioBtn.setOnClickListener {
                    if (duitLayout.radioBtn.isChecked) {
                        onPaymentMethodSelected?.invoke(data.payment_type_id,data.is_online==1)
                    }
                }

                root.setOnClickListener {
                    onPaymentMethodSelected?.invoke(0,data.is_online==1)

                    selectedPosition = if (selectedPosition == absoluteAdapterPosition) {
                        -1
                    } else {
                        absoluteAdapterPosition
                    }
                    notifyDataSetChanged()
                }
            }
        }
    }

    private val diffUtil = object : DiffUtil.ItemCallback<PaymentMethod>() {
        override fun areItemsTheSame(
            oldItem: PaymentMethod,
            newItem: PaymentMethod
        ): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(
            oldItem: PaymentMethod,
            newItem: PaymentMethod
        ): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, diffUtil)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PaymentMethodsAdapter.ViewHolder {
        context = parent.context
        return ViewHolder(
            ItemPaymentMethodsBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: PaymentMethodsAdapter.ViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int = differ.currentList.size

    private var onOrderClick: ((id: Int) -> Unit)? = null

    fun setOnOrderClickListener(listener: (id: Int) -> Unit) {
        onOrderClick = listener
    }

    private var onPaymentMethodSelected: ((id: Int,isOnline:Boolean) -> Unit)? = null

    fun setOnPaymentMethodSelectedListener(listener: (id: Int,isOnline:Boolean) -> Unit) {
        onPaymentMethodSelected = listener
    }

}