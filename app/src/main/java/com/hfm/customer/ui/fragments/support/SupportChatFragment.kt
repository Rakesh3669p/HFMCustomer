package com.hfm.customer.ui.fragments.support

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import com.hfm.customer.R
import com.hfm.customer.databinding.DialogueMediaPickupBinding
import com.hfm.customer.databinding.FragmentChatBinding
import com.hfm.customer.ui.dashBoard.chat.adapter.ChatAdapter
import com.hfm.customer.ui.fragments.support.adapter.SupportChatAdapter
import com.hfm.customer.ui.fragments.support.model.Message

import com.hfm.customer.ui.fragments.support.model.SupportMessages
import com.hfm.customer.ui.loginSignUp.LoginActivity
import com.hfm.customer.utils.Loader
import com.hfm.customer.utils.NoInternetDialog
import com.hfm.customer.utils.Resource
import com.hfm.customer.utils.SessionManager
import com.hfm.customer.utils.containsSensitiveWords
import com.hfm.customer.utils.createFileFromContentUri
import com.hfm.customer.utils.formatToTwoDecimalPlaces
import com.hfm.customer.utils.initRecyclerView
import com.hfm.customer.utils.netWorkFailure
import com.hfm.customer.utils.showToast
import com.hfm.customer.utils.toOrderChatFormattedDate
import com.hfm.customer.viewModel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject


@AndroidEntryPoint
class SupportChatFragment : Fragment(), View.OnClickListener {

    private var messagesList: MutableList<Message> = ArrayList()

    private val productId: String = ""
    private var sellerId: String = ""
    private var chatId: String = ""

    private lateinit var binding: FragmentChatBinding
    private var currentView: View? = null

    @Inject
    lateinit var supportChatAdapter: SupportChatAdapter

    @Inject
    lateinit var sessionManager: SessionManager

    private lateinit var appLoader: Loader
    private lateinit var noInternetDialog: NoInternetDialog

    private val mainViewModel: MainViewModel by viewModels()
    private var imgFile: File? = null

    private var orderIdData = ""
    private var saleId = ""
    private var keyBoardOpened = false
    private var supportId = 0
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_chat, container, false)
            binding = FragmentChatBinding.bind(currentView!!)
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
        binding.toolBarTitle.text = "HFM Support"

        val spannable = SpannableString(getString(R.string.view_order_lbl))
        spannable.setSpan(UnderlineSpan(), 0, getString(R.string.view_order_lbl).length, 0)
        binding.viewOrder.text = spannable

        binding.noticeMessage.root.isVisible = false
        initRecyclerView(requireContext(), binding.chatRv, supportChatAdapter)

        appLoader = Loader(requireContext())
        noInternetDialog = NoInternetDialog(requireContext())
        noInternetDialog.setOnDismissListener { init() }

        supportId = arguments?.getInt("supportId") ?: 0
        mainViewModel.getSupportMessages(supportId)

        binding.root.viewTreeObserver.addOnGlobalLayoutListener {
            val r = Rect()
            binding.root.getWindowVisibleDisplayFrame(r)
            val screenHeight: Int = binding.edtMessage.rootView.height
            val heightDiff: Int = screenHeight - (r.bottom - r.top)
            val isKeyboardOpen = heightDiff > screenHeight * 0.15
            if (isKeyboardOpen) {
                if (!keyBoardOpened) {
                    keyBoardOpened = true
                    binding.chatRv.scrollToPosition(supportChatAdapter.itemCount - 1)
                }
            } else {
                keyBoardOpened = false
            }
        }

    }


    private fun setObserver() {
        mainViewModel.supportChatMessages.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    when (response.data?.httpcode) {
                        200 -> {
                            setChatData(response.data.data.support_messages)
                        }

                        401 -> {
                            sessionManager.isLogin = false
                            startActivity(Intent(requireActivity(), LoginActivity::class.java))
                            requireActivity().finish()
                        }

                        else -> {
                            showToast(response.data?.message.toString())
                        }
                    }
                }

                is Resource.Loading -> Unit
                is Resource.Error -> apiError(response.message)
            }
        }

        mainViewModel.sendSupportMessage.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    when (response.data?.httpcode) {
                        200 -> {
                            imgFile = null
                            binding.edtMessage.setText("")
                            binding.gallery.setImageResource(R.drawable.ic_gallery)
                            mainViewModel.getSupportMessages(supportId)
                            /* if (response.data.data.chat_message.isNotEmpty()) {
                                            response.data.data.chat_message[0].let {
                                               *//* messagesList.add(response.data.data.chat_message[0])
                                                chatAdapter.differ.submitList(messagesList)
                                                chatAdapter.notifyDataSetChanged()
                                                binding.chatRv.postDelayed({
                                                    binding.chatRv.scrollToPosition(chatAdapter.itemCount - 1)
                                                }, 150)*//*
                                            }
                                        }*/
                        }
                        401 -> {
                            sessionManager.isLogin = false
                            startActivity(Intent(requireActivity(), LoginActivity::class.java))
                            requireActivity().finish()
                        }
                        else -> {
                            showToast(response.data?.message.toString())
                        }
                    }
                }

                is Resource.Loading -> Unit
                is Resource.Error -> apiError(response.message)
            }
        }
    }

    private fun setChatData(messages: SupportMessages) {

        messagesList = messages.messages.toMutableList()
        supportChatAdapter.differ.submitList(messagesList)
        binding.chatRv.postDelayed({
            binding.chatRv.scrollToPosition(supportChatAdapter.itemCount - 1)
        }, 150)
        with(binding) {
            if(messages.order_details.isNotEmpty()) {
                ordersLayout.isVisible = true
                messages.order_details[0].let {
                    orderIdData = it.order_id
                    saleId = if (it.sale_id != null) it.sale_id.toString() else ""

                    ordersLayout.isVisible = !it.order_id.isNullOrEmpty()
                    orderDate.isVisible = !it.order_id.isNullOrEmpty()
                    amount.isVisible = !it.order_id.isNullOrEmpty()

                    orderId.text = "Order #: ${it.order_id}"
                    orderDate.text = it.order_date.toChatFormattedDate()
                    amount.text = "RM ${it.grand_total?.let { formatToTwoDecimalPlaces(it) }} (${it.products.size}  Items)"
                }

            }
        }
    }


    private fun String.toChatFormattedDate(): String {
        val inputDateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.US)
        val outputDateFormat = SimpleDateFormat("E, d' 'MMM yyyy", Locale.US)

        return try {
            val date = inputDateFormat.parse(this)
            outputDateFormat.format(date)
        } catch (e: Exception) {
            // Handle parsing or formatting errors here
            this // Return the original string if there's an error
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
            back.setOnClickListener(this@SupportChatFragment)
            viewOrder.setOnClickListener(this@SupportChatFragment)
            sendMessage.setOnClickListener(this@SupportChatFragment)
            gallery.setOnClickListener(this@SupportChatFragment)

        }
    }

    private fun validAndSendMessage() {
        val message = binding.edtMessage.text.toString()
        if (message.isEmpty() && imgFile == null) {
            showToast("Please type a message to send..")
            return
        }
        if (containsSensitiveWords(message)) {
            val currentTime = LocalTime.now()
            val timeFormat = DateTimeFormatter.ofPattern("HH:mm:ss")
            val formattedTime = currentTime.format(timeFormat)

            binding.edtMessage.setText("")
            messagesList.map { it.warning = false }
            val warningMessage = Message(
                align = "right",
                created_at = formattedTime,
                from = "You",
                image = "",
                me = 1,
                message = message,
                read_status = 0,
                warningMessage = true,
                warning = true,
            )
            messagesList.add(warningMessage)
            supportChatAdapter.differ.submitList(messagesList)
            supportChatAdapter.notifyDataSetChanged()

            binding.chatRv.postDelayed({
                binding.chatRv.scrollToPosition(supportChatAdapter.itemCount - 1)
            }, 100)

        } else {

            val requestBodyMap = mutableMapOf<String, RequestBody?>()
            if (imgFile != null) {
                requestBodyMap["image\"; filename=\"message.jpg\""] =
                    imgFile?.asRequestBody("image/jpeg".toMediaTypeOrNull())
            }
            requestBodyMap["access_token"] = sessionManager.token.toRequestBody(MultipartBody.FORM)
            requestBodyMap["message"] = message.toRequestBody(MultipartBody.FORM)
            requestBodyMap["support_id"] = supportId.toString().toRequestBody(MultipartBody.FORM)
            mainViewModel.sendSupportMessage(requestBodyMap)
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
            binding.viewOrder.id -> {
                val bundle = Bundle()
                bundle.putString("orderId", orderIdData)
                bundle.putString("saleId", saleId)
                findNavController().navigate(R.id.orderDetailsFragment, bundle)
            }

            binding.sendMessage.id -> validAndSendMessage()
            binding.gallery.id -> {
                showImagePickupDialog()
            }
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
                    binding.gallery.setImageURI(fileUri)
                }

                ImagePicker.RESULT_ERROR -> {
                    showToast(ImagePicker.getError(data))
                }
            }
        }
}