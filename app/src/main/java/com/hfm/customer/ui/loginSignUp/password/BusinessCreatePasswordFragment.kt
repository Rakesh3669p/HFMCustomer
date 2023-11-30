package com.hfm.customer.ui.loginSignUp.password

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.hfm.customer.R
import com.hfm.customer.commonModel.CityData
import com.hfm.customer.commonModel.CountryData
import com.hfm.customer.commonModel.StateData
import com.hfm.customer.databinding.FragmentBusinessCreatePasswordBinding
import com.hfm.customer.ui.loginSignUp.LoginSignUpViewModel
import com.hfm.customer.utils.Loader
import com.hfm.customer.utils.NoInternetDialog
import com.hfm.customer.utils.Resource
import com.hfm.customer.utils.SessionManager
import com.hfm.customer.utils.netWorkFailure
import com.hfm.customer.utils.showToast
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class BusinessCreatePasswordFragment : Fragment(), View.OnClickListener {
    private lateinit var binding: FragmentBusinessCreatePasswordBinding
    private var currentView: View? = null

    private val loginSignUpViewModel: LoginSignUpViewModel by activityViewModels()
    @Inject
    lateinit var sessionManager: SessionManager

    private lateinit var appLoader: Loader
    private lateinit var noInternetDialog: NoInternetDialog

    var from = ""
    var email = ""
    var companyName = ""
    var companyRegisterNo = ""
    var companyContactNo = ""
    var natureOfBusiness = ""
    var phoneCode = ""
    var countryId = ""
    var stateId = ""
    var cityId = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        currentView ?: inflater.inflate(R.layout.fragment_business_create_password, container, false)?.apply {
            currentView = this
            binding = FragmentBusinessCreatePasswordBinding.bind(this)
            init()
            setOnClickListener()
        }
        return currentView
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setObserver()
    }

    private fun init() {
        appLoader = Loader(requireContext())
        noInternetDialog = NoInternetDialog(requireContext())
        loginSignUpViewModel.getCountriesList()

        arguments?.let {
            email = it.getString("email").toString()
            companyName = it.getString("companyName").toString()
            companyRegisterNo = it.getString("companyRegisterNo").toString()
            companyContactNo = it.getString("companyContactNo").toString()
            natureOfBusiness = it.getInt("natureOfBusiness").toString()
            phoneCode = it.getInt("phoneCode").toString()
        }
    }

    private fun setObserver() {
        loginSignUpViewModel.countries.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    if (response.data?.httpcode == 200) {
                        setCountriesDropDown(response.data.data)
                    }
                }

                is Resource.Loading -> Unit
                is Resource.Error -> apiError(response.message)
            }
        }

        loginSignUpViewModel.states.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    if (response.data?.httpcode == 200) {
                        setStatesDropDown(response.data.data)
                    }
                }

                is Resource.Loading -> Unit
                is Resource.Error -> apiError(response.message)
            }
        }

        loginSignUpViewModel.cities.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    if (response.data?.httpcode == 200) {
                        setCitiesDropDown(response.data.data)
                    }
                }
                is Resource.Loading -> Unit
                is Resource.Error -> apiError(response.message)
            }
        }

        loginSignUpViewModel.registerUser.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    if (response.data?.httpcode == 200) {
                        showToast(response.data.success)
                        findNavController().navigate(R.id.action_businessCreatePasswordFragment_to_loginFragment)
                    }
                }
                is Resource.Loading -> appLoader.show()
                is Resource.Error -> apiError(response.message)
            }
        }
    }

    private fun setCitiesDropDown(data: CityData) {
        val cities = data.city.map { it.city_name }
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, cities).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.citySpinner.adapter = this
            }
        cityId = data.city[0].id.toString()
        binding.citySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>,
                selectedItemView: View,
                position: Int,
                id: Long
            ) {
                cityId = data.city[position].id.toString()
            }
            override fun onNothingSelected(parentView: AdapterView<*>?) {}
        }
    }

    private fun setStatesDropDown(data: StateData) {
        val states = data.state.map { it.state_name }
        ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, states).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.stateSpinner.adapter = this
        }
        binding.stateSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>,
                selectedItemView: View,
                position: Int,
                id: Long
            ) {
                stateId = data.state[position].id.toString()
                loginSignUpViewModel.getCitiesList(data.state[position].id)

            }
            override fun onNothingSelected(parentView: AdapterView<*>?) {}
        }
    }

    private fun setCountriesDropDown(data: CountryData) {
        val countries = data.country.map { it.country_name }
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            countries
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        binding.countrySpinner.adapter = adapter
        binding.countrySpinner.setSelection(131)
        binding.countrySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>,
                selectedItemView: View,
                position: Int,
                id: Long
            ) {
                countryId = data.country[position].id.toString()
                loginSignUpViewModel.getStateList(data.country[position].id)
            }
            override fun onNothingSelected(parentView: AdapterView<*>?) {}
        }
    }

    private fun setOnClickListener() {
        with(binding) {
            register.setOnClickListener(this@BusinessCreatePasswordFragment)
        }
    }

    private fun registerUser() {
        val password = binding.password.text.toString()
        val confirmPassword = binding.password.text.toString()
        val address = binding.address.text.toString()
        val pinCode = binding.postCode.text.toString()
        val refCode = binding.referral.text.toString()

        if (password.length <= 7) {
            showToast("Min 8 characters required.")
            return
        }

        if (password != confirmPassword) {
            showToast("Passwords do not match.")
            return
        }


        if (address.isBlank()) {
            showToast("Please enter a valid address.")
            return
        }

        if (pinCode.isBlank() || pinCode.length < 5) {
            showToast("Please enter a valid postal code.")
            return
        }



        loginSignUpViewModel.registerUser(
            companyName,
            "business",
            email,
            password,
            confirmPassword,
            refCode,
            natureOfBusiness,
            companyRegisterNo,
            phoneCode,
            companyContactNo,
            address,
            countryId,
            stateId,
            cityId,
            pinCode,

        )
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
            binding.register.id -> registerUser()
        }
    }

}