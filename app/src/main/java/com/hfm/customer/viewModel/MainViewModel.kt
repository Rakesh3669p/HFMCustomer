package com.hfm.customer.viewModel

import android.icu.util.LocaleData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.hfm.customer.commonModel.HomeMainCategoriesModel
import com.hfm.customer.data.Repository
import com.hfm.customer.ui.dashBoard.home.model.FeatureProductsModel
import com.hfm.customer.ui.dashBoard.home.model.FlashSaleModel
import com.hfm.customer.ui.dashBoard.home.model.HomeBottomBannerModel
import com.hfm.customer.ui.dashBoard.home.model.HomeBrandsModel
import com.hfm.customer.ui.dashBoard.home.model.HomeFlashSaleCategory
import com.hfm.customer.ui.dashBoard.home.model.HomeMiddleBanner
import com.hfm.customer.ui.dashBoard.home.model.TrendingNowModel
import com.hfm.customer.ui.dashBoard.home.model.WholeSaleModel
import com.hfm.customer.ui.dashBoard.profile.model.ProfileModel
import com.hfm.customer.ui.fragments.myOrders.model.BulkOrdersListModel
import com.hfm.customer.ui.fragments.products.productDetails.model.BulkOrderRequestModel
import com.hfm.customer.ui.fragments.products.productList.model.ProductListModel
import com.hfm.customer.ui.fragments.products.productDetails.model.ProductDetailsModel
import com.hfm.customer.ui.fragments.products.productDetails.model.SellerVoucherModel
import com.hfm.customer.utils.Resource
import com.hfm.customer.utils.SessionManager
import com.hfm.customer.utils.SingleLiveEvent
import com.hfm.customer.utils.checkResponseBody
import com.hfm.customer.utils.checkThrowable
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: Repository,
    private val sessionManager: SessionManager
) : ViewModel() {

    val productDetails = SingleLiveEvent<Resource<ProductDetailsModel>>()
    val categories = SingleLiveEvent<Resource<HomeMainCategoriesModel>>()

    val homeMainBanner = SingleLiveEvent<Resource<HomeMainCategoriesModel>>()
    val homeMiddleBanner = SingleLiveEvent<Resource<HomeMiddleBanner>>()
    val homeBottomBanner = SingleLiveEvent<Resource<HomeBottomBannerModel>>()
    val homeFlashSaleCategory = SingleLiveEvent<Resource<HomeFlashSaleCategory>>()
    val homeFlashSaleProducts = SingleLiveEvent<Resource<FlashSaleModel>>()
    val homeWholeSaleProducts = SingleLiveEvent<Resource<WholeSaleModel>>()
    val homeTrendingNow = SingleLiveEvent<Resource<TrendingNowModel>>()
    val homeFeatureProducts = SingleLiveEvent<Resource<FeatureProductsModel>>()
    val homeBrands = SingleLiveEvent<Resource<HomeBrandsModel>>()

    val productList = SingleLiveEvent<Resource<ProductListModel>>()
    val sellerVouchers = SingleLiveEvent<Resource<SellerVoucherModel>>()
    val profile = SingleLiveEvent<Resource<ProfileModel>>()
    val sendBulkOrderRequest = SingleLiveEvent<Resource<BulkOrderRequestModel>>()
    val bulkOrderList = SingleLiveEvent<Resource<BulkOrdersListModel>>()


    fun getProductDetails(productId: String) = viewModelScope.launch {
        val jsonObject = JsonObject()
        jsonObject.addProperty("access_token", sessionManager.token)
        jsonObject.addProperty("id", productId)
        jsonObject.addProperty("lang_id", "1")
        jsonObject.addProperty("device_id", "876546587")
        jsonObject.addProperty("page_url", "products/us/img")
        jsonObject.addProperty("os_type", "APP")
        safeGetProductDetailsCall(jsonObject)
    }

    private suspend fun safeGetProductDetailsCall(jsonObject: JsonObject) {
        productDetails.postValue(Resource.Loading())
        try {
            val response = repository.getProductDetails(jsonObject)
            if (response.isSuccessful)
                productDetails.postValue(Resource.Success(checkResponseBody(response.body()) as ProductDetailsModel))
            else
                productDetails.postValue(Resource.Error(response.message(), null))
        } catch (t: Throwable) {
            productDetails.postValue(Resource.Error(checkThrowable(t), null))
        }
    }


    fun getCategories() = viewModelScope.launch {
        val jsonObject = JsonObject()
        jsonObject.addProperty("access_token", "")
        jsonObject.addProperty("lang_id", "1")
        jsonObject.addProperty("page_url", "https://www.dummy.com/blog/")
        jsonObject.addProperty("device_id", "876546587")
        jsonObject.addProperty("os_type", "APP")
        safeGetCategoriesCall(jsonObject)
    }

    private suspend fun safeGetCategoriesCall(jsonObject: JsonObject) {
        categories.postValue(Resource.Loading())
        try {
            val response = repository.getCategories(jsonObject)
            if (response.isSuccessful)
                categories.postValue(Resource.Success(checkResponseBody(response.body()) as HomeMainCategoriesModel))
            else
                categories.postValue(Resource.Error(response.message(), null))
        } catch (t: Throwable) {
            categories.postValue(Resource.Error(checkThrowable(t), null))
        }
    }


    fun getMainBanner() = viewModelScope.launch {
        val jsonObject = JsonObject()
        jsonObject.addProperty("access_token", "")
        jsonObject.addProperty("lang_id", "1")
        jsonObject.addProperty("page_url", "https://www.dummy.com/blog/")
        jsonObject.addProperty("device_id", "876546587")
        jsonObject.addProperty("os_type", "APP")
        safeGetMainBannerCall(jsonObject)
    }

    private suspend fun safeGetMainBannerCall(jsonObject: JsonObject) {
        categories.postValue(Resource.Loading())
        try {
            val response = repository.getMainBanner(jsonObject)
            if (response.isSuccessful)
                categories.postValue(Resource.Success(checkResponseBody(response.body()) as HomeMainCategoriesModel))
            else
                categories.postValue(Resource.Error(response.message(), null))
        } catch (t: Throwable) {
            categories.postValue(Resource.Error(checkThrowable(t), null))
        }
    }

    fun getHomePageData() = viewModelScope.launch {

        val jsonObject1 = JsonObject()
        jsonObject1.addProperty("access_token", "")
        jsonObject1.addProperty("lang_id", "1")
        jsonObject1.addProperty("page_url", "https://www.dummy.com/blog/")
        jsonObject1.addProperty("device_id", "876546587")
        jsonObject1.addProperty("os_type", "APP")


        val middleBannerJson = JsonObject()
        middleBannerJson.addProperty("access_token", "")
        middleBannerJson.addProperty("lang_id", "1")
        middleBannerJson.addProperty("device_id", "54655656fdf")
        middleBannerJson.addProperty("page_url", "876546587")
        middleBannerJson.addProperty("os_type", "APP")

        val bottomBannerJson = JsonObject()
        bottomBannerJson.addProperty("access_token", "")
        bottomBannerJson.addProperty("lang_id", "1")
        bottomBannerJson.addProperty("device_id", "54655656fdf")
        bottomBannerJson.addProperty("page_url", "876546587")
        bottomBannerJson.addProperty("os_type", "APP")

        val brandsJson = JsonObject()
        brandsJson.addProperty("sort_by_name", "")
        brandsJson.addProperty("limit", "5")

        val trendingJson = JsonObject()
        trendingJson.addProperty("page_url", "")
        trendingJson.addProperty("lang_id", "1")
        trendingJson.addProperty("os_type", "APP")


        val mainCatJson = JsonObject()
        mainCatJson.addProperty("access_token", "")
        mainCatJson.addProperty("lang_id", "1")
        mainCatJson.addProperty("page_url", "https://www.dummy.com/blog/")
        mainCatJson.addProperty("device_id", "876546587")
        mainCatJson.addProperty("os_type", "APP")
        safeGetCategoriesCall(mainCatJson)

        val homeMainCategoriesResponse = async { safeGetMainHomeCategories(mainCatJson) }
        val homeMainBannerResponse = async { safeGetHomeMainBannerCall(jsonObject1) }
        val homeMiddleBannerResponse = async { safeGetHomeMiddleBannerCall(middleBannerJson) }
        val homeBottomBannerResponse = async { safeGetHomeBottomBannerCall(bottomBannerJson) }
        val homeBrandsResponse = async { safeGetHomeBrandsCall(brandsJson) }
        val homeTrendingResponse = async { safeGetTrendingCall(trendingJson) }
        val homeFlashSaleProductsResponse = async { safeGetHomeFlashSaleProducts(mainCatJson, "1") }
        val homeWholeSaleProductsResponse = async { safeGetHomeWholeSaleProducts(mainCatJson, "1") }
        val homeFeaturesProductsResponse = async { safeGetHomeFeatureProducts(mainCatJson, "1") }

        when (val homeMainBannerData = homeMainBannerResponse.await()) {
            is Resource.Success -> homeMainBanner.postValue(Resource.Success(homeMainBannerData.data as HomeMainCategoriesModel?))
            is Resource.Loading -> Unit
            is Resource.Error -> homeMainBanner.postValue(Resource.Error(homeMainBannerData.message))
        }

        when (val homeMiddleBannerData = homeMiddleBannerResponse.await()) {
            is Resource.Success -> homeMiddleBanner.postValue(Resource.Success(homeMiddleBannerData.data as HomeMiddleBanner?))
            is Resource.Loading -> Unit
            is Resource.Error -> homeMiddleBanner.postValue(Resource.Error(homeMiddleBannerData.message))
        }

        when (val homeBottomBannerData = homeBottomBannerResponse.await()) {
            is Resource.Success -> homeBottomBanner.postValue(Resource.Success(homeBottomBannerData.data as HomeBottomBannerModel?))
            is Resource.Loading -> Unit
            is Resource.Error -> homeBottomBanner.postValue(Resource.Error(homeBottomBannerData.message))
        }

        when (val homeBrandsData = homeBrandsResponse.await()) {
            is Resource.Success -> homeBrands.postValue(Resource.Success(homeBrandsData.data as HomeBrandsModel?))
            is Resource.Loading -> Unit
            is Resource.Error -> homeBrands.postValue(Resource.Error(homeBrandsData.message))
        }

        when (val homeTrendingData = homeTrendingResponse.await()) {
            is Resource.Success -> homeTrendingNow.postValue(Resource.Success(homeTrendingData.data as TrendingNowModel?))
            is Resource.Loading -> Unit
            is Resource.Error -> homeTrendingNow.postValue(Resource.Error(homeTrendingData.message))
        }

        when (val homeMainCategoriesData = homeMainCategoriesResponse.await()) {
            is Resource.Success -> categories.postValue(Resource.Success(homeMainCategoriesData.data as HomeMainCategoriesModel?))
            is Resource.Loading -> Unit
            is Resource.Error -> categories.postValue(Resource.Error(homeMainCategoriesData.message))
        }

        when (val homeFlashSaleProductsData = homeFlashSaleProductsResponse.await()) {
            is Resource.Success -> homeFlashSaleProducts.postValue(Resource.Success(homeFlashSaleProductsData.data as FlashSaleModel))
            is Resource.Loading -> Unit
            is Resource.Error -> homeFlashSaleProducts.postValue(Resource.Error(homeFlashSaleProductsData.message))
        }

        when (val homeWholeSaleProductsData = homeWholeSaleProductsResponse.await()) {
            is Resource.Success -> homeWholeSaleProducts.postValue(Resource.Success(homeWholeSaleProductsData.data as WholeSaleModel))
            is Resource.Loading -> Unit
            is Resource.Error -> homeWholeSaleProducts.postValue(Resource.Error(homeWholeSaleProductsData.message))
        }

        when (val homeFeaturesProductsData = homeFeaturesProductsResponse.await()) {
            is Resource.Success -> homeFeatureProducts.postValue(Resource.Success(homeFeaturesProductsData.data as FeatureProductsModel))
            is Resource.Loading -> Unit
            is Resource.Error -> homeFeatureProducts.postValue(Resource.Error(homeFeaturesProductsData.message))
        }
    }

    fun getBrandsList() =viewModelScope.launch{
        val brandsJson = JsonObject()
        brandsJson.addProperty("sort_by_name", "")
        brandsJson.addProperty("limit", "")
        val homeBrandsResponse = async { safeGetHomeBrandsCall(brandsJson) }
        when (val homeBrandsData = homeBrandsResponse.await()) {
            is Resource.Success -> homeBrands.postValue(Resource.Success(homeBrandsData.data as HomeBrandsModel?))
            is Resource.Loading -> Unit
            is Resource.Error -> homeBrands.postValue(Resource.Error(homeBrandsData.message))
        }
    }

    private suspend fun safeGetHomeBrandsCall(brandsJson: JsonObject): Resource<out Any> {
        homeBrands.postValue(Resource.Loading())
        return try {
            val response = repository.getBrandsList(brandsJson)
            if (response.isSuccessful)
                Resource.Success(checkResponseBody(response.body()) as HomeBrandsModel)
            else
                Resource.Error(response.message(), null)
        } catch (t: Throwable) {
            Resource.Error(checkThrowable(t), null)
        }
    }

    private suspend fun safeGetTrendingCall(trendingJson: JsonObject): Resource<out Any> {
        homeTrendingNow.postValue(Resource.Loading())
        return try {
            val response = repository.getTrendingNow(trendingJson)
            if (response.isSuccessful)
                Resource.Success(checkResponseBody(response.body()) as TrendingNowModel)
            else
                Resource.Error(response.message(), null)
        } catch (t: Throwable) {
            Resource.Error(checkThrowable(t), null)
        }
    }

    private suspend fun safeGetMainHomeCategories(jsonObject: JsonObject): Resource<out Any> {
        categories.postValue(Resource.Loading())
        return try {
            val response = repository.getCategories(jsonObject)
            if (response.isSuccessful)
                Resource.Success(checkResponseBody(response.body()) as HomeMainCategoriesModel)
            else
                Resource.Error(response.message(), null)
        } catch (t: Throwable) {
            Resource.Error(checkThrowable(t), null)
        }
    }

    private suspend fun safeGetHomeMainBannerCall(jsonObject: JsonObject): Resource<out Any> {
        categories.postValue(Resource.Loading())
        return try {
            val response = repository.getMainBanner(jsonObject)
            if (response.isSuccessful)
                Resource.Success(checkResponseBody(response.body()) as HomeMainCategoriesModel)
            else
                Resource.Error(response.message(), null)
        } catch (t: Throwable) {
            Resource.Error(checkThrowable(t), null)
        }
    }

    private suspend fun safeGetHomeMiddleBannerCall(jsonObject: JsonObject): Resource<out Any> {
        homeMiddleBanner.postValue(Resource.Loading())
        return try {
            val response = repository.getMiddleBanner(jsonObject)
            if (response.isSuccessful)
                Resource.Success(checkResponseBody(response.body()) as HomeMiddleBanner)
            else
                Resource.Error(response.message(), null)
        } catch (t: Throwable) {
            Resource.Error(checkThrowable(t), null)
        }
    }

    private suspend fun safeGetHomeBottomBannerCall(jsonObject: JsonObject): Resource<out Any> {
        homeBottomBanner.postValue(Resource.Loading())
        return try {
            val response = repository.getBottomBanner(jsonObject)
            if (response.isSuccessful)
                Resource.Success(checkResponseBody(response.body()) as HomeBottomBannerModel)
            else
                Resource.Error(response.message(), null)
        } catch (t: Throwable) {
            Resource.Error(checkThrowable(t), null)
        }
    }

    private suspend fun safeGetHomeFlashSaleProducts(
        jsonObject: JsonObject,
        page: String
    ): Resource<out Any> {
        homeFlashSaleProducts.postValue(Resource.Loading())
        return try {
            val response = repository.getHomeFlashSaleProducts(jsonObject, page)
            if (response.isSuccessful)
                Resource.Success(checkResponseBody(response.body()) as FlashSaleModel)
            else
                Resource.Error(response.message(), null)
        } catch (t: Throwable) {
            Resource.Error(checkThrowable(t), null)
        }
    }

    private suspend fun safeGetHomeWholeSaleProducts(
        jsonObject: JsonObject,
        page: String
    ): Resource<out Any> {
        homeWholeSaleProducts.postValue(Resource.Loading())
        return try {
            val response = repository.getWholeSaleProducts(jsonObject, page)
            if (response.isSuccessful)
                Resource.Success(checkResponseBody(response.body()) as WholeSaleModel)
            else
                Resource.Error(response.message(), null)
        } catch (t: Throwable) {
            Resource.Error(checkThrowable(t), null)
        }
    }

    private suspend fun safeGetHomeFeatureProducts(
        jsonObject: JsonObject,
        page: String
    ): Resource<out Any> {
        homeFeatureProducts.postValue(Resource.Loading())
        return try {
            val response = repository.getFeatureProducts(jsonObject, page)
            if (response.isSuccessful)
                Resource.Success(checkResponseBody(response.body()) as FeatureProductsModel)
            else
                Resource.Error(response.message(), null)
        } catch (t: Throwable) {
            Resource.Error(checkThrowable(t), null)
        }
    }


    fun getProductList(
        catId: String,
        subCatId: String = "",
        maxPrice: String = "",
        minPrice: String = "",
        lowToHigh: String = "",
        highToLow: String = "",
        popular: String = "",
        search: String = "",
        deviceId: String,
        page: Int
    ) = viewModelScope.launch {
        val jsonObject = JsonObject()
        jsonObject.addProperty("lang_id", "1")
        jsonObject.addProperty("category_id", catId)
        jsonObject.addProperty("subcategory_id", subCatId)
        jsonObject.addProperty("brand_id", "")
        jsonObject.addProperty("max_price", maxPrice)
        jsonObject.addProperty("min_price", minPrice)
        jsonObject.addProperty("low_to_high", lowToHigh)
        jsonObject.addProperty("high_to_low", highToLow)
        jsonObject.addProperty("latest", "")
        jsonObject.addProperty("popular", popular)
        jsonObject.addProperty("access_token", sessionManager.token)
        jsonObject.addProperty("keyword", search)
        jsonObject.addProperty("device_id", deviceId)
        jsonObject.addProperty("page_url", "https://www.dummy.com/blog/")
        jsonObject.addProperty("os_type", "APP")
        jsonObject.addProperty("limit", "20")
        jsonObject.addProperty("offset", page)

        safeGetProductListCall(jsonObject)
    }

    private suspend fun safeGetProductListCall(jsonObject: JsonObject) {
        productList.postValue(Resource.Loading())
        try {
            val response = repository.getProductList(jsonObject)
            if (response.isSuccessful)
                productList.postValue(Resource.Success(checkResponseBody(response.body()) as ProductListModel))
            else
                productList.postValue(Resource.Error(response.message(), null))
        } catch (t: Throwable) {
            productList.postValue(Resource.Error(checkThrowable(t), null))
        }
    }

    fun getSellerVouchers(sellerId: String) = viewModelScope.launch {
        val jsonObject = JsonObject()
        jsonObject.addProperty("lang_id", "1")
        jsonObject.addProperty("access_token", sessionManager.token)
        jsonObject.addProperty("seller_id", sellerId)

        safeGetSellerVouchersCall(jsonObject)
    }

    private suspend fun safeGetSellerVouchersCall(jsonObject: JsonObject) {
        sellerVouchers.postValue(Resource.Loading())
        try {
            val response = repository.getSellerVouchers(jsonObject)
            if (response.isSuccessful)
                sellerVouchers.postValue(Resource.Success(checkResponseBody(response.body()) as SellerVoucherModel))
            else
                sellerVouchers.postValue(Resource.Error(response.message(), null))
        } catch (t: Throwable) {
            sellerVouchers.postValue(Resource.Error(checkThrowable(t), null))
        }
    }

    fun getProfile() = viewModelScope.launch {
        val jsonObject = JsonObject()
        jsonObject.addProperty("access_token", sessionManager.token)
        safeGetProfileCall(jsonObject)
    }

    private suspend fun safeGetProfileCall(jsonObject: JsonObject) {
        profile.postValue(Resource.Loading())
        try {
            val response = repository.getProfile(jsonObject)
            if (response.isSuccessful)
                profile.postValue(Resource.Success(checkResponseBody(response.body()) as ProfileModel))
            else
                profile.postValue(Resource.Error(response.message(), null))
        } catch (t: Throwable) {
            profile.postValue(Resource.Error(checkThrowable(t), null))
        }
    }

    fun sendBulkOrderRequest(
        productId: String,
        name: String,
        email: String,
        qty: String,
        phone: String,
        date: String,
        deliveryAddress: String,
        remarks: String,
        unitOfMeasures: Int
    ) = viewModelScope.launch {
        val jsonObject = JsonObject().apply {
            addProperty("access_token", sessionManager.token)
            addProperty("prd_id", productId)
            addProperty("name", name)
            addProperty("quantity", qty)
            addProperty("unit_of_measure", unitOfMeasures)
            addProperty("contact_no", phone)
            addProperty("date_needed", date)
            addProperty("email", email)
            addProperty("shipping_address", deliveryAddress)
            addProperty("remarks", remarks)
        }

        safeSendBulkOrderRequestCall(jsonObject)
    }

    private suspend fun safeSendBulkOrderRequestCall(jsonObject: JsonObject) {
        sendBulkOrderRequest.postValue(Resource.Loading())
        try {
            val response = repository.sendBulkOrderRequest(jsonObject)
            if (response.isSuccessful)
                sendBulkOrderRequest.postValue(Resource.Success(checkResponseBody(response.body()) as BulkOrderRequestModel))
            else
                sendBulkOrderRequest.postValue(Resource.Error(response.message(), null))
        } catch (t: Throwable) {
            sendBulkOrderRequest.postValue(Resource.Error(checkThrowable(t), null))
        }
    }

    fun getBulkOrders(pageNo: Int) = viewModelScope.launch {
        val jsonObject = JsonObject().apply {
            addProperty("access_token", sessionManager.token)
            addProperty("lang_id", 1)
            addProperty("search", "")
            addProperty("order_status", "")
            addProperty("order_time", LocalDate.now().year)
            addProperty("limit", "20")
            addProperty("offset", "")
        }

        safeGetBulkOrdersCall(jsonObject)
    }

    private suspend fun safeGetBulkOrdersCall(jsonObject: JsonObject) {
        bulkOrderList.postValue(Resource.Loading())
        try {
            val response = repository.getBulkOrders(jsonObject)
            if (response.isSuccessful)
                bulkOrderList.postValue(Resource.Success(checkResponseBody(response.body()) as BulkOrdersListModel))
            else
                bulkOrderList.postValue(Resource.Error(response.message(), null))
        } catch (t: Throwable) {
            bulkOrderList.postValue(Resource.Error(checkThrowable(t), null))
        }
    }

}