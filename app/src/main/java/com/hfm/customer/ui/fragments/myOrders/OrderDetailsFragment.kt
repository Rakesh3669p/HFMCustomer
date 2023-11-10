package com.hfm.customer.ui.fragments.myOrders

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
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
import coil.transform.CircleCropTransformation
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
import com.hfm.customer.utils.loadImage
import com.hfm.customer.utils.netWorkFailure
import com.hfm.customer.utils.replaceBaseUrl
import com.hfm.customer.utils.showToast
import com.hfm.customer.utils.toOrderDetailsFormattedDate
import com.hfm.customer.viewModel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject
import kotlin.math.roundToInt


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
        binding.loader.isVisible = true
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
                        binding.loader.isVisible = false
                        binding.noDataLayout.root.isVisible = false
                        orderData = response.data.data
                        setOrderData(response.data.data)
                    } else {
                        binding.noDataLayout.root.isVisible = true
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
                        binding.downLoadInvoice.isVisible = false
                        orderedDate.text = it.date.toOrderDetailsFormattedDate()
                        ordered.alpha = if (it.available == 1) 1f else 0.7f
                    }

                    "processing" -> {
                        binding.downLoadInvoice.isVisible = false
                        inProcessDate.text = it.date.toOrderDetailsFormattedDate()
                        inProcess.alpha = if (it.available == 1) 1f else 0.7f
                    }

                    "pickup" -> {
                        binding.downLoadInvoice.isVisible = false
                        readyToShipDate.text = it.date.toOrderDetailsFormattedDate()
                        readyToShip.alpha = if (it.available == 1) 1f else 0.7f
                    }

                    "delivery_inprogress" -> {
                        binding.downLoadInvoice.isVisible = false
                        deliveryInProgressDate.text = it.date.toOrderDetailsFormattedDate()
                        deliveryInProgress.alpha = if (it.available == 1) 1f else 0.7f
                    }

                    "delivered" -> {
                        binding.downLoadInvoice.isVisible = true
                        deliveredDate.text = it.date.toOrderDetailsFormattedDate()
                        delivered.alpha = if (it.available == 1) 1f else 0.7f
                    }
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setOrderData(data: MyOrdersData) {
        with(binding) {
            data.purchase[0].let {
                orderDetails = it
                if (orderDetails.order_type == "bulk_order") {
                    chat.text = "Support"
                } else if (orderDetails.order_type == "normal_order") {
                    chat.text = "Chat"
                }
                orderId.text = "Order #:${it.order_id}"
                orderedDate.text = "${it.order_date} | ${it.order_time}"
                ordered.alpha = 1f
                val orangeColor = ContextCompat.getColor(requireContext(), R.color.orange)
                val greenColor = ContextCompat.getColor(requireContext(), R.color.green)
                val redColor = ContextCompat.getColor(requireContext(), R.color.red)
                viewTrackDetails.isVisible =
                    orderDetails.delivery_partner.lowercase() == "dhl shipping" || orderDetails.delivery_partner.lowercase() == "citylink shipping"

                paymentReceiptCv.isVisible = it.order_status != "delivered"
                submit.isVisible = binding.paymentReceiptCv.isVisible

                downLoadInvoice.isVisible = it.order_status == "delivered"

                if(it.frontend_order_status.lowercase().contains("payment pending")){
                    requestStatus.setTextColor(orangeColor)
                    requestStatus.text = it.frontend_order_status
                }else if(it.frontend_order_status.lowercase().contains("in process")){
                    requestStatus.setTextColor(orangeColor)
                    requestStatus.text = it.frontend_order_status
                }else if(it.frontend_order_status.lowercase().contains("estimated delivery")){
                    requestStatus.setTextColor(greenColor)
                    requestStatus.text = it.frontend_order_status
                }else if(it.frontend_order_status.lowercase().contains("delivered")){
                    requestStatus.setTextColor(greenColor)
                    requestStatus.text = it.frontend_order_status
                }else if(it.frontend_order_status.lowercase().contains("cancelled")){
                    paymentReceiptCv.isVisible = false
                    submit.isVisible = false
                    requestStatus.setTextColor(redColor)
                    requestStatus.text = it.frontend_order_status
                }


                requestedDate.text = "${it.order_date}"
                orderAmount.text =
                    "RM ${formatToTwoDecimalPlaces(it.grand_total)} (${it.products.size} Items)"

                paymentMethod.text = it.payment_mode

                if (it.order_status == "cancelled"||it.order_status == "cancel_initiated") {
                    paymentStatus.isVisible = false
                    paymentStatusLbl.isVisible = false
                    orderTrackingCv.isVisible = false

                    cancelledLbl.isVisible = true
                    cancelledReason.isVisible = true
                    paymentMethodDivider1.isVisible = true
                    if(!it.cancel_order_detail.cancel_notes.isNullOrEmpty()) {
                        cancelledReason.text = it.cancel_order_detail.cancel_notes
                    }else{
                        cancelledReason.text = "Order has been cancelled"
                    }
                } else {
                    paymentStatus.isVisible = true
                    paymentStatusLbl.isVisible = true
                    orderTrackingCv.isVisible = true

                    cancelledLbl.isVisible = false
                    cancelledReason.isVisible = false
                    paymentMethodDivider1.isVisible = false
                }

                when (it.order_status) {
                    "accepted","delivered" -> {
                        when (it.payment_upload_status) {
                            0 -> paymentStatus.text = "Not Uploaded"
                            1 -> paymentStatus.text = "Uploaded"
                            2 -> {
                                val remarks = if (it.reject_remarks.isNullOrEmpty()) "" else "(${it.reject_remarks})"
                                paymentStatus.text = "Rejected $remarks"
                                paymentStatus.setTextColor(redColor)
                            }
                        }
                    }
                }
                billingAddress.text = "${it.shipping_address.address1}, ${it.shipping_address.address2},\n${it.shipping_address.city}, ${it.shipping_address.state}, ${it.shipping_address.country}, ${it.shipping_address.zip_code}\nPhone: ${it.shipping_address.phone}"
                shippingAddress.text = "${it.shipping_address.address1}, ${it.shipping_address.address2},\n${it.shipping_address.city}, ${it.shipping_address.state}, ${it.shipping_address.country}, ${it.shipping_address.zip_code}\nPhone: ${it.shipping_address.phone}"
                initRecyclerView(requireContext(), productsRv, productAdapter)
                productAdapter.differ.submitList(it.products)
                productAdapter.setDeliveredOrder(it.order_status == "delivered")
                productAdapter.setDataFrom("orderDetails")
                seller.text = it.store_name
                deliveryPartner.text = it.delivery_partner
                subtotalLbl.text = "Subtotal (${it.products.size} Items)"
                subtotal.text = "RM ${formatToTwoDecimalPlaces(it.sub_total)}"
                shippingAmount.text = "RM ${formatToTwoDecimalPlaces(it.delivery_charges)}"
                wallet.text =
                    "RM -${formatToTwoDecimalPlaces(it.wallet_amount.toString().toDouble())}"
                storeVoucher.text = "RM -${formatToTwoDecimalPlaces(it.seller_voucher_amount)}"
                platformVoucher.text = "RM -${
                    formatToTwoDecimalPlaces(
                        it.platform_voucher_amount.toString().toDouble()
                    )
                }"


                totalAmount.text = "${formatToTwoDecimalPlaces(it.grand_total)}"
                totalSavings.isVisible = false

                if (it.payment_uploaded_image.isNullOrEmpty() && it.order_status == "cancelled"||it.order_status == "cancel_initiated" || it.payment_mode == "Online Payment") {
                    paymentReceiptCv.isVisible = false
                    submit.isVisible = false
                } else if (it.payment_uploaded_image.isNullOrEmpty() || it.payment_status == "rejected") {
                    uploadPaymentMethodLbl.text = "Upload Payment Receipt"
                    uploadedImage.isVisible = false
                    uploadImage.isVisible = true
                    uploadPaymentMethod.isVisible = true
                    submit.isVisible = true
                } else {
                    uploadPaymentMethodLbl.text = "Uploaded Payment Receipt"
                    uploadedImage.isVisible = true
                    uploadedImage.loadImage(replaceBaseUrl(it.payment_uploaded_image))
                    uploadImage.isVisible = false
                    uploadPaymentMethod.isVisible = false
                    submit.isVisible = false
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

        productAdapter.setOnRateProductClickListener { productId ->
            val bundle = Bundle()
            bundle.putString("productId", productId)
            findNavController().navigate(R.id.submitReviewFragment, bundle)
        }
    }

    private fun downloadInvoice() {
        val invoice = orderData.purchase[0].invoice
        if(invoice.isNullOrEmpty()) {
            showToast("Invoice not generated yet...")
            return
        }

        val request = DownloadManager.Request(Uri.parse(invoice))
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)

        // Set the notification visibility to show the download progress
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)

        // Set the destination to the "Downloads" directory
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, orderData.purchase[0].order_id)

        val downloadManager = context?.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadId = downloadManager.enqueue(request)

        // Optionally, you can create a BroadcastReceiver to handle when the download is complete and open the downloaded PDF file
        val onComplete = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        val downloadReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1) == downloadId) {
                   showToast("Invoice downloaded...")
                }
            }
        }

        context?.registerReceiver(downloadReceiver, onComplete)


    }


    override fun onClick(v: View?) {
        when (v?.id) {
            binding.back.id -> findNavController().popBackStack()
            binding.viewTrackDetails.id -> {
                val bundle = Bundle()
                bundle.putString("saleId", orderDetails.sale_id.toString())
                findNavController().navigate(R.id.orderTrackingFragment, bundle)
            }

            binding.downLoadInvoice.id -> downloadInvoice()
            binding.uploadImage.id -> showImagePickupDialog()
            binding.submit.id -> uploadPaymentSlip()
            binding.chat.id -> {
                orderData.purchase[0].let { it ->
                    if (it.order_type == "normal_order") {
                        val chatId =
                            if (it.chat_id.isNullOrEmpty()) 0 else orderData.purchase[0].chat_id.toInt()
                        val bundle = Bundle()
                        bundle.putString("orderId", it.order_id)
                        bundle.putString(
                            "orderDateTime", it.order_date
                        )
                        bundle.putString("storeName", it.store_name)
                        bundle.putString("orderAmount", it.grand_total.toString())
                        bundle.putString("itemsCount", it.products.size.toString())
                        bundle.putInt("chatId", chatId)
                        bundle.putString("saleId", it.sale_id.toString())
                        bundle.putString("sellerId", it.seller_id.toString())
                        findNavController().navigate(R.id.chatFragment, bundle)
                    } else {
                        findNavController().navigate(R.id.supportFragment)
                    }
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