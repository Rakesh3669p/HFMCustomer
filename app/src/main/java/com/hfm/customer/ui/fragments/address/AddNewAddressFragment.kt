package com.hfm.customer.ui.fragments.address

import android.annotation.SuppressLint
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.hfm.customer.R
import com.hfm.customer.commonModel.City
import com.hfm.customer.commonModel.Country
import com.hfm.customer.commonModel.State
import com.hfm.customer.databinding.FragmentAddAddressBinding
import com.hfm.customer.ui.loginSignUp.LoginSignUpViewModel
import com.hfm.customer.utils.Loader
import com.hfm.customer.utils.NoInternetDialog
import com.hfm.customer.utils.Resource
import com.hfm.customer.utils.bitmapFromVector
import com.hfm.customer.utils.makeInvisible
import com.hfm.customer.utils.makeVisible
import com.hfm.customer.utils.netWorkFailure
import com.hfm.customer.utils.showToast
import com.hfm.customer.viewModel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class AddNewAddressFragment : Fragment(), View.OnClickListener {
    private var isDefault: Int = 0
    private var from: String = ""
    private var addressId: Int = 0
    private var addressType: Int = 1
    private var cityId: String = ""
    private var stateId: String = ""
    private var countryId: String = ""
    private var phoneCode: String = ""
    private var latitude: String = "0.0"
    private var longitude: String = "0.0"
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
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var mMap: GoogleMap

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_add_address, container, false)
            binding = FragmentAddAddressBinding.bind(currentView!!)
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
        noInternetDialog.setOnDismissListener { init() }
        setMap()
        loginViewModel.getCountriesList()

        arguments?.let {
            from = it.getString("from").toString()
            if(from =="update") {
                addressId = it.getInt("addressId")
                addressType = if (it.getString("addressType")?.lowercase() == "home") 1 else 2
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
                isDefault = it.getInt("isDefault")?:0
            }
        }
        if(from == "update") {
            with(binding){
                name.setText(customerName)
                mobileNumber.setText(customerNumber)
                houseNo.setText(homeFlatNo)
                streetName.setText(streetBuildingName)
                postCode.setText(pinCode)
                setAsDefaultCheckBox.isChecked = isDefault == 1
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun setMap() {
        mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync { googleMap ->
            mMap = googleMap
            val lat = 3.140853
            val long = 101.693207
            val latLong = LatLng(lat, long)
            val cameraPosition = CameraPosition.Builder().target(latLong).zoom(11f).build()
            val marker = mMap.addMarker(MarkerOptions().position(latLong))
            marker?.setIcon(bitmapFromVector(requireContext(), R.drawable.ic_location_24))
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
            mMap.setOnMapClickListener {
                marker?.position = it
                mMap.moveCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.Builder().target(it).zoom(11f).build()))
                reverseGeocode(it.latitude,it.longitude)
            }
        }
    }

    private fun reverseGeocode(latitude: Double, longitude: Double) {
        val geocoder = Geocoder(requireContext())
        try {
            val addresses: List<Address> = geocoder.getFromLocation(latitude, longitude, 1) as List<Address>

            if (addresses.isNotEmpty()) {
                val address = addresses[0]
                val city = address.locality
                val state = address.adminArea
                val country = address.countryName
                val countryCode = address.countryCode
                mainViewModel.getCountryCode(countryCode)
                with(binding){
                    countrySpinner.makeInvisible()
                    stateSpinner.makeInvisible()
                    citySpinner.makeInvisible()

                    countryText.makeVisible()
                    stateText.makeVisible()
                    cityText.makeVisible()

                    countryText.text = country
                    stateText.text = state
                    cityText.text = city

                }



            } else {
                showToast("No address found for this location")
            }
        } catch (e: Exception) {
            showToast("Error while reverse geocoding: ${e.message}")
        }
    }

    private fun setObserver() {
        loginViewModel.countries.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {

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
                    while (appLoader.isShowing) {
                        appLoader.dismiss()
                    }
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

        mainViewModel.addNewAddress.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    if (response.data?.httpcode == 200) {
                        findNavController().navigate(R.id.action_addressFormToAddressList)
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

        mainViewModel.updateAddress.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    if (response.data?.httpcode == 200) {
                        findNavController().navigate(R.id.action_addressFormToAddressList)
                    } else {
                        showToast(response.data?.message.toString())
                    }
                }

                is Resource.Loading -> appLoader.show()
                is Resource.Error -> apiError(response.message)
            }
        }

        mainViewModel.countryCode.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {

                    if (response.data?.httpcode == 200) {
                        val name = binding.stateText.text.toString()
                        mainViewModel.getStateCode(name, response.data.data.country.id.toString())
                        countryId = response.data.data.country.id.toString()
                    } else {
                        appLoader.dismiss()
                        showToast(response.data?.message.toString())
                    }
                }

                is Resource.Loading -> appLoader.show()
                is Resource.Error -> apiError(response.message)
            }
        }

        mainViewModel.stateCode.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {

                    if (response.data?.httpcode == 200) {
                        val name = binding.cityText.text.toString()
                        mainViewModel.getCityCode(name, response.data.data.state.id.toString())
                        stateId = response.data.data.state.id.toString()
                    } else {
                        appLoader.dismiss()
                        showToast(response.data?.message.toString())
                    }
                }

                is Resource.Loading -> appLoader.show()
                is Resource.Error -> apiError(response.message)
            }
        }

        mainViewModel.cityCode.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    while (appLoader.isShowing) {
                        appLoader.dismiss()
                    }
                    if (response.data?.httpcode == 200) {
                        cityId = response.data.data.city.id.toString()
                    } else {
                        showToast(response.data?.message.toString())
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
            override fun onItemSelected(parentView: AdapterView<*>, selectedItemView: View, position: Int, id: Long) {
                onItemSelectedAction(position)
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {}
        }
    }

    private fun setCountriesSpinner(country: List<Country>) {
        val phoneCodes = country.map { it.phonecode.toString() }
        val countries = country.map { it.country_name }
        val countryIds = country.map { it.id.toString() }

        setSpinnerWithList(binding.countryCodeSpinner, phoneCodes, 131) { position ->
            phoneCode = phoneCodes[position]
            countryId = countryIds[position]
            binding.countrySpinner.setSelection(position)
            loginViewModel.getStateList(countryIds[position].toInt())
        }

        setSpinnerWithList(binding.countrySpinner, countries, 131) { position ->
            phoneCode = phoneCodes[position]
            countryId = countryIds[position]
            binding.countryCodeSpinner.setSelection(position)
            loginViewModel.getStateList(countryIds[position].toInt())
        }
    }

    private fun setStateSpinner(state: List<State>?) {
        if (state.isNullOrEmpty())
            return

        val states = state.map { it.state_name }

        setSpinnerWithList(binding.stateSpinner, states, 0) { position ->
            if (position >= 0 && position < state.size) {
                stateId = state[position].id.toString()
                loginViewModel.getCitiesList(state[position].id)
            }
        }
    }

    private fun setCitySpinner(city: List<City>) {
        val cities = city.map { it.city_name }

        setSpinnerWithList(binding.citySpinner, cities, 0) { position ->
            cityId = city[position].id.toString()
        }
    }



    private fun setOnClickListener() {
        with(binding) {
            save.setOnClickListener(this@AddNewAddressFragment)
            cancel.setOnClickListener(this@AddNewAddressFragment)
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
            normalRadioButton.setOnClickListener { addressType = 1 }
            businessRadioButton.setOnClickListener { addressType = 2 }


            if (customerName.isEmpty()) {
                showToast("Name is Required")
                return
            }

            if (customerNumber.isEmpty()) {
                showToast("Mobile Number is Required")
                return
            }

            if (customerNumber.length < 5) {
                showToast("Mobile Number should be more than 5 numbers")
                return
            }

            if (homeFlatNo.isEmpty()) {
                showToast("House No is Required.")
                return
            }
            if (streetBuildingName.isEmpty()) {
                showToast("Street/Building Name is Required")
                return
            }
            if (pinCode.isEmpty()) {
                showToast("Post Code is Required")
                return
            }
            val isDefault = if (setAsDefaultCheckBox.isChecked) 1 else 2
            if(from == "update") {
                mainViewModel.updateAddress(
                    addressId,
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
            }else{
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
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.save.id -> validateAndUpdateAddress()
            binding.back.id -> findNavController().popBackStack()
            binding.cancel.id -> findNavController().popBackStack()
        }
    }
}