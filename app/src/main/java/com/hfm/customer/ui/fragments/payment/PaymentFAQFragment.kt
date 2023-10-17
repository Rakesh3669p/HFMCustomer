package com.hfm.customer.ui.fragments.payment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.hfm.customer.R
import com.hfm.customer.databinding.FragmentPaymentFaqBinding
import com.hfm.customer.databinding.FragmentPaymentMethodBinding
import com.hfm.customer.ui.fragments.payment.adapter.PaymentsFAQAdapter
import com.hfm.customer.ui.fragments.payment.model.PaymentFAQData
import com.hfm.customer.utils.Loader
import com.hfm.customer.utils.NoInternetDialog
import com.hfm.customer.utils.Resource
import com.hfm.customer.utils.initRecyclerView
import com.hfm.customer.utils.netWorkFailure
import com.hfm.customer.utils.showToast
import com.hfm.customer.viewModel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class PaymentFAQFragment : Fragment(), View.OnClickListener {

    private lateinit var binding: FragmentPaymentFaqBinding
    private var currentView: View? = null
    private val mainViewModel:MainViewModel by viewModels()
    private lateinit var appLoader:Loader
    private lateinit var noInternetDialog:NoInternetDialog
    @Inject lateinit var faqAdapter: PaymentsFAQAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_payment_faq, container, false)
            binding = FragmentPaymentFaqBinding.bind(currentView!!)
            init()
            setObserver()
            setOnClickListener()
        }
        return currentView!!
    }


    private fun init() {
        appLoader = Loader(requireContext())
        noInternetDialog = NoInternetDialog(requireContext())
        noInternetDialog.setOnDismissListener { init() }
        mainViewModel.paymentFaq()
    }


    private fun setObserver() {
        mainViewModel.paymentFaq.observe(viewLifecycleOwner){response->
            when(response){
                is Resource.Success->{
                    appLoader.dismiss()
                    if(response.data?.httpcode == 200){
                        setFaqData(response.data.data)
                    }else{
                        showToast(response.data?.message.toString())
                    }
                }
                is Resource.Loading->appLoader.show()
                is Resource.Error->apiError(response.message)
            }

        }
    }

    private fun setFaqData(data: PaymentFAQData) {
        initRecyclerView(requireContext(),binding.faqRv,faqAdapter)
        faqAdapter.differ.submitList(data.faq)
    }

    private fun apiError(message: String?) {
        appLoader.dismiss()
        showToast(message.toString())
        if (message == netWorkFailure) {
            noInternetDialog.show()
        }
    }
    private fun setOnClickListener() {
        with(binding) {
            back.setOnClickListener(this@PaymentFAQFragment)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.back.id-> findNavController().popBackStack()
        }
    }
}