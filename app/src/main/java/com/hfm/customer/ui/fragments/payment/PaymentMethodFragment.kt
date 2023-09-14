package com.hfm.customer.ui.fragments.payment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.hfm.customer.R
import com.hfm.customer.databinding.BottomSheetCreateTicketBinding
import com.hfm.customer.databinding.FragmentPaymentMethodBinding
import com.hfm.customer.databinding.FragmentSupportBinding
import com.hfm.customer.ui.fragments.vouchers.adapter.SupportAdapter
import com.hfm.customer.utils.initRecyclerView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class PaymentMethodFragment : Fragment(), View.OnClickListener {

    private lateinit var binding: FragmentPaymentMethodBinding
    private var currentView: View? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_payment_method, container, false)
            binding = FragmentPaymentMethodBinding.bind(currentView!!)
            init()
            setObserver()
            setOnClickListener()
        }
        return currentView!!
    }


    private fun init() {
    }


    private fun setObserver() {}


    private fun setOnClickListener() {
        with(binding) {
        }
    }


    override fun onClick(v: View?) {
        when (v?.id) {
        }
    }
}