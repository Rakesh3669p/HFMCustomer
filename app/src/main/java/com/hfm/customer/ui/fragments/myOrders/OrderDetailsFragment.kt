package com.hfm.customer.ui.fragments.myOrders

import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.hfm.customer.R
import com.hfm.customer.databinding.FragmentOrderDetailsBinding
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class OrderDetailsFragment : Fragment(), View.OnClickListener {
    private lateinit var binding: FragmentOrderDetailsBinding
    private var currentView: View? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_order_details, container, false)
            binding = FragmentOrderDetailsBinding.bind(currentView!!)
            init()
            setObserver()
            setOnClickListener()
        }
        return currentView!!
    }

    private fun init() {
        with(binding) {
            viewTrackDetails.paintFlags = viewTrackDetails.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        }
    }

    private fun setObserver() {}

    private fun setOnClickListener() {
        with(binding) {
            viewTrackDetails.setOnClickListener(this@OrderDetailsFragment)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.viewTrackDetails.id-> findNavController().navigate(R.id.orderTrackingFragment)
        }
    }
}