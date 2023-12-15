package com.hfm.customer.ui.loginSignUp.register

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDialog
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.GraphRequest
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.hfm.customer.R
import com.hfm.customer.databinding.FragmentNormalRegisterBinding
import com.hfm.customer.databinding.TermsAndConditionsPopUpBinding
import com.hfm.customer.ui.dashBoard.DashBoardActivity
import com.hfm.customer.ui.loginSignUp.LoginSignUpViewModel
import com.hfm.customer.utils.Loader
import com.hfm.customer.utils.NoInternetDialog
import com.hfm.customer.utils.Resource
import com.hfm.customer.utils.SessionManager
import com.hfm.customer.utils.isValidEmail
import com.hfm.customer.utils.netWorkFailure
import com.hfm.customer.utils.showToast
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONException
import javax.inject.Inject


@AndroidEntryPoint
class NormalRegisterFragment : Fragment(), View.OnClickListener {
    private var termsAndCondition: String = ""

    private lateinit var binding: FragmentNormalRegisterBinding
    private var currentView: View? = null
    private val loginSignUpViewModel: LoginSignUpViewModel by viewModels()
    private lateinit var appLoader: Loader
    private var mGoogleSignInClient: GoogleSignInClient? = null
    private var callbackManager: CallbackManager? = null
    private lateinit var noInternetDialog: NoInternetDialog
    @Inject lateinit var sessionManager:SessionManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_normal_register, container, false)
            binding = FragmentNormalRegisterBinding.bind(currentView!!)
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
        loginSignUpViewModel.getTermsConditions()
        noInternetDialog = NoInternetDialog(requireContext())
        noInternetDialog.setOnDismissListener { init() }
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
                        val refCode = binding.referral.text.toString().trim()
                        val bundle = Bundle()
                        bundle.apply { putString("name", name) }
                        bundle.apply { putString("email", email) }
                        bundle.apply { putString("refCode", refCode) }
                        bundle.apply { putString("type", "normal") }
                        findNavController().popBackStack()
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
            sendOtp.setOnClickListener(this@NormalRegisterFragment)
            termsAndConditions.setOnClickListener(this@NormalRegisterFragment)
            alreadyHaveAccount.setOnClickListener(this@NormalRegisterFragment)
            facebook.setOnClickListener(this@NormalRegisterFragment)
            google.setOnClickListener(this@NormalRegisterFragment)
        }
    }

    private fun sendOTP() {

        val email = binding.email.text.toString()
        val name = binding.name.text.toString()
        if (name.isEmpty()) {
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
        bindingDialog.description.scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY
        bindingDialog.description.isHorizontalScrollBarEnabled = true
        bindingDialog.description.isVerticalScrollBarEnabled = true

        bindingDialog.description.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                view!!.context.startActivity(Intent(Intent.ACTION_VIEW, request?.url))
                return true
            }
        }

        bindingDialog.description.loadDataWithBaseURL(null, termsAndCondition, "text/html", "UTF-8", null)


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
    private val googleSignInLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val task: Task<GoogleSignInAccount> =
                    GoogleSignIn.getSignedInAccountFromIntent(result.data)
                handleSignInResult(task)
            }
        }
    private fun faceBookLogin() {
        callbackManager = CallbackManager.Factory.create()
        LoginManager.getInstance().logIn(this, listOf("public_profile", "email"))
        LoginManager.getInstance().registerCallback(callbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(loginResult: LoginResult) {
                    if (loginResult.accessToken.token != null) {
                        requestData()
                    }
                }

                override fun onCancel() {
                    Log.d("MainActivity", "Facebook onCancel.")
                }

                override fun onError(error: FacebookException) {
                    Log.d("MainActivity", "Facebook onError.")
                }
            })
    }

    private fun requestData() {
        val request = GraphRequest.newMeRequest(
            AccessToken.getCurrentAccessToken()
        ) { _, response ->
            val json = response!!.getJSONObject()
            try {
                if (json != null) {
                    var profileAvatar = ""
                    if (json.getJSONObject("picture") != null && json.getJSONObject("picture")
                            .getJSONObject("data") != null && json.getJSONObject("picture")
                            .getJSONObject("data").getString("url") != null
                    ) {
                        profileAvatar =
                            json.getJSONObject("picture").getJSONObject("data").getString("url")
                    }

                    sessionManager.loginType = "F"
                    loginSignUpViewModel.socialLogin(
                        email = json.getString("email"),
                        name = json.getString("name"),
                        avatar = profileAvatar,
                        loginId = json.getString("id"),
                        type = "google",
                        deviceId = sessionManager.deviceId
                    )

                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
        val parameters = Bundle()
        parameters.putString("fields", "id,name,first_name,last_name,picture.type(large),email")
        request.parameters = parameters
        request.executeAsync()
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
                deviceId = sessionManager.deviceId
            )
        } catch (e: ApiException) {
            showToast("Google Login failed")
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager!!.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.sendOtp.id -> sendOTP()
            binding.termsAndConditions.id -> showTermsAndConditions()
            binding.alreadyHaveAccount.id -> findNavController().popBackStack()
            binding.facebook.id -> faceBookLogin()
            binding.google.id -> googleSignInLauncher.launch(mGoogleSignInClient!!.signInIntent)

        }

    }
}