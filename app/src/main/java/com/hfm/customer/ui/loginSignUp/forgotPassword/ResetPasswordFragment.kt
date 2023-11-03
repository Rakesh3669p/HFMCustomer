package com.hfm.customer.ui.loginSignUp.forgotPassword

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.hfm.customer.R
import com.hfm.customer.databinding.FragmentResetPasswordBinding
import com.hfm.customer.ui.loginSignUp.LoginSignUpViewModel


class ResetPasswordFragment : Fragment() ,View.OnClickListener {
    private lateinit var binding: FragmentResetPasswordBinding
    private var currentView: View? = null
    private val loginSignUpViewModel: LoginSignUpViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_reset_password, container, false)
            binding = FragmentResetPasswordBinding.bind(currentView!!)
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
    }

    private fun setObserver() {}

    private fun setOnClickListener() {
        with(binding){
            sendResetLink.setOnClickListener(this@ResetPasswordFragment)
            back.setOnClickListener(this@ResetPasswordFragment)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.sendResetLink.id->{}
            binding.back.id-> findNavController().popBackStack()
        }
    }
}