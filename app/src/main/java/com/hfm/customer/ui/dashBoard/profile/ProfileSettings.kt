package com.hfm.customer.ui.dashBoard.profile

import android.app.Activity
import android.app.DatePickerDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.hfm.customer.databinding.FragmentProfileSettingsBinding
import com.hfm.customer.ui.dashBoard.profile.model.ProfileData
import com.hfm.customer.utils.Loader
import com.hfm.customer.utils.NoInternetDialog
import com.hfm.customer.utils.Resource
import com.hfm.customer.utils.SessionManager
import com.hfm.customer.utils.business
import com.hfm.customer.utils.getDeviceId
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
import java.time.LocalDate
import javax.inject.Inject

@AndroidEntryPoint
class ProfileSettings : Fragment(), View.OnClickListener {
    private var type: String = ""

    private var password: String = ""

    private var imgFile: File? = null

    private lateinit var binding: FragmentProfileSettingsBinding
    private var currentView: View? = null

    @Inject
    lateinit var sessionManager: SessionManager

    private val mainViewModel: MainViewModel by viewModels()
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
            currentView = inflater.inflate(R.layout.fragment_profile_settings, container, false)
            binding = FragmentProfileSettingsBinding.bind(currentView!!)
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

        mainViewModel.updateProfileCustomer.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    showToast(response.data?.message.toString())
                    if (response.data?.httpcode == 200) {
                        findNavController().navigate(R.id.action_profileSettingsToProfile)
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

    private fun setProfile(data: ProfileData) {
        with(binding) {
            data.profile[0].let {
                type = it.customer_type
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
                dobEdt.text = if (it.birthday == "null") "" else it.birthday
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
            )
            .createIntent { intent ->
                startForProfileImageResult.launch(intent)
            }
    }

    private fun setOnClickListener() {
        with(binding) {
            dobEdt.setOnClickListener(this@ProfileSettings)
            save.setOnClickListener(this@ProfileSettings)
            changePasswordEdt.setOnClickListener(this@ProfileSettings)
            back.setOnClickListener(this@ProfileSettings)
        }
    }

    private val dateSetListener =
        DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            val monthSelection = if (month.toString().length == 1) "0$month" else month
            binding.dobEdt.text = "$year-$monthSelection-$dayOfMonth"
        }

    private fun pickDate() {
        val currentDate = LocalDate.now()
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            dateSetListener,
            currentDate.year,
            currentDate.monthValue,
            currentDate.dayOfMonth
        )
        datePickerDialog.show()
    }

    private fun updateProfile() {
        val name = binding.nameEdt.text.toString()
        val email = binding.emailEdt.text.toString()
        val dob = binding.dobEdt.text.toString()
        if (name.isEmpty()) {
            showToast("Please enter a valid name")
            return
        }
        if (!email.isValidEmail()) {
            showToast("Please enter a valid email")
            return
        }



        if (type == business) {

        } else {

            val requestBodyMap = mutableMapOf<String, RequestBody?>()
            if(imgFile!=null) {
                requestBodyMap["profile_img\"; filename=\"profile.jpg\""] =
                    imgFile?.asRequestBody("image/jpeg".toMediaTypeOrNull())
            }
            requestBodyMap["access_token"] = sessionManager.token.toRequestBody(MultipartBody.FORM)
            requestBodyMap["first_name"] = name.toRequestBody(MultipartBody.FORM)
            requestBodyMap["email"] = email.toRequestBody(MultipartBody.FORM)
            requestBodyMap["birthday"] = dob.toRequestBody(MultipartBody.FORM)
            requestBodyMap["os_type"] = "APP".toRequestBody(MultipartBody.FORM)
            requestBodyMap["device_id"] = getDeviceId(requireContext()).toRequestBody(MultipartBody.FORM)
            requestBodyMap["password"] = password.toRequestBody(MultipartBody.FORM)
            requestBodyMap["password_confirmation"] = password.toRequestBody(MultipartBody.FORM)
            mainViewModel.updateProfileCustomer(requestBodyMap)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.dobEdt.id -> pickDate()
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

                else -> {
                    showToast("Task Cancelled")
                }
            }
        }
}