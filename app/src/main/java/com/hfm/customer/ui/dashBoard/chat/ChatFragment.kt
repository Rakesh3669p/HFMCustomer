package com.hfm.customer.ui.dashBoard.chat

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_IMAGES
import android.Manifest.permission.READ_MEDIA_VIDEO
import android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import com.hfm.customer.R
import com.hfm.customer.databinding.DialogueMediaPickupBinding
import com.hfm.customer.databinding.FragmentChatBinding
import com.hfm.customer.ui.dashBoard.chat.adapter.ChatAdapter
import com.hfm.customer.ui.dashBoard.chat.model.Message
import com.hfm.customer.ui.dashBoard.chat.model.Messages
import com.hfm.customer.utils.Loader
import com.hfm.customer.utils.NoInternetDialog
import com.hfm.customer.utils.Resource
import com.hfm.customer.utils.SessionManager
import com.hfm.customer.utils.containsSensitiveWords
import com.hfm.customer.utils.createFileFromContentUri
import com.hfm.customer.utils.getDeviceId
import com.hfm.customer.utils.initRecyclerView
import com.hfm.customer.utils.netWorkFailure
import com.hfm.customer.utils.showToast
import com.hfm.customer.utils.showToastLong
import com.hfm.customer.viewModel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@AndroidEntryPoint
class ChatFragment : Fragment(), View.OnClickListener {

    private var messagesList: MutableList<Message> = ArrayList()

    private val productId: String = ""
    private var sellerId: String = ""
    private var chatId: String = ""

    private lateinit var binding: FragmentChatBinding
    private var currentView: View? = null

    @Inject
    lateinit var chatAdapter: ChatAdapter

    @Inject
    lateinit var sessionManager: SessionManager

    private lateinit var appLoader: Loader
    private lateinit var noInternetDialog: NoInternetDialog

    private val mainViewModel: MainViewModel by viewModels()
    private var imgFile: File? = null

    private var orderIdData = ""
    private var saleId = ""
    private var orderDateTime = ""
    private var orderAmount = ""
    private var qty = ""
    var keyBoardOpened = false
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
        initRecyclerView(requireContext(), binding.chatRv, chatAdapter)

        appLoader = Loader(requireContext())
        noInternetDialog = NoInternetDialog(requireContext())
        noInternetDialog.setOnDismissListener { init() }

        val from = arguments?.getString("from").toString()
        val storeName = arguments?.getString("storeName").toString()
        sellerId = arguments?.getString("sellerId").toString()
        chatId = arguments?.getInt("chatId").toString()
        saleId = arguments?.getString("saleId").toString()
        binding.noticeMessage.root.isVisible = from == "chatList"
        binding.toolBarTitle.text = storeName
        mainViewModel.getChatMessage(sellerId, chatId)

        orderIdData = arguments?.getString("orderId") ?: ""
        orderDateTime = arguments?.getString("orderDateTime") ?: ""
        orderAmount = arguments?.getString("orderAmount") ?: ""
        qty = arguments?.getString("itemsCount") ?: ""

        with(binding) {
            ordersLayout.isVisible = !orderIdData.isNullOrEmpty()
            orderDate.isVisible = !orderDateTime.isNullOrEmpty()
            amount.isVisible = !orderAmount.isNullOrEmpty()

            orderId.text = "# $orderIdData"
            orderDate.text = orderDateTime
            amount.text = "RM $orderAmount ($qty Items)"

        }

        binding.root.viewTreeObserver.addOnGlobalLayoutListener {
            val r = Rect()
            binding.root.getWindowVisibleDisplayFrame(r)
            val screenHeight: Int = binding.edtMessage.rootView.height
            val heightDiff: Int = screenHeight - (r.bottom - r.top)
            val isKeyboardOpen = heightDiff > screenHeight * 0.15
            if (isKeyboardOpen) {
                if (!keyBoardOpened) {
                    keyBoardOpened = true
                    binding.chatRv.scrollToPosition(chatAdapter.itemCount - 1)
                }
            } else {
                keyBoardOpened = false
            }
        }

    }


    private fun setObserver() {
        mainViewModel.chatMessages.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    if (response.data?.httpcode == 200) {
                        setChatData(response.data.data.messages)
                    } else {
//                        showToast(response.data?.message.toString())
                    }
                }

                is Resource.Loading -> Unit
                is Resource.Error -> apiError(response.message)
            }
        }

        mainViewModel.sendMessage.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    if (response.data?.httpcode == 200) {
                        binding.edtMessage.setText("")
                        binding.gallery.setImageResource(R.drawable.ic_gallery)
                        if (response.data.data.chat_message.isNotEmpty()) {
                            response.data.data.chat_message[0].let {
                                messagesList.add(response.data.data.chat_message[0])
                                chatAdapter.differ.submitList(messagesList)
                                chatAdapter.notifyDataSetChanged()
                                binding.chatRv.postDelayed({
                                    binding.chatRv.scrollToPosition(chatAdapter.itemCount - 1)
                                }, 150)
                            }
                        }
                    } else {
                        showToast(response.data?.message.toString())
                    }
                }

                is Resource.Loading -> Unit
                is Resource.Error -> apiError(response.message)
            }
        }
    }

    private fun setChatData(messages: Messages) {

        messagesList = messages.messages.toMutableList()
        chatAdapter.differ.submitList(messagesList)
        binding.chatRv.postDelayed({
            binding.chatRv.scrollToPosition(chatAdapter.itemCount - 1)
        }, 150)
        with(binding) {
            ordersLayout.isVisible = messages.order_id != null
            orderIdData = messages.order_id
            saleId = if (messages.sale_id != null) messages.sale_id.toString() else ""
            orderId.text = "# ${messages.order_id}"
            orderDate.isVisible = false
            amount.isVisible = false


            ordersLayout.isVisible = !messages.order_id.isNullOrEmpty()
            orderDate.isVisible = !messages.order_id.isNullOrEmpty()
            amount.isVisible = !messages.order_id.isNullOrEmpty()

            orderId.text = "# ${messages.order_id}"
            orderDate.text = messages.order_date.toString()
            amount.text = "RM $amount"
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
            back.setOnClickListener(this@ChatFragment)
            viewOrder.setOnClickListener(this@ChatFragment)
            sendMessage.setOnClickListener(this@ChatFragment)
            gallery.setOnClickListener(this@ChatFragment)

        }
    }

    private fun validAndSendMessage() {
        val message = binding.edtMessage.text.toString()
        if (containsSensitiveWords(message)) {
            val currentTime = LocalTime.now()
            val timeFormat = DateTimeFormatter.ofPattern("HH:mm:ss")
            val formattedTime = currentTime.format(timeFormat)

            binding.edtMessage.setText("")
            messagesList.map { it.warning = false }
            val warningMessage = Message(
                align = "right",
                chat_date = "",
                chat_time = formattedTime,
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
            chatAdapter.differ.submitList(messagesList)
            chatAdapter.notifyDataSetChanged()

            binding.chatRv.postDelayed({
                binding.chatRv.scrollToPosition(chatAdapter.itemCount - 1)
            }, 100)

        } else {

            val requestBodyMap = mutableMapOf<String, RequestBody?>()
            if (imgFile != null) {
                requestBodyMap["image\"; filename=\"message.jpg\""] =
                    imgFile?.asRequestBody("image/jpeg".toMediaTypeOrNull())
            }
            requestBodyMap["access_token"] = sessionManager.token.toRequestBody(MultipartBody.FORM)
            requestBodyMap["message"] = message.toRequestBody(MultipartBody.FORM)
            requestBodyMap["seller_id"] = sellerId.toRequestBody(MultipartBody.FORM)
            requestBodyMap["prd_id"] = productId.toRequestBody(MultipartBody.FORM)
            requestBodyMap["sale_id"] = saleId.toRequestBody(MultipartBody.FORM)
            mainViewModel.sendMessage(requestBodyMap)
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

                com.github.dhaval2404.imagepicker.ImagePicker.RESULT_ERROR -> {
                    showToast(com.github.dhaval2404.imagepicker.ImagePicker.getError(data))
                }

                else -> {
                    showToast("Task Cancelled")
                }
            }
        }
}