package com.hfm.customer.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.hfm.customer.BuildConfig
import com.hfm.customer.commonModel.AppUpdateModel
import com.hfm.customer.commonModel.ApplyWalletModel
import com.hfm.customer.commonModel.HomeMainCategoriesModel
import com.hfm.customer.commonModel.NotificationViewedModel
import com.hfm.customer.commonModel.RatingReviewsModel
import com.hfm.customer.commonModel.SuccessModel
import com.hfm.customer.commonModel.TermsConditionsModel
import com.hfm.customer.data.Repository
import com.hfm.customer.ui.dashBoard.home.model.FeatureProductsModel
import com.hfm.customer.ui.dashBoard.home.model.FlashSaleModel
import com.hfm.customer.ui.dashBoard.home.model.HomeBottomBannerModel
import com.hfm.customer.ui.dashBoard.home.model.HomeBrandsModel
import com.hfm.customer.ui.dashBoard.home.model.HomeMiddleBanner
import com.hfm.customer.ui.dashBoard.home.model.TrendingNowModel
import com.hfm.customer.ui.dashBoard.home.model.WholeSaleModel
import com.hfm.customer.ui.dashBoard.profile.model.ProfileModel
import com.hfm.customer.ui.fragments.address.model.AddressModel
import com.hfm.customer.ui.fragments.address.model.CityCodeModel
import com.hfm.customer.ui.fragments.address.model.CountryCodeModel
import com.hfm.customer.ui.fragments.address.model.StateCodeModel
import com.hfm.customer.ui.fragments.blogs.model.BlogDetailsModel
import com.hfm.customer.ui.fragments.blogs.model.BlogsModel
import com.hfm.customer.ui.fragments.brands.model.BrandsModel
import com.hfm.customer.ui.fragments.cart.model.CartModel
import com.hfm.customer.ui.fragments.cart.model.CouponAppliedModel
import com.hfm.customer.ui.dashBoard.chat.model.ChatListModel
import com.hfm.customer.ui.dashBoard.chat.model.ChatMessageModel
import com.hfm.customer.ui.dashBoard.chat.model.MessageSentModel
import com.hfm.customer.ui.fragments.checkOut.model.CheckOutModel
import com.hfm.customer.ui.fragments.commonPage.model.PageModel
import com.hfm.customer.ui.fragments.myOrders.model.BulkOrdersListModel
import com.hfm.customer.ui.fragments.myOrders.model.MyOrdersModel
import com.hfm.customer.ui.fragments.myOrders.model.OrderHistoryModel
import com.hfm.customer.ui.fragments.notifications.model.NotificationModel
import com.hfm.customer.ui.fragments.orderTracking.OrderTrackingModel
import com.hfm.customer.ui.fragments.payment.model.PaymentFAQModel
import com.hfm.customer.ui.fragments.payment.model.PlaceOrderModel
import com.hfm.customer.ui.fragments.products.productDetails.model.AddToCartModel
import com.hfm.customer.ui.fragments.products.productDetails.model.BulkOrderRequestModel
import com.hfm.customer.ui.fragments.products.productList.model.ProductListModel
import com.hfm.customer.ui.fragments.products.productDetails.model.ProductDetailsModel
import com.hfm.customer.ui.fragments.search.model.RelatedSearchTermsModel
import com.hfm.customer.ui.fragments.store.model.StoreDetailsModel
import com.hfm.customer.ui.fragments.store.model.StoreProductsModel
import com.hfm.customer.ui.fragments.store.model.StoreReviewsModel
import com.hfm.customer.ui.fragments.support.model.SupportMessagesModel
import com.hfm.customer.ui.fragments.support.model.SupportTicketsModel
import com.hfm.customer.ui.fragments.vouchers.model.VoucherListModel
import com.hfm.customer.ui.fragments.wallet.model.WalletModel
import com.hfm.customer.ui.fragments.wishlist.model.StoreWishlistModel
import com.hfm.customer.ui.fragments.wishlist.model.WishListModel
import com.hfm.customer.utils.Resource
import com.hfm.customer.utils.SessionManager
import com.hfm.customer.utils.SingleLiveEvent
import com.hfm.customer.utils.checkResponseBody
import com.hfm.customer.utils.checkThrowable
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import okhttp3.RequestBody
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: Repository,
    private val sessionManager: SessionManager
) : ViewModel() {

    val showHomeLoader = SingleLiveEvent<Boolean>()
    val checkLogin = SingleLiveEvent<Resource<SuccessModel>>()
    val checkAppUpdate = SingleLiveEvent<Resource<AppUpdateModel>>()
    val productDetails = SingleLiveEvent<Resource<ProductDetailsModel>>()
    val categories: MutableLiveData<Resource<HomeMainCategoriesModel>> = MutableLiveData()

    val homeMainBanner = SingleLiveEvent<Resource<HomeMainCategoriesModel>>()
    val homeMiddleBanner = SingleLiveEvent<Resource<HomeMiddleBanner>>()
    val homeBottomBanner = SingleLiveEvent<Resource<HomeBottomBannerModel>>()
    val homeFlashSaleProducts = SingleLiveEvent<Resource<FlashSaleModel>>()
    val homeWholeSaleProducts = SingleLiveEvent<Resource<WholeSaleModel>>()
    val homeTrendingNow = SingleLiveEvent<Resource<TrendingNowModel>>()
    val homeFeatureProducts = SingleLiveEvent<Resource<FeatureProductsModel>>()
    val homeBrands = SingleLiveEvent<Resource<HomeBrandsModel>>()
    val brands = SingleLiveEvent<Resource<BrandsModel>>()
    val pageData = SingleLiveEvent<Resource<PageModel>>()
    val blogsList = SingleLiveEvent<Resource<BlogsModel>>()
    val blogsDetails = SingleLiveEvent<Resource<BlogDetailsModel>>()
    val followShop = SingleLiveEvent<Resource<SuccessModel>>()
    val unFollowShop = SingleLiveEvent<Resource<SuccessModel>>()
    val followedShops = SingleLiveEvent<Resource<StoreWishlistModel>>()
    val wallet = SingleLiveEvent<Resource<WalletModel>>()
    val selectCart = SingleLiveEvent<Resource<SuccessModel>>()
    val chatList: MutableLiveData<Resource<ChatListModel>> = MutableLiveData()

    val chatMessages = SingleLiveEvent<Resource<ChatMessageModel>>()
    val supportChatMessages = SingleLiveEvent<Resource<SupportMessagesModel>>()

    val productList = SingleLiveEvent<Resource<ProductListModel>>()
    val sellerVouchers = SingleLiveEvent<Resource<VoucherListModel>>()
    val platformVouchers = SingleLiveEvent<Resource<VoucherListModel>>()
    val claimedVouchers = SingleLiveEvent<Resource<VoucherListModel>>()
    val claimStoreVoucher = SingleLiveEvent<Resource<SuccessModel>>()
    val applyPlatformVoucher = SingleLiveEvent<Resource<CouponAppliedModel>>()
    val applySellerVoucher = SingleLiveEvent<Resource<CouponAppliedModel>>()
    val relatedSearchTerms = SingleLiveEvent<Resource<RelatedSearchTermsModel>>()
    val notifications = SingleLiveEvent<Resource<NotificationModel>>()
    val notificationViewed = SingleLiveEvent<Resource<NotificationViewedModel>>()
    val profile: MutableLiveData<Resource<ProfileModel>> = MutableLiveData()
    val productReview: MutableLiveData<Resource<RatingReviewsModel>> = MutableLiveData()
    val sendBulkOrderRequest = SingleLiveEvent<Resource<BulkOrderRequestModel>>()
    val bulkOrderList = SingleLiveEvent<Resource<BulkOrdersListModel>>()
    val addBulkOrdersAction = SingleLiveEvent<Resource<SuccessModel>>()
    val addToWishList = SingleLiveEvent<Resource<SuccessModel>>()
    val removeFromWishList = SingleLiveEvent<Resource<SuccessModel>>()
    val removeCoupon = SingleLiveEvent<Resource<SuccessModel>>()
    val wishListProducts = SingleLiveEvent<Resource<WishListModel>>()
    val address = SingleLiveEvent<Resource<AddressModel>>()
    val checkOutInfo = SingleLiveEvent<Resource<CheckOutModel>>()
    val addNewAddress = SingleLiveEvent<Resource<SuccessModel>>()
    val deleteAddress = SingleLiveEvent<Resource<SuccessModel>>()
    val deleteAccount = SingleLiveEvent<Resource<SuccessModel>>()
    val defaultAddress = SingleLiveEvent<Resource<SuccessModel>>()
    val updateAddress = SingleLiveEvent<Resource<SuccessModel>>()
    val addToCart = SingleLiveEvent<Resource<AddToCartModel>>()
    val addToCartMultiple = SingleLiveEvent<Resource<AddToCartModel>>()
    val cart = SingleLiveEvent<Resource<CartModel>>()
    val deleterCartProduct = SingleLiveEvent<Resource<AddToCartModel>>()
    val deleteCartProductForVariant = SingleLiveEvent<Resource<SuccessModel>>()
    val checkAvailability = SingleLiveEvent<Resource<SuccessModel>>()
    val updateProfileCustomer = SingleLiveEvent<Resource<SuccessModel>>()
    val submitReview = SingleLiveEvent<Resource<SuccessModel>>()
    val ratingReview = SingleLiveEvent<Resource<RatingReviewsModel>>()
    val sendMessage = SingleLiveEvent<Resource<MessageSentModel>>()
    val sendSupportMessage = SingleLiveEvent<Resource<SuccessModel>>()
    val orderHistory = SingleLiveEvent<Resource<OrderHistoryModel>>()
    val createSupportTicket = SingleLiveEvent<Resource<SuccessModel>>()
    val supportTickets = SingleLiveEvent<Resource<SupportTicketsModel>>()
    val updateProfileBusiness = SingleLiveEvent<Resource<SuccessModel>>()
    val updateCartCount = SingleLiveEvent<Resource<SuccessModel>>()
    val storeDetails = SingleLiveEvent<Resource<StoreDetailsModel>>()
    val storeProducts = SingleLiveEvent<Resource<StoreProductsModel>>()
    val storeReview = SingleLiveEvent<Resource<StoreReviewsModel>>()
    val myOrders = SingleLiveEvent<Resource<MyOrdersModel>>()
    val placeOrder = SingleLiveEvent<Resource<PlaceOrderModel>>()
    val uploadOrderReceipt = SingleLiveEvent<Resource<SuccessModel>>()
    val paymentFaq = SingleLiveEvent<Resource<PaymentFAQModel>>()
    val countryCode = SingleLiveEvent<Resource<CountryCodeModel>>()
    val stateCode = SingleLiveEvent<Resource<StateCodeModel>>()
    val cityCode = SingleLiveEvent<Resource<CityCodeModel>>()
    val termsConditions = SingleLiveEvent<Resource<TermsConditionsModel>>()
    val orderTracking = SingleLiveEvent<Resource<OrderTrackingModel>>()
    val applyWallet = SingleLiveEvent<Resource<ApplyWalletModel>>()
    val removeWallet = SingleLiveEvent<Resource<SuccessModel>>()
    val updateshipingOption = SingleLiveEvent<Resource<SuccessModel>>()
    val bannerActivity = SingleLiveEvent<Resource<SuccessModel>>()
    val logOut = SingleLiveEvent<Resource<SuccessModel>>()


    fun getAppUpdate() = viewModelScope.launch {
        val jsonObject = JsonObject()
        jsonObject.addProperty("platform", "android")
        jsonObject.addProperty("version_code", BuildConfig.VERSION_CODE)
        safeGetAppUpdateCall(jsonObject)
    }

    private suspend fun safeGetAppUpdateCall(jsonObject: JsonObject) {
        checkAppUpdate.postValue(Resource.Loading())
        try {
            val response = repository.getAppUpdate(jsonObject)
            if (response.isSuccessful)
                checkAppUpdate.postValue(Resource.Success(checkResponseBody(response.body()) as AppUpdateModel))
            else
                checkAppUpdate.postValue(Resource.Error(response.message(), null))
        } catch (t: Throwable) {
            checkAppUpdate.postValue(Resource.Error(checkThrowable(t), null))
        }
    }

    fun checkLogin() = viewModelScope.launch {
        val jsonObject = JsonObject()
        jsonObject.addProperty("access_token", sessionManager.token)
        jsonObject.addProperty("deviceToken", "")
        jsonObject.addProperty("deviceId", sessionManager.deviceId)
        jsonObject.addProperty("deviceName", "")
        jsonObject.addProperty("os", "app")
        safeCheckLoginCall(jsonObject)
    }

    private suspend fun safeCheckLoginCall(jsonObject: JsonObject) {
        checkLogin.postValue(Resource.Loading())
        try {
            val response = repository.checkLogin(jsonObject)
            if (response.isSuccessful)
                checkLogin.postValue(Resource.Success(checkResponseBody(response.body()) as SuccessModel))
            else
                checkLogin.postValue(Resource.Error(response.message(), null))
        } catch (t: Throwable) {
            checkLogin.postValue(Resource.Error(checkThrowable(t), null))
        }
    }

    fun getProductDetails(productId: String) = viewModelScope.launch {
        val jsonObject = JsonObject()
        jsonObject.addProperty("access_token", sessionManager.token)
        jsonObject.addProperty("id", productId)
        jsonObject.addProperty("lang_id", "1")
        jsonObject.addProperty("device_id", "876546587")
        jsonObject.addProperty("page_url", "products/us/img")
        jsonObject.addProperty("os_type", "app")
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
        jsonObject.addProperty("os_type", "app")
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
        jsonObject.addProperty("os_type", "app")
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
        showHomeLoader.postValue(true)
        val jsonObject1 = JsonObject()
        jsonObject1.addProperty("access_token", sessionManager.token)
        jsonObject1.addProperty("lang_id", "1")
        jsonObject1.addProperty("page_url", "https://www.dummy.com/blog/")
        jsonObject1.addProperty("device_id", sessionManager.deviceId)
        jsonObject1.addProperty("os_type", "app")


        val middleBannerJson = JsonObject()
        middleBannerJson.addProperty("access_token", "")
        middleBannerJson.addProperty("lang_id", "1")
        middleBannerJson.addProperty("device_id", sessionManager.deviceId)
        middleBannerJson.addProperty("page_url", "https://www.dummy.com/blog/")
        middleBannerJson.addProperty("os_type", "app")

        val bottomBannerJson = JsonObject()
        bottomBannerJson.addProperty("access_token", "")
        bottomBannerJson.addProperty("lang_id", "1")
        bottomBannerJson.addProperty("device_id", sessionManager.deviceId)
        bottomBannerJson.addProperty("page_url", "https://www.dummy.com/blog/")
        bottomBannerJson.addProperty("os_type", "app")

        val brandsJson = JsonObject()
        brandsJson.addProperty("sort_by_name", "")
        brandsJson.addProperty("limit", "")

        val trendingJson = JsonObject()
        trendingJson.addProperty("page_url", "")
        trendingJson.addProperty("lang_id", "1")
        trendingJson.addProperty("os_type", "app")


        val mainCatJson = JsonObject()
        mainCatJson.addProperty("access_token", "")
        mainCatJson.addProperty("lang_id", "1")
        mainCatJson.addProperty("page_url", "https://www.dummy.com/blog/")
        mainCatJson.addProperty("device_id", sessionManager.deviceId)
        mainCatJson.addProperty("os_type", "app")
        safeGetCategoriesCall(mainCatJson)

        val featureProductsJson = JsonObject()
        featureProductsJson.addProperty("lang_id", 1)
        featureProductsJson.addProperty("access_token", sessionManager.token)
        featureProductsJson.addProperty("category_id", "")
        featureProductsJson.addProperty("subcategory_id", "")
        featureProductsJson.addProperty("brand_id", "")
        featureProductsJson.addProperty("max_price", "")
        featureProductsJson.addProperty("min_price", "")
        featureProductsJson.addProperty("low_to_high", "")
        featureProductsJson.addProperty("high_to_low", "")
        featureProductsJson.addProperty("latest", "")
        featureProductsJson.addProperty("device_id", sessionManager.deviceId)
        featureProductsJson.addProperty("page_url", "http://brands/us/img")
        featureProductsJson.addProperty("os_type", "app")

        safeGetCategoriesCall(mainCatJson)

        val homeMainCategoriesResponse = async { safeGetMainHomeCategories(mainCatJson) }
        val homeMainBannerResponse = async { safeGetHomeMainBannerCall(jsonObject1) }
        val homeMiddleBannerResponse = async { safeGetHomeMiddleBannerCall(middleBannerJson) }
        val homeBrandsResponse = async { safeGetHomeBrandsCall(brandsJson) }
        val homeTrendingResponse = async { safeGetTrendingCall(trendingJson) }
        val homeFlashSaleProductsResponse = async { safeGetHomeFlashSaleProducts(mainCatJson, "1") }
        val homeWholeSaleProductsResponse = async { safeGetHomeWholeSaleProducts(mainCatJson, "1") }
        val homeFeaturesProductsResponse = async { safeGetHomeFeatureProducts(featureProductsJson) }
//        val homeBottomBannerResponse = async { safeGetHomeBottomBannerCall(bottomBannerJson) }

        val responses = listOf(
            homeMainCategoriesResponse.await(),
            homeMainBannerResponse.await(),
            homeMiddleBannerResponse.await(),
            homeBrandsResponse.await(),
            homeTrendingResponse.await(),
            homeFlashSaleProductsResponse.await(),
            homeWholeSaleProductsResponse.await(),
            homeFeaturesProductsResponse.await()
        )
        showHomeLoader.postValue(false)

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

        /*when (val homeBottomBannerData = homeBottomBannerResponse.await()) {
            is Resource.Success -> homeBottomBanner.postValue(Resource.Success(homeBottomBannerData.data as HomeBottomBannerModel?))
            is Resource.Loading -> Unit
            is Resource.Error -> homeBottomBanner.postValue(Resource.Error(homeBottomBannerData.message))
        }*/

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
            is Resource.Success -> homeFlashSaleProducts.postValue(
                Resource.Success(
                    homeFlashSaleProductsData.data as FlashSaleModel
                )
            )

            is Resource.Loading -> Unit
            is Resource.Error -> homeFlashSaleProducts.postValue(
                Resource.Error(
                    homeFlashSaleProductsData.message
                )
            )
        }

        when (val homeWholeSaleProductsData = homeWholeSaleProductsResponse.await()) {
            is Resource.Success -> homeWholeSaleProducts.postValue(
                Resource.Success(
                    homeWholeSaleProductsData.data as WholeSaleModel
                )
            )

            is Resource.Loading -> Unit
            is Resource.Error -> homeWholeSaleProducts.postValue(
                Resource.Error(
                    homeWholeSaleProductsData.message
                )
            )
        }

        when (val homeFeaturesProductsData = homeFeaturesProductsResponse.await()) {
            is Resource.Success -> homeFeatureProducts.postValue(
                Resource.Success(
                    homeFeaturesProductsData.data as FeatureProductsModel
                )
            )

            is Resource.Loading -> Unit
            is Resource.Error -> homeFeatureProducts.postValue(
                Resource.Error(
                    homeFeaturesProductsData.message
                )
            )
        }
    }

    fun getBrandsList() = viewModelScope.launch {
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
        jsonObject: JsonObject
    ): Resource<out Any> {
        homeFeatureProducts.postValue(Resource.Loading())
        return try {
            val response = repository.getFeatureProducts(jsonObject)
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
        brandId: String = "",
        maxPrice: String = "",
        minPrice: String = "",
        lowToHigh: String = "",
        highToLow: String = "",
        popular: String = "",
        latest: String = "",
        search: String = "",
        frozen: Int = 0,
        wholeSale: Int = 0,
        flashSale: Int = 0,
        chilled: Int = 0,
        deviceId: String,
        random: Int,
        page: Int
    ) = viewModelScope.launch {
        val jsonObject = JsonObject()
        jsonObject.addProperty("lang_id", "1")
        jsonObject.addProperty("category_id", catId)
        jsonObject.addProperty("subcategory_id", subCatId)
        jsonObject.addProperty("brand_id", brandId)
        jsonObject.addProperty("max_price", maxPrice)
        jsonObject.addProperty("min_price", minPrice)
        jsonObject.addProperty("low_to_high", lowToHigh)
        jsonObject.addProperty("high_to_low", highToLow)
        jsonObject.addProperty("latest", latest)
        jsonObject.addProperty("popular", popular)
        jsonObject.addProperty("frozen", frozen)
        jsonObject.addProperty("wholesale", wholeSale)
        jsonObject.addProperty("flash_deal", flashSale)
        jsonObject.addProperty("chilled", chilled)
        jsonObject.addProperty("access_token", sessionManager.token)
        jsonObject.addProperty("keyword", search)
        jsonObject.addProperty("device_id", deviceId)
        jsonObject.addProperty("page_url", "products/us/img")
        jsonObject.addProperty("os_type", "app")
        jsonObject.addProperty("limit", 60)
        jsonObject.addProperty("random", random)
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

    fun getSellerVouchers(sellerId: String, available: Int) = viewModelScope.launch {
        val jsonObject = JsonObject()
        jsonObject.addProperty("lang_id", "1")
        jsonObject.addProperty("access_token", sessionManager.token)
        jsonObject.addProperty("seller_id", sellerId)
        jsonObject.addProperty("available", available)
        safeGetSellerVouchersCall(jsonObject)
    }

    private suspend fun safeGetSellerVouchersCall(jsonObject: JsonObject) {
        sellerVouchers.postValue(Resource.Loading())
        try {
            val response = repository.getSellerVouchers(jsonObject)
            if (response.isSuccessful)
                sellerVouchers.postValue(Resource.Success(checkResponseBody(response.body()) as VoucherListModel))
            else
                sellerVouchers.postValue(Resource.Error(response.message(), null))
        } catch (t: Throwable) {
            sellerVouchers.postValue(Resource.Error(checkThrowable(t), null))
        }
    }

    fun getProfile() = viewModelScope.launch {

        val jsonObject = JsonObject()
        jsonObject.addProperty("access_token", sessionManager.token)
        if (sessionManager.isLogin) {
            safeGetProfileCall(jsonObject)
        }
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

    fun getProductReview(
        productId: String,
        limit: Int,
        pageNo: Int = 0,
        rating: String = "",
        media: String = ""
    ) = viewModelScope.launch {
        val jsonObject = JsonObject()
        jsonObject.addProperty("access_token", sessionManager.token)
        jsonObject.addProperty("product_id", productId)
        jsonObject.addProperty("limit", limit)
        jsonObject.addProperty("offset", pageNo)
        jsonObject.addProperty("rating", rating)
        jsonObject.addProperty("media_filter", media)
        jsonObject.addProperty("os_type", "app")
        safeGetProductReviewCall(jsonObject)

    }

    private suspend fun safeGetProductReviewCall(jsonObject: JsonObject) {
        productReview.postValue(Resource.Loading())
        try {
            val response = repository.getProductReview(jsonObject)
            if (response.isSuccessful)
                productReview.postValue(Resource.Success(checkResponseBody(response.body()) as RatingReviewsModel))
            else
                productReview.postValue(Resource.Error(response.message(), null))
        } catch (t: Throwable) {
            productReview.postValue(Resource.Error(checkThrowable(t), null))
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

    fun getBulkOrders(search: String = "") = viewModelScope.launch {
        val jsonObject = JsonObject().apply {
            addProperty("access_token", sessionManager.token)
            addProperty("lang_id", 1)
            addProperty("search", search)
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

    fun addBulkOrdersAction(requestId: String, status: Int) = viewModelScope.launch {
        val jsonObject = JsonObject().apply {
            addProperty("access_token", sessionManager.token)
            addProperty("lang_id", 1)
            addProperty("bulkrequest_id", requestId)
            addProperty("request_status", status)
        }

        safeAddBulkOrdersActionCall(jsonObject)
    }

    private suspend fun safeAddBulkOrdersActionCall(jsonObject: JsonObject) {
        addBulkOrdersAction.postValue(Resource.Loading())
        try {
            val response = repository.addBulkOrdersAction(jsonObject)
            if (response.isSuccessful)
                addBulkOrdersAction.postValue(Resource.Success(checkResponseBody(response.body()) as SuccessModel))
            else
                addBulkOrdersAction.postValue(Resource.Error(response.message(), null))
        } catch (t: Throwable) {
            addBulkOrdersAction.postValue(Resource.Error(checkThrowable(t), null))
        }
    }


    fun addToWishList(productId: String) = viewModelScope.launch {
        val jsonObject = JsonObject()
        jsonObject.addProperty("access_token", sessionManager.token)
        jsonObject.addProperty("product_id", productId)
        jsonObject.addProperty("type", "APP")
        jsonObject.addProperty("os_type", "app")
        safeAddToWishListCall(jsonObject)
    }


    private suspend fun safeAddToWishListCall(jsonObject: JsonObject) {
        addToWishList.postValue(Resource.Loading())
        try {
            val response = repository.addToWishList(jsonObject)
            if (response.isSuccessful)
                addToWishList.postValue(Resource.Success(checkResponseBody(response.body()) as SuccessModel))
            else
                addToWishList.postValue(Resource.Error(response.message(), null))
        } catch (t: Throwable) {
            addToWishList.postValue(Resource.Error(checkThrowable(t), null))
        }
    }

    fun removeFromWishList(productId: String) = viewModelScope.launch {
        val jsonObject = JsonObject()
        jsonObject.addProperty("access_token", sessionManager.token)
        jsonObject.addProperty("product_id", productId)
        jsonObject.addProperty("type", "app")
        safeRemoveFromWishListCall(jsonObject)
    }

    private suspend fun safeRemoveFromWishListCall(jsonObject: JsonObject) {
        removeFromWishList.postValue(Resource.Loading())
        try {
            val response = repository.removeFromWishList(jsonObject)
            if (response.isSuccessful)
                removeFromWishList.postValue(Resource.Success(checkResponseBody(response.body()) as SuccessModel))
            else
                removeFromWishList.postValue(Resource.Error(response.message(), null))
        } catch (t: Throwable) {
            removeFromWishList.postValue(Resource.Error(checkThrowable(t), null))
        }
    }

    fun removeCoupon(couponId: String, couponType: String) = viewModelScope.launch {
        val jsonObject = JsonObject()
        jsonObject.addProperty("access_token", sessionManager.token)
        jsonObject.addProperty("coupon_id", couponId)
        jsonObject.addProperty("coupon_type", couponType)
        jsonObject.addProperty("lang_id", "")
        safeRemoveCouponCall(jsonObject)
    }

    private suspend fun safeRemoveCouponCall(jsonObject: JsonObject) {
        removeCoupon.postValue(Resource.Loading())
        try {
            val response = repository.removeCoupon(jsonObject)
            if (response.isSuccessful)
                removeCoupon.postValue(Resource.Success(checkResponseBody(response.body()) as SuccessModel))
            else
                removeCoupon.postValue(Resource.Error(response.message(), null))
        } catch (t: Throwable) {
            removeCoupon.postValue(Resource.Error(checkThrowable(t), null))
        }
    }

    fun getWishListProducts() = viewModelScope.launch {
        val jsonObject = JsonObject()
        jsonObject.addProperty("access_token", sessionManager.token)
        jsonObject.addProperty("lang_id", 1)
        jsonObject.addProperty("os_type", "app")
        safeWishListProductsCall(jsonObject)
    }

    private suspend fun safeWishListProductsCall(jsonObject: JsonObject) {
        wishListProducts.postValue(Resource.Loading())
        try {
            val response = repository.getWishListProducts(jsonObject)
            if (response.isSuccessful)
                wishListProducts.postValue(Resource.Success(checkResponseBody(response.body()) as WishListModel))
            else
                wishListProducts.postValue(Resource.Error(response.message(), null))
        } catch (t: Throwable) {
            wishListProducts.postValue(Resource.Error(checkThrowable(t), null))
        }
    }

    fun getAddress() = viewModelScope.launch {
        val jsonObject = JsonObject()
        jsonObject.addProperty("access_token", sessionManager.token)
        jsonObject.addProperty("os_type", "app")
        safeGetAddressCall(jsonObject)
    }

    private suspend fun safeGetAddressCall(jsonObject: JsonObject) {
        address.postValue(Resource.Loading())
        try {
            val response = repository.getAddress(jsonObject)
            if (response.isSuccessful)
                address.postValue(Resource.Success(checkResponseBody(response.body()) as AddressModel))
            else
                address.postValue(Resource.Error(response.message(), null))
        } catch (t: Throwable) {
            address.postValue(Resource.Error(checkThrowable(t), null))
        }
    }

    fun getCheckoutInfo() = viewModelScope.launch {
        val jsonObject = JsonObject()
        jsonObject.addProperty("access_token", sessionManager.token)
        jsonObject.addProperty("os_type", "app")
        safeGetCheckoutInfoCall(jsonObject)
    }

    private suspend fun safeGetCheckoutInfoCall(jsonObject: JsonObject) {
        checkOutInfo.postValue(Resource.Loading())
        try {
            val response = repository.getCheckOutInfo(jsonObject)
            if (response.isSuccessful)
                checkOutInfo.postValue(Resource.Success(checkResponseBody(response.body()) as CheckOutModel))
            else
                checkOutInfo.postValue(Resource.Error(response.message(), null))
        } catch (t: Throwable) {
            checkOutInfo.postValue(Resource.Error(checkThrowable(t), null))
        }
    }

    fun addNewAddress(
        name: String,
        address_type: Int,
        country_code: String,
        phone: String,
        country: String,
        state: String,
        city: String,
        address1: String,
        address2: String,
        pincode: String,
        latitude: String,
        longitude: String,
        is_default: Int,
        house: String,
        street: String,
    ) = viewModelScope.launch {
        val jsonObject = JsonObject()
        jsonObject.addProperty("access_token", sessionManager.token)
        jsonObject.addProperty("name", name)
        jsonObject.addProperty("address_type", address_type)
        jsonObject.addProperty("country_code", country_code)
        jsonObject.addProperty("phone", phone)
        jsonObject.addProperty("street", street)
        jsonObject.addProperty("house", house)
        jsonObject.addProperty("address1", address1)
        jsonObject.addProperty("address2", address2)
        jsonObject.addProperty("country", country)
        jsonObject.addProperty("state", state)
        jsonObject.addProperty("city", city)
        jsonObject.addProperty("pincode", pincode)
        jsonObject.addProperty("latitude", latitude.toDouble())
        jsonObject.addProperty("longitude", longitude.toDouble())
        jsonObject.addProperty("is_default", is_default)
        jsonObject.addProperty("os_type", "app")
        safeAddNewAddressCall(jsonObject)
    }

    private suspend fun safeAddNewAddressCall(jsonObject: JsonObject) {
        addNewAddress.postValue(Resource.Loading())
        try {
            val response = repository.addNewAddress(jsonObject)
            if (response.isSuccessful)
                addNewAddress.postValue(Resource.Success(checkResponseBody(response.body()) as SuccessModel))
            else
                addNewAddress.postValue(Resource.Error(response.message(), null))
        } catch (t: Throwable) {
            addNewAddress.postValue(Resource.Error(checkThrowable(t), null))
        }
    }

    fun deleteAddress(addressId: String) = viewModelScope.launch {
        val jsonObject = JsonObject()
        jsonObject.addProperty("access_token", sessionManager.token)
        jsonObject.addProperty("address_id", addressId)

        safeDeleteAddressCall(jsonObject)
    }

    private suspend fun safeDeleteAddressCall(jsonObject: JsonObject) {
        deleteAddress.postValue(Resource.Loading())
        try {
            val response = repository.deleteAddress(jsonObject)
            if (response.isSuccessful)
                deleteAddress.postValue(Resource.Success(checkResponseBody(response.body()) as SuccessModel))
            else
                deleteAddress.postValue(Resource.Error(response.message(), null))
        } catch (t: Throwable) {
            deleteAddress.postValue(Resource.Error(checkThrowable(t), null))
        }
    }

    fun deleteAccount() = viewModelScope.launch {
        val jsonObject = JsonObject()
        jsonObject.addProperty("access_token", sessionManager.token)
        safeDeleteAccountCall(jsonObject)
    }

    private suspend fun safeDeleteAccountCall(jsonObject: JsonObject) {
        deleteAccount.postValue(Resource.Loading())
        try {
            val response = repository.deleteAccount(jsonObject)
            if (response.isSuccessful)
                deleteAccount.postValue(Resource.Success(checkResponseBody(response.body()) as SuccessModel))
            else
                deleteAccount.postValue(Resource.Error(response.message(), null))
        } catch (t: Throwable) {
            deleteAccount.postValue(Resource.Error(checkThrowable(t), null))
        }
    }

    fun defaultAddress(addressId: String) = viewModelScope.launch {
        val jsonObject = JsonObject()
        jsonObject.addProperty("access_token", sessionManager.token)
        jsonObject.addProperty("address_id", addressId)
        jsonObject.addProperty("is_default", 1)
        safeDefaultAddressCall(jsonObject)
    }

    private suspend fun safeDefaultAddressCall(jsonObject: JsonObject) {
        defaultAddress.postValue(Resource.Loading())
        try {
            val response = repository.defaultAddress(jsonObject)
            if (response.isSuccessful)
                defaultAddress.postValue(Resource.Success(checkResponseBody(response.body()) as SuccessModel))
            else
                defaultAddress.postValue(Resource.Error(response.message(), null))
        } catch (t: Throwable) {
            defaultAddress.postValue(Resource.Error(checkThrowable(t), null))
        }
    }

    fun updateAddress(
        address_id: Int,
        name: String,
        address_type: Int,
        country_code: String,
        phone: String,
        country: String,
        state: String,
        city: String,
        address1: String,
        address2: String,
        pincode: String,
        latitude: String,
        longitude: String,
        is_default: Int,
        house: String,
        street: String,
    ) = viewModelScope.launch {
        val jsonObject = JsonObject()
        jsonObject.addProperty("access_token", sessionManager.token)
        jsonObject.addProperty("address_id", address_id)
        jsonObject.addProperty("name", name)
        jsonObject.addProperty("address_type", address_type)
        jsonObject.addProperty("country_code", country_code)
        jsonObject.addProperty("phone", phone)
        jsonObject.addProperty("street", street)
        jsonObject.addProperty("house", house)
        jsonObject.addProperty("address1", address1)
        jsonObject.addProperty("address2", address2)
        jsonObject.addProperty("country", country)
        jsonObject.addProperty("state", state)
        jsonObject.addProperty("city", city)
        jsonObject.addProperty("pincode", pincode)
        jsonObject.addProperty("latitude", latitude)
        jsonObject.addProperty("longitude", longitude)
        jsonObject.addProperty("is_default", is_default)
        safeUpdateAddressCall(jsonObject)
    }

    private suspend fun safeUpdateAddressCall(jsonObject: JsonObject) {
        updateAddress.postValue(Resource.Loading())
        try {
            val response = repository.updateAddress(jsonObject)
            if (response.isSuccessful)
                updateAddress.postValue(Resource.Success(checkResponseBody(response.body()) as SuccessModel))
            else
                updateAddress.postValue(Resource.Error(response.message(), null))
        } catch (t: Throwable) {
            updateAddress.postValue(Resource.Error(checkThrowable(t), null))
        }
    }

    fun addToCart(productId: String, quantity: String) = viewModelScope.launch {
        val jsonObject = JsonObject()
        jsonObject.addProperty("access_token", sessionManager.token)
        jsonObject.addProperty("product_id", productId)
        jsonObject.addProperty("quantity", quantity)
        jsonObject.addProperty("cart_type", "app")
        safeAddToCartCall(jsonObject)
    }

    private suspend fun safeAddToCartCall(jsonObject: JsonObject) {
        addToCart.postValue(Resource.Loading())
        try {
            val response = repository.addToCart(jsonObject)
            if (response.isSuccessful)
                addToCart.postValue(Resource.Success(checkResponseBody(response.body()) as AddToCartModel))
            else
                addToCart.postValue(Resource.Error(response.message(), null))
        } catch (t: Throwable) {
            addToCart.postValue(Resource.Error(checkThrowable(t), null))
        }
    }


    fun addToCartMultiple(productId: String, quantity: String) = viewModelScope.launch {
        val jsonObject = JsonObject()
        jsonObject.addProperty("access_token", sessionManager.token)
        jsonObject.addProperty("product_id", productId)
        jsonObject.addProperty("quantity", quantity)
        jsonObject.addProperty("cart_type", "app")
        safeAddToCartMultipleCall(jsonObject)
    }

    private suspend fun safeAddToCartMultipleCall(jsonObject: JsonObject) {
        addToCartMultiple.postValue(Resource.Loading())
        try {
            val response = repository.addToCartMultiple(jsonObject)
            if (response.isSuccessful)
                addToCartMultiple.postValue(Resource.Success(checkResponseBody(response.body()) as AddToCartModel))
            else
                addToCartMultiple.postValue(Resource.Error(response.message(), null))
        } catch (t: Throwable) {
            addToCartMultiple.postValue(Resource.Error(checkThrowable(t), null))
        }
    }


    fun getCart() = viewModelScope.launch {
        val jsonObject = JsonObject()
        jsonObject.addProperty("access_token", sessionManager.token)
        jsonObject.addProperty("lang_id", "1")
        jsonObject.addProperty("device_id", sessionManager.deviceId)
        jsonObject.addProperty("page_url", "CART")
        jsonObject.addProperty("os_type", "app")
        safeGetCartCall(jsonObject)
    }

    private suspend fun safeGetCartCall(jsonObject: JsonObject) {
        cart.postValue(Resource.Loading())
        try {
            val response = repository.getCart(jsonObject)
            if (response.isSuccessful)
                cart.postValue(Resource.Success(checkResponseBody(response.body()) as CartModel))
            else
                cart.postValue(Resource.Error(response.message(), null))
        } catch (t: Throwable) {
            cart.postValue(Resource.Error(checkThrowable(t), null))
        }
    }

    fun deleterCartProduct(cartId: String) = viewModelScope.launch {
        val jsonObject = JsonObject()
        jsonObject.addProperty("access_token", sessionManager.token)
        jsonObject.addProperty("cart_id", cartId)
        safeDeleterCartProductCall(jsonObject)
    }

    private suspend fun safeDeleterCartProductCall(jsonObject: JsonObject) {
        deleterCartProduct.postValue(Resource.Loading())
        try {
            val response = repository.deleterCartProduct(jsonObject)
            if (response.isSuccessful)
                deleterCartProduct.postValue(Resource.Success(checkResponseBody(response.body()) as AddToCartModel))
            else
                deleterCartProduct.postValue(Resource.Error(response.message(), null))
        } catch (t: Throwable) {
            deleterCartProduct.postValue(Resource.Error(checkThrowable(t), null))
        }
    }


    fun getPlatFormVouchers(available: Int) = viewModelScope.launch {
        val jsonObject = JsonObject()
        jsonObject.addProperty("access_token", sessionManager.token)
        jsonObject.addProperty("available", available)
        jsonObject.addProperty("lang_id", 1)
        safePlatFormVouchersCall(jsonObject)
    }

    private suspend fun safePlatFormVouchersCall(jsonObject: JsonObject) {
        platformVouchers.postValue(Resource.Loading())
        try {
            val response = repository.getPlatFormVouchers(jsonObject)
            if (response.isSuccessful)
                platformVouchers.postValue(Resource.Success(checkResponseBody(response.body()) as VoucherListModel))
            else
                platformVouchers.postValue(Resource.Error(response.message(), null))
        } catch (t: Throwable) {
            platformVouchers.postValue(Resource.Error(checkThrowable(t), null))
        }
    }

    fun getClaimedVouchers() = viewModelScope.launch {
        val jsonObject = JsonObject()
        jsonObject.addProperty("access_token", sessionManager.token)
        jsonObject.addProperty("lang_id", 1)
        safeGetClaimedVouchersCall(jsonObject)
    }

    private suspend fun safeGetClaimedVouchersCall(jsonObject: JsonObject) {
        claimedVouchers.postValue(Resource.Loading())
        try {
            val response = repository.getClaimedVouchers(jsonObject)
            if (response.isSuccessful)
                claimedVouchers.postValue(Resource.Success(checkResponseBody(response.body()) as VoucherListModel))
            else
                claimedVouchers.postValue(Resource.Error(response.message(), null))
        } catch (t: Throwable) {
            claimedVouchers.postValue(Resource.Error(checkThrowable(t), null))
        }
    }

    fun claimStoreVoucher(couponCode: String, sellerId: String) = viewModelScope.launch {
        val jsonObject = JsonObject()
        jsonObject.addProperty("access_token", sessionManager.token)
        jsonObject.addProperty("lang_id", 1)
        jsonObject.addProperty("seller_id", sellerId)
        jsonObject.addProperty("coupon_code", couponCode)
        safeClaimStoreVoucherCall(jsonObject)
    }

    private suspend fun safeClaimStoreVoucherCall(jsonObject: JsonObject) {
        claimStoreVoucher.postValue(Resource.Loading())
        try {
            val response = repository.claimStoreVoucher(jsonObject)
            if (response.isSuccessful)
                claimStoreVoucher.postValue(Resource.Success(checkResponseBody(response.body()) as SuccessModel))
            else
                claimStoreVoucher.postValue(Resource.Error(response.message(), null))
        } catch (t: Throwable) {
            claimStoreVoucher.postValue(Resource.Error(checkThrowable(t), null))
        }
    }

    fun applyPlatFormVouchers(couponCode: String, subTotal: String) = viewModelScope.launch {
        val jsonObject = JsonObject()
        jsonObject.addProperty("access_token", sessionManager.token)
        jsonObject.addProperty("lang_id", 1)
        jsonObject.addProperty("cart_subtotal", subTotal)
        jsonObject.addProperty("coupon_code", couponCode)
        jsonObject.addProperty("os_type", "app")
        safeApplyPlatFormVouchersCall(jsonObject)
    }

    private suspend fun safeApplyPlatFormVouchersCall(jsonObject: JsonObject) {
        applyPlatformVoucher.postValue(Resource.Loading())
        try {
            val response = repository.applyPlatFormVouchers(jsonObject)
            if (response.isSuccessful)
                applyPlatformVoucher.postValue(Resource.Success(checkResponseBody(response.body()) as CouponAppliedModel))
            else
                applyPlatformVoucher.postValue(Resource.Error(response.message(), null))
        } catch (t: Throwable) {
            applyPlatformVoucher.postValue(Resource.Error(checkThrowable(t), null))
        }
    }

    fun applySellerVouchers(sellerId: String, couponCode: String, sellerSubtotal: Double) =
        viewModelScope.launch {
            val jsonObject = JsonObject()
            jsonObject.addProperty("lang_id", 1)
            jsonObject.addProperty("access_token", sessionManager.token)
            jsonObject.addProperty("coupon_code", couponCode)
            jsonObject.addProperty("seller_id", sellerId)
            jsonObject.addProperty("seller_subtotal", sellerSubtotal)
            jsonObject.addProperty("os_type", "app")
            safeApplySellerVouchersCall(jsonObject)
        }

    private suspend fun safeApplySellerVouchersCall(jsonObject: JsonObject) {
        applySellerVoucher.postValue(Resource.Loading())
        try {
            val response = repository.applySellerVouchers(jsonObject)
            if (response.isSuccessful)
                applySellerVoucher.postValue(Resource.Success(checkResponseBody(response.body()) as CouponAppliedModel))
            else
                applySellerVoucher.postValue(Resource.Error(response.message(), null))
        } catch (t: Throwable) {
            applySellerVoucher.postValue(Resource.Error(checkThrowable(t), null))
        }
    }

    fun relatedSearchTerms(keyword: String) = viewModelScope.launch {
        val jsonObject = JsonObject()
        jsonObject.addProperty("keyword", keyword)
        jsonObject.addProperty("device_id", 1)
        jsonObject.addProperty("page_url", "products/us/img")
        jsonObject.addProperty("os_type", "app")
        safeRelatedSearchTermsCall(jsonObject)
    }

    private suspend fun safeRelatedSearchTermsCall(jsonObject: JsonObject) {
        relatedSearchTerms.postValue(Resource.Loading())
        try {
            val response = repository.relatedSearchTerms(jsonObject)
            if (response.isSuccessful)
                relatedSearchTerms.postValue(Resource.Success(checkResponseBody(response.body()) as RelatedSearchTermsModel))
            else
                relatedSearchTerms.postValue(Resource.Error(response.message(), null))
        } catch (t: Throwable) {
            relatedSearchTerms.postValue(Resource.Error(checkThrowable(t), null))
        }
    }

    fun getNotifications(pageNo: Int) = viewModelScope.launch {
        val jsonObject = JsonObject()
        jsonObject.addProperty("access_token", sessionManager.token)
        jsonObject.addProperty("limit", 20)
        jsonObject.addProperty("offset", pageNo)
        safeNotificationsCall(jsonObject)
    }

    private suspend fun safeNotificationsCall(jsonObject: JsonObject) {
        notifications.postValue(Resource.Loading())
        try {
            val response = repository.getNotifications(jsonObject)
            if (response.isSuccessful)
                notifications.postValue(Resource.Success(checkResponseBody(response.body()) as NotificationModel))
            else
                notifications.postValue(Resource.Error(response.message(), null))
        } catch (t: Throwable) {
            notifications.postValue(Resource.Error(checkThrowable(t), null))
        }
    }

    fun viewedNotification(notificationId: Int) = viewModelScope.launch {
        val jsonObject = JsonObject()
        jsonObject.addProperty("access_token", sessionManager.token)
        jsonObject.addProperty("notification_id", notificationId)
        safeNotificationViewedCall(jsonObject)
    }

    private suspend fun safeNotificationViewedCall(jsonObject: JsonObject) {
        notificationViewed.postValue(Resource.Loading())
        try {
            val response = repository.notificationViewed(jsonObject)
            if (response.isSuccessful)
                notificationViewed.postValue(Resource.Success(checkResponseBody(response.body()) as NotificationViewedModel))
            else
                notificationViewed.postValue(Resource.Error(response.message(), null))
        } catch (t: Throwable) {
            notificationViewed.postValue(Resource.Error(checkThrowable(t), null))
        }
    }


    fun checkAvailability(productId: String, pinCode: String) = viewModelScope.launch {
        val jsonObject = JsonObject()
        jsonObject.addProperty("access_token", sessionManager.token)
        jsonObject.addProperty("product_id", productId)
        jsonObject.addProperty("postcode", pinCode)
        safeCheckAvailabilityCall(jsonObject)
    }

    private suspend fun safeCheckAvailabilityCall(jsonObject: JsonObject) {
        checkAvailability.postValue(Resource.Loading())
        try {
            val response = repository.checkAvailability(jsonObject)
            if (response.isSuccessful)
                checkAvailability.postValue(Resource.Success(checkResponseBody(response.body()) as SuccessModel))
            else
                checkAvailability.postValue(Resource.Error(response.message(), null))
        } catch (t: Throwable) {
            checkAvailability.postValue(Resource.Error(checkThrowable(t), null))
        }
    }

    fun updateProfileCustomer(requestBody: MutableMap<String, RequestBody?>) =
        viewModelScope.launch {
            safeUpdateProfileCustomerCall(requestBody)
        }

    private suspend fun safeUpdateProfileCustomerCall(requestBody: MutableMap<String, RequestBody?>) {
        updateProfileCustomer.postValue(Resource.Loading())
        try {
            val response = repository.updateProfileCustomer(requestBody)
            if (response.isSuccessful)
                updateProfileCustomer.postValue(Resource.Success(checkResponseBody(response.body()) as SuccessModel))
            else
                updateProfileCustomer.postValue(Resource.Error(response.message(), null))
        } catch (t: Throwable) {
            updateProfileCustomer.postValue(Resource.Error(checkThrowable(t), null))
        }
    }

    fun submitReview(requestBody: MutableMap<String, RequestBody?>) =
        viewModelScope.launch {
            safeSubmitReviewCall(requestBody)
        }

    private suspend fun safeSubmitReviewCall(requestBody: MutableMap<String, RequestBody?>) {
        submitReview.postValue(Resource.Loading())
        try {
            val response = repository.submitReview(requestBody)
            if (response.isSuccessful)
                submitReview.postValue(Resource.Success(checkResponseBody(response.body()) as SuccessModel))
            else
                submitReview.postValue(Resource.Error(response.message(), null))
        } catch (t: Throwable) {
            submitReview.postValue(Resource.Error(checkThrowable(t), null))
        }
    }

    fun getReviews(productId: String, limit: Int, rating: String) =
        viewModelScope.launch {
            val jsonObject = JsonObject()
            jsonObject.addProperty("access_token", sessionManager.token)
            jsonObject.addProperty("product_id", productId)
            jsonObject.addProperty("limit", 20)
            jsonObject.addProperty("offset", 0)
            jsonObject.addProperty("rating", rating)
            jsonObject.addProperty("media_filter", "")
            jsonObject.addProperty("os_type", "app")
            safeGetReviewsCall(jsonObject)
        }

    private suspend fun safeGetReviewsCall(jsonObject: JsonObject) {
        ratingReview.postValue(Resource.Loading())
        try {
            val response = repository.getReviews(jsonObject)
            if (response.isSuccessful)
                ratingReview.postValue(Resource.Success(checkResponseBody(response.body()) as RatingReviewsModel))
            else
                ratingReview.postValue(Resource.Error(response.message(), null))
        } catch (t: Throwable) {
            ratingReview.postValue(Resource.Error(checkThrowable(t), null))
        }
    }

    fun createSupportTicket(requestBody: MutableMap<String, RequestBody?>) = viewModelScope.launch {
        safeCreateSupportTicketCall(requestBody)
    }

    private suspend fun safeCreateSupportTicketCall(requestBody: MutableMap<String, RequestBody?>) {
        createSupportTicket.postValue(Resource.Loading())
        try {
            val response = repository.createSupportTicket(requestBody)
            if (response.isSuccessful)
                createSupportTicket.postValue(Resource.Success(checkResponseBody(response.body()) as SuccessModel))
            else
                createSupportTicket.postValue(Resource.Error(response.message(), null))
        } catch (t: Throwable) {
            createSupportTicket.postValue(Resource.Error(checkThrowable(t), null))
        }
    }

    fun getSupportTickets(pageNo: Int) = viewModelScope.launch {
        val jsonObject = JsonObject()
        jsonObject.addProperty("access_token", sessionManager.token)
        jsonObject.addProperty("limit", 50)
        jsonObject.addProperty("offset", pageNo)
        safeGetSupportTicketsCall(jsonObject)
    }

    private suspend fun safeGetSupportTicketsCall(jsonObject: JsonObject) {
        supportTickets.postValue(Resource.Loading())
        try {
            val response = repository.getSupportTickets(jsonObject)
            if (response.isSuccessful)
                supportTickets.postValue(Resource.Success(checkResponseBody(response.body()) as SupportTicketsModel))
            else
                supportTickets.postValue(Resource.Error(response.message(), null))
        } catch (t: Throwable) {
            supportTickets.postValue(Resource.Error(checkThrowable(t), null))
        }
    }

    fun updateProfileBusiness(requestBody: MutableMap<String, RequestBody?>) =
        viewModelScope.launch {
            safeUpdateProfileBusinessCall(requestBody)
        }

    private suspend fun safeUpdateProfileBusinessCall(requestBody: MutableMap<String, RequestBody?>) {
        updateProfileBusiness.postValue(Resource.Loading())
        try {
            val response = repository.updateProfileBusiness(requestBody)
            if (response.isSuccessful)
                updateProfileBusiness.postValue(Resource.Success(checkResponseBody(response.body()) as SuccessModel))
            else
                updateProfileBusiness.postValue(Resource.Error(response.message(), null))
        } catch (t: Throwable) {
            updateProfileBusiness.postValue(Resource.Error(checkThrowable(t), null))
        }
    }

    fun updateCartQty(cartId: String, qty: Int) = viewModelScope.launch {
        val jsonObject = JsonObject()
        jsonObject.addProperty("access_token", sessionManager.token)
        jsonObject.addProperty("cart_id", cartId)
        jsonObject.addProperty("quantity", qty)
        jsonObject.addProperty("cart_type", "app")
        safeUpdateCartQtyCall(jsonObject)
    }

    private suspend fun safeUpdateCartQtyCall(jsonObject: JsonObject) {
        updateCartCount.postValue(Resource.Loading())
        try {
            val response = repository.updateCartQty(jsonObject)
            if (response.isSuccessful)
                updateCartCount.postValue(Resource.Success(checkResponseBody(response.body()) as SuccessModel))
            else
                updateCartCount.postValue(Resource.Error(response.message(), null))
        } catch (t: Throwable) {
            updateCartCount.postValue(Resource.Error(checkThrowable(t), null))
        }
    }


    fun getStoreDetails(sellerId: String, categoryId: String = "") = viewModelScope.launch {
        val jsonObject = JsonObject()
        jsonObject.addProperty("seller_id", sellerId)
        jsonObject.addProperty("category_id", categoryId)
        jsonObject.addProperty("limit", 150)
        jsonObject.addProperty("offset", 0)
        jsonObject.addProperty("frozen", "")
        jsonObject.addProperty("wholesale", "")
        jsonObject.addProperty("chilled", "")
        jsonObject.addProperty("low_to_high", "")
        jsonObject.addProperty("high_to_low", "")
        jsonObject.addProperty("latest", "")
        jsonObject.addProperty("popular", "")
        jsonObject.addProperty("lang_id", "1")
        jsonObject.addProperty("access_token", sessionManager.token)
        jsonObject.addProperty("device_id", sessionManager.deviceId)
        jsonObject.addProperty("page_url", "http://shopproducts/us/img")
        jsonObject.addProperty("os_type", "app")
        safeGetStoreDetailsCall(jsonObject)
    }

    private suspend fun safeGetStoreDetailsCall(jsonObject: JsonObject) {
        storeDetails.postValue(Resource.Loading())
        try {
            val response = repository.getStoreDetails(jsonObject)
            if (response.isSuccessful)
                storeDetails.postValue(Resource.Success(checkResponseBody(response.body()) as StoreDetailsModel))
            else
                storeDetails.postValue(Resource.Error(response.message(), null))
        } catch (t: Throwable) {
            storeDetails.postValue(Resource.Error(checkThrowable(t), null))
        }
    }

    fun getStoreProducts(sellerId: Int, search: String, categoryId: String = "") =
        viewModelScope.launch {
            val jsonObject = JsonObject()
            jsonObject.addProperty("seller_id", sellerId)
            jsonObject.addProperty("category_id", categoryId)
            jsonObject.addProperty("limit", 150)
            jsonObject.addProperty("offset", 0)
            jsonObject.addProperty("frozen", "")
            jsonObject.addProperty("wholesale", "")
            jsonObject.addProperty("chilled", "")
            jsonObject.addProperty("low_to_high", "")
            jsonObject.addProperty("high_to_low", "")
            jsonObject.addProperty("latest", "")
            jsonObject.addProperty("popular", "")
            jsonObject.addProperty("lang_id", "1")
            jsonObject.addProperty("keyword", search)
            jsonObject.addProperty("access_token", sessionManager.token)
            jsonObject.addProperty("device_id", sessionManager.deviceId)
            jsonObject.addProperty("page_url", "http://shopproducts/us/img")
            jsonObject.addProperty("os_type", "app")
            safeGetStoreProductsCall(jsonObject)
        }

    private suspend fun safeGetStoreProductsCall(jsonObject: JsonObject) {
        storeProducts.postValue(Resource.Loading())
        try {
            val response = repository.getStoreProducts(jsonObject)
            if (response.isSuccessful)
                storeProducts.postValue(Resource.Success(checkResponseBody(response.body()) as StoreProductsModel))
            else
                storeProducts.postValue(Resource.Error(response.message(), null))
        } catch (t: Throwable) {
            storeDetails.postValue(Resource.Error(checkThrowable(t), null))
        }
    }

    fun getStoreReviews(sellerId: Int, rating: String, pageNo: Int, media: String="") = viewModelScope.launch {
        val jsonObject = JsonObject()
        jsonObject.addProperty("access_token", sessionManager.token)
        jsonObject.addProperty("seller_id", sellerId)
        jsonObject.addProperty("rating", rating)
        jsonObject.addProperty("media", media)
        jsonObject.addProperty("limit", 50)
        jsonObject.addProperty("offset", pageNo)
        jsonObject.addProperty("lang_id", "")
        jsonObject.addProperty("os_type", "app")
        safeGetStoreReviewsCall(jsonObject)
    }

    private suspend fun safeGetStoreReviewsCall(jsonObject: JsonObject) {
        storeReview.postValue(Resource.Loading())
        try {
            val response = repository.getStoreReviews(jsonObject)
            if (response.isSuccessful)
                storeReview.postValue(Resource.Success(checkResponseBody(response.body()) as StoreReviewsModel))
            else
                storeReview.postValue(Resource.Error(response.message(), null))
        } catch (t: Throwable) {
            storeReview.postValue(Resource.Error(checkThrowable(t), null))
        }
    }

    fun getMyOrders(orderStatus: String, pageNo: Int, search: String = "") = viewModelScope.launch {
        val jsonObject = JsonObject()
        jsonObject.addProperty("access_token", sessionManager.token)
        jsonObject.addProperty("lang_id", 1)
        jsonObject.addProperty("order_status", orderStatus)

        jsonObject.addProperty("payment_status", "")
        /*if (orderStatus == "to_pay") {
            jsonObject.addProperty("payment_status", "pending")
        } else {
        }*/

        jsonObject.addProperty("order_time", "")
        jsonObject.addProperty("limit", 15)
        jsonObject.addProperty("offset", pageNo)
        jsonObject.addProperty("search", search)
        jsonObject.addProperty("os_type", "app")
        safeGetMyOrdersCall(jsonObject)
    }

    private suspend fun safeGetMyOrdersCall(jsonObject: JsonObject) {
        myOrders.postValue(Resource.Loading())
        try {
            val response = repository.myOrders(jsonObject)
            if (response.isSuccessful)
                myOrders.postValue(Resource.Success(checkResponseBody(response.body()) as MyOrdersModel))
            else
                myOrders.postValue(Resource.Error(response.message(), null))
        } catch (t: Throwable) {
            myOrders.postValue(Resource.Error(checkThrowable(t), null))
        }
    }

    fun getBrands(name: String) = viewModelScope.launch {
        val jsonObject = JsonObject()
        jsonObject.addProperty("sort_by_name", if (name != "1" || name.isEmpty()) "1" else "")
        jsonObject.addProperty("filter_by_name", name)
        jsonObject.addProperty("limit", 150)
        safeGetBrandsCall(jsonObject)
    }

    private suspend fun safeGetBrandsCall(jsonObject: JsonObject) {
        brands.postValue(Resource.Loading())
        try {
            val response = repository.getBrands(jsonObject)
            if (response.isSuccessful)
                brands.postValue(Resource.Success(checkResponseBody(response.body()) as BrandsModel))
            else
                brands.postValue(Resource.Error(response.message(), null))
        } catch (t: Throwable) {
            brands.postValue(Resource.Error(checkThrowable(t), null))
        }
    }

    fun getPageData(name: Int) = viewModelScope.launch {
        val jsonObject = JsonObject()
        jsonObject.addProperty("page_id", name)
        jsonObject.addProperty("identifier", "")
        safeGetPageDataCall(jsonObject)
    }

    private suspend fun safeGetPageDataCall(jsonObject: JsonObject) {
        pageData.postValue(Resource.Loading())
        try {
            val response = repository.getPageData(jsonObject)
            if (response.isSuccessful)
                pageData.postValue(Resource.Success(checkResponseBody(response.body()) as PageModel))
            else
                pageData.postValue(Resource.Error(response.message(), null))
        } catch (t: Throwable) {
            pageData.postValue(Resource.Error(checkThrowable(t), null))
        }
    }

    fun getBlogs(catId: Int, pageNo: Int) = viewModelScope.launch {
        val jsonObject = JsonObject()
        jsonObject.addProperty("category_id", catId)
        jsonObject.addProperty("limit", "20")
        jsonObject.addProperty("offset", pageNo)
        safeGetBlogsCall(jsonObject)
    }

    private suspend fun safeGetBlogsCall(jsonObject: JsonObject) {
        blogsList.postValue(Resource.Loading())
        try {
            val response = repository.getBlogsList(jsonObject)
            if (response.isSuccessful)
                blogsList.postValue(Resource.Success(checkResponseBody(response.body()) as BlogsModel))
            else
                blogsList.postValue(Resource.Error(response.message(), null))
        } catch (t: Throwable) {
            blogsList.postValue(Resource.Error(checkThrowable(t), null))
        }
    }

    fun getBlogsDetails(id: Int) = viewModelScope.launch {
        val jsonObject = JsonObject()
        jsonObject.addProperty("blog_id", id)
        safeGetBlogsDetailsCall(jsonObject)
    }

    private suspend fun safeGetBlogsDetailsCall(jsonObject: JsonObject) {
        blogsDetails.postValue(Resource.Loading())
        try {
            val response = repository.getBlogDetails(jsonObject)
            if (response.isSuccessful)
                blogsDetails.postValue(Resource.Success(checkResponseBody(response.body()) as BlogDetailsModel))
            else
                blogsDetails.postValue(Resource.Error(response.message(), null))
        } catch (t: Throwable) {
            blogsDetails.postValue(Resource.Error(checkThrowable(t), null))
        }
    }


    fun followShop(id: String) = viewModelScope.launch {
        val jsonObject = JsonObject()
        jsonObject.addProperty("access_token", sessionManager.token)
        jsonObject.addProperty("seller_id", id)
        safeFollowShopCall(jsonObject)
    }

    private suspend fun safeFollowShopCall(jsonObject: JsonObject) {
        followShop.postValue(Resource.Loading())
        try {
            val response = repository.followShop(jsonObject)
            if (response.isSuccessful)
                followShop.postValue(Resource.Success(checkResponseBody(response.body()) as SuccessModel))
            else
                followShop.postValue(Resource.Error(response.message(), null))
        } catch (t: Throwable) {
            followShop.postValue(Resource.Error(checkThrowable(t), null))
        }
    }

    fun unFollowShop(id: String) = viewModelScope.launch {
        val jsonObject = JsonObject()
        jsonObject.addProperty("access_token", sessionManager.token)
        jsonObject.addProperty("seller_id", id)
        safeUnFollowShopCall(jsonObject)
    }

    private suspend fun safeUnFollowShopCall(jsonObject: JsonObject) {
        unFollowShop.postValue(Resource.Loading())
        try {
            val response = repository.unFollowShop(jsonObject)
            if (response.isSuccessful)
                unFollowShop.postValue(Resource.Success(checkResponseBody(response.body()) as SuccessModel))
            else
                unFollowShop.postValue(Resource.Error(response.message(), null))
        } catch (t: Throwable) {
            unFollowShop.postValue(Resource.Error(checkThrowable(t), null))
        }
    }

    fun followedShops() = viewModelScope.launch {
        val jsonObject = JsonObject()
        jsonObject.addProperty("access_token", sessionManager.token)
        jsonObject.addProperty("lang_id", 1)
        safeFollowedShopsCall(jsonObject)
    }

    private suspend fun safeFollowedShopsCall(jsonObject: JsonObject) {
        followedShops.postValue(Resource.Loading())
        try {
            val response = repository.followedShops(jsonObject)
            if (response.isSuccessful)
                followedShops.postValue(Resource.Success(checkResponseBody(response.body()) as StoreWishlistModel))
            else
                followedShops.postValue(Resource.Error(response.message(), null))
        } catch (t: Throwable) {
            followedShops.postValue(Resource.Error(checkThrowable(t), null))
        }
    }


    fun getWallet() = viewModelScope.launch {
        val jsonObject = JsonObject()
        jsonObject.addProperty("access_token", sessionManager.token)
        safeGetWalletCall(jsonObject)
    }

    private suspend fun safeGetWalletCall(jsonObject: JsonObject) {
        wallet.postValue(Resource.Loading())
        try {
            val response = repository.getWallet(jsonObject)
            if (response.isSuccessful)
                wallet.postValue(Resource.Success(checkResponseBody(response.body()) as WalletModel))
            else
                wallet.postValue(Resource.Error(response.message(), null))
        } catch (t: Throwable) {
            wallet.postValue(Resource.Error(checkThrowable(t), null))
        }
    }

    fun getChatList() = viewModelScope.launch {
        val jsonObject = JsonObject()
        jsonObject.addProperty("access_token", sessionManager.token)
        safeGetChatListCall(jsonObject)
    }

    private suspend fun safeGetChatListCall(jsonObject: JsonObject) {
        chatList.postValue(Resource.Loading())
        try {
            val response = repository.getChatList(jsonObject)
            if (response.isSuccessful)
                chatList.postValue(Resource.Success(checkResponseBody(response.body()) as ChatListModel))
            else
                chatList.postValue(Resource.Error(response.message(), null))
        } catch (t: Throwable) {
            chatList.postValue(Resource.Error(checkThrowable(t), null))
        }
    }

    fun getChatMessage(sellerId: String, chatId: String) = viewModelScope.launch {
        val jsonObject = JsonObject()
        jsonObject.addProperty("access_token", sessionManager.token)
        jsonObject.addProperty("chat_id", chatId)
        jsonObject.addProperty("seller_id", sellerId)
        safeGetChatMessageCall(jsonObject)
    }

    private suspend fun safeGetChatMessageCall(jsonObject: JsonObject) {
        chatMessages.postValue(Resource.Loading())
        try {
            val response = repository.getChatMessage(jsonObject)
            if (response.isSuccessful)
                chatMessages.postValue(Resource.Success(checkResponseBody(response.body()) as ChatMessageModel))
            else
                chatMessages.postValue(Resource.Error(response.message(), null))
        } catch (t: Throwable) {
            chatMessages.postValue(Resource.Error(checkThrowable(t), null))
        }
    }

    fun getSupportMessages(supportId: Int) = viewModelScope.launch {
        val jsonObject = JsonObject()
        jsonObject.addProperty("access_token", sessionManager.token)
        jsonObject.addProperty("support_id", supportId)
        safeGetSupportMessagesCall(jsonObject)
    }

    private suspend fun safeGetSupportMessagesCall(jsonObject: JsonObject) {
        supportChatMessages.postValue(Resource.Loading())
        try {
            val response = repository.getSupportMessage(jsonObject)
            if (response.isSuccessful)
                supportChatMessages.postValue(Resource.Success(checkResponseBody(response.body()) as SupportMessagesModel))
            else
                supportChatMessages.postValue(Resource.Error(response.message(), null))
        } catch (t: Throwable) {
            supportChatMessages.postValue(Resource.Error(checkThrowable(t), null))
        }
    }

    fun sendSupportMessage(requestBody: MutableMap<String, RequestBody?>) {
        viewModelScope.launch {
            safeSendSupportMessageCall(requestBody)
        }
    }

    private suspend fun safeSendSupportMessageCall(requestBody: MutableMap<String, RequestBody?>) {
        sendSupportMessage.postValue(Resource.Loading())
        try {
            val response = repository.sendSupportMessage(requestBody)
            if (response.isSuccessful)
                sendSupportMessage.postValue(Resource.Success(checkResponseBody(response.body()) as SuccessModel))
            else
                sendSupportMessage.postValue(Resource.Error(response.message(), null))
        } catch (t: Throwable) {
            sendSupportMessage.postValue(Resource.Error(checkThrowable(t), null))
        }
    }

    fun selectCart(cartId: String, status: Int) = viewModelScope.launch {
        val jsonObject = JsonObject()
        jsonObject.addProperty("access_token", sessionManager.token)
        jsonObject.addProperty("cart_id", cartId)
        jsonObject.addProperty("select_status", status)
        safeSelectCartCall(jsonObject)
    }

    private suspend fun safeSelectCartCall(jsonObject: JsonObject) {
        selectCart.postValue(Resource.Loading())
        try {
            val response = repository.selectCart(jsonObject)
            if (response.isSuccessful)
                selectCart.postValue(Resource.Success(checkResponseBody(response.body()) as SuccessModel))
            else
                selectCart.postValue(Resource.Error(response.message(), null))
        } catch (t: Throwable) {
            selectCart.postValue(Resource.Error(checkThrowable(t), null))
        }
    }

    fun sendMessage(requestBody: MutableMap<String, RequestBody?>) {
        viewModelScope.launch {
            safeSendMessageCall(requestBody)
        }
    }

    private suspend fun safeSendMessageCall(requestBody: MutableMap<String, RequestBody?>) {
        sendMessage.postValue(Resource.Loading())
        try {
            val response = repository.sendMessage(requestBody)
            if (response.isSuccessful)
                sendMessage.postValue(Resource.Success(checkResponseBody(response.body()) as MessageSentModel))
            else
                sendMessage.postValue(Resource.Error(response.message(), null))
        } catch (t: Throwable) {
            sendMessage.postValue(Resource.Error(checkThrowable(t), null))
        }
    }

    fun getOrderHistory(sale_id: String) = viewModelScope.launch {
        val jsonObject = JsonObject()
        jsonObject.addProperty("access_token", sessionManager.token)
        jsonObject.addProperty("sale_id", sale_id)
        jsonObject.addProperty("lang_id", 1)
        safeGetOrderHistoryCall(jsonObject)
    }

    private suspend fun safeGetOrderHistoryCall(jsonObject: JsonObject) {
        orderHistory.postValue(Resource.Loading())
        try {
            val response = repository.getOrderHistory(jsonObject)
            if (response.isSuccessful)
                orderHistory.postValue(Resource.Success(checkResponseBody(response.body()) as OrderHistoryModel))
            else
                orderHistory.postValue(Resource.Error(response.message(), null))
        } catch (t: Throwable) {
            orderHistory.postValue(Resource.Error(checkThrowable(t), null))
        }
    }

    fun placeOrder(jsonObject: JsonObject) = viewModelScope.launch {
        safePlaceOrderCall(jsonObject)
    }

    private suspend fun safePlaceOrderCall(jsonObject: JsonObject) {
        placeOrder.postValue(Resource.Loading())
        try {
            val response = repository.placeOrder(jsonObject)
            if (response.isSuccessful)
                placeOrder.postValue(Resource.Success(checkResponseBody(response.body()) as PlaceOrderModel))
            else
                placeOrder.postValue(Resource.Error(response.message(), null))
        } catch (t: Throwable) {
            placeOrder.postValue(Resource.Error(checkThrowable(t), null))
        }
    }


    fun uploadOrderReceipt(requestBody: MutableMap<String, RequestBody?>) =
        viewModelScope.launch {
            safeUploadOrderReceiptCall(requestBody)
        }

    private suspend fun safeUploadOrderReceiptCall(requestBody: MutableMap<String, RequestBody?>) {
        uploadOrderReceipt.postValue(Resource.Loading())
        try {
            val response = repository.uploadOrderReceipt(requestBody)
            if (response.isSuccessful)
                uploadOrderReceipt.postValue(Resource.Success(checkResponseBody(response.body()) as SuccessModel))
            else
                uploadOrderReceipt.postValue(Resource.Error(response.message(), null))
        } catch (t: Throwable) {
            uploadOrderReceipt.postValue(Resource.Error(checkThrowable(t), null))
        }
    }

    fun paymentFaq() =
        viewModelScope.launch {
            safePaymentFaqCall()
        }

    private suspend fun safePaymentFaqCall() {
        paymentFaq.postValue(Resource.Loading())
        try {
            val response = repository.paymentFaq()
            if (response.isSuccessful)
                paymentFaq.postValue(Resource.Success(checkResponseBody(response.body()) as PaymentFAQModel))
            else
                paymentFaq.postValue(Resource.Error(response.message(), null))
        } catch (t: Throwable) {
            paymentFaq.postValue(Resource.Error(checkThrowable(t), null))
        }
    }

    fun getCountryCode(name: String) =
        viewModelScope.launch {
            safeGetCountryCodeCall(name)
        }

    private suspend fun safeGetCountryCodeCall(name: String) {
        countryCode.postValue(Resource.Loading())
        try {
            val response = repository.getCountryCode(name)
            if (response.isSuccessful)
                countryCode.postValue(Resource.Success(checkResponseBody(response.body()) as CountryCodeModel))
            else
                countryCode.postValue(Resource.Error(response.message(), null))
        } catch (t: Throwable) {
            countryCode.postValue(Resource.Error(checkThrowable(t), null))
        }
    }


    fun getStateCode(name: String, countryId: String) =
        viewModelScope.launch {
            safeGetStateCodeCall(name, countryId)
        }

    private suspend fun safeGetStateCodeCall(name: String, countryId: String) {
        stateCode.postValue(Resource.Loading())
        try {
            val response = repository.getStateCode(name, countryId)
            if (response.isSuccessful)
                stateCode.postValue(Resource.Success(checkResponseBody(response.body()) as StateCodeModel))
            else
                stateCode.postValue(Resource.Error(response.message(), null))
        } catch (t: Throwable) {
            stateCode.postValue(Resource.Error(checkThrowable(t), null))
        }
    }

    fun getCityCode(name: String, stateId: String) =
        viewModelScope.launch {
            safeCityCodeCall(name, stateId)
        }

    private suspend fun safeCityCodeCall(name: String, stateId: String) {
        cityCode.postValue(Resource.Loading())
        try {
            val response = repository.getCityCode(name, stateId)
            if (response.isSuccessful)
                cityCode.postValue(Resource.Success(checkResponseBody(response.body()) as CityCodeModel))
            else
                cityCode.postValue(Resource.Error(response.message(), null))
        } catch (t: Throwable) {
            cityCode.postValue(Resource.Error(checkThrowable(t), null))
        }
    }

    fun getTermsConditions() = viewModelScope.launch {
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

    fun getOrderTracking(saleId: String) = viewModelScope.launch {
        val jsonObject = JsonObject()
        jsonObject.addProperty("access_token", sessionManager.token)
        jsonObject.addProperty("sale_id", saleId)
        jsonObject.addProperty("lang_id", 1)
        safeGetOrderHistoryCall(jsonObject)
        safeGetOrderTrackingCall(jsonObject)
    }

    private suspend fun safeGetOrderTrackingCall(jsonObject: JsonObject) {
        orderTracking.postValue(Resource.Loading())
        try {
            val response = repository.orderTracking(jsonObject)
            if (response.isSuccessful)
                orderTracking.postValue(Resource.Success(checkResponseBody(response.body()) as OrderTrackingModel))
            else
                orderTracking.postValue(Resource.Error(response.message(), null))
        } catch (t: Throwable) {
            orderTracking.postValue(Resource.Error(checkThrowable(t), null))
        }
    }

    fun applyWallet(points: String) = viewModelScope.launch {
        val jsonObject = JsonObject()
        jsonObject.addProperty("access_token", sessionManager.token)
        jsonObject.addProperty("points", points)
        safeApplyWalletCall(jsonObject)
    }

    private suspend fun safeApplyWalletCall(jsonObject: JsonObject) {
        applyWallet.postValue(Resource.Loading())
        try {
            val response = repository.applyWallet(jsonObject)
            if (response.isSuccessful)
                applyWallet.postValue(Resource.Success(checkResponseBody(response.body()) as ApplyWalletModel))
            else
                applyWallet.postValue(Resource.Error(response.message(), null))
        } catch (t: Throwable) {
            applyWallet.postValue(Resource.Error(checkThrowable(t), null))
        }
    }

    fun removeWallet() = viewModelScope.launch {
        val jsonObject = JsonObject()
        jsonObject.addProperty("access_token", sessionManager.token)
        safeRemoveWalletCall(jsonObject)
    }

    private suspend fun safeRemoveWalletCall(jsonObject: JsonObject) {
        removeWallet.postValue(Resource.Loading())
        try {
            val response = repository.removeWallet(jsonObject)
            if (response.isSuccessful)
                removeWallet.postValue(Resource.Success(checkResponseBody(response.body()) as SuccessModel))
            else
                removeWallet.postValue(Resource.Error(response.message(), null))
        } catch (t: Throwable) {
            removeWallet.postValue(Resource.Error(checkThrowable(t), null))
        }
    }

    fun updateShipping(sellerId: Int, shippingOption: Int) = viewModelScope.launch {
        val jsonObject = JsonObject()
        jsonObject.addProperty("access_token", sessionManager.token)
        jsonObject.addProperty("seller_id", sellerId)
        jsonObject.addProperty("shipping_option", shippingOption)
        safeUpdateShippingCall(jsonObject)
    }

    private suspend fun safeUpdateShippingCall(jsonObject: JsonObject) {
        updateshipingOption.postValue(Resource.Loading())
        try {
            val response = repository.updateShipping(jsonObject)
            if (response.isSuccessful)
                updateshipingOption.postValue(Resource.Success(checkResponseBody(response.body()) as SuccessModel))
            else
                updateshipingOption.postValue(Resource.Error(response.message(), null))
        } catch (t: Throwable) {
            updateshipingOption.postValue(Resource.Error(checkThrowable(t), null))
        }
    }

    fun bannerActivity(sellerId: Int, bannerType: String) = viewModelScope.launch {
        val jsonObject = JsonObject()
        jsonObject.addProperty("access_token", sessionManager.token)
        if (bannerType == "seller") {
            jsonObject.addProperty("seller_id", sellerId)
        }
        jsonObject.addProperty("banner_type", bannerType)
        safeSellerBannerActivityCall(jsonObject)
    }

    private suspend fun safeSellerBannerActivityCall(jsonObject: JsonObject) {
        bannerActivity.postValue(Resource.Loading())
        try {
            val response = repository.sellerBannerActivity(jsonObject)
            if (response.isSuccessful)
                bannerActivity.postValue(Resource.Success(checkResponseBody(response.body()) as SuccessModel))
            else
                bannerActivity.postValue(Resource.Error(response.message(), null))
        } catch (t: Throwable) {
            bannerActivity.postValue(Resource.Error(checkThrowable(t), null))
        }
    }

    fun logout() = viewModelScope.launch {
        val jsonObject = JsonObject()
        jsonObject.addProperty("access_token", sessionManager.token)
        safeLogoutCall(jsonObject)
    }

    private suspend fun safeLogoutCall(jsonObject: JsonObject) {
        logOut.postValue(Resource.Loading())
        try {
            val response = repository.logout(jsonObject)
            if (response.isSuccessful)
                logOut.postValue(Resource.Success(checkResponseBody(response.body()) as SuccessModel))
            else
                logOut.postValue(Resource.Error(response.message(), null))
        } catch (t: Throwable) {
            logOut.postValue(Resource.Error(checkThrowable(t), null))
        }
    }
}
