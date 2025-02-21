package com.hfm.customer.data

import com.google.gson.JsonObject
import okhttp3.RequestBody
import javax.inject.Inject

class Repository @Inject constructor(private val service: HFMCustomerAPI) {

    suspend fun login(jsonObject: JsonObject) = service.login(jsonObject)
    suspend fun socialLogin(jsonObject: JsonObject) = service.socialLogin(jsonObject)
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
    suspend fun getFeatureProducts(jsonObject: JsonObject) = service.getFeatureProducts(jsonObject)
    suspend fun getBrandsList(jsonObject: JsonObject) = service.getHomeBrands(jsonObject)


    suspend fun getProductList(jsonObject: JsonObject) = service.getProductList(jsonObject)
    suspend fun getSellerVouchers(jsonObject: JsonObject) = service.getSellerVouchers(jsonObject)
    suspend fun getProfile(jsonObject: JsonObject) = service.getProfile(jsonObject)
    suspend fun getProductReview(jsonObject: JsonObject) = service.getProductReview(jsonObject)
    suspend fun sendBulkOrderRequest(jsonObject: JsonObject) = service.sendBulkOrderRequest(jsonObject)
    suspend fun getBulkOrders(jsonObject: JsonObject) = service.getBulkOrders(jsonObject)
    suspend fun addBulkOrdersAction(jsonObject: JsonObject) = service.addBulkOrdersAction(jsonObject)
    suspend fun getBusinessCategories() = service.getBusinessCategories()
    suspend fun addToWishList(jsonObject: JsonObject) = service.addToWishList(jsonObject)
    suspend fun removeFromWishList(jsonObject: JsonObject) = service.removeFromWishList(jsonObject)
    suspend fun removeCoupon(jsonObject: JsonObject) = service.removeCoupon(jsonObject)
    suspend fun getUomList() = service.getUnitOfMeasurements()
    suspend fun getWishListProducts(jsonObject: JsonObject) = service.getWishListProducts(jsonObject)
    suspend fun getAddress(jsonObject: JsonObject) = service.getAddress(jsonObject)
    suspend fun getCheckOutInfo(jsonObject: JsonObject) = service.getCheckOutInfo(jsonObject)
    suspend fun addNewAddress(jsonObject: JsonObject) = service.addNewAddress(jsonObject)
    suspend fun deleteAddress(jsonObject: JsonObject) = service.deleteAddress(jsonObject)
    suspend fun deleteAccount(jsonObject: JsonObject) = service.deleteAccount(jsonObject)
    suspend fun defaultAddress(jsonObject: JsonObject) = service.defaultAddress(jsonObject)
    suspend fun updateAddress(jsonObject: JsonObject) = service.updateAddress(jsonObject)
    suspend fun makeDefaultAddress(jsonObject: JsonObject) = service.makeDefaultAddress(jsonObject)
    suspend fun addToCart(jsonObject: JsonObject) = service.addToCart(jsonObject)
    suspend fun addToCartMultiple(jsonObject: JsonObject) = service.addToCartMultiple(jsonObject)
    suspend fun getCart(jsonObject: JsonObject) = service.getCart(jsonObject)
    suspend fun deleterCartProduct(jsonObject: JsonObject) = service.deleterCartProduct(jsonObject)
    suspend fun getPlatFormVouchers(jsonObject: JsonObject) = service.getPlatFormVouchers(jsonObject)
    suspend fun getClaimedVouchers(jsonObject: JsonObject) = service.getClaimedVouchers(jsonObject)
    suspend fun claimStoreVoucher(jsonObject: JsonObject) = service.claimStoreVoucher(jsonObject)
    suspend fun applyPlatFormVouchers(jsonObject: JsonObject) = service.applyPlatFormVouchers(jsonObject)
    suspend fun applySellerVouchers(jsonObject: JsonObject) = service.applySellerVouchers(jsonObject)
    suspend fun relatedSearchTerms(jsonObject: JsonObject) = service.relatedSearchTerms(jsonObject)
    suspend fun getNotifications(jsonObject: JsonObject) = service.getNotifications(jsonObject)
    suspend fun notificationViewed(jsonObject: JsonObject) = service.notificationViewed(jsonObject)
    suspend fun checkAvailability(jsonObject: JsonObject) = service.checkAvailability(jsonObject)
    suspend fun updateProfileCustomer(requestBody: MutableMap<String, RequestBody?>) = service.updateProfileCustomer(requestBody)
    suspend fun getReviews(jsonObject: JsonObject) = service.getReviews(jsonObject)
    suspend fun getReferral(jsonObject: JsonObject) = service.getReferral(jsonObject)
    suspend fun submitReview(requestBody: MutableMap<String, RequestBody?>) = service.submitReview(requestBody)
    suspend fun updateProfileBusiness(requestBody: MutableMap<String, RequestBody?>) = service.updateProfileBusiness(requestBody)
    suspend fun updateCartQty(jsonObject: JsonObject) = service.updateCartQty(jsonObject)
    suspend fun getStoreDetails(jsonObject: JsonObject) = service.getStoreDetails(jsonObject)
    suspend fun getStoreProducts(jsonObject: JsonObject) = service.getStoreProducts(jsonObject)
    suspend fun getStoreReviews(jsonObject: JsonObject) = service.getStoreReviews(jsonObject)
    suspend fun myOrders(jsonObject: JsonObject) = service.getMyOrders(jsonObject)
    suspend fun getBrands(jsonObject: JsonObject) = service.getBrands(jsonObject)
    suspend fun getPageData(jsonObject: JsonObject) = service.getPageData(jsonObject)
    suspend fun getBlogsList(jsonObject: JsonObject) = service.getBlogs(jsonObject)
    suspend fun getBlogDetails(jsonObject: JsonObject) = service.getBlogDetails(jsonObject)
    suspend fun createSupportTicket(requestBody: MutableMap<String, RequestBody?>) = service.createSupportTicket(requestBody)
    suspend fun getSupportTickets(jsonObject: JsonObject) = service.getSupportTickets(jsonObject)
    suspend fun followShop(jsonObject: JsonObject) = service.followShop(jsonObject)
    suspend fun unFollowShop(jsonObject: JsonObject) = service.unFollowShop(jsonObject)
    suspend fun followedShops(jsonObject: JsonObject) = service.followedShops(jsonObject)
    suspend fun getWallet(jsonObject: JsonObject) = service.getWallet(jsonObject)
    suspend fun getChatList(jsonObject: JsonObject) = service.getChatList(jsonObject)
    suspend fun getChatMessage(jsonObject: JsonObject) = service.getChatMessage(jsonObject)
    suspend fun getSupportMessage(jsonObject: JsonObject) = service.getSupportMessage(jsonObject)
    suspend fun sendSupportMessage(requestBody: MutableMap<String, RequestBody?>) = service.sendSupportMessage(requestBody)
    suspend fun selectCart(jsonObject: JsonObject) = service.selectCart(jsonObject)
    suspend fun getOrderHistory(jsonObject: JsonObject) = service.getOrderHistory(jsonObject)
    suspend fun sendMessage(requestBody: MutableMap<String, RequestBody?>) = service.sendMessage(requestBody)
    suspend fun placeOrder(jsonObject: JsonObject) = service.placeOrder(jsonObject)
    suspend fun uploadOrderReceipt(requestBody: MutableMap<String, RequestBody?>) = service.uploadOrderReceipt(requestBody)
    suspend fun paymentFaq() = service.paymentFaq()
    suspend fun getCountryCode(countryCode:String) = service.getCountryCode(countryCode)
    suspend fun getStateCode(name:String,countryId:String) = service.getStateCode(name,countryId)
    suspend fun getCityCode(name:String,stateId:String) = service.getCityCode(name,  stateId)
    suspend fun getTermsConditions() = service.getTermsConditions()
    suspend fun getAppUpdate(jsonObject: JsonObject) = service.getAppUpdate(jsonObject)
    suspend fun checkLogin(jsonObject: JsonObject) = service.checkLogin(jsonObject)
    suspend fun orderTracking(jsonObject: JsonObject) = service.orderTracking(jsonObject)
    suspend fun applyWallet(jsonObject: JsonObject) = service.applyWallet(jsonObject)
    suspend fun removeWallet(jsonObject: JsonObject) = service.removeWallet(jsonObject)
    suspend fun updateShipping(jsonObject: JsonObject) = service.updateShipping(jsonObject)
    suspend fun sellerBannerActivity(jsonObject: JsonObject) = service.sellerBannerActivity(jsonObject)
    suspend fun forgotPassword(jsonObject: JsonObject) = service.forgotPassword(jsonObject)
    suspend fun updateDeviceToken(jsonObject: JsonObject) = service.updateDeviceToken(jsonObject)
    suspend fun logout(jsonObject: JsonObject) = service.logout(jsonObject)
}