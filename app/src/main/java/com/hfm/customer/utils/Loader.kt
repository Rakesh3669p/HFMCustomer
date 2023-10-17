package com.hfm.customer.utils

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.LinearLayout
import com.hfm.customer.R
import com.hfm.customer.databinding.AppLoaderBinding


class Loader(context: Context) : Dialog(context) {
    lateinit var binding: AppLoaderBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AppLoaderBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window?.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )

        window?.setBackgroundDrawableResource(R.color.trans)
    }
}