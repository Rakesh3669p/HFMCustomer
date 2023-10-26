package com.hfm.customer.ui.fragments.payment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.hfm.customer.R
import com.hfm.customer.databinding.FragmentPaymentMethodBinding
import com.hfm.customer.utils.Loader
import com.hfm.customer.utils.NoInternetDialog
import com.hfm.customer.utils.Resource
import com.hfm.customer.utils.netWorkFailure
import com.hfm.customer.utils.showToast
import com.hfm.customer.viewModel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class PaymentMethodFragment : Fragment(), View.OnClickListener {

    private var receivedJsonObject: JsonObject? = null

    private lateinit var binding: FragmentPaymentMethodBinding
    private var currentView: View? = null

    private val mainViewModel: MainViewModel by viewModels()

    private lateinit var appLoader: Loader
    private lateinit var noInternetDialog: NoInternetDialog
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
        appLoader = Loader(requireContext())
        noInternetDialog = NoInternetDialog(requireContext())
        noInternetDialog.setOnDismissListener { init() }

        val jsonStr = arguments?.getString("payLoad")
        receivedJsonObject = JsonParser().parse(jsonStr).asJsonObject

    }


    private fun setObserver() {
        mainViewModel.placeOrder.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    if (response.data?.httpcode == 200) {

                        if (binding.onlinePaymentRadioBtn.isChecked || binding.creditCardRadioBtn.isChecked) {
                            val bundle = Bundle()
                            bundle.putString("orderId",response.data.data.order_id)
                            bundle.putString("amount","1")

                            findNavController().navigate(R.id.IPay88Fragment,bundle)
                        } else {
                            showToast("Order Placed Successfully..")
                            findNavController().navigate(R.id.action_paymentMethodFragment_to_myOrdersFragment)
                        }





                    } else {
                        showToast(response.data?.message.toString())
                    }
                }

                is Resource.Loading -> appLoader.show()
                is Resource.Error -> apiError(response.message)
            }

        }
    }

    private fun apiError(message: String?) {
        appLoader.dismiss()
        showToast(message.toString())
        if (message == netWorkFailure) {
            noInternetDialog.show()
        }
    }

    private fun showPaymentMode(paymentMode: Int) {
        with(binding) {
            // Reset rotation for all arrows
            bankTransferArrow.rotation = 0F
            duitNowArrow.rotation = 0F
            onlinePaymentArrow.rotation = 0F
            creditCardArrow.rotation = 0F

            // Hide all layouts initially
            bankTransferLayout.root.isVisible = false
            duitLayout.root.isVisible = false
            onlinePaymentLayout.isVisible = false
            creditCardLayout.isVisible = false

            // Determine which layout to show and rotate its arrow
            when (paymentMode) {
                1 -> {
                    bankTransferLayout.root.isVisible = true
                    bankTransferArrow.rotation = if (bankTransferLayout.root.isVisible) 90F else 0F
                }

                2 -> {
                    duitLayout.root.isVisible = true
                    duitNowArrow.rotation = if (duitLayout.root.isVisible) 90F else 0F
                }

                3 -> {
                    onlinePaymentLayout.isVisible = true
                    onlinePaymentArrow.rotation = if (onlinePaymentLayout.isVisible) 90F else 0F
                }

                4 -> {
                    creditCardLayout.isVisible = true
                    creditCardArrow.rotation = if (creditCardLayout.isVisible) 90F else 0F
                }
            }
        }
    }

    private fun checkPaymentMethods() {

        with(binding) {
            if (onlinePaymentRadioBtn.isChecked || creditCardRadioBtn.isChecked) {
                mainViewModel.placeOrder(receivedJsonObject!!)
            } else if (duitLayout.radioBtn.isChecked) {
                mainViewModel.placeOrder(receivedJsonObject!!)
            } else if (bankTransferLayout.radioBtn.isChecked) {
                mainViewModel.placeOrder(receivedJsonObject!!)
            } else {
                showToast("Please Select any Payment Method to Proceed.")
            }
        }


    }

    private fun setOnClickListener() {
        with(binding) {
            continueLbl.setOnClickListener(this@PaymentMethodFragment)
            howToPayLbl.setOnClickListener(this@PaymentMethodFragment)
            back.setOnClickListener(this@PaymentMethodFragment)
            bankTransfer.setOnClickListener(this@PaymentMethodFragment)
            duitNowLbl.setOnClickListener(this@PaymentMethodFragment)
            onlinePaymentLbl.setOnClickListener(this@PaymentMethodFragment)
            creditCardLbl.setOnClickListener(this@PaymentMethodFragment)
        }
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            binding.continueLbl.id -> {
                checkPaymentMethods()
            }

            binding.howToPayLbl.id -> findNavController().navigate(R.id.paymentFAQFragment)
            binding.back.id -> findNavController().popBackStack()
            binding.bankTransfer.id -> showPaymentMode(1)
            binding.duitNowLbl.id -> showPaymentMode(2)
            binding.onlinePaymentLbl.id -> showPaymentMode(3)
            binding.creditCardLbl.id -> showPaymentMode(4)
        }
    }


}