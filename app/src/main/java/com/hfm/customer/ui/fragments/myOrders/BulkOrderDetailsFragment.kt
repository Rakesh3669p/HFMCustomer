package com.hfm.customer.ui.fragments.myOrders

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import coil.load
import com.hfm.customer.R
import com.hfm.customer.databinding.FragmentBulkOrderDetailsBinding
import com.hfm.customer.ui.fragments.myOrders.adapter.MyAllOrdersProductsAdapter
import com.hfm.customer.ui.fragments.myOrders.model.BulkOrdersData
import com.hfm.customer.ui.fragments.myOrders.model.BulkrequestOrderDetail
import com.hfm.customer.utils.Loader
import com.hfm.customer.utils.NoInternetDialog
import com.hfm.customer.utils.Resource
import com.hfm.customer.utils.SessionManager
import com.hfm.customer.utils.formatToTwoDecimalPlaces
import com.hfm.customer.utils.netWorkFailure
import com.hfm.customer.utils.replaceBaseUrl
import com.hfm.customer.utils.showToast
import com.hfm.customer.viewModel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class BulkOrderDetailsFragment : Fragment(), View.OnClickListener {
    private lateinit var orderData: BulkOrdersData

    private lateinit var orderDetails: BulkrequestOrderDetail

    private lateinit var binding: FragmentBulkOrderDetailsBinding
    private var currentView: View? = null

    private val mainViewModel: MainViewModel by viewModels()

    private lateinit var appLoader: Loader
    private lateinit var noInternetDialog: NoInternetDialog


    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var productAdapter: MyAllOrdersProductsAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_bulk_order_details, container, false)
            binding = FragmentBulkOrderDetailsBinding.bind(currentView!!)
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
        val orderId = arguments?.getString("orderId").toString()
        mainViewModel.getBulkOrders(0, orderId)
    }

    private fun setObserver() {

        mainViewModel.bulkOrderList.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    if (response.data?.httpcode == 200) {
                        orderData = response.data.data
                        setOrderData(orderData)
                    } else {
                        showToast(response.data?.message.toString())
                    }
                }

                is Resource.Loading -> appLoader.show()
                is Resource.Error -> apiError(response.message)
            }

        }


    }


    private fun setOrderData(data: BulkOrdersData) {
        with(binding) {

            data.bulkrequest_order_details[0].let {
                orderDetails = it
                requestId.text = "Request #:${it.bulkrequest_order_id}"
                orderId.text = "OrderId #:${it.order_id}"
                requestedDate.text = "${it.request_date} | ${it.request_time}"
                requestStatus.text = it.request_status.toString()
                val orangeColor = ContextCompat.getColor(requireContext(), R.color.orange)
                val greenColor = ContextCompat.getColor(requireContext(), R.color.green)
                val redColor = ContextCompat.getColor(requireContext(), R.color.red)
                if (it.product_image.isNotEmpty()) {
                    productImage.load(replaceBaseUrl(it.product_image[0].image))
                }
                productName.text = it.product_name
                productVariant.text = "Qty: ${it.quantity}"
                unitOfMeasures.text = "Unit Of Measure: ${it.unit_of_measure}"


                dateNeeded.text = it.date_needed
                subtotal.text = "RM ${it.sale_price}"
                deliveryCharges.text = "RM ${it.shipping_charges}"
                shippingAddress.text = it.shipping_address
                remarks.text = it.remarks
                requestStatus1.text = it.request_status.toString()
                quotationStatus.text = it.quotation_status.toString()
                if(it.sale_price!=null) {
                    grandTotal.text =
                        "RM ${formatToTwoDecimalPlaces(it.sale_price.toString().toDouble())}"
                }else{
                    grandTotal.text = "RM "
                }

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

    private fun setOnClickListener() {
        with(binding) {
            back.setOnClickListener(this@BulkOrderDetailsFragment)
            accept.setOnClickListener(this@BulkOrderDetailsFragment)
            reject.setOnClickListener(this@BulkOrderDetailsFragment)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.back.id -> findNavController().popBackStack()
            binding.accept.id -> showToast("Under Construction")
            binding.reject.id -> showToast("Under Construction")

        }
    }

}