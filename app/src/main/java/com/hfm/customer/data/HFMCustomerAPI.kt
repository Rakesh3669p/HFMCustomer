package com.hfm.customer.data

import com.google.gson.JsonObject
import com.hfm.customer.BuildConfig
import com.hfm.customer.commonModel.CityListModel
import com.hfm.customer.commonModel.CountryListModel
import com.hfm.customer.commonModel.HomeMainCategoriesModel
import com.hfm.customer.commonModel.StateListModel
import com.hfm.customer.commonModel.SuccessModel
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
import com.hfm.customer.ui.loginSignUp.login.model.LoginModel
import com.hfm.customer.ui.loginSignUp.register.model.BusinessCategoryModel
import org.json.JSONObject
import retrofit2.Response
import retrofit2.http.*

interface HFMCustomerAPI {

    companion object {
        const val BASE_URL = BuildConfig.BASE_URL
    }


    @POST("customer/login")
    suspend fun login(
        @Body jsonObject: JsonObject
    ): Response<LoginModel>

    @POST("customer/register")
    suspend fun registerUser(
        @Body jsonObject: JsonObject
    ): Response<SuccessModel>

    @POST("customer/register/send-email/otp")
    suspend fun registerSendOTP(
        @Body jsonObject: JsonObject
    ): Response<SuccessModel>

    @POST("customer/register/verify-email/otp")
    suspend fun registerVerifyOTP(
        @Body jsonObject: JsonObject
    ): Response<SuccessModel>


    @POST("customer/product-detail")
    suspend fun getProductDetails(
        @Body jsonObject: JsonObject
    ): Response<ProductDetailsModel>

    @POST("customer/country")
    suspend fun getCountriesList(): Response<CountryListModel>

    @POST("customer/state")
    suspend fun getStatesList(
        @Body jsonObject: JsonObject
    ): Response<StateListModel>

    @POST("customer/city")
    suspend fun getCitiesList(
        @Body jsonObject: JsonObject
    ): Response<CityListModel>


    @POST("customer/home/main-banner-section")
    suspend fun getCategories(
        @Body jsonObject: JsonObject
    ): Response<HomeMainCategoriesModel>

    @POST("customer/home/shock-sale-products")
    suspend fun getFlashSaleProducts(
        @Body jsonObject: JsonObject,
        @Query("page") page: String
    ): Response<FlashSaleModel>

    @POST("customer/home/main-banner-section")
    suspend fun getMainBanner(
        @Body jsonObject: JsonObject
    ): Response<HomeMainCategoriesModel>

    @POST("customer/home/middle-banner-section")
    suspend fun getMiddleBanner(
        @Body jsonObject: JsonObject
    ): Response<HomeMiddleBanner>

    @POST("customer/home/bottom-banner-section")
    suspend fun getBottomBanner(
        @Body jsonObject: JsonObject
    ): Response<HomeBottomBannerModel>

    @POST("customer/home/category-flashsale")
    suspend fun getHomeFlashSaleCategory(
        @Body jsonObject: JsonObject
    ): Response<HomeFlashSaleCategory>


    @POST("customer/home/wholesale-products")
    suspend fun getWholeSaleProducts(
        @Body jsonObject: JsonObject,
        @Query("page") page: String
    ): Response<WholeSaleModel>


    @POST("customer/home/trending-now")
    suspend fun getTrendingNow(
        @Body jsonObject: JsonObject
    ): Response<TrendingNowModel>

    @POST("customer/product-featured")
    suspend fun getFeatureProducts(
        @Body jsonObject: JsonObject
    ): Response<FeatureProductsModel>

    @POST("customer/brand")
    suspend fun getHomeBrands(
        @Body jsonObject: JsonObject
    ): Response<HomeBrandsModel>

    @POST("customer/product-list-filter")
    suspend fun getProductList(
        @Body jsonObject: JsonObject
    ): Response<ProductListModel>

    @POST("customer/seller-coupon-list")
    suspend fun getSellerVouchers(
        @Body jsonObject: JsonObject
    ): Response<SellerVoucherModel>

    @POST("customer/profile")
    suspend fun getProfile(
        @Body jsonObject: JsonObject
    ): Response<ProfileModel>

    @POST("customer/order/request-bulkorder")
    suspend fun sendBulkOrderRequest(
        @Body jsonObject: JsonObject
    ): Response<BulkOrderRequestModel>

    @POST("customer/order/bulkorder-details")
    suspend fun getBulkOrders(
        @Body jsonObject: JsonObject
    ): Response<BulkOrdersListModel>

    @GET("customer/business-category")
    suspend fun getBusinessCategories(): Response<BusinessCategoryModel>

}