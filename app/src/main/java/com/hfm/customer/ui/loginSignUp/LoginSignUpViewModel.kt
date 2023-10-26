package com.hfm.customer.ui.loginSignUp

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.hfm.customer.BuildConfig
import com.hfm.customer.commonModel.CityListModel
import com.hfm.customer.commonModel.CountryListModel
import com.hfm.customer.commonModel.StateListModel
import com.hfm.customer.commonModel.SuccessModel
import com.hfm.customer.commonModel.TermsConditionsModel
import com.hfm.customer.data.Repository
import com.hfm.customer.ui.loginSignUp.login.model.LoginModel
import com.hfm.customer.ui.loginSignUp.register.model.BusinessCategoryModel
import com.hfm.customer.utils.Resource
import com.hfm.customer.utils.SingleLiveEvent
import com.hfm.customer.utils.checkResponseBody
import com.hfm.customer.utils.checkThrowable
import com.hfm.customer.utils.getDeviceName
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class LoginSignUpViewModel @Inject constructor(private val repository: Repository) : ViewModel() {


    val login = SingleLiveEvent<Resource<LoginModel>>()
    val socialLogin = SingleLiveEvent<Resource<LoginModel>>()
    val registerUser = SingleLiveEvent<Resource<SuccessModel>>()
    val sendRegisterOtp = SingleLiveEvent<Resource<SuccessModel>>()
    val registerVerifyOtp = SingleLiveEvent<Resource<SuccessModel>>()
    val countries = SingleLiveEvent<Resource<CountryListModel>>()
    val states = SingleLiveEvent<Resource<StateListModel>>()
    val cities = SingleLiveEvent<Resource<CityListModel>>()
    val businessCategories = SingleLiveEvent<Resource<BusinessCategoryModel>>()
    val termsConditions:MutableLiveData<Resource<TermsConditionsModel>> = MutableLiveData()

    fun registerUser(
        firstName: String = "",
        customerType: String = "",
        email: String = "",
        password: String = "",
        passwordConfirmation: String = "",
        refCode: String = "",
        businessCategory: String = "",
        registrationNo: String = "",
        countryCode: String = "",
        contactNo: String = "",
        address: String = "",
        country: String = "",
        state: String = "",
        city: String = "",
        pinCode: String = "",
    ) = viewModelScope.launch {
        val jsonObject = JsonObject()

        jsonObject.addProperty("first_name", firstName)
        jsonObject.addProperty("customer_type", customerType)
        jsonObject.addProperty("email", email)
        jsonObject.addProperty("password", password)
        jsonObject.addProperty("password_confirmation", passwordConfirmation)
        jsonObject.addProperty("ref_code", refCode)
        jsonObject.addProperty("business_category", businessCategory)
        jsonObject.addProperty("registration_no", registrationNo)
        jsonObject.addProperty("country_code", countryCode)
        jsonObject.addProperty("contact_no", contactNo)
        jsonObject.addProperty("address", address)
        jsonObject.addProperty("country", country)
        jsonObject.addProperty("state", state)
        jsonObject.addProperty("city", city)
        jsonObject.addProperty("pincode", pinCode)
        safeRegisterUser(jsonObject)
    }

    private suspend fun safeRegisterUser(jsonObject: JsonObject) {
        registerUser.postValue(Resource.Loading())
        try {
            val response = repository.registerUser(jsonObject)
            if (response.isSuccessful)
                registerUser.postValue(Resource.Success(checkResponseBody(response.body()) as SuccessModel))
            else
                registerUser.postValue(Resource.Error(response.message(), null))
        } catch (t: Throwable) {
            registerUser.postValue(Resource.Error(checkThrowable(t), null))
        }
    }


    fun login(email: String, password: String, deviceId: String) = viewModelScope.launch {
        val jsonObject = JsonObject()
        jsonObject.addProperty("input", email)
        jsonObject.addProperty("password", password)
        jsonObject.addProperty("deviceToken", "dasdsad")
        jsonObject.addProperty("deviceId", deviceId)
        jsonObject.addProperty("deviceName", getDeviceName())
        jsonObject.addProperty("os", "APP")
        safeLogin(jsonObject)
    }

    private suspend fun safeLogin(jsonObject: JsonObject) {
        login.postValue(Resource.Loading())
        try {
            val response = repository.login(jsonObject)
            if (response.isSuccessful)
                login.postValue(Resource.Success(checkResponseBody(response.body()) as LoginModel))
            else
                login.postValue(Resource.Error(response.message(), null))
        } catch (t: Throwable) {
            login.postValue(Resource.Error(checkThrowable(t), null))
        }
    }

    fun socialLogin(email: String, name: String,avatar:String,loginId:String,type:String, deviceId: String) = viewModelScope.launch {
        val jsonObject = JsonObject()
        jsonObject.addProperty("social_media", type)
        jsonObject.addProperty("login_id", loginId)
        jsonObject.addProperty("fname", name)
        jsonObject.addProperty("email", email)
        jsonObject.addProperty("phone", "")
        jsonObject.addProperty("country_code", "")
        jsonObject.addProperty("avatar", avatar)
        jsonObject.addProperty("model", "")
        jsonObject.addProperty("osversion", "")
        jsonObject.addProperty("appversion", BuildConfig.VERSION_CODE)
        jsonObject.addProperty("os", "Android")
        jsonObject.addProperty("manufacturer", "")
        jsonObject.addProperty("device_name", "")
        jsonObject.addProperty("deviceId", deviceId)
        jsonObject.addProperty("deviceToken", deviceId)
        safeSocialLogin(jsonObject)
    }

    private suspend fun safeSocialLogin(jsonObject: JsonObject) {
        socialLogin.postValue(Resource.Loading())
        try {
            val response = repository.socialLogin(jsonObject)
            if (response.isSuccessful)
                socialLogin.postValue(Resource.Success(checkResponseBody(response.body()) as LoginModel))
            else
                socialLogin.postValue(Resource.Error(response.message(), null))
        } catch (t: Throwable) {
            socialLogin.postValue(Resource.Error(checkThrowable(t), null))
        }
    }

    fun sendRegisterOTP(email: String) = viewModelScope.launch {
        val jsonObject = JsonObject()
        jsonObject.addProperty("email", email)
        safeSendRegisterOTP(jsonObject)
    }

    private suspend fun safeSendRegisterOTP(jsonObject: JsonObject) {
        sendRegisterOtp.postValue(Resource.Loading())
        try {
            val response = repository.registerSendOTP(jsonObject)
            if (response.isSuccessful)
                sendRegisterOtp.postValue(Resource.Success(checkResponseBody(response.body()) as SuccessModel))
            else
                sendRegisterOtp.postValue(Resource.Error(response.message(), null))
        } catch (t: Throwable) {
            sendRegisterOtp.postValue(Resource.Error(checkThrowable(t), null))
        }
    }

    fun registerVerifyOtp(email: String, otp: String) = viewModelScope.launch {
        val jsonObject = JsonObject()
        jsonObject.addProperty("email", email)
        jsonObject.addProperty("otp", otp)
        safeRegisterVerifyOTP(jsonObject)
    }

    private suspend fun safeRegisterVerifyOTP(jsonObject: JsonObject) {
        registerVerifyOtp.postValue(Resource.Loading())
        try {
            val response = repository.registerVerifyOTP(jsonObject)
            if (response.isSuccessful)
                registerVerifyOtp.postValue(Resource.Success(checkResponseBody(response.body()) as SuccessModel))
            else
                registerVerifyOtp.postValue(Resource.Error(response.message(), null))
        } catch (t: Throwable) {
            registerVerifyOtp.postValue(Resource.Error(checkThrowable(t), null))
        }
    }

    fun getCountriesList() = viewModelScope.launch { safeCountriesListCall() }

    private suspend fun safeCountriesListCall() {
        countries.postValue(Resource.Loading())
        try {
            val response = repository.getCountriesList()
            if (response.isSuccessful)
                countries.postValue(Resource.Success(checkResponseBody(response.body()) as CountryListModel))
            else
                countries.postValue(Resource.Error(response.message(), null))
        } catch (t: Throwable) {
            countries.postValue(Resource.Error(checkThrowable(t), null))
        }
    }

    fun getStateList(countryId: Int) =
        viewModelScope.launch {
            val jsonObject = JsonObject()
            jsonObject.addProperty("country_id", countryId)
            safeRegisterVerifyOTP(jsonObject)
            safeStateListCall(jsonObject)
        }

    private suspend fun safeStateListCall(jsonObject: JsonObject) {
        states.postValue(Resource.Loading())
        try {
            val response = repository.getStatesList(jsonObject)
            if (response.isSuccessful)
                states.postValue(Resource.Success(checkResponseBody(response.body()) as StateListModel))
            else
                states.postValue(Resource.Error(response.message(), null))
        } catch (t: Throwable) {
            states.postValue(Resource.Error(checkThrowable(t), null))
        }
    }

    fun getCitiesList(stateId: Int) =
        viewModelScope.launch {
            val jsonObject = JsonObject()
            jsonObject.addProperty("state_id", stateId)
            safeRegisterVerifyOTP(jsonObject)

            safeCitiesListCall(jsonObject)
        }

    private suspend fun safeCitiesListCall(jsonObject: JsonObject) {
        cities.postValue(Resource.Loading())
        try {
            val response = repository.getCitiesList(jsonObject)
            if (response.isSuccessful)
                cities.postValue(Resource.Success(checkResponseBody(response.body()) as CityListModel))
            else
                cities.postValue(Resource.Error(response.message(), null))
        } catch (t: Throwable) {
            cities.postValue(Resource.Error(checkThrowable(t), null))
        }
    }

    fun getBusinessCategories() = viewModelScope.launch { safeBusinessCategoriesCall() }

    private suspend fun safeBusinessCategoriesCall() {
        businessCategories.postValue(Resource.Loading())
        try {
            val response = repository.getBusinessCategories()
            if (response.isSuccessful)
                businessCategories.postValue(Resource.Success(checkResponseBody(response.body()) as BusinessCategoryModel))
            else
                cities.postValue(Resource.Error(response.message(), null))
        } catch (t: Throwable) {
            cities.postValue(Resource.Error(checkThrowable(t), null))
        }
    }

    fun getTermsConditions() =
        viewModelScope.launch {
            safeGetTermsConditionsCall()
        }

    private suspend fun safeGetTermsConditionsCall() {
        termsConditions.postValue(Resource.Loading())
        try {
            val response = repository.getTermsConditions()
            if (response.isSuccessful)
                termsConditions.postValue(Resource.Success(checkResponseBody(response.body()) as TermsConditionsModel))
            else
                termsConditions.postValue(Resource.Error(response.message(), null))
        } catch (t: Throwable) {
            termsConditions.postValue(Resource.Error(checkThrowable(t), null))
        }
    }

}