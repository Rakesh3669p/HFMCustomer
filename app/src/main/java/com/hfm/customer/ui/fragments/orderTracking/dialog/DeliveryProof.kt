package com.hfm.customer.ui.fragments.orderTracking.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.LinearLayout
import com.hfm.customer.R
import com.hfm.customer.databinding.AppLoaderBinding
import com.hfm.customer.databinding.DeliveredImagePopupBinding
import com.hfm.customer.databinding.PromotionPopUpBinding


class DeliveryProof(context: Context, imageUrl:String) : Dialog(context) {
    lateinit var binding: DeliveredImagePopupBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DeliveredImagePopupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window?.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        window?.setBackgroundDrawableResource(R.color.blackTrans300)
        binding.close.setOnClickListener {
            this.dismiss()
        }
    }
}