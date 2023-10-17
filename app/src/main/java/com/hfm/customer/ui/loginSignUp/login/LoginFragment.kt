package com.hfm.customer.ui.loginSignUp.login

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDialog
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.facebook.CallbackManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.hfm.customer.R
import com.hfm.customer.databinding.FragmentLoginBinding
import com.hfm.customer.databinding.TermsAndConditionsPopUpBinding
import com.hfm.customer.ui.dashBoard.DashBoardActivity
import com.hfm.customer.ui.loginSignUp.LoginSignUpViewModel
import com.hfm.customer.utils.Loader
import com.hfm.customer.utils.NoInternetDialog
import com.hfm.customer.utils.Resource
import com.hfm.customer.utils.SessionManager
import com.hfm.customer.utils.getDeviceId
import com.hfm.customer.utils.isValidEmail
import com.hfm.customer.utils.netWorkFailure
import com.hfm.customer.utils.showToast
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LoginFragment : Fragment(), View.OnClickListener {
    private lateinit var binding: FragmentLoginBinding
    private var currentView: View? = null
    private val loginSignUpViewModel: LoginSignUpViewModel by activityViewModels()
    @Inject
    lateinit var sessionManager: SessionManager
    private lateinit var appLoader: Loader
    private lateinit var noInternetDialog: NoInternetDialog
    private var mGoogleSignInClient: GoogleSignInClient? = null
    private var callbackManager: CallbackManager? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_login, container, false)
            binding = FragmentLoginBinding.bind(currentView!!)
            init()
            setObserver()
            setOnClickListener()
        }
        return currentView!!
    }

    private fun init() {
        appLoader = Loader(requireContext())
        noInternetDialog = NoInternetDialog(requireContext())
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(requireContext(), gso)
    }

    private fun setObserver() {
        loginSignUpViewModel.login.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    if (response.data?.httpcode == 200) {
                        sessionManager.isLogin = true
                        sessionManager.token = response.data.data.access_token
                        startActivity(Intent(requireActivity(), DashBoardActivity::class.java))
                        requireActivity().finish()
                    } else {
                        showToast(response.data?.message.toString())
                    }
                }

                is Resource.Loading -> appLoader.show()
                is Resource.Error -> {
                    appLoader.dismiss()
                    showToast(response.message.toString())
                    if (response.message.toString() == netWorkFailure) {
                        noInternetDialog.show()
                    }
                }
            }
        }

        loginSignUpViewModel.socialLogin.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    if (response.data?.httpcode == 200) {
                        sessionManager.isLogin = true
                        sessionManager.token = response.data.data.access_token
                        startActivity(Intent(requireActivity(), DashBoardActivity::class.java))
                        requireActivity().finish()
                    } else {
                        showToast(response.data?.message.toString())
                    }
                }

                is Resource.Loading -> appLoader.show()
                is Resource.Error -> {
                    appLoader.dismiss()
                    showToast(response.message.toString())
                    if (response.message.toString() == netWorkFailure) {
                        noInternetDialog.show()
                    }
                }
            }
        }
    }

    private fun setOnClickListener() {
        with(binding) {
            newToHFM.setOnClickListener(this@LoginFragment)
            login.setOnClickListener(this@LoginFragment)
            forgotPassword.setOnClickListener(this@LoginFragment)
            skipLbl.setOnClickListener(this@LoginFragment)
            facebook.setOnClickListener(this@LoginFragment)
            google.setOnClickListener(this@LoginFragment)
        }
    }

    private fun validateAndLogin() {
        val email = binding.email.text.toString()
        val password = binding.password.text.toString()
        if (!email.isValidEmail() || password.isEmpty()) {
            showToast("please enter valid email id or password")
            return
        }
        loginSignUpViewModel.login(email, password, getDeviceId(requireContext()))
    }


    private val googleSignInLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val task: Task<GoogleSignInAccount> =
                    GoogleSignIn.getSignedInAccountFromIntent(result.data)
                handleSignInResult(task)
            }
        }

    private fun handleSignInResult(task: Task<GoogleSignInAccount>) {
        try {
            val account = task.getResult(ApiException::class.java)
            var profileAvatar = ""
            if (account.photoUrl != null) {
                profileAvatar = account.photoUrl.toString()
            }
            sessionManager.loginType = "G"
            loginSignUpViewModel.socialLogin(
                email = account.email.toString(),
                name = account.displayName.toString(),
                avatar = profileAvatar,
                loginId = account.id.toString(),
                type = "google",
                deviceId = getDeviceId(requireContext())
            )
        } catch (e: ApiException) {
            showToast("Google Login failed")
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.login.id -> validateAndLogin()
            binding.newToHFM.id -> findNavController().navigate(R.id.registerFragment)
            binding.forgotPassword.id -> findNavController().navigate(R.id.resetPasswordFragment)
            binding.skipLbl.id -> {
                startActivity(Intent(requireContext(), DashBoardActivity::class.java))
                requireActivity().finish()
            }

            binding.facebook.id -> {}
            binding.google.id -> googleSignInLauncher.launch(mGoogleSignInClient!!.signInIntent)
        }
    }
}