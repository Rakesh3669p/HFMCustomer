package com.hfm.customer.ui.dashBoard.chat

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import com.hfm.customer.ui.dashBoard.chat.model.Message
import com.hfm.customer.ui.dashBoard.chat.model.Messages
import com.hfm.customer.ui.loginSignUp.LoginActivity
import com.hfm.customer.utils.Loader
import com.hfm.customer.utils.NoInternetDialog
import com.hfm.customer.utils.Resource
import com.hfm.customer.utils.SessionManager
import com.hfm.customer.utils.containsSensitiveWords
import com.hfm.customer.utils.createFileFromContentUri
import com.hfm.customer.utils.formatToTwoDecimalPlaces
import com.hfm.customer.utils.hasEmailAddress
import com.hfm.customer.utils.hasNumberGreaterThan10Digits
import com.hfm.customer.utils.initRecyclerView
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
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
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
    private var keyBoardOpened = false
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

        val spannable = SpannableString(getString(R.string.view_order_lbl))
        spannable.setSpan(UnderlineSpan(), 0, getString(R.string.view_order_lbl).length, 0)
        binding.viewOrder.text = spannable

        val noticeSpannable = SpannableString("Notice")
        noticeSpannable.setSpan(UnderlineSpan(), 0, "Notice".length, 0)
        binding.noticeMessage.notice.text = noticeSpannable

        appLoader = Loader(requireContext())
        noInternetDialog = NoInternetDialog(requireContext())
        noInternetDialog.setOnDismissListener { init() }

        val from = arguments?.getString("from").toString()
        val storeName = arguments?.getString("storeName").toString()
        sellerId = arguments?.getString("sellerId").toString()
        chatId = arguments?.getInt("chatId").toString()
        saleId = arguments?.getString("saleId").toString()
        binding.noticeMessage.root.isVisible = from == "chatList"
//        startAutoScrollLoop()
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

            orderId.text = "Order #: $orderIdData"

            if (orderDate.isVisible) {
                orderDate.text = orderDateTime.toChatFormattedDate()
            }
            if (amount.isVisible) {
                amount.text = "RM ${formatToTwoDecimalPlaces(orderAmount.toDouble())} ($qty Items)"
            }

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

    private val handler: Handler = Handler(Looper.getMainLooper())
    private val scrollSpeed = 1


    private fun startAutoScrollLoop() {
        /*  handler.postDelayed(object : Runnable {
              override fun run() {
                  binding.titleScroll.scrollBy(0, scrollSpeed)

                  if (binding.titleScroll.scrollY >= binding.toolBarTitle.height - binding.titleScroll.height) {
                      binding.titleScroll.scrollTo(0, 0)
                  }

                  handler.postDelayed(this, 10) // Adjust the delay as needed
              }
          }, 10)*/
    }

    private fun setObserver() {
        mainViewModel.chatMessages.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    when (response.data?.httpcode) {
                        200 -> {
                            setChatData(response.data.data.messages)
                        }

                        401 -> {
                            sessionManager.isLogin = false
                            startActivity(Intent(requireActivity(), LoginActivity::class.java))
                            requireActivity().finish()
                        }

                        else -> {
                            //                        showToast(response.data?.message.toString())
                        }
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
                    binding.sendMessage.isClickable = true
                    if (response.data?.httpcode == 200) {
                        imgFile = null
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
                    } else if (response.data?.httpcode == 401) {
                        sessionManager.isLogin = false
                        startActivity(Intent(requireActivity(), LoginActivity::class.java))
                        requireActivity().finish()
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
        chatAdapter.setSellerName(messages.seller_name)
        binding.chatRv.postDelayed({
            binding.chatRv.scrollToPosition(chatAdapter.itemCount - 1)
        }, 150)
        with(binding) {
            ordersLayout.isVisible = messages.order_id != null
            orderIdData = messages.order_id
            saleId = if (messages.sale_id != null) messages.sale_id.toString() else ""

            ordersLayout.isVisible = !messages.order_id.isNullOrEmpty()
            orderDate.isVisible = !messages.order_id.isNullOrEmpty()
            amount.isVisible = !messages.order_id.isNullOrEmpty()

            orderId.text = "Order #: ${messages.order_id}"
            orderDate.text = "${messages.order_date?.toChatFormattedDate()}"
            amount.text = "RM ${
                messages.grand_total?.toDouble()
                    ?.let { formatToTwoDecimalPlaces(it) }
            } (${messages.quantity} Items)"
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
        binding.sendMessage.isClickable = true
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
        if (message.isEmpty() && imgFile == null) {
            showToast("Please type a message to send..")
            return
        }

        if (containsSensitiveWords(message) || hasNumberGreaterThan10Digits(message) || hasEmailAddress(
                message
            )
        ) {
            val currentDateTime = LocalDateTime.now()

            val timeFormat = DateTimeFormatter.ofPattern("HH:mm:ss")
            val dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            val dateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")


            val formattedTime = currentDateTime.format(timeFormat)
            val createdAt = currentDateTime.format(dateTimeFormat)
            val chatDate = currentDateTime.format(dateFormat)

            binding.edtMessage.setText("")
            messagesList.map { it.warning = false }
            val warningMessage = Message(
                align = "right",
                chat_date = chatDate,
                chat_time = formattedTime,
                created_at = createdAt,
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
            binding.sendMessage.isClickable = false
            mainViewModel.sendMessage(requestBodyMap)
        }
    }

    private fun showImagePickupDialog() {
        val appCompatDialog = Dialog(requireContext())
        val bindingDialog = DialogueMediaPickupBinding.inflate(layoutInflater)
        appCompatDialog.setContentView(bindingDialog.root)
        appCompatDialog.setCancelable(true)
        appCompatDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))


        appCompatDialog.setCancelable(true)
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