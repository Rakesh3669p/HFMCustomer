package com.hfm.customer.ui.fragments.orderTracking

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import coil.load
import com.hfm.customer.R
import com.hfm.customer.databinding.DeliveryProofBinding
import com.hfm.customer.databinding.FragmentOrderTrackingBinding
import com.hfm.customer.ui.fragments.orderTracking.adapter.OrdersTrackingStatusAdapter
import com.hfm.customer.ui.fragments.orderTracking.dialog.DeliveryProof
import com.hfm.customer.utils.Loader
import com.hfm.customer.utils.NoInternetDialog
import com.hfm.customer.utils.Resource
import com.hfm.customer.utils.initRecyclerView
import com.hfm.customer.utils.loadImage
import com.hfm.customer.utils.netWorkFailure
import com.hfm.customer.utils.showToast
import com.hfm.customer.viewModel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class OrderTrackingFragment : Fragment(), View.OnClickListener {

    private var deliveryProof: String=""
    private var saleId: String? = ""
    private lateinit var binding: FragmentOrderTrackingBinding
    private var currentView: View? = null
    private val mainViewModel: MainViewModel by viewModels()
    private lateinit var appLoader: Loader
    private lateinit var noInternetDialog: NoInternetDialog


    @Inject
    lateinit var trackingStatusAdapter: OrdersTrackingStatusAdapter
    private lateinit var deliveryProofImage: DeliveryProof

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_order_tracking, container, false)
            binding = FragmentOrderTrackingBinding.bind(currentView!!)
            init()
            setObserver()
            setOnClickListener()
        }
        return currentView!!
    }

    private fun init() {

        appLoader = Loader(requireContext())
        deliveryProofImage = DeliveryProof(requireContext(), "")
        noInternetDialog = NoInternetDialog(requireContext())
        noInternetDialog.setOnDismissListener { noInternetDialog.dismiss() }
        saleId = arguments?.getString("saleId") ?: ""
        mainViewModel.getOrderTracking(saleId.toString())

    }

    private fun setObserver() {
        mainViewModel.orderTracking.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    if (response.data?.httpcode == "200") {
                        deliveryProof = response.data.data.delivery_proof
                        setTrackingData(response.data.data)
                    } else {
                        showToast(response.data?.message.toString())
                    }
                }

                is Resource.Loading -> appLoader.show()

                is Resource.Error -> apiError(response.message.toString())
            }
        }
    }

    private fun setTrackingData(data: TrackingData) {
        initRecyclerView(requireContext(), binding.courierStatusRv, trackingStatusAdapter)
        trackingStatusAdapter.differ.submitList(data.events)
        trackingStatusAdapter.setShippingMethod(data.shipping_method)
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
            back.setOnClickListener(this@OrderTrackingFragment)
        }

        trackingStatusAdapter.setOnViewOrderClickListener {
            val appCompatDialog = Dialog(requireContext())
            val bindingDialog = DeliveryProofBinding.inflate(layoutInflater)
            appCompatDialog.setContentView(bindingDialog.root)
            appCompatDialog.setCancelable(true)
            bindingDialog.productImage.loadImage(deliveryProof)
            bindingDialog.close.setOnClickListener { appCompatDialog.dismiss() }
            appCompatDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            appCompatDialog.show()
        }
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            binding.back.id -> findNavController().popBackStack()
        }
    }
}