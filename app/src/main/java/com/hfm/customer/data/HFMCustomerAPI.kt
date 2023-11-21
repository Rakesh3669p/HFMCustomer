package com.hfm.customer.data

import com.google.gson.JsonObject
import com.hfm.customer.BuildConfig
import com.hfm.customer.commonModel.AppUpdateModel
import com.hfm.customer.commonModel.ApplyWalletModel
import com.hfm.customer.commonModel.CityListModel
import com.hfm.customer.commonModel.CountryListModel
import com.hfm.customer.commonModel.HomeMainCategoriesModel
import com.hfm.customer.commonModel.RatingReviewsModel
import com.hfm.customer.commonModel.StateListModel
import com.hfm.customer.commonModel.SuccessModel
import com.hfm.customer.commonModel.TermsConditionsModel
import com.hfm.customer.ui.dashBoard.home.model.FeatureProductsModel
import com.hfm.customer.ui.dashBoard.home.model.FlashSaleModel
import com.hfm.customer.ui.dashBoard.home.model.HomeBottomBannerModel
import com.hfm.customer.ui.dashBoard.home.model.HomeBrandsModel
import com.hfm.customer.ui.dashBoard.home.model.HomeFlashSaleCategory
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
import com.hfm.customer.ui.fragments.products.productDetails.model.SellerVoucherModel
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
import com.hfm.customer.ui.loginSignUp.login.model.LoginModel
import com.hfm.customer.ui.loginSignUp.register.model.BusinessCategoryModel
import okhttp3.RequestBody
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

    @POST("customer/social/login")
    suspend fun socialLogin(
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


    @POST("customer/home/app-main-banner-section")
    suspend fun getCategories(
        @Body jsonObject: JsonObject
    ): Response<HomeMainCategoriesModel>

    @POST("customer/home/shock-sale-products")
    suspend fun getFlashSaleProducts(
        @Body jsonObject: JsonObject,
        @Query("page") page: String
    ): Response<FlashSaleModel>

    @POST("customer/home/app-main-banner-section")
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
    ): Response<VoucherListModel>

    @POST("customer/coupon-list")
    suspend fun getPlatFormVouchers(
        @Body jsonObject: JsonObject
    ): Response<VoucherListModel>

    @POST("customer/coupon/platform")
    suspend fun applyPlatFormVouchers(
        @Body jsonObject: JsonObject
    ): Response<CouponAppliedModel>

    @POST("customer/coupon/seller")
    suspend fun applySellerVouchers(
        @Body jsonObject: JsonObject
    ): Response<CouponAppliedModel>

    @POST("customer/profile")
    suspend fun getProfile(
        @Body jsonObject: JsonObject
    ): Response<ProfileModel>

    @POST("customer/product/reviews")
    suspend fun getProductReview(
        @Body jsonObject: JsonObject
    ): Response<RatingReviewsModel>

    @POST("customer/order/request-bulkorder")
    suspend fun sendBulkOrderRequest(
        @Body jsonObject: JsonObject
    ): Response<BulkOrderRequestModel>

    @POST("customer/order/bulkorder-details")
    suspend fun getBulkOrders(
        @Body jsonObject: JsonObject
    ): Response<BulkOrdersListModel>


    @POST("customer/order/bulkorder/quotation_status")
    suspend fun addBulkOrdersAction(
        @Body jsonObject: JsonObject
    ): Response<SuccessModel>


    @GET("customer/business-category")
    suspend fun getBusinessCategories(): Response<BusinessCategoryModel>

    @POST("customer/add/wishlist")
    suspend fun addToWishList(@Body jsonObject: JsonObject): Response<SuccessModel>

    @POST("customer/remove/wishlist")
    suspend fun removeFromWishList(@Body jsonObject: JsonObject): Response<SuccessModel>

    @POST("customer/cart/remove-coupon")
    suspend fun removeCoupon(@Body jsonObject: JsonObject): Response<SuccessModel>

    @POST("customer/wishlist")
    suspend fun getWishListProducts(@Body jsonObject: JsonObject): Response<WishListModel>

    @POST("customer/address")
    suspend fun getAddress(@Body jsonObject: JsonObject): Response<AddressModel>

    @POST("customer/order/checkout-info")
    suspend fun getCheckOutInfo(@Body jsonObject: JsonObject): Response<CheckOutModel>

    @POST("customer/add/address")
    suspend fun addNewAddress(@Body jsonObject: JsonObject): Response<SuccessModel>

    @POST("customer/remove/address")
    suspend fun deleteAddress(@Body jsonObject: JsonObject): Response<SuccessModel>

    @POST("customer/default/address")
    suspend fun defaultAddress(@Body jsonObject: JsonObject): Response<SuccessModel>

    @POST("customer/edit/address")
    suspend fun updateAddress(@Body jsonObject: JsonObject): Response<SuccessModel>

    @POST("customer/default/address")
    suspend fun makeDefaultAddress(@Body jsonObject: JsonObject): Response<SuccessModel>

    @POST("customer/add-cart")
    suspend fun addToCart(@Body jsonObject: JsonObject): Response<AddToCartModel>

    @POST("customer/add-cart-multiple")
    suspend fun addToCartMultiple(@Body jsonObject: JsonObject): Response<AddToCartModel>

    @POST("customer/app-cart")
    suspend fun getCart(@Body jsonObject: JsonObject): Response<CartModel>

    @POST("customer/delete-cart")
    suspend fun deleterCartProduct(@Body jsonObject: JsonObject): Response<AddToCartModel>

    @POST("customer/home/related-search-terms")
    suspend fun relatedSearchTerms(@Body jsonObject: JsonObject): Response<RelatedSearchTermsModel>

    @POST("customer/view/notifications")
    suspend fun getNotifications(@Body jsonObject: JsonObject): Response<NotificationModel>

    @POST("customer/notification/update")
    suspend fun notificationViewed(@Body jsonObject: JsonObject): Response<SuccessModel>

    @POST("customer/shipping-availability")
    suspend fun checkAvailability(@Body jsonObject: JsonObject): Response<SuccessModel>

    @Multipart
    @POST("customer/edit/profile")
    suspend fun updateProfileCustomer(
        @PartMap map: MutableMap<String, RequestBody?>,
    ): Response<SuccessModel>

    @Multipart
    @POST("customer/product/post-product-review")
    suspend fun submitReview(
        @PartMap map: MutableMap<String, RequestBody?>,
    ): Response<SuccessModel>

    @POST("customer/product/reviews")
    suspend fun getReviews(
        @Body jsonObject: JsonObject,
    ): Response<RatingReviewsModel>

    @Multipart
    @POST("customer/edit/business/profile")
    suspend fun updateProfileBusiness(
        @PartMap map: MutableMap<String, RequestBody?>,
    ): Response<SuccessModel>

    @POST("customer/cart/change-qty")
    suspend fun updateCartQty(@Body jsonObject: JsonObject): Response<SuccessModel>

    @POST("customer/shop-detail")
    suspend fun getStoreDetails(@Body jsonObject: JsonObject): Response<StoreDetailsModel>

    @POST("customer/shop-detail-products")
    suspend fun getStoreProducts(@Body jsonObject: JsonObject): Response<StoreProductsModel>

    @POST("customer/shop-reviews")
    suspend fun getStoreReviews(@Body jsonObject: JsonObject): Response<StoreReviewsModel>

    @POST("customer/mypurchase")
    suspend fun getMyOrders(@Body jsonObject: JsonObject): Response<MyOrdersModel>

    @POST("customer/app-brand")
    suspend fun getBrands(@Body jsonObject: JsonObject): Response<BrandsModel>

    @POST("customer/pages-detail")
    suspend fun getPageData(@Body jsonObject: JsonObject): Response<PageModel>

    @POST("customer/blog/list")
    suspend fun getBlogs(@Body jsonObject: JsonObject): Response<BlogsModel>

    @POST("customer/blog/details")
    suspend fun getBlogDetails(@Body jsonObject: JsonObject): Response<BlogDetailsModel>

    @Multipart
    @POST("customer/create-ticket")
    suspend fun createSupportTicket(@PartMap requestBody: MutableMap<String, RequestBody?>): Response<SuccessModel>

    @POST("customer/list-ticket")
    suspend fun getSupportTickets(@Body jsonObject: JsonObject): Response<SupportTicketsModel>

    @POST("customer/follow/shop")
    suspend fun followShop(@Body jsonObject: JsonObject): Response<SuccessModel>

    @POST("customer/unfollow/shop")
    suspend fun unFollowShop(@Body jsonObject: JsonObject): Response<SuccessModel>

    @POST("customer/follow-list")
    suspend fun followedShops(@Body jsonObject: JsonObject): Response<StoreWishlistModel>

    @POST("customer/wallet/amount")
    suspend fun getWallet(@Body jsonObject: JsonObject): Response<WalletModel>

    @POST("customer/chat/message")
    suspend fun getChatMessage(@Body jsonObject: JsonObject): Response<ChatMessageModel>

    @POST("customer/support-message")
    suspend fun getSupportMessage(@Body jsonObject: JsonObject): Response<SupportMessagesModel>

    @Multipart
    @POST("customer/add-ticket-message")
    suspend fun sendSupportMessage(@PartMap map: MutableMap<String, RequestBody?>): Response<SuccessModel>

    @POST("customer/chat/list")
    suspend fun getChatList(@Body jsonObject: JsonObject): Response<ChatListModel>

    @POST("customer/cart-select-tem")
    suspend fun selectCart(@Body jsonObject: JsonObject): Response<SuccessModel>

    @POST("customer/order/order-history")
    suspend fun getOrderHistory(@Body jsonObject: JsonObject): Response<OrderHistoryModel>

    @Multipart
    @POST("customer/chat/send")
    suspend fun sendMessage(@PartMap map: MutableMap<String, RequestBody?>): Response<MessageSentModel>

    @POST("customer/order/placeorder")
    suspend fun placeOrder(@Body jsonObject: JsonObject): Response<PlaceOrderModel>

    @Multipart
    @POST("customer/order/payment_upload")
    suspend fun uploadOrderReceipt(@PartMap map: MutableMap<String, RequestBody?>): Response<SuccessModel>

    @GET("customer/payment-faq")
    suspend fun paymentFaq(): Response<PaymentFAQModel>

    @POST("customer/country/find-by-name")
    suspend fun getCountryCode(
        @Query("country_code") page: String
    ): Response<CountryCodeModel>


    @POST("customer/state/find-by-name")
    suspend fun getStateCode(
        @Query("name") name: String,
        @Query("country_id") countryId: String
    ): Response<StateCodeModel>

    @POST("customer/city/find-by-name")
    suspend fun getCityCode(
        @Query("name") name: String,
        @Query("state_id") stateId: String
    ): Response<CityCodeModel>

    @GET("customer/terms-conditions")
    suspend fun getTermsConditions(): Response<TermsConditionsModel>

    @POST("customer/check-app-update")
    suspend fun getAppUpdate(
        @Body jsonObject: JsonObject
    ): Response<AppUpdateModel>

    @POST("customer/validate-token")
    suspend fun checkLogin(
        @Body jsonObject: JsonObject
    ): Response<SuccessModel>

    @POST("customer/dhl-tracking")
    suspend fun orderTracking(
        @Body jsonObject: JsonObject
    ): Response<OrderTrackingModel>


    @POST("customer/apply-wallet")
    suspend fun applyWallet(
        @Body jsonObject: JsonObject
    ): Response<ApplyWalletModel>

    @POST("customer/remove-wallet")
    suspend fun removeWallet(
        @Body jsonObject: JsonObject
    ): Response<SuccessModel>

    @POST("customer/shipping-option")
    suspend fun updateShipping(
        @Body jsonObject: JsonObject
    ): Response<SuccessModel>

    @POST("customer/banner-activity")
    suspend fun sellerBannerActivity(
        @Body jsonObject: JsonObject
    ): Response<SuccessModel>

    @POST("customer/forgot/password")
    suspend fun forgotPassword(
        @Body jsonObject: JsonObject
    ): Response<SuccessModel>


    @POST("customer/logout")
    suspend fun logout(
        @Body jsonObject: JsonObject
    ): Response<SuccessModel>


}