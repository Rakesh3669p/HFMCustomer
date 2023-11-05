package com.hfm.customer.utils

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.WindowManager
import android.widget.LinearLayout
import com.hfm.customer.R
import com.hfm.customer.databinding.AppLoaderBinding


class Loader(context: Context) : Dialog(context,R.style.loaderDialog) {
    lateinit var binding: AppLoaderBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AppLoaderBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setCancelable(false)
        window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
    }
}