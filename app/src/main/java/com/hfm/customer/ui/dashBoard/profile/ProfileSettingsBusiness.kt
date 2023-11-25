package com.hfm.customer.ui.dashBoard.profile

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import coil.ImageLoader
import coil.request.ImageRequest
import coil.util.DebugLogger
import com.github.dhaval2404.imagepicker.ImagePicker
import com.hfm.customer.R
import com.hfm.customer.commonModel.Country
import com.hfm.customer.databinding.FragmentProfileSettingsBusinessBinding
import com.hfm.customer.ui.dashBoard.profile.model.ProfileData
import com.hfm.customer.ui.loginSignUp.LoginSignUpViewModel
import com.hfm.customer.ui.loginSignUp.register.model.BusinessData
import com.hfm.customer.utils.Loader
import com.hfm.customer.utils.NoInternetDialog
import com.hfm.customer.utils.Resource
import com.hfm.customer.utils.SessionManager
import com.hfm.customer.utils.isValidEmail
import com.hfm.customer.utils.netWorkFailure
import com.hfm.customer.utils.showToast
import com.hfm.customer.viewModel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class ProfileSettingsBusiness : Fragment(), View.OnClickListener {
    private var natureOfBusinessId: Int = -1
    private  var phoneCode: String = ""

    private var type: String = ""

    private var password: String = ""

    private var imgFile: File? = null

    private lateinit var binding: FragmentProfileSettingsBusinessBinding
    private var currentView: View? = null

    @Inject
    lateinit var sessionManager: SessionManager

    private val mainViewModel: MainViewModel by viewModels()
    private val loginViewModel: LoginSignUpViewModel by viewModels()

    private lateinit var appLoader: Loader
    private lateinit var noInternetDialog: NoInternetDialog
    private val navigation: NavController by lazy {
        findNavController()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_profile_settings_business, container, false)
            binding = FragmentProfileSettingsBusinessBinding.bind(currentView!!)
            init()
            setOnClickListener()
        }
        return currentView!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setObserver()
        lifecycleScope.launch {
            delay(1000)
            navigation.currentBackStackEntry?.savedStateHandle?.getLiveData<String>("password")
                ?.observe(viewLifecycleOwner) {
                    password = it.toString()
                }
            if (password.isNotEmpty()) {
                binding.changePasswordEdt.text = password
            }
        }
    }

    private fun init() {
        appLoader = Loader(requireContext())
        noInternetDialog = NoInternetDialog(requireContext())
        mainViewModel.getProfile()
        loginViewModel.getCountriesList()
        loginViewModel.getBusinessCategories()
    }

    private fun setObserver() {

        mainViewModel.profile.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    if (response.data?.httpcode == "200") {
                        setProfile(response.data.data)
                    } else {
                        showToast(response.data?.message.toString())
                    }
                }

                is Resource.Loading -> appLoader.show()
                is Resource.Error -> apiError(response.message)
            }
        }

        loginViewModel.countries.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    if (response.data?.httpcode == 200) {
                        setCountriesSpinner(response.data.data.country)
                    } else {
                        showToast(response.data?.message.toString())
                    }
                }

                is Resource.Loading -> appLoader.show()
                is Resource.Error -> {
                    appLoader.dismiss()
                    showToast(response.message.toString())
                    if (response.message == netWorkFailure) {
                        noInternetDialog.show()
                    }
                }
            }
        }



        loginViewModel.businessCategories.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    if (response.data?.httpcode == 200) {
                        setNatureOfBusiness(response.data.data)
                    }
                }

                is Resource.Loading -> Unit
                is Resource.Error -> {
                    showToast(response.message.toString())
                    if (response.message.toString() == netWorkFailure) {
                        noInternetDialog.show()
                    }
                }
            }
        }
        mainViewModel.updateProfileBusiness.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    showToast(response.data?.message.toString())
                    if (response.data?.httpcode == 200) {
                        findNavController().navigate(R.id.action_profileSettingsBusinessToProfile)
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

    private fun setCountriesSpinner(country: List<Country>) {
        val phoneCodes = country.map { it.phonecode.toString() }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, phoneCodes).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        val defaultSelection = 131
        val selectedPhoneCodeIndex = if(!phoneCode.isNullOrEmpty()) phoneCodes.indexOf(phoneCode) else defaultSelection
        phoneCode = phoneCodes[selectedPhoneCodeIndex]

        binding.countryCodeSpinner.adapter = adapter
        binding.countryCodeSpinner.setSelection(selectedPhoneCodeIndex)

        binding.countryCodeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>, selectedItemView: View, position: Int, id: Long) {
                phoneCode = country[position].phonecode.toString()
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {}
        }
    }

    private fun setNatureOfBusiness(businessData: BusinessData) {
        val businessNames = businessData.business_categoryies.map { it.name }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, businessNames).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        val defaultIndex = businessData.business_categoryies.indexOfFirst { it.id == natureOfBusinessId }
        val selectedIndex = if (defaultIndex >= 0) defaultIndex else 0

        binding.natureOfBusiness.adapter = adapter
        binding.natureOfBusiness.setSelection(selectedIndex)

        binding.natureOfBusiness.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>, selectedItemView: View, position: Int, id: Long) {
                natureOfBusinessId = businessData.business_categoryies[position].id
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {}
        }
    }



    private fun setProfile(data: ProfileData) {
        with(binding) {
            data.profile[0].let {
                type = it.customer_type
                phoneCode = it.country_code.toString()
                val imageOriginal = it.profile_image
                val imageReplaced =
                    imageOriginal.replace("https://uat.hfm.synuos.com", "http://4.194.191.242")

                val imageLoader = ImageLoader.Builder(requireContext())
                    .logger(DebugLogger())
                    .build()

                val request = ImageRequest.Builder(requireContext())
                    .data(imageReplaced)
                    .target(profileImage)
                    .placeholder(R.drawable.user)
                    .error(R.drawable.user)
                    .build()
                lifecycleScope.launch {
                    imageLoader.execute(request)
                }
                nameEdt.setText("${it.first_name} ${it.last_name}")
                emailEdt.setText(it.email)
                registerNoEdt.setText(it.business_details.registration_no)
                mobileNumberEdt.setText(it.business_details.contact_no)
                natureOfBusinessId = it.business_details.business_type.toInt()

                pickImage.setOnClickListener {
                    showImagePickupDialog()
                }
            }
        }
    }

    private fun showImagePickupDialog() {
        ImagePicker.with(this)
            .compress(1024)
            .maxResultSize(
                1080,
                1080
            )  //Final image resolution will be less than 1080 x 1080(Optional)
            .createIntent { intent ->
                startForProfileImageResult.launch(intent)
            }
    }

    private fun setOnClickListener() {
        with(binding) {
            save.setOnClickListener(this@ProfileSettingsBusiness)
            changePasswordEdt.setOnClickListener(this@ProfileSettingsBusiness)
            back.setOnClickListener(this@ProfileSettingsBusiness)
        }
    }



    private fun updateProfile() {
        val name = binding.nameEdt.text.toString()
        val email = binding.emailEdt.text.toString()
        val registerNo = binding.registerNoEdt.text.toString()
        val contactNo = binding.mobileNumberEdt.text.toString()
        if (name.isEmpty()) {
            showToast("Please enter a valid company name")
            return
        }
        if (registerNo.isEmpty()) {
            showToast("Please enter a register no")
            return
        }
        if (contactNo.isEmpty()) {
            showToast("Please enter a contact no")
            return
        }
        if (!email.isValidEmail()) {
            showToast("Please enter a valid email")
            return
        }
        val requestBodyMap = mutableMapOf<String, RequestBody?>()
        if(imgFile!=null) {
            requestBodyMap["profile_img\"; filename=\"profile.jpg\""] =
                imgFile?.asRequestBody("image/jpeg".toMediaTypeOrNull())
        }
        requestBodyMap["access_token"] = sessionManager.token.toRequestBody(MultipartBody.FORM)
        requestBodyMap["first_name"] = name.toRequestBody(MultipartBody.FORM)
        requestBodyMap["email"] = email.toRequestBody(MultipartBody.FORM)
        requestBodyMap["contact_no"] = contactNo.toRequestBody(MultipartBody.FORM)
        requestBodyMap["business_category"] = natureOfBusinessId.toString().toRequestBody(MultipartBody.FORM)
        requestBodyMap["registration_no"] = registerNo.toRequestBody(MultipartBody.FORM)
        requestBodyMap["state"] = "".toRequestBody(MultipartBody.FORM)
        requestBodyMap["country"] = "".toRequestBody(MultipartBody.FORM)
        requestBodyMap["city"] = "".toRequestBody(MultipartBody.FORM)
        requestBodyMap["address"] = "".toRequestBody(MultipartBody.FORM)
        requestBodyMap["pincode"] = "".toRequestBody(MultipartBody.FORM)
        requestBodyMap["birthday"] = "2000-04-20".toRequestBody(MultipartBody.FORM)
        requestBodyMap["password"] = password.toRequestBody(MultipartBody.FORM)
        requestBodyMap["password_confirmation"] = password.toRequestBody(MultipartBody.FORM)
        requestBodyMap["device_id"] = sessionManager.deviceId.toRequestBody(MultipartBody.FORM)
        requestBodyMap["os_type"] = "APP".toRequestBody(MultipartBody.FORM)
        mainViewModel.updateProfileBusiness(requestBodyMap)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.save.id -> updateProfile()
            binding.changePasswordEdt.id -> {
                val bundle = Bundle()
                bundle.putString("from", "profile")
                findNavController().navigate(R.id.createPasswordFragment, bundle)
            }

            binding.back.id -> findNavController().popBackStack()
        }
    }


    private val startForProfileImageResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            val resultCode = result.resultCode
            val data = result.data
            when (resultCode) {
                Activity.RESULT_OK -> {
                    val fileUri = data?.data!!
                    imgFile = File(fileUri.path)
                    binding.profileImage.setImageURI(fileUri)
                }

                ImagePicker.RESULT_ERROR -> {
                    showToast(ImagePicker.getError(data))
                }

            }
        }
}