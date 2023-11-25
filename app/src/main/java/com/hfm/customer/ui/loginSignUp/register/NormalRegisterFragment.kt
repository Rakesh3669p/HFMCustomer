package com.hfm.customer.ui.loginSignUp.register

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.text.Html
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatDialog
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.findNavController
import com.hfm.customer.R
import com.hfm.customer.databinding.FragmentNormalRegisterBinding
import com.hfm.customer.databinding.TermsAndConditionsPopUpBinding
import com.hfm.customer.ui.loginSignUp.LoginSignUpViewModel
import com.hfm.customer.utils.Loader
import com.hfm.customer.utils.Resource
import com.hfm.customer.utils.isValidEmail
import com.hfm.customer.utils.netWorkFailure
import com.hfm.customer.utils.showToast
import com.hfm.customer.viewModel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class NormalRegisterFragment : Fragment(), View.OnClickListener {
    private var termsAndCondition: String = ""

    private lateinit var binding: FragmentNormalRegisterBinding
    private var currentView: View? = null
    private val loginSignUpViewModel: LoginSignUpViewModel by viewModels()
    private lateinit var appLoader: Loader
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_normal_register, container, false)
            binding = FragmentNormalRegisterBinding.bind(currentView!!)
            init()
            setObserver()
            setOnClickListener()
        }
        return currentView!!
    }

    private fun init() {
        appLoader = Loader(requireContext())
        loginSignUpViewModel.getTermsConditions()

    }

    private fun setObserver() {

        loginSignUpViewModel.sendRegisterOtp.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    if (response.data?.httpcode == 200) {
                        showToast(response.data.message)
                        val name = binding.name.text.toString().trim()
                        val email = binding.email.text.toString().trim()
                        val bundle = Bundle()
                        bundle.apply { putString("name", name) }
                        bundle.apply { putString("email", email) }
                        bundle.apply { putString("type", "normal") }
                        findNavController().navigate(R.id.otpFragment, bundle)
                    } else {
                        showToast(response.data?.message.toString())
                    }
                }

                is Resource.Loading -> appLoader.show()
                is Resource.Error -> {
                    appLoader.dismiss()
                    if (response.message == netWorkFailure) {
                        showToast("Check your internet connection")
                    }
                }
            }
        }
        loginSignUpViewModel.termsConditions.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    if (response.data?.httpcode == "200") {
                        termsAndCondition = response.data.data.terms_conditions.customer_register
                    } else {
                        showToast(response.data?.message.toString())
                    }
                }

                is Resource.Loading -> appLoader.show()
                is Resource.Error -> {
                    appLoader.dismiss()
                    if (response.message == netWorkFailure) {
                        showToast("Check your internet connection")
                    }
                }
            }
        }
    }

    private fun setOnClickListener() {
        with(binding) {
            sendOtp.setOnClickListener(this@NormalRegisterFragment)
            termsAndConditions.setOnClickListener(this@NormalRegisterFragment)
            alreadyHaveAccount.setOnClickListener(this@NormalRegisterFragment)
        }
    }

    private fun sendOTP() {

        val email = binding.email.text.toString()
        val name = binding.name.text.toString()
        if (!name.isValidEmail()) {
            showToast("Please enter a valid name")
            return
        }
        if (!email.isValidEmail()) {
            showToast("Please enter a valid email")
            return
        }

        if (!binding.termsAndConditionsCheckBox.isChecked) {
            showToast("Please agree terms and conditions")
            return
        }

        loginSignUpViewModel.sendRegisterOTP(email)
    }

    private fun showTermsAndConditions() {
        val appCompatDialog = AppCompatDialog(requireContext())
        val bindingDialog = TermsAndConditionsPopUpBinding.inflate(layoutInflater)
        appCompatDialog.setContentView(bindingDialog.root)
        appCompatDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        appCompatDialog.window!!.attributes.width = LinearLayout.LayoutParams.MATCH_PARENT
        appCompatDialog.setCancelable(false)

        bindingDialog.description.settings.javaScriptEnabled = true
        bindingDialog.description.loadDataWithBaseURL(null, termsAndCondition, "text/html", "UTF-8", null)
        bindingDialog.description.webViewClient = WebViewClient()
        bindingDialog.description.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                view!!.context.startActivity(Intent(Intent.ACTION_VIEW, request?.url))
                return true
            }
        }


        bindingDialog.decline.isVisible = false
        bindingDialog.accept.isVisible = false
        bindingDialog.agreeCheckBox.isVisible = false
        appCompatDialog.show()
        bindingDialog.close.setOnClickListener {
            appCompatDialog.dismiss()
        }
        bindingDialog.decline.setOnClickListener {
            appCompatDialog.dismiss()
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.sendOtp.id -> sendOTP()
            binding.termsAndConditions.id -> showTermsAndConditions()
            binding.alreadyHaveAccount.id -> findNavController().popBackStack()

        }

    }


}