package com.hfm.customer.ui.fragments.payment

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.hfm.customer.R
import com.hfm.customer.databinding.DialogueOrderSuccessBinding
import com.hfm.customer.databinding.FragmentPaymentMethodBinding
import com.hfm.customer.ui.fragments.checkOut.model.PaymentMethod
import com.hfm.customer.ui.fragments.payment.adapter.PaymentMethodsAdapter
import com.hfm.customer.utils.Loader
import com.hfm.customer.utils.NoInternetDialog
import com.hfm.customer.utils.Resource
import com.hfm.customer.utils.cartCount
import com.hfm.customer.utils.initRecyclerView
import com.hfm.customer.utils.netWorkFailure
import com.hfm.customer.utils.showToast
import com.hfm.customer.viewModel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@SuppressLint("SetTextI18n")
@AndroidEntryPoint
class PaymentMethodFragment : Fragment(), View.OnClickListener {

    private var paymentMethodId: Int = 0
    private var isOnline: Boolean = false
    private var receivedJsonObject: JsonObject? = null

    private lateinit var binding: FragmentPaymentMethodBinding
    private var currentView: View? = null

    private val mainViewModel: MainViewModel by viewModels()

    private lateinit var appLoader: Loader
    private lateinit var noInternetDialog: NoInternetDialog

    @Inject
    lateinit var paymentMethodsAdapter: PaymentMethodsAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_payment_method, container, false)
            binding = FragmentPaymentMethodBinding.bind(currentView!!)
            init()
            setOnClickListener()
        }
        return currentView!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setObserver()
    }

    private fun init() {
        appLoader = Loader(requireContext())
        noInternetDialog = NoInternetDialog(requireContext())
        noInternetDialog.setOnDismissListener { init() }
        mainViewModel.getCheckoutInfo()
        val jsonStr = arguments?.getString("payLoad")
        receivedJsonObject = JsonParser().parse(jsonStr).asJsonObject
        val spannable = SpannableString(getString(R.string.how_to_make_payment_lbl))
        spannable.setSpan(UnderlineSpan(), 0, getString(R.string.view_order_lbl).length, 0)
        binding.howToPayLbl.text = spannable
    }


    private fun setObserver() {

        mainViewModel.checkOutInfo.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    if (response.data?.httpcode == 200) {
                        setPaymentMethods(response.data.data.payment_methods)
                    }
                }

                is Resource.Loading -> appLoader.show()
                is Resource.Error -> apiError(response.message.toString())
            }
        }

        mainViewModel.placeOrder.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    if (response.data?.httpcode == 200) {
                        cartCount.postValue(response.data.data.cart_count)
                        if (isOnline) {
                            val bundle = Bundle()
                            bundle.putString("orderId", response.data.data.order_id)
                            bundle.putString("amount", response.data.data.amount)
                            bundle.putString("paymentUrl", response.data.data.payment_url)
                            findNavController().navigate(R.id.action_paymentMethodFragment_to_IPay88, bundle)
                        } else {
                            showSuccessDialog(response.data.data.order_id)
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

    private fun setPaymentMethods(paymentMethods: List<PaymentMethod>) {
        initRecyclerView(requireContext(), binding.paymentMethodsRv, paymentMethodsAdapter)
        paymentMethodsAdapter.differ.submitList(paymentMethods)
    }

    private fun apiError(message: String?) {
        appLoader.dismiss()
        showToast(message.toString())
        if (message == netWorkFailure) {
            noInternetDialog.show()
        }
    }


    private fun showSuccessDialog(orderId: String) {
        val appCompatDialog = Dialog(requireContext())
        val bindingDialog = DialogueOrderSuccessBinding.inflate(layoutInflater)
        appCompatDialog.setContentView(bindingDialog.root)
        appCompatDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        appCompatDialog.setCancelable(false)
        bindingDialog.title.text = "Thank you for placing order.\nYour order id is $orderId"
        bindingDialog.desc.text = "We will notify you regarding the status of the order."
        bindingDialog.ok.setOnClickListener {
            appCompatDialog.dismiss()
            findNavController().navigate(R.id.action_paymentMethodFragment_to_homeFragment)
        }
        appCompatDialog.show()
        lifecycleScope.launch {
            delay(3000)
            if (appCompatDialog.isShowing) {
                appCompatDialog.dismiss()
                findNavController().navigate(R.id.action_paymentMethodFragment_to_myOrdersFragment)
            }
        }
    }


    private fun checkPaymentMethods() {
        if (paymentMethodId <= 0) showToast("Please select any payment method to proceed.")
        else mainViewModel.placeOrder(receivedJsonObject!!)
    }

    private fun setOnClickListener() {
        with(binding) {
            continueLbl.setOnClickListener(this@PaymentMethodFragment)
            howToPayLbl.setOnClickListener(this@PaymentMethodFragment)
            back.setOnClickListener(this@PaymentMethodFragment)
        }
        paymentMethodsAdapter.setOnPaymentMethodSelectedListener { it, isOnline ->
            paymentMethodId = it
            receivedJsonObject!!.addProperty("payment_type", it)
            this.isOnline = isOnline
        }
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            binding.continueLbl.id -> checkPaymentMethods()
            binding.howToPayLbl.id -> findNavController().navigate(R.id.paymentFAQFragment)
            binding.back.id -> findNavController().popBackStack()

        }
    }
}