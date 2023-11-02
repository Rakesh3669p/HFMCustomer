package com.hfm.customer.ui.loginSignUp.register

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.text.Html
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Spinner
import androidx.appcompat.app.AppCompatDialog
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.hfm.customer.R
import com.hfm.customer.commonModel.CountryData
import com.hfm.customer.databinding.FragmentBusinessRegisterBinding
import com.hfm.customer.databinding.TermsAndConditionsPopUpBinding
import com.hfm.customer.ui.loginSignUp.LoginSignUpViewModel
import com.hfm.customer.ui.loginSignUp.register.model.BusinessCategoryy
import com.hfm.customer.ui.loginSignUp.register.model.BusinessData
import com.hfm.customer.utils.Loader
import com.hfm.customer.utils.NoInternetDialog
import com.hfm.customer.utils.Resource
import com.hfm.customer.utils.business
import com.hfm.customer.utils.isValidEmail
import com.hfm.customer.utils.netWorkFailure
import com.hfm.customer.utils.showToast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BusinessRegisterFragment : Fragment(), View.OnClickListener {
    private var termsAndCondition: String = ""

    private var phoneCode: String = ""
    private var natureOfBusinessId: Int = 0

    private var natureOfBusiness: String = ""

    private lateinit var binding: FragmentBusinessRegisterBinding
    private var currentView: View? = null
    private val loginSignUpViewModel: LoginSignUpViewModel by viewModels()
    private lateinit var noInternetDialog: NoInternetDialog

    private var companyName = ""
    private var companyRegisterNo = ""
    private var companyContactNo = ""
    private var email = ""

    private lateinit var appLoader: Loader
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_business_register, container, false)
            binding = FragmentBusinessRegisterBinding.bind(currentView!!)
            init()
            setObserver()
            setOnClickListener()
        }
        return currentView!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    private fun init() {
        appLoader = Loader(requireContext())
        loginSignUpViewModel.getBusinessCategories()
        loginSignUpViewModel.getCountriesList()
        loginSignUpViewModel.getTermsConditions()

        noInternetDialog = NoInternetDialog(requireContext())
        noInternetDialog.setOnDismissListener {
            init()
        }
    }

    private fun setObserver() {
        loginSignUpViewModel.businessCategories.observe(viewLifecycleOwner) { response ->
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

        loginSignUpViewModel.countries.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    if (response.data?.httpcode == 200) {
                        setCountriesDropDown(response.data.data)
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


        loginSignUpViewModel.termsConditions.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {

                    if (response.data?.httpcode == "200") {
                        termsAndCondition =
                            response.data.data.terms_conditions.business_customer_terms
                    } else {
                        showToast(response.data?.message.toString())
                    }
                }

                is Resource.Loading -> Unit
                is Resource.Error -> {
                    if (response.message == netWorkFailure) {
                        showToast("Check your internet connection")
                    }
                }
            }
        }


        loginSignUpViewModel.sendRegisterOtp.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    if (response.data?.httpcode == 200) {
                        showToast(response.data.message)
                        val bundle = Bundle().apply {
                            putString("from", business)
                            putString("companyName", companyName)
                            putString("companyRegisterNo", companyRegisterNo)
                            putString("companyContactNo", companyContactNo)
                            putString("email", email)
                            putString("phoneCode", phoneCode)
                            putInt("natureOfBusiness", natureOfBusinessId)
                        }

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
    }

    private fun setCountriesDropDown(data: CountryData) {
        val phoneCodes = data.country.map { "+${it.phonecode}" }
        val countries = data.country.map { it.country_name }
        val countryIds = data.country.map { it.id.toString() }

        setSpinnerWithList(binding.countryCodeSpinner, phoneCodes, 131) { position ->
            phoneCode = data.country[position].phonecode.toString()
        }

    }


    private fun setSpinnerWithList(
        spinner: Spinner,
        items: List<String>,
        initialSelection: Int,
        onItemSelectedAction: (position: Int) -> Unit
    ) {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            items
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.setSelection(initialSelection)

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>,
                selectedItemView: View,
                position: Int,
                id: Long
            ) {
                onItemSelectedAction(position)
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {}
        }
    }

    private fun setNatureOfBusiness(businessData: BusinessData) {
        val businessCategory: MutableList<BusinessCategoryy> =
            businessData.business_categoryies as MutableList<BusinessCategoryy>
        val businessCat = BusinessCategoryy(
            created_at = "",
            created_by = "",
            crm_id = 0,
            id = 0,
            is_active = 0,
            is_deleted = 0,
            modified_by = 0,
            name = "Nature of business",
            updated_at = "",
        )
        businessCategory.add(0, businessCat)
        val businessNames = businessData.business_categoryies.map { it.name }

        setSpinnerWithList(binding.natureOfBusiness, businessNames, 0) { position ->
            natureOfBusiness = businessData.business_categoryies[position].name
            natureOfBusinessId = businessData.business_categoryies[position].id
        }
    }

    private fun setOnClickListener() {
        with(binding) {
            sendOtp.setOnClickListener(this@BusinessRegisterFragment)
            termsAndConditions.setOnClickListener(this@BusinessRegisterFragment)
            alreadyHaveAccount.setOnClickListener(this@BusinessRegisterFragment)
        }
    }

    private fun validateAndRedirect() {
        companyName = binding.companyName.text.toString()
        companyRegisterNo = binding.companyRegisterNo.text.toString()
        companyContactNo = binding.companyContactNo.text.toString()
        email = binding.email.text.toString()

        if (companyName.isEmpty()) {
            showToast("please enter a valid company name")
            return
        }

        if (companyRegisterNo.isEmpty() || companyRegisterNo.length < 5) {
            showToast("please enter a valid company registered no")
            return
        }

        if (natureOfBusinessId <= 0) {
            showToast("please select your nature of business")
            return
        }

        if (companyContactNo.isEmpty() || companyContactNo.length < 5) {
            showToast("please enter a valid company contact no")
            return
        }

        if (!email.isValidEmail()) {
            showToast("please enter a valid email")
            return
        }

        if (!binding.termsAndConditionsCheckBox.isChecked) {
            showToast("please check the Terms and Conditions..")
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
        bindingDialog.description.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(termsAndCondition, Html.FROM_HTML_MODE_COMPACT)
        } else {
            Html.fromHtml(termsAndCondition)
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
            binding.sendOtp.id -> validateAndRedirect()
            binding.termsAndConditions.id -> showTermsAndConditions()
            binding.alreadyHaveAccount.id -> findNavController().popBackStack()
        }

    }
}