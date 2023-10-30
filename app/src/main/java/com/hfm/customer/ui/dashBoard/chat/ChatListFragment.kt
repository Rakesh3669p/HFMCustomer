package com.hfm.customer.ui.dashBoard.chat

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import com.hfm.customer.R
import com.hfm.customer.databinding.DialogNoChatBinding
import com.hfm.customer.databinding.DialogueMediaPickupBinding
import com.hfm.customer.databinding.FragmentChatListBinding
import com.hfm.customer.ui.dashBoard.chat.adapter.ChatUserAdapter
import com.hfm.customer.ui.loginSignUp.LoginActivity
import com.hfm.customer.utils.Loader
import com.hfm.customer.utils.NoInternetDialog
import com.hfm.customer.utils.Resource
import com.hfm.customer.utils.SessionManager
import com.hfm.customer.utils.initRecyclerView
import com.hfm.customer.utils.netWorkFailure
import com.hfm.customer.utils.showToast
import com.hfm.customer.viewModel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ChatListFragment : Fragment(), View.OnClickListener {

    private var orderCount: Int = 0
    private lateinit var binding: FragmentChatListBinding
    private var currentView: View? = null
    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var chatUserAdapter: ChatUserAdapter

    private lateinit var appLoader: Loader
    private lateinit var noInternetDialog: NoInternetDialog

    private val mainViewModel: MainViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_chat_list, container, false)
            binding = FragmentChatListBinding.bind(currentView!!)
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
        mainViewModel.getChatList()

    }

    private fun setObserver() {
        mainViewModel.chatList.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    if (response.data?.httpcode == 200) {
                        orderCount = response.data.data.order_count
                        if (response.data.data.list.isNotEmpty()) {
                            initRecyclerView(requireContext(), binding.chatUsersRv, chatUserAdapter)
                            chatUserAdapter.differ.submitList(response.data.data.list)
                        }
                    }  else if (response.data?.httpcode == 401) {
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

    private fun apiError(message: String?) {
        appLoader.dismiss()
        showToast(message.toString())
        if (message == netWorkFailure) {
            noInternetDialog.show()
        }
    }

    private fun showAlertDialog() {
        val appCompatDialog = Dialog(requireContext())
        val bindingDialog = DialogNoChatBinding.inflate(layoutInflater)
        appCompatDialog.setContentView(bindingDialog.root)
        appCompatDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        appCompatDialog.setCancelable(false)
        appCompatDialog.show()
        bindingDialog.ok.setOnClickListener {
            appCompatDialog.dismiss()
        }
    }

    private fun setOnClickListener() {
        with(binding) {
            back.setOnClickListener(this@ChatListFragment)
            createNewChat.setOnClickListener(this@ChatListFragment)

        }

        chatUserAdapter.setOnChatClickListener {
            val bundle = Bundle().apply {
                putString("from", "chatList")
                putString("sellerId", (it.seller_id))
                putInt("chatId", it.chat_id)
                putString("storeName", it.store_name)
            }
            findNavController().navigate(R.id.chatFragment, bundle)
        }
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            binding.back.id -> findNavController().popBackStack()
            binding.createNewChat.id -> {
                if (sessionManager.isLogin) {
                    if (orderCount <= 0) {
                        showAlertDialog()
                        return
                    }
                    val bundle = Bundle()
                    bundle.putString("from", "chat")
                    findNavController().navigate(R.id.myAllOrdersFragment, bundle)
                } else {
                    startActivity(Intent(requireContext(), LoginActivity::class.java))
                    requireActivity().finish()
                }
            }
        }
    }


}