package com.hfm.customer.utils

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.LinearLayout
import androidx.core.view.isVisible
import coil.load
import coil.request.ImageRequest
import coil.request.ImageResult
import com.hfm.customer.R
import com.hfm.customer.databinding.PromotionPopUpBinding


class PromotionBanner(context: Context, private val imageUrl: String, private val callBack: () -> Unit) : Dialog(context) {
    lateinit var binding: PromotionPopUpBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = PromotionPopUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window?.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        binding.productImage.loadImage(imageUrl)

        binding.productImage.load(imageUrl){
            target(
                onSuccess = {
                    binding.close.isVisible = true
                },
                onError = {
                    binding.close.isVisible = true
                }
            )
        }

        binding.productImage.setOnClickListener {
            callBack.invoke()
        }

        window?.setBackgroundDrawableResource(R.color.blackTrans300)

        binding.close.setOnClickListener {
            this.dismiss()
        }


    }


}