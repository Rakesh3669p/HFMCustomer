package com.hfm.customer.ui.loginSignUp.password

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.hfm.customer.R
import com.hfm.customer.databinding.FragmentCreatePasswordBinding
import com.hfm.customer.ui.loginSignUp.LoginSignUpViewModel
import com.hfm.customer.utils.Loader
import com.hfm.customer.utils.NoInternetDialog
import com.hfm.customer.utils.Resource
import com.hfm.customer.utils.SessionManager
import com.hfm.customer.utils.netWorkFailure
import com.hfm.customer.utils.showToast
import com.hfm.customer.viewModel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

@AndroidEntryPoint
class CreatePasswordFragment : Fragment(), View.OnClickListener {
    private lateinit var binding: FragmentCreatePasswordBinding
    private var currentView: View? = null
    private val loginSignUpViewModel: LoginSignUpViewModel by activityViewModels()
    private val mainViewModel: MainViewModel by viewModels()

    private lateinit var appLoader: Loader
    private lateinit var noInternetDialog: NoInternetDialog
    @Inject
    lateinit var sessionManager: SessionManager

    private var from = ""

    private var firstName = ""
    private var dob = ""
    private var customerType = ""
    private var email = ""
    private var password = ""
    private var confirmPassword = ""
    private var refCode = ""
    private var businessCategory = ""
    private var registrationNo = ""
    private var countryCode = ""
    private var contactNo = ""
    private var address = ""
    private var country = ""
    private var state = ""
    private var city = ""
    private var pinCode = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_create_password, container, false)
            binding = FragmentCreatePasswordBinding.bind(currentView!!)
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

        customerType = arguments?.getString("customerType").toString()
        firstName = arguments?.getString("name").toString()
        email = arguments?.getString("email").toString()
        from = arguments?.getString("from").toString()
        binding.back.isVisible = from == "profile"
        if (from == "profile") {
            with(binding) {
                firstName = arguments?.getString("name").toString()
                email = arguments?.getString("email").toString()
                dob = arguments?.getString("dob").toString()
                titleLbl.text = requireActivity().getString(R.string.change_password_lbl)
                register.text = requireActivity().getString(R.string.updateLbl)
                alreadyHaveAccount.isVisible = false
            }
        }
    }

    private fun setObserver() {
        loginSignUpViewModel.registerUser.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    if (response.data?.httpcode == 200) {
                        showToast("Registered Successfully")
                        findNavController().navigate(R.id.action_createPasswordFragment_to_loginFragment)
                    }else{
                        showToast(response.data?.message.toString())
                    }
                }

                is Resource.Loading -> appLoader.show()
                is Resource.Error -> apiError(response.message)
            }
        }

        mainViewModel.updateProfileCustomer.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    if (response.data?.httpcode == 200) {
                        showToast("Password updated successfully")
                        findNavController().popBackStack()
                    }
                }

                is Resource.Loading -> appLoader.show()
                is Resource.Error -> apiError(response.message)
            }
        }
    }

    private fun setOnClickListener() {
        with(binding) {
            register.setOnClickListener(this@CreatePasswordFragment)
            back.setOnClickListener(this@CreatePasswordFragment)
        }
    }

    private fun validateAndRegister() {

        password = binding.password.text.toString().trim()
        confirmPassword = binding.confirmPassword.text.toString().trim()
        if (password.length <= 7) {
            showToast("Min 8 characters required.")
        } else if (password != confirmPassword) {
            showToast("Password and confirm password does not match!")
        } else {
            if (from == "profile") {
                updatePassword()

            } else {
                loginSignUpViewModel.registerUser(
                    firstName,
                    customerType,
                    email,
                    password,
                    confirmPassword,
                    refCode,
                    businessCategory,
                    registrationNo,
                    countryCode,
                    contactNo,
                    address,
                    country,
                    state,
                    city,
                    pinCode
                )
            }

        }
    }

    private fun updatePassword() {
        val requestBodyMap = mutableMapOf<String, RequestBody?>()
        requestBodyMap["access_token"] = sessionManager.token.toRequestBody(MultipartBody.FORM)
        requestBodyMap["first_name"] = firstName.toRequestBody(MultipartBody.FORM)
        requestBodyMap["email"] = email.toRequestBody(MultipartBody.FORM)
        requestBodyMap["birthday"] = dob.toRequestBody(MultipartBody.FORM)
        requestBodyMap["os_type"] = "APP".toRequestBody(MultipartBody.FORM)
        requestBodyMap["device_id"] = sessionManager.deviceId.toRequestBody(MultipartBody.FORM)
        requestBodyMap["password"] = password.toRequestBody(MultipartBody.FORM)
        requestBodyMap["password_confirmation"] = password.toRequestBody(MultipartBody.FORM)
        mainViewModel.updateProfileCustomer(requestBodyMap)
    }

    private fun apiError(message: String?) {
        appLoader.dismiss()
        showToast(message.toString())
        if (message == netWorkFailure) {
            noInternetDialog.show()
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.register.id -> validateAndRegister()
            binding.back.id -> findNavController().popBackStack()
        }
    }
}