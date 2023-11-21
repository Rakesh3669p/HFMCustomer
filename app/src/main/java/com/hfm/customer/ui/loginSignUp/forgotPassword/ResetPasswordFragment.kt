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
import com.hfm.customer.utils.Loader
import com.hfm.customer.utils.NoInternetDialog
import com.hfm.customer.utils.Resource
import com.hfm.customer.utils.isValidEmail
import com.hfm.customer.utils.netWorkFailure
import com.hfm.customer.utils.showToast


class ResetPasswordFragment : Fragment() ,View.OnClickListener {
    private lateinit var binding: FragmentResetPasswordBinding
    private var currentView: View? = null
    private val loginSignUpViewModel: LoginSignUpViewModel by activityViewModels()
    private lateinit var appLoader:Loader
    private lateinit var noInternetDialog:NoInternetDialog

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

    private fun init(){
        appLoader = Loader(requireContext())
        noInternetDialog = NoInternetDialog(requireContext())
        noInternetDialog.setOnCancelListener { init() }
    }

    private fun setObserver() {
        loginSignUpViewModel.forgotPassword.observe(viewLifecycleOwner){response->
            when(response){
                is Resource.Success->{
                    appLoader.dismiss()
                    showToast("Reset password link sent to your register email address!")
                    findNavController().popBackStack()
                }

                is Resource.Loading->appLoader.show()
                is Resource.Error->apiError(response.message)
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
        with(binding){
            sendResetLink.setOnClickListener(this@ResetPasswordFragment)
            back.setOnClickListener(this@ResetPasswordFragment)
        }
    }

    private fun sendResetLink() {
        val email = binding.email.text.toString()
        if(!email.isValidEmail()){
            showToast("Please enter an valid email id")
            return
        }
        loginSignUpViewModel.forgotPassword(email)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.sendResetLink.id-> sendResetLink()
            binding.back.id-> findNavController().popBackStack()
        }
    }


}