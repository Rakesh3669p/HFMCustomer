package com.hfm.customer.ui.fragments.address

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.hfm.customer.R
import com.hfm.customer.commonModel.City
import com.hfm.customer.commonModel.Country
import com.hfm.customer.commonModel.State
import com.hfm.customer.databinding.FragmentAddAddressBinding
import com.hfm.customer.ui.loginSignUp.LoginSignUpViewModel
import com.hfm.customer.utils.Loader
import com.hfm.customer.utils.NoInternetDialog
import com.hfm.customer.utils.Resource
import com.hfm.customer.utils.netWorkFailure
import com.hfm.customer.utils.showToast
import com.hfm.customer.viewModel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class AddNewAddressFragment : Fragment(), View.OnClickListener {
    private var from: String = ""
    private var addressId: Int = 0
    private var addressType: Int = 0
    private var cityId: String = ""
    private var stateId: String = ""
    private var countryId: String = ""
    private var phoneCode: String = ""
    private var latitude: String = ""
    private var longitude: String = ""
    private var customerName: String = ""
    private var customerNumber: String = ""
    private var homeFlatNo: String = ""
    private var streetBuildingName: String = ""
    private var pinCode: String = ""
    private lateinit var binding: FragmentAddAddressBinding
    private var currentView: View? = null
    private val mainViewModel: MainViewModel by viewModels()
    private val loginViewModel: LoginSignUpViewModel by viewModels()
    private lateinit var noInternetDialog: NoInternetDialog
    private lateinit var appLoader: Loader

    private var type = "new"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_add_address, container, false)
            binding = FragmentAddAddressBinding.bind(currentView!!)
            init()
            setObserver()
            setOnClickListener()
        }
        return currentView!!
    }

    private fun init() {
        appLoader = Loader(requireContext())
        noInternetDialog = NoInternetDialog(requireContext())
        noInternetDialog.setOnDismissListener { init() }
        loginViewModel.getCountriesList()

        arguments?.let {
            from = it.getString("from").toString()
            addressId = it.getInt("addressId")
            addressType = if(it.getString("addressType")?.lowercase() == "home") 0 else 1
            cityId = it.getString("cityId").toString()
            stateId = it.getString("stateId").toString()
            countryId = it.getString("countryId").toString()
            phoneCode = it.getString("phoneCode").toString()
            latitude = it.getString("latitude").toString()
            longitude = it.getString("longitude").toString()
            customerName = it.getString("customerName").toString()
            customerNumber = it.getString("customerNumber").toString()
            homeFlatNo = it.getString("homeFlatNo").toString()
            streetBuildingName = it.getString("streetBuildingName").toString()
            pinCode = it.getString("pinCode").toString()
        }
        if(from == "update") {
            with(binding){
                name.setText(customerName)
                mobileNumber.setText(customerNumber)
                houseNo.setText(homeFlatNo)
                streetName.setText(streetBuildingName)
                postCode.setText(pinCode)

            }
        }
    }

    private fun setObserver() {
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

        loginViewModel.states.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    if (response.data?.httpcode == 200) {
                        setStateSpinner(response.data.data.state)
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

        loginViewModel.cities.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    if (response.data?.httpcode == 200) {
                        setCitySpinner(response.data.data.city)
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
    }


    private fun setCountriesSpinner(country: List<Country>) {
        val phoneCodes: MutableList<String> = ArrayList()
        country.forEach { phoneCodes.add(it.phonecode.toString()) }
        val adapter: ArrayAdapter<String> =
            ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item, phoneCodes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.countryCodeSpinner.adapter = adapter
        if(from == "update"){
            val selectedPhoneCode = phoneCode.indexOf(phoneCode)
            binding.countryCodeSpinner.setSelection(selectedPhoneCode)
        }else{
            binding.countryCodeSpinner.setSelection(131)
        }
        phoneCode = country[131].phonecode.toString()
        loginViewModel.getStateList(country[131].id)
        binding.countryCodeSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parentView: AdapterView<*>,
                    selectedItemView: View,
                    position: Int,
                    id: Long
                ) {
                    phoneCode = country[position].phonecode.toString()
                    countryId = country[position].id.toString()
                    loginViewModel.getStateList(country[position].id)
                    binding.countrySpinner.setSelection(position)
                }

                override fun onNothingSelected(parentView: AdapterView<*>?) {}
            }


        val countries: MutableList<String> = ArrayList()
        val countryIds: MutableList<String> = ArrayList()
        country.forEach { countries.add(it.country_name)
            countryIds.add(it.id.toString())}
        val countrySpinnerAdapter: ArrayAdapter<String> =
            ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item, countries)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.countrySpinner.adapter = countrySpinnerAdapter
        if(from == "update"){
            val selectedCountry = countryIds.indexOf(countryId)
            if(selectedCountry>=0) {
                binding.countryCodeSpinner.setSelection(selectedCountry)
                countryId = country[selectedCountry].id.toString()
            }
        }else{
            binding.countryCodeSpinner.setSelection(131)
            countryId = country[131].id.toString()
        }

        if(countryId.toString()!="null") {
            loginViewModel.getStateList(countryId.toInt())
        }
        binding.countrySpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parentView: AdapterView<*>,
                    selectedItemView: View,
                    position: Int,
                    id: Long
                ) {
                    phoneCode = country[position].phonecode.toString()
                    countryId = country[position].id.toString()
                    binding.countryCodeSpinner.setSelection(position)

                    loginViewModel.getStateList(country[position].id)
                }

                override fun onNothingSelected(parentView: AdapterView<*>?) {}
            }
    }

    private fun setStateSpinner(state: List<State>) {
        val states: MutableList<String> = ArrayList()
        state.forEach { states.add(it.state_name) }
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
                stateId = state[position].id.toString()
                loginViewModel.getCitiesList(state[position].id)
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {}
        }
    }

    private fun setCitySpinner(city: List<City>) {
        val cities: MutableList<String> = ArrayList()
        city.forEach { cities.add(it.city_name) }
        val adapter: ArrayAdapter<String> =
            ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item, cities)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.citySpinner.adapter = adapter
        cityId = city[0].id.toString()
        binding.citySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>,
                selectedItemView: View,
                position: Int,
                id: Long
            ) {
                cityId = city[position].id.toString()
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {}
        }
    }


    private fun setOnClickListener() {
        with(binding) {
            save.setOnClickListener(this@AddNewAddressFragment)
            back.setOnClickListener(this@AddNewAddressFragment)
        }
    }


    private fun validateAndUpdateAddress() {
        with(binding) {
            customerName = name.text.toString()
            customerNumber = mobileNumber.text.toString()
            homeFlatNo = houseNo.text.toString()
            streetBuildingName = streetName.text.toString()
            pinCode = postCode.text.toString()
            normalRadioButton.setOnClickListener { addressType = 0 }
            businessRadioButton.setOnClickListener { addressType = 1 }


            if (customerName.isEmpty()) {
                showToast("Please Enter a Valid Name")
                return
            }

            if (customerNumber.isEmpty() || customerNumber.length < 5) {
                showToast("Please Enter a Valid Mobile Number")
                return
            }

            if (homeFlatNo.isEmpty()) {
                showToast("Please Enter a Valid House No.")
                return
            }
            if (streetBuildingName.isEmpty()) {
                showToast("Please Enter a Valid Street/Building Name")
                return
            }
            if (pinCode.isEmpty()) {
                showToast("Please Enter a Post Code")
                return
            }
            val isDefault = if (setAsDefaultCheckBox.isChecked) 1 else 0
            mainViewModel.addNewAddress(
                customerName,
                addressType,
                phoneCode,
                customerNumber,
                countryId,
                stateId,
                cityId,
                homeFlatNo,
                streetBuildingName,
                pinCode,
                latitude,
                longitude,
                isDefault,
                homeFlatNo,
                streetBuildingName
            )
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.save.id -> validateAndUpdateAddress()
            binding.back.id -> findNavController().popBackStack()

        }
    }

}