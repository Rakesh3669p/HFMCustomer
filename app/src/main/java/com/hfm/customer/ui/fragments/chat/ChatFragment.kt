package com.hfm.customer.ui.fragments.chat

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.hfm.customer.R
import com.hfm.customer.databinding.FragmentChatBinding
import com.hfm.customer.ui.fragments.chat.adapter.ChatAdapter
import com.hfm.customer.ui.fragments.chat.adapter.ChatUserAdapter
import com.hfm.customer.utils.initRecyclerView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ChatFragment : Fragment() , View.OnClickListener {

    private lateinit var binding: FragmentChatBinding
    private var currentView: View? = null

    @Inject lateinit var chatAdapter: ChatAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_chat, container, false)
            binding = FragmentChatBinding.bind(currentView!!)
            init()
            setRecyclerViews()
            setObserver()
            setOnClickListener()
        }
        return currentView!!
    }

    private fun init() {
        initRecyclerView(requireContext(),binding.chatRv,chatAdapter)
    }

    private fun setRecyclerViews() {
        with(binding){
        }
    }


    private fun setObserver() {}


    private fun setOnClickListener() {
        with(binding) {
            back.setOnClickListener (this@ChatFragment)
        }
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            binding.back.id->findNavController().popBackStack()
        }
    }
}