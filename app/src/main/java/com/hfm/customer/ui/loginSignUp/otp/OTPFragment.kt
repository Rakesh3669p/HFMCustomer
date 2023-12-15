package com.hfm.customer.ui.loginSignUp.otp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.work.impl.utils.INITIAL_ID
import com.hfm.customer.R
import com.hfm.customer.databinding.FragmentOtpBinding
import com.hfm.customer.ui.dashBoard.DashBoardActivity
import com.hfm.customer.ui.loginSignUp.LoginSignUpViewModel
import com.hfm.customer.utils.Loader
import com.hfm.customer.utils.NoInternetDialog
import com.hfm.customer.utils.Resource
import com.hfm.customer.utils.business
import com.hfm.customer.utils.netWorkFailure
import com.hfm.customer.utils.showToast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OTPFragment : Fragment(), View.OnClickListener {

    private var phoneCode: String = ""
    private lateinit var binding: FragmentOtpBinding
    private var currentView: View? = null
    private val loginSignUpViewModel: LoginSignUpViewModel by activityViewModels()
    private lateinit var appLoader: Loader
    private lateinit var noInternetDialog: NoInternetDialog

    private var firstName = ""
    private var customerType = ""
    private var email = ""
    private var from = ""
    private var companyName = ""
    private var companyRegisterNo = ""
    private var companyContactNo = ""
    private var natureOfBusiness = ""
    private var country = ""
    private var state = ""
    private var city = ""
    private var refCode = ""

    private var countdownTimer: CountDownTimer? = null

    override fun onStart() {
        super.onStart()
        val imm = requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.root, 0)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_otp, container, false)
            binding = FragmentOtpBinding.bind(currentView!!)
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
        customerType = arguments?.getString("type").toString()
        customerType = arguments?.getString("type").toString()
        from = arguments?.getString("from").toString()
        firstName = arguments?.getString("name").toString()
        email = arguments?.getString("email").toString()
        refCode = arguments?.getString("refCode").toString()
        binding.otpField1.requestFocus()

        if (from == business) {
            arguments?.let {
                companyName = it.getString("companyName").toString()
                companyRegisterNo = it.getString("companyRegisterNo").toString()
                companyContactNo = it.getString("companyContactNo").toString()
                natureOfBusiness = it.getInt("natureOfBusiness").toString()
                country = it.getString("country").toString()
                state = it.getString("state").toString()
                city = it.getString("city").toString()
                phoneCode = it.getString("phoneCode").toString()
            }
        }

        with(binding) {
            otpField1.doOnTextChanged { _, _, _, count -> if (count > 0) otpField2.requestFocus() }
            otpField2.doOnTextChanged { _, _, _, count -> if (count > 0) otpField3.requestFocus() else otpField1.requestFocus() }
            otpField3.doOnTextChanged { _, _, _, count -> if (count > 0) otpField4.requestFocus() else otpField2.requestFocus() }
            otpField4.doOnTextChanged { _, _, _, count -> if (count == 0) otpField3.requestFocus() else if (count > 0) validateAndVerifyOTP() }
        }
        startCountdown()
    }


    private fun setObserver() {
        loginSignUpViewModel.registerVerifyOtp.observe(viewLifecycleOwner) { response ->
            when (response) {

                is Resource.Success -> {
                    appLoader.dismiss()
                    if (response.data?.httpcode == 200) {
                        if (from == business) {
                            val bundle = Bundle().apply {
                                putString("companyName", companyName)
                                putString("companyRegisterNo", companyRegisterNo)
                                putString("companyContactNo", companyContactNo)
                                putString("natureOfBusiness", natureOfBusiness)
                                putString("email", email)
                                putString("country", country)
                                putString("state", state)
                                putString("city", city)
                                putString("phoneCode", phoneCode)
                            }
                            findNavController().popBackStack()
                            findNavController().navigate(
                                R.id.businessCreatePasswordFragment,
                                bundle
                            )

                        } else {
                            val bundle = Bundle().apply {
                                putString("name", firstName)
                                putString("email", email)
                                putString("refCode", refCode)
                                putString("customerType", customerType)
                            }

                            findNavController().popBackStack()
                            findNavController().navigate(R.id.createPasswordFragment, bundle)
                        }


                    } else showToast(response.data?.message.toString())
                }

                is Resource.Loading -> appLoader.show()
                is Resource.Error -> apiError(response.message)
            }

        }

        loginSignUpViewModel.registerUser.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    showToast(response.data?.message.toString())
                    if (response.data?.httpcode == 200) {
                        if (customerType == "normal") {
                            startActivity(Intent(requireContext(), DashBoardActivity::class.java))
                            requireActivity().finish()
                        }
                    }
                }

                is Resource.Loading -> Unit
                is Resource.Error -> apiError(response.message)
            }
        }
        loginSignUpViewModel.sendRegisterOtp.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    showToast(response.data?.message.toString())
                    if (response.data?.httpcode == 200) {
                        startCountdown()
                    }
                }

                is Resource.Loading -> appLoader.show()
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

    private fun setOnClickListener() {
        with(binding) {
            verify.setOnClickListener(this@OTPFragment)
            resend.setOnClickListener(this@OTPFragment)
        }
    }

    private fun validateAndVerifyOTP() {
        with(binding) {
            val otp = "${otpField1.text}${otpField2.text}${otpField3.text}${otpField4.text}"
            if (otp.isEmpty()) {
                showToast("Please Enter a valid OTP")
                return
            }
            if (from == business) {
                loginSignUpViewModel.registerVerifyOtp(email, otp)
            } else {
                loginSignUpViewModel.registerVerifyOtp(email, otp)
            }
        }
    }

    private fun startCountdown() {
        countdownTimer = object : CountDownTimer(180000, 1000) { // 30 seconds, tick every 1 second
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = millisUntilFinished / 1000
                binding.alreadyHaveAccount.text = "Resend OTP in: "
                binding.resend.text = "$secondsRemaining seconds"
                binding.resend.isClickable = false
            }

            override fun onFinish() {
                binding.alreadyHaveAccount.text = getString(R.string.receiveOtp)
                binding.resend.text = getString(R.string.resendOtp)
                binding.resend.isClickable = true

                // You can enable a "Resend" button or perform the resend OTP action here.
            }
        }.start()
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            binding.verify.id -> validateAndVerifyOTP()
            binding.resend.id -> loginSignUpViewModel.sendRegisterOTP(email)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        countdownTimer?.cancel()
    }
}