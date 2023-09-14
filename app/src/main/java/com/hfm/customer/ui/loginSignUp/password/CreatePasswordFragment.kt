package com.hfm.customer.ui.loginSignUp.password

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.hfm.customer.R
import com.hfm.customer.databinding.FragmentCreatePasswordBinding
import com.hfm.customer.databinding.FragmentOtpBinding
import com.hfm.customer.ui.dashBoard.DashBoardActivity
import com.hfm.customer.ui.loginSignUp.LoginSignUpViewModel
import com.hfm.customer.utils.Loader
import com.hfm.customer.utils.Resource
import com.hfm.customer.utils.netWorkFailure
import com.hfm.customer.utils.showToast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CreatePasswordFragment : Fragment(),View.OnClickListener {
    private lateinit var binding: FragmentCreatePasswordBinding
    private var currentView: View? = null
    private val loginSignUpViewModel: LoginSignUpViewModel by activityViewModels()

    private lateinit var appLoader:Loader

    private var firstName = ""
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
            setObserver()
            setOnClickListener()
        }
        return currentView!!
    }

    private fun init() {
        appLoader = Loader(requireContext())
        customerType = arguments?.getString("customerType").toString()
        firstName = arguments?.getString("name").toString()
        email = arguments?.getString("email").toString()
    }

    private fun setObserver() {
        loginSignUpViewModel.registerUser.observe(viewLifecycleOwner){response->
            when(response){
                is Resource.Success->{
                    appLoader.dismiss()
                    showToast("Registered Successfully")
                    if(response.data?.httpcode==200){
                        startActivity(Intent(requireActivity(),DashBoardActivity::class.java))
                        requireActivity().finish()
                    }
                }
                is Resource.Loading-> appLoader.show()
                is Resource.Error->{
                    showToast(response.message.toString())
                    if(response.message.toString() == netWorkFailure){

                    }
                }
            }

        }
    }

    private fun setOnClickListener() {
        with(binding){
            register.setOnClickListener(this@CreatePasswordFragment)
        }
    }

    private fun validateAndRegister() {
        password = binding.password.text.toString().trim()
        confirmPassword = binding.confirmPassword.text.toString().trim()
        if (password.length <= 8) {
            showToast("password length must be more than 8")
        } else if (password != confirmPassword) {
            showToast("Passwords do not match.")
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

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.register.id->{
                validateAndRegister()
            }
        }

    }
}