package com.hfm.customer.ui.fragments.myOrders

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import coil.load
import com.github.dhaval2404.imagepicker.ImagePicker
import com.hfm.customer.R
import com.hfm.customer.databinding.DialogueMediaPickupBinding
import com.hfm.customer.databinding.FragmentOrderDetailsBinding
import com.hfm.customer.ui.fragments.myOrders.adapter.MyAllOrdersProductsAdapter
import com.hfm.customer.ui.fragments.myOrders.model.MyOrdersData
import com.hfm.customer.ui.fragments.myOrders.model.OrderHistoryData
import com.hfm.customer.ui.fragments.myOrders.model.Purchase
import com.hfm.customer.utils.Loader
import com.hfm.customer.utils.NoInternetDialog
import com.hfm.customer.utils.Resource
import com.hfm.customer.utils.SessionManager
import com.hfm.customer.utils.createFileFromContentUri
import com.hfm.customer.utils.formatToTwoDecimalPlaces
import com.hfm.customer.utils.initRecyclerView
import com.hfm.customer.utils.netWorkFailure
import com.hfm.customer.utils.replaceBaseUrl
import com.hfm.customer.utils.showToast
import com.hfm.customer.viewModel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject


@AndroidEntryPoint
class OrderDetailsFragment : Fragment(), View.OnClickListener {
    private lateinit var orderData: MyOrdersData

    private lateinit var orderDetails: Purchase

    private lateinit var binding: FragmentOrderDetailsBinding
    private var currentView: View? = null

    private val mainViewModel: MainViewModel by viewModels()

    private lateinit var appLoader: Loader
    private lateinit var noInternetDialog: NoInternetDialog
    private var imgFile: File? = null


    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var productAdapter: MyAllOrdersProductsAdapter
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
        appLoader = Loader(requireContext())
        noInternetDialog = NoInternetDialog(requireContext())
        noInternetDialog.setOnDismissListener { init() }
        val orderId = arguments?.getString("orderId").toString()
        val saleId = arguments?.getString("saleId").toString()
        mainViewModel.getMyOrders("", 0, orderId)
        mainViewModel.getOrderHistory(saleId)
        with(binding) {
            viewTrackDetails.paintFlags = viewTrackDetails.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        }
    }

    private fun setObserver() {
        mainViewModel.myOrders.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    if (response.data?.httpcode == "200") {
                        orderData = response.data.data
                        setOrderData(response.data.data)
                    } else {
                        showToast(response.data?.message.toString())
                    }
                }

                is Resource.Loading -> appLoader.show()
                is Resource.Error -> apiError(response.message)
            }

        }


        mainViewModel.orderHistory.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    if (response.data?.httpcode == "200") {
                        setOrderHistory(response.data.data)
                    } else {
                        showToast(response.data?.message.toString())
                    }
                }

                is Resource.Loading -> appLoader.show()
                is Resource.Error -> apiError(response.message)
            }

        }

        mainViewModel.uploadOrderReceipt.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    if (response.data?.httpcode == 200) {
                        imgFile?.delete()
                        binding.submit.isVisible = false
                    } else {
                        showToast(response.data?.message.toString())
                    }
                }

                is Resource.Loading -> appLoader.show()
                is Resource.Error -> apiError(response.message)
            }

        }
    }

    private fun setOrderHistory(data: OrderHistoryData) {
        with(binding) {

            data.order.forEach {
                when (it.identifier) {
                    "created" -> {
                        orderedDate.text = it.date
                        ordered.alpha = if (it.available == 1) 1f else 0.7f
                    }

                    "processing" -> {
                        inProcessDate.text = it.date
                        inProcess.alpha = if (it.available == 1) 1f else 0.7f
                    }

                    "pickup" -> {
                        readyToShipDate.text = it.date
                        readyToShip.alpha = if (it.available == 1) 1f else 0.7f
                    }

                    "delivery_inprogress" -> {
                        deliveryInProgressDate.text = it.date
                        deliveryInProgress.alpha = if (it.available == 1) 1f else 0.7f
                    }

                    "delivered" -> {
                        deliveredDate.text = it.date
                        delivered.alpha = if (it.available == 1) 1f else 0.7f
                    }
                }
            }
        }
    }

    private fun setOrderData(data: MyOrdersData) {
        with(binding) {

            data.purchase[0].let {
                orderDetails = it
                orderId.text = "Order #:${it.order_id}"
                orderedDate.text = "${it.order_date} | ${it.order_time}"
                ordered.alpha = 1f
                val orangeColor = ContextCompat.getColor(requireContext(), R.color.orange)
                val greenColor = ContextCompat.getColor(requireContext(), R.color.green)
                val redColor = ContextCompat.getColor(requireContext(), R.color.red)
                when (it.order_status) {
                    "delivered" -> {
                        requestStatus.setTextColor(greenColor)
                        requestStatus.text = "Delivered on ${it.delivered_date}"
                    }

                    else -> {
                        val input = it.order_status
                        val output = input.substring(0, 1).uppercase() + input.substring(1)
                        requestStatus.text = "$output"
                    }
                }
                requestedDate.text = "${it.order_date}"
                orderAmount.text =
                    "RM ${formatToTwoDecimalPlaces(it.grand_total)} (${it.products.size} Items)"
                paymentMethod.text = it.payment_mode
                billingAddress.text =
                    "${it.shipping_address.address1}, ${it.shipping_address.address2},\n${it.shipping_address.city}, ${it.shipping_address.state}, ${it.shipping_address.country}, ${it.shipping_address.zip_code}\nPhone: ${it.shipping_address.phone}"
                shippingAddress.text =
                    "${it.shipping_address.address1}, ${it.shipping_address.address2},\n${it.shipping_address.city}, ${it.shipping_address.state}, ${it.shipping_address.country}, ${it.shipping_address.zip_code}\nPhone: ${it.shipping_address.phone}"
                initRecyclerView(requireContext(), productsRv, productAdapter)
                productAdapter.differ.submitList(it.products)
                productAdapter.setDataFrom("orderDetails")
                seller.text = it.store_name
                deliveryPartner.text = it.delivery_partner
                priceLbl.text = "Price (${it.products.size} Items)"
                price.text = "RM ${formatToTwoDecimalPlaces(it.sub_total)}"
                deliveryCharges.text = "RM ${formatToTwoDecimalPlaces(it.delivery_charges)}"
                if (it.platform_voucher_amount.toString() != "false" || it.platform_voucher_amount.toString() != "null" || it.platform_voucher_amount.toString() != null) {
                    voucher.isVisible = true
                    voucher.text = "RM ${
                        formatToTwoDecimalPlaces(
                            it.platform_voucher_amount.toString().toDouble()
                        )
                    }"
                } else {
                    voucher.isVisible = false
                }

                if (it.discount.toString() != "false" || it.discount.toString() != "null" || it.discount.toString() != null) {
                    discount.isVisible = true
                    discount.text =
                        "RM ${formatToTwoDecimalPlaces(it.discount.toString().toDouble())}"
                } else {
                    discount.isVisible = false
                }

                if (it.wallet_amount.toString() != "false" || it.wallet_amount.toString() != "null" || it.wallet_amount.toString() != null) {
                    walletAmount.isVisible = true
                    walletAmount.text =
                        "RM ${formatToTwoDecimalPlaces(it.wallet_amount.toString().toDouble())}"
                } else {
                    walletAmount.isVisible = false
                }


                totalAmount.text = "RM ${formatToTwoDecimalPlaces(it.grand_total)}"
                totalSavings.isVisible = false
                if (it.payment_uploaded_image.isNullOrEmpty()) {
                    binding.uploadedImage.isVisible = false
                    binding.uploadImage.isVisible = true
                    binding.uploadPaymentMethod.isVisible = true
                    binding.submit.isVisible = true
                } else {
                    binding.uploadedImage.isVisible = true
                    binding.uploadedImage.load(replaceBaseUrl(it.payment_uploaded_image))
                    binding.uploadImage.isVisible = false
                    binding.uploadPaymentMethod.isVisible = false
                    binding.submit.isVisible = false
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

    private fun showImagePickupDialog() {
        val appCompatDialog = Dialog(requireContext())
        val bindingDialog = DialogueMediaPickupBinding.inflate(layoutInflater)
        appCompatDialog.setContentView(bindingDialog.root)
        appCompatDialog.setCancelable(true)
        appCompatDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))


        appCompatDialog.setCancelable(false)
        appCompatDialog.show()
        bindingDialog.camera.setOnClickListener {
            ImagePicker.with(this).cameraOnly()
                .compress(1024)
                .maxResultSize(
                    720,
                    720
                )
                .createIntent { intent ->
                    startForProfileImageResult.launch(intent)
                }
            appCompatDialog.dismiss()
        }
        bindingDialog.gallery.setOnClickListener {
            ImagePicker.with(this).galleryOnly()
                .compress(1024)
                .maxResultSize(
                    720,
                    720
                )
                .createIntent { intent ->
                    startForProfileImageResult.launch(intent)
                }
            appCompatDialog.dismiss()
        }

    }

    private fun uploadPaymentSlip() {
        if (!this::orderDetails.isInitialized) return
        val requestBodyMap = mutableMapOf<String, RequestBody?>()
        if (imgFile != null) {
            requestBodyMap["payment_receipt\"; filename=\"paymentReceipt${orderDetails.sale_id}.jpg\""] =
                imgFile?.asRequestBody("image/jpeg".toMediaTypeOrNull())
        }
        requestBodyMap["access_token"] = sessionManager.token.toRequestBody(MultipartBody.FORM)
        requestBodyMap["lang_id"] = "1".toRequestBody(MultipartBody.FORM)
        requestBodyMap["sale_id"] =
            orderDetails.sale_id.toString().toRequestBody(MultipartBody.FORM)
        requestBodyMap["order_type"] = orderDetails.order_type.toRequestBody(MultipartBody.FORM)
        mainViewModel.uploadOrderReceipt(requestBodyMap)
    }

    private fun setOnClickListener() {
        with(binding) {
            back.setOnClickListener(this@OrderDetailsFragment)
            viewTrackDetails.setOnClickListener(this@OrderDetailsFragment)
            downLoadInvoice.setOnClickListener(this@OrderDetailsFragment)
            uploadImage.setOnClickListener(this@OrderDetailsFragment)
            submit.setOnClickListener(this@OrderDetailsFragment)
            chat.setOnClickListener(this@OrderDetailsFragment)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.back.id -> findNavController().popBackStack()
            binding.viewTrackDetails.id -> findNavController().navigate(R.id.orderTrackingFragment)
            binding.downLoadInvoice.id -> {}
            binding.uploadImage.id -> showImagePickupDialog()
            binding.submit.id -> uploadPaymentSlip()
            binding.chat.id -> {
                orderData.purchase[0].let { it ->

                    val chatId =
                        if (it.chat_id.isNullOrEmpty()) 0 else orderData.purchase[0].chat_id.toInt()
                    val bundle = Bundle()
                    bundle.putString("orderId", it.order_id)
                    bundle.putString(
                        "orderDateTime",
                        "${it.order_date} | ${it.order_time}"
                    )
                    bundle.putString("storeName", it.store_name)
                    bundle.putString("orderAmount", it.grand_total.toString())
                    bundle.putString("itemsCount", it.products.size.toString())
                    bundle.putInt("chatId", chatId)
                    bundle.putString("saleId", it.sale_id.toString())
                    bundle.putString("sellerId", it.seller_id.toString())
                    findNavController().navigate(R.id.chatFragment, bundle)
                }


            }
        }
    }


    private val startForProfileImageResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            val resultCode = result.resultCode
            val data = result.data
            when (resultCode) {
                Activity.RESULT_OK -> {
                    val fileUri = data?.data!!
                    imgFile = createFileFromContentUri(requireActivity(), fileUri)
                    binding.uploadedImage.isVisible = true
                    binding.uploadedImage.setImageURI(fileUri)
                    binding.uploadImage.isVisible = false
                    binding.uploadPaymentMethod.isVisible = false
                }

                ImagePicker.RESULT_ERROR -> {
                    showToast(ImagePicker.getError(data))
                }
            }
        }
}