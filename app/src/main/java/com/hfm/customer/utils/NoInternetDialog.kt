package com.hfm.customer.utils

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.hfm.customer.R
import com.hfm.customer.databinding.AppLoaderBinding
import com.hfm.customer.databinding.FragmentNoInternetBinding
import com.hfm.customer.ui.dashBoard.DashBoardActivity
import com.hfm.customer.ui.fragments.noInternet.NoInternetFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class NoInternetDialog(context: Context) : Dialog(context) {
    lateinit var binding: FragmentNoInternetBinding
    private lateinit var appLoader: Loader

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentNoInternetBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window?.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        window?.setBackgroundDrawableResource(R.color.blackTrans200)
        appLoader = Loader(context)

        binding.retry.setOnClickListener {
            appLoader.show()
            CoroutineScope(Dispatchers.Main).launch {
                delay(1000)
                appLoader.dismiss()
                if (isInternetAvailable(context)) {
                    dismiss()
                } else {
                    Toast.makeText(context, "No Internet", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}