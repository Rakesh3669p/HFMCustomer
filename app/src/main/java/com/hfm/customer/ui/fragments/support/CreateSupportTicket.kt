package com.hfm.customer.ui.fragments.support

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import com.hfm.customer.R
import com.hfm.customer.databinding.DialogueMediaPickupBinding
import com.hfm.customer.databinding.FragmentCreateTicketBinding
import com.hfm.customer.utils.Loader
import com.hfm.customer.utils.NoInternetDialog
import com.hfm.customer.utils.Resource
import com.hfm.customer.utils.SessionManager
import com.hfm.customer.utils.createFileFromContentUri
import com.hfm.customer.utils.formatToTwoDecimalPlaces
import com.hfm.customer.utils.netWorkFailure
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
class CreateSupportTicket : Fragment(), View.OnClickListener {
    private lateinit var binding: FragmentCreateTicketBinding
    private var currentView: View? = null
    private val mainViewModel: MainViewModel by viewModels()

    private lateinit var appLoader: Loader
    private lateinit var noInternetDialog: NoInternetDialog
    @Inject lateinit var sessionManager:SessionManager
    private var imgFile: File? = null
    private var orderId: String = ""
    private var orderDateTime: String = ""
    private var orderAmount: String = ""
    private var itemsCount: String = ""
    private var saleId: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_create_ticket, container, false)
            binding = FragmentCreateTicketBinding.bind(currentView!!)
            init()

            setOnClickListener()
        }
        return currentView!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setObserver()
    }

    @SuppressLint("SetTextI18n")
    private fun init() {
        appLoader = Loader(requireContext())
        noInternetDialog = NoInternetDialog(requireContext())
        noInternetDialog.setOnDismissListener { init() }

        arguments?.let {
            orderId = it.getString("orderId").toString()
            orderDateTime = it.getString("orderDateTime").toString()
            orderAmount = it.getString("orderAmount").toString()
            itemsCount = it.getString("itemsCount").toString()
            saleId = it.getString("saleId")?:""
        }

        if(saleId.isNotEmpty()){
            binding.ticketSpinner.setSelection(1)
            binding.orderDetailsGroup.isVisible = true
            binding.ordersLayout.isVisible = true

            binding.orderId.text = "Order #:${orderId}"
            binding.orderDate.text = orderDateTime
            binding.amount.text = "RM ${formatToTwoDecimalPlaces(orderAmount.toDouble())} (${itemsCount} Items)"
            val spannable = SpannableString(getString(R.string.view_order_lbl))
            spannable.setSpan(UnderlineSpan(), 0, getString(R.string.view_order_lbl).length, 0)
            binding.viewOrder.text = spannable

        }
    }

    private fun setOnClickListener() {
        with(binding) {

            back.setOnClickListener (this@CreateSupportTicket)
            send.setOnClickListener (this@CreateSupportTicket)
            orderEdt.setOnClickListener (this@CreateSupportTicket)
            imageUploadLayout.setOnClickListener (this@CreateSupportTicket)
            closeBtn.setOnClickListener (this@CreateSupportTicket)

            binding.ticketSpinner.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parentView: AdapterView<*>,
                        selectedItemView: View,
                        position: Int,
                        id: Long
                    ) {

                        binding.orderDetailsGroup.isVisible = position == 1
                        if(position==0){
                            binding.ordersLayout.isVisible = false
                        }
                    }

                    override fun onNothingSelected(parentView: AdapterView<*>?) {}
                }
        }

    }

    private fun setObserver() {
        mainViewModel.createSupportTicket.observe(viewLifecycleOwner){response->
            when(response) {
                is Resource.Success->{
                    appLoader.dismiss()
                     if(response.data?.httpcode == 200){
                         findNavController().navigate(R.id.action_createSupportTicketFragment_to_supportFragment)
                     }else{
                         showToast(response.data?.message.toString())
                     }

                }
                is Resource.Loading->appLoader.show()
                is Resource.Error-> apiError(response.message)
            }
        }
    }

    private fun sendATicket() {
        val title = binding.titleEdt.text.toString()
        val message = binding.messageEdt.text.toString()

        if(title.isEmpty()){
            showToast("Please enter the title")
            return
        }

        if(message.isEmpty()){
            showToast("Please enter the message")
            return
        }

        if(binding.ticketSpinner.selectedItemPosition==1&&saleId.isEmpty()){
            showToast("Please select the order")
            return
        }


        val requestBodyMap = mutableMapOf<String, RequestBody?>()
        if(imgFile!=null) {
            requestBodyMap["image\"; filename=\"profile.jpg\""] =
                imgFile?.asRequestBody("image/jpeg".toMediaTypeOrNull())
        }
        requestBodyMap["access_token"] = sessionManager.token.toRequestBody(MultipartBody.FORM)
        requestBodyMap["subject"] = title.toRequestBody(MultipartBody.FORM)
        requestBodyMap["message"] = message.toRequestBody(MultipartBody.FORM)
        requestBodyMap["sale_id"] = saleId.toRequestBody(MultipartBody.FORM)
        mainViewModel.createSupportTicket(requestBodyMap)

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
                    startForImageResult.launch(intent)
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
                    startForImageResult.launch(intent)
                }
            appCompatDialog.dismiss()
        }

    }


    override fun onClick(v: View?) {
        when (v?.id) {
            binding.back.id -> findNavController().popBackStack()
            binding.send.id -> sendATicket()
            binding.orderEdt.id -> findNavController().navigate(R.id.myAllOrdersFragment)
            binding.imageUploadLayout.id -> showImagePickupDialog()
            binding.closeBtn.id -> binding.ticketSpinner.setSelection(0,true)
        }
    }


    private val startForImageResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            val resultCode = result.resultCode
            val data = result.data
            when (resultCode) {
                Activity.RESULT_OK -> {
                    val fileUri = data?.data!!
                    imgFile = createFileFromContentUri(requireActivity(), fileUri)
                    binding.uploadedImage.isVisible = true
                    binding.uploadedImage.setImageURI(fileUri)
                }

                ImagePicker.RESULT_ERROR -> {
                    showToast(ImagePicker.getError(data))
                }

                else -> {}
            }
        }

}