package com.hfm.customer.data

import com.google.gson.JsonObject
import org.json.JSONObject
import javax.inject.Inject

class Repository @Inject constructor(private val service: HFMCustomerAPI) {

    suspend fun login(jsonObject: JsonObject) = service.login(jsonObject)
    suspend fun registerSendOTP(jsonObject: JsonObject) = service.registerSendOTP(jsonObject)
    suspend fun registerUser(jsonObject: JsonObject) = service.registerUser(jsonObject)
    suspend fun registerVerifyOTP(jsonObject: JsonObject) = service.registerVerifyOTP(jsonObject)
    suspend fun getProductDetails(jsonObject: JsonObject) = service.getProductDetails(jsonObject)
    suspend fun getCountriesList() = service.getCountriesList()
    suspend fun getStatesList(jsonObject: JsonObject) = service.getStatesList(jsonObject)
    suspend fun getCitiesList(jsonObject: JsonObject) = service.getCitiesList(jsonObject )

    suspend fun getCategories(jsonObject: JsonObject) = service.getCategories(jsonObject)
    suspend fun getMainBanner(jsonObject: JsonObject) = service.getMainBanner(jsonObject)

    suspend fun getMiddleBanner(jsonObject: JsonObject) = service.getMiddleBanner(jsonObject)
    suspend fun getBottomBanner(jsonObject: JsonObject) = service.getBottomBanner(jsonObject)
    suspend fun getHomeFlashSaleCategory(jsonObject: JsonObject) = service.getHomeFlashSaleCategory(jsonObject)
    suspend fun getHomeFlashSaleProducts(jsonObject: JsonObject,page:String) = service.getFlashSaleProducts(jsonObject,page)
    suspend fun getWholeSaleProducts(jsonObject: JsonObject,page: String) = service.getWholeSaleProducts(jsonObject,page)
    suspend fun getTrendingNow(jsonObject: JsonObject) = service.getTrendingNow(jsonObject)
    suspend fun getFeatureProducts(jsonObject: JsonObject, page: String) = service.getFeatureProducts(jsonObject)
    suspend fun getBrandsList(jsonObject: JsonObject) = service.getHomeBrands(jsonObject)


    suspend fun getProductList(jsonObject: JsonObject) = service.getProductList(jsonObject)
    suspend fun getSellerVouchers(jsonObject: JsonObject) = service.getSellerVouchers(jsonObject)
    suspend fun getProfile(jsonObject: JsonObject) = service.getProfile(jsonObject)
    suspend fun sendBulkOrderRequest(jsonObject: JsonObject) = service.sendBulkOrderRequest(jsonObject)
    suspend fun getBulkOrders(jsonObject: JsonObject) = service.getBulkOrders(jsonObject)
    suspend fun getBusinessCategories() = service.getBusinessCategories()


}