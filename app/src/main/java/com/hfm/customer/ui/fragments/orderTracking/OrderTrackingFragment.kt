package com.hfm.customer.ui.fragments.orderTracking

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.hfm.customer.R
import com.hfm.customer.databinding.FragmentOrderTrackingBinding
import com.hfm.customer.databinding.FragmentSubmitReviewBinding
import com.hfm.customer.ui.fragments.orderTracking.adapter.OrdersTrackingStatusAdapter
import com.hfm.customer.ui.fragments.orderTracking.dialog.DeliveryProof
import com.hfm.customer.utils.Loader
import com.hfm.customer.utils.NoInternetDialog
import com.hfm.customer.utils.PromotionBanner
import com.hfm.customer.utils.initRecyclerView
import com.hfm.customer.utils.initRecyclerViewGrid
import com.hfm.customer.viewModel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class OrderTrackingFragment : Fragment(), View.OnClickListener {

    private lateinit var binding: FragmentOrderTrackingBinding
    private var currentView: View? = null
    private val mainViewModel: MainViewModel by viewModels()
    private lateinit var appLoader: Loader
    private lateinit var noInternetDialog: NoInternetDialog

    @Inject lateinit var trackingStatusAdapter: OrdersTrackingStatusAdapter
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
        initRecyclerView(requireContext(),binding.courierStatusRv,trackingStatusAdapter)
    }

    private fun setObserver() {
    }


    private fun setOnClickListener() {
        with(binding) {
            back.setOnClickListener(this@OrderTrackingFragment)
            viewOrderProof.setOnClickListener(this@OrderTrackingFragment)

        }
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            binding.back.id -> findNavController().popBackStack()
            binding.viewOrderProof.id -> deliveryProofImage.show()
        }
    }
}