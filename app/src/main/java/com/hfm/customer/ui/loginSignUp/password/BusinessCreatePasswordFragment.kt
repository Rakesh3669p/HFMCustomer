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
import org.json.JSONObject
import javax.inject.Inject


@AndroidEntryPoint
class BusinessCreatePasswordFragment : Fragment(), View.OnClickListener {
    private lateinit var binding: FragmentBusinessCreatePasswordBinding
    private var currentView: View? = null
    private val loginSignUpViewModel: LoginSignUpViewModel by activityViewModels()
    private lateinit var appLoader: Loader

    private lateinit var noInternetDialog: NoInternetDialog

    @Inject
    lateinit var sessionManager: SessionManager

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

        if (currentView == null) {
            currentView =
                inflater.inflate(R.layout.fragment_business_create_password, container, false)
            binding = FragmentBusinessCreatePasswordBinding.bind(currentView!!)
            init()
            setObserver()
            setOnClickListener()
        }
        return currentView!!
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
                is Resource.Error -> {
                    appLoader.dismiss()
                    if (response.message.toString() == netWorkFailure) {
                        noInternetDialog.show()
                    }

                }
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
                is Resource.Error -> {
                    appLoader.dismiss()
                    if (response.message.toString() == netWorkFailure) {
                        noInternetDialog.show()
                    }

                }
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
                is Resource.Error -> {
                    appLoader.dismiss()
                    if (response.message.toString() == netWorkFailure) {
                        noInternetDialog.show()
                    }

                }
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
                is Resource.Error -> {
                    appLoader.dismiss()
                    if (response.message == netWorkFailure) {
                        noInternetDialog.show()
                    }
                }
            }
        }
    }

    private fun setCitiesDropDown(data: CityData) {
        val cities: MutableList<String> = ArrayList()
        data.city.forEach { cities.add(it.city_name) }

        val adapter: ArrayAdapter<String> =
            ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item, cities)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.citySpinner.adapter = adapter
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
        val states: MutableList<String> = ArrayList()
        data.state.forEach { states.add(it.state_name) }
        val adapter: ArrayAdapter<String> =
            ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item, states)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.stateSpinner.adapter = adapter
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
        val countries: MutableList<String> = ArrayList()
        data.country.forEach { countries.add(it.country_name) }
        val adapter: ArrayAdapter<String> =
            ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item, countries)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.countrySpinner.adapter = adapter
        binding.countrySpinner.setSelection(131)
        binding.countrySpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
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

        if (password.length <= 8) {
            showToast("password length must be more than 8")
            return
        }

        if (password != confirmPassword) {
            showToast("Passwords do not match.")
            return
        }


        if (address.isEmpty()) {
            showToast("Please enter a valid address")
            return
        }

        if (pinCode.isEmpty() || pinCode.length < 5) {
            showToast("Please enter a valid postal code")
            return
        }



        loginSignUpViewModel.registerUser(
            companyName,
            "business",
            email,
            password,
            confirmPassword,
            "",
            natureOfBusiness,
            companyRegisterNo,
            phoneCode,
            companyContactNo,
            address,
            countryId,
            stateId,
            cityId,
            pinCode
        )
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.register.id -> registerUser()
        }

    }


}