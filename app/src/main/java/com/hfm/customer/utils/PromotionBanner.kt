package com.hfm.customer.utils

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.LinearLayout
import com.hfm.customer.R
import com.hfm.customer.databinding.AppLoaderBinding
import com.hfm.customer.databinding.PromotionPopUpBinding


class PromotionBanner(context: Context,imageUrl:String) : Dialog(context) {
    lateinit var binding: PromotionPopUpBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = PromotionPopUpBinding.inflate(layoutInflater)
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