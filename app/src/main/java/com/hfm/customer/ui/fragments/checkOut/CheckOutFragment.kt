package com.hfm.customer.ui.fragments.checkOut

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.JsonObject
import com.hfm.customer.R
import com.hfm.customer.databinding.BottomSheetVoucherBinding
import com.hfm.customer.databinding.FragmentCheckoutBinding
import com.hfm.customer.databinding.InternationalTermsAndConditionsPopUpBinding
import com.hfm.customer.ui.fragments.address.model.Address
import com.hfm.customer.ui.fragments.cart.adapter.PlatformVoucherAdapter
import com.hfm.customer.ui.fragments.cart.model.CartData
import com.hfm.customer.ui.fragments.cart.model.Coupon
import com.hfm.customer.ui.fragments.checkOut.adapter.CheckOutStoreAdapter
import com.hfm.customer.ui.fragments.checkOut.model.ShippingOption
import com.hfm.customer.ui.fragments.products.productDetails.adapter.ReviewsAdapter
import com.hfm.customer.utils.Loader
import com.hfm.customer.utils.NoInternetDialog
import com.hfm.customer.utils.Resource
import com.hfm.customer.utils.SessionManager
import com.hfm.customer.utils.formatToTwoDecimalPlaces
import com.hfm.customer.utils.initRecyclerView
import com.hfm.customer.utils.moveToLogin
import com.hfm.customer.utils.netWorkFailure
import com.hfm.customer.utils.showToast
import com.hfm.customer.utils.showToastLong
import com.hfm.customer.viewModel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt


@AndroidEntryPoint
class CheckOutFragment : Fragment(), View.OnClickListener {

    private var goodsTermsAndConditions: String = ""
    private lateinit var cartData: CartData

    private var grandTotal: Double = 0.0
    private var selectingVoucherSellerId: String = ""
    private var platformVouchers: List<Coupon> = ArrayList()
    private var sellerVouchers: List<Coupon> = ArrayList()
    private var sellerSelectedVoucher: Coupon? = null
    private var platFormSelectedVoucher: Coupon? = null

    private var shippingOptions: List<ShippingOption> = ArrayList()

    private var addressId: Int = 0
    private var addressList: List<Address> = ArrayList()
    private lateinit var binding: FragmentCheckoutBinding
    private var currentView: View? = null

    @Inject
    lateinit var reviewsAdapter: ReviewsAdapter

    private lateinit var appLoader: Loader
    private lateinit var noInternetDialog: NoInternetDialog
    private val mainViewModel: MainViewModel by viewModels()

    private lateinit var platformVoucherDialog: BottomSheetDialog

    @Inject
    lateinit var checkOutAdapter: CheckOutStoreAdapter

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var platformVoucherAdapter: PlatformVoucherAdapter

    @Inject
    lateinit var sellerVoucherAdapter: PlatformVoucherAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_checkout, container, false)
            binding = FragmentCheckoutBinding.bind(currentView!!)
            init()
            setOnClickListener()
        }
        return currentView!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setObserver()
        lifecycleScope.launch {
            delay(500)
            findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Int>("addressId")
                ?.observe(viewLifecycleOwner) {
                    addressId = it ?: 0
                }

            if (addressId >= 0) {
                val address = addressList.find { it.id == addressId }
                address?.let { setAddress(it) }
            }
        }
    }

    private fun init() {
        appLoader = Loader(requireContext())
        noInternetDialog = NoInternetDialog(requireContext())
        binding.loader.isVisible = true
        mainViewModel.getPlatFormVouchers(1)
        mainViewModel.getAddress()
        mainViewModel.getTermsConditions()
//        mainViewModel.getCheckoutInfo()
    }

    private fun setObserver() {
        mainViewModel.updateshipingOption.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    when (response.data?.httpcode) {
                        200 -> {
                            mainViewModel.getCart()
                        }

                        401 -> {
                            requireActivity().moveToLogin(sessionManager)
                        }

                        else -> {
                            showToast(response.data?.message.toString())
                        }
                    }
                }
                is Resource.Loading -> appLoader.show()
                is Resource.Error -> apiError(response.message)
            }
        }

        mainViewModel.address.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    if (response.data?.httpcode == "200") {
                        addressList = response.data.data.address_list
                        response.data.data.address_list.forEach {
                            if (it.is_default == 1) {
                                setAddress(it)
                            }
                        }
                    }
                }
                is Resource.Loading -> Unit
                is Resource.Error -> apiError(response.message)
            }
        }

        mainViewModel.applyWallet.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    when (response.data?.httpcode) {
                        200 -> {
                            mainViewModel.getCart()
                        }

                        401 -> {
                            requireActivity().moveToLogin(sessionManager)
                        }

                        else -> {
//                            showToast(response.data?.message.toString())
                        }
                    }
                }

                is Resource.Loading -> Unit
                is Resource.Error -> apiError(response.message)
            }
        }

        mainViewModel.removeWallet.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    when (response.data?.httpcode) {
                        200 -> {
                            mainViewModel.getCart()
                        }

                        401 -> {
                            requireActivity().moveToLogin(sessionManager)
                        }

                        else -> {
//                            showToast(response.data?.message.toString())
                        }
                    }
                }

                is Resource.Loading -> Unit
                is Resource.Error -> apiError(response.message)
            }
        }

        mainViewModel.termsConditions.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    if (response.data?.httpcode == "200") {
                        goodsTermsAndConditions = response.data.data.terms_conditions.goods_tc
                    }
                }

                is Resource.Loading -> Unit
                is Resource.Error -> apiError(response.message)
            }
        }


        mainViewModel.platformVouchers.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    if (response.data?.httpcode == 200) {
                        platformVouchers = response.data.data.coupon_list
                    }
                }

                is Resource.Loading -> Unit
                is Resource.Error -> apiError(response.message)
            }
        }

        mainViewModel.sellerVouchers.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    if (response.data?.httpcode == 200) {

                        sellerVouchers = response.data.data.coupon_list
                        showSellerVoucherBottomSheet()
                    }
                }

                is Resource.Loading -> appLoader.show()
                is Resource.Error -> apiError(response.message)
            }
        }

        mainViewModel.applyPlatformVoucher.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    if (response.data?.httpcode == 200) {
                        if (this::platformVoucherDialog.isInitialized) {
                            platformVoucherDialog.dismiss()
                        }

                        if(response.data.status == "success"){
                            mainViewModel.getCheckoutInfo()
                            showToast("Voucher Applied")
                        }else{
                            showToastLong(response.data.message.toString())
                        }
                    } else {
                        platformVoucherDialog.dismiss()
                        showToast(response.data?.message.toString())
                    }
                }

                is Resource.Loading -> appLoader.show()
                is Resource.Error ->
                    apiError(response.message)
            }
        }


        mainViewModel.applySellerVoucher.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    if (this::platformVoucherDialog.isInitialized) platformVoucherDialog.dismiss()
                    if (response.data?.httpcode == 200)
                        mainViewModel.getCheckoutInfo() else showToast(response.data?.message.toString()
                    )
                }

                is Resource.Loading -> appLoader.show()
                is Resource.Error ->
                    apiError(response.message)
            }
        }
        mainViewModel.removeCoupon.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    if (response.data?.httpcode == 200) {
                        mainViewModel.getCheckoutInfo()
                    } else {
                        showToast(response.data?.message.toString())
                    }
                }

                is Resource.Loading -> appLoader.show()
                is Resource.Error ->
                    apiError(response.message)
            }
        }

        mainViewModel.checkOutInfo.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    if (response.data?.httpcode == 200) {
                        mainViewModel.getCart()
                        shippingOptions = response.data.data.shipping_options
                    } else {
                        showToast(response.data?.message.toString())
                    }
                }

                is Resource.Loading -> Unit
                is Resource.Error -> apiError(response.message)
            }
        }


        mainViewModel.cart.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    while (appLoader.isShowing) appLoader.dismiss()
                    if (response.data?.httpcode == 200) {
                        setProducts(response.data.data)
                    }
                }

                is Resource.Loading -> appLoader.show()
                is Resource.Error -> apiError(response.message)
            }
        }
    }

    private fun setProducts(cartData: CartData) {
        this.cartData = cartData

        if(cartData.wallet_text.isNotEmpty()){
            showToast(cartData.wallet_text)
        }

        if(cartData.platform_coupon_text.isNotEmpty()){
            showToast(cartData.platform_coupon_text)
        }

        with(binding) {
            checkOutRv.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = checkOutAdapter
            }

            val animator: RecyclerView.ItemAnimator? = checkOutRv.itemAnimator
            if (animator is DefaultItemAnimator) {
                animator.supportsChangeAnimations = false
                checkOutRv.itemAnimator = null
            }

            val checkOutData = cartData.seller_product.filter { sellerProductItem ->
                sellerProductItem.seller.products.any {
                    it.cart_selected.toString().toDouble() > 0
                }
            }

            checkOutRv.itemAnimator?.let { animator ->
                if (animator is DefaultItemAnimator) {
                    animator.supportsChangeAnimations = false
                    checkOutRv.itemAnimator = null
                }
            }
            checkOutAdapter.setActivity(requireActivity())
            checkOutAdapter.differ.submitList(checkOutData)
            checkOutAdapter.setShippingOptions(shippingOptions)

            val productCount = checkOutData.sumOf { sellerProducts ->
                sellerProducts.seller.products.count { product ->
                    product.cart_selected.toString().toDouble() > 0
                }
            }

            storeVoucher.text =
                if (cartData.seller_voucher_amt > 0) "RM -${cartData.seller_voucher_amt}" else "RM 0.00"
            platformVoucher.text =
                if (cartData.platform_voucher_amt > 0) "RM -${cartData.platform_voucher_amt}" else "RM 0.00"

            subtotalLbl.text = if (productCount > 0) "Subtotal($productCount Items)" else "Subtotal"
            subtotal.text =
                "RM ${formatToTwoDecimalPlaces(cartData.total_offer_cost.toString().toDouble())}"
            shippingAmount.text =
                "RM ${formatToTwoDecimalPlaces(cartData.shipping_charges.toString().toDouble())}"
            shippingDiscount.text =
                if (cartData.shipping_discount > 0) "RM -${formatToTwoDecimalPlaces(cartData.shipping_discount)}"
                else "RM 0.00"
            grandTotal = cartData.grand_total.toString().toDouble()
            totalAmount.text = formatToTwoDecimalPlaces(grandTotal)

            voucherDetailsLayout.isVisible = cartData.is_platform_coupon_applied == 1
            addVoucher.isVisible = cartData.is_platform_coupon_applied == 0

            voucherName.text = cartData.platform_coupon_data.title

            if (cartData.platform_coupon_data.is_free_shipping == 1) {
                voucherDescription.text = getString(R.string.free_shipping_applied_lbl)
            } else {
                voucherDescription.text = "You saved additional RM ${formatToTwoDecimalPlaces(cartData.platform_coupon_data.platform_discount_amount)}"
            }

            val walletBalance = cartData.wallet_balance
            if (walletBalance != "false") {
                points.text = "${formatToTwoDecimalPlaces(cartData.wallet_balance.toDouble())}"
                pointsCash.text = "(RM ${formatToTwoDecimalPlaces(cartData.wallet_cash.toDouble())})"
            } else {
                points.text = "0"
                pointsCash.text = "(RM 0.00)"
                wallet.text = "RM 0.00"
            }

            useWalletPoints.isChecked = cartData.is_wallet_applied == 1
            if (useWalletPoints.isChecked) {
                wallet.text =
                    "RM -${formatToTwoDecimalPlaces(cartData.wallet_applied_cash.toDouble())}"
            } else {
                wallet.text = "RM 0.00"
            }

            binding.root.invalidate()

        }
        binding.loader.isVisible = false
    }

    private fun showVoucherBottomSheet() {

        val platformVoucherBinding = BottomSheetVoucherBinding.inflate(layoutInflater)
        platformVoucherDialog =
            BottomSheetDialog(requireActivity(), R.style.MyTransparentBottomSheetDialogTheme)
        platformVoucherDialog.setContentView(platformVoucherBinding.root)
        val bottomSheet: FrameLayout? =
            platformVoucherDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet)
        if (bottomSheet != null) {
            BottomSheetBehavior.from(bottomSheet).state = BottomSheetBehavior.STATE_EXPANDED
        }
        initRecyclerView(
            requireContext(),
            platformVoucherBinding.vouchersRv,
            platformVoucherAdapter
        )

        platformVoucherAdapter.setOnItemClickListener { position ->
            platFormSelectedVoucher = platformVouchers[position]
        }

        with(platformVoucherBinding) {
            noVouchers.isVisible = platformVouchers.isEmpty() == true
            requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
            titleLbl.text = "Select Platform Vouchers"
            voucherCode.setOnClickListener {
                voucherCode.isFocusable = true
                voucherCode.requestFocus()
            }

            applyVoucher.setOnClickListener {
                val couponCode = voucherCode.text.toString()
                if (couponCode.isEmpty()) {
                    showToast("Please enter a coupon code or select to apply")
                }

                mainViewModel.applyPlatFormVouchers(
                    couponCode,
                    cartData.total_offer_cost.toString()
                )
            }
            apply.setOnClickListener {
                if (platFormSelectedVoucher?.couponCode.isNullOrEmpty()) {
                    showToast("Please enter a coupon code or select to apply")

                }
                mainViewModel.applyPlatFormVouchers(
                    platFormSelectedVoucher?.couponCode ?: "",
                    cartData.total_offer_cost.toString()
                )
            }
            cancel.setOnClickListener {
                platformVoucherDialog.dismiss()
            }
        }
        platformVoucherAdapter.differ.submitList(platformVouchers)
        platformVoucherAdapter.notifyDataSetChanged()
        platformVoucherDialog.show()
    }

    private fun showSellerVoucherBottomSheet() {
        val sellerVoucherBinding = BottomSheetVoucherBinding.inflate(layoutInflater)
        platformVoucherDialog =
            BottomSheetDialog(requireActivity(), R.style.MyTransparentBottomSheetDialogTheme)
        platformVoucherDialog.setContentView(sellerVoucherBinding.root)
        val bottomSheet: FrameLayout? =
            platformVoucherDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet)
        if (bottomSheet != null) {
            BottomSheetBehavior.from(bottomSheet).state = BottomSheetBehavior.STATE_EXPANDED
        }
        initRecyclerView(requireContext(), sellerVoucherBinding.vouchersRv, sellerVoucherAdapter)

        sellerVoucherAdapter.setOnItemClickListener { position ->
            sellerSelectedVoucher = sellerVouchers[position]
        }

        with(sellerVoucherBinding) {
            noVouchers.isVisible = sellerVouchers.isEmpty() == true
            requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
            titleLbl.text = "Select Store Vouchers"
            voucherCode.setOnClickListener {
                voucherCode.isFocusable = true
                voucherCode.requestFocus()
            }

            applyVoucher.setOnClickListener {
                val sellerData =
                    cartData.seller_product.find { it.seller.seller_id.toString() == selectingVoucherSellerId }

                val couponCode = voucherCode.text.toString()
                if (couponCode.isEmpty()) {
                    showToast("Please enter a coupon code or select to apply")
                    return@setOnClickListener
                }

                mainViewModel.applySellerVouchers(
                    selectingVoucherSellerId,
                    couponCode,
                    sellerData?.seller_subtotal ?: 0.0,sellerData?.shipping?:0.0
                )
            }

            apply.setOnClickListener {
                val sellerData =
                    cartData.seller_product.find { it.seller.seller_id.toString() == selectingVoucherSellerId }
                if (sellerSelectedVoucher?.couponCode.isNullOrEmpty()) {
                    showToast("Please enter a coupon code or select to apply")
                    return@setOnClickListener
                }

                mainViewModel.applySellerVouchers(
                    selectingVoucherSellerId,
                    sellerSelectedVoucher?.couponCode ?: "",
                    sellerData?.seller_subtotal ?: 0.0,sellerData?.shipping?:0.0
                )
                sellerSelectedVoucher = null
            }
            cancel.setOnClickListener { platformVoucherDialog.dismiss() }
        }
        sellerVoucherAdapter.differ.submitList(sellerVouchers)
        platformVoucherAdapter.notifyDataSetChanged()
        platformVoucherDialog.show()
    }


    private fun setAddress(address: Address) {
        with(binding) {
            addressId = address.id
            customerName.text = address.name.toString()
            customerAddress.text =
                "${address.house},${address.street},\n${address.city}, ${address.state}, ${address.country}, ${address.pincode}"
            customerMobile.text = address.phone.toString()
            mainViewModel.getCheckoutInfo()
        }
    }

    private fun apiError(message: String?) {
        appLoader.dismiss()
        showToast(message.toString())
        if (message == netWorkFailure) {
            noInternetDialog.show()
        }
    }


    private fun setOnClickListener() {
        with(binding) {
            back.setOnClickListener(this@CheckOutFragment)
            addVoucher.setOnClickListener(this@CheckOutFragment)
            removeVoucher.setOnClickListener(this@CheckOutFragment)
            manageAddress.setOnClickListener(this@CheckOutFragment)
            placeOrder.setOnClickListener(this@CheckOutFragment)

            useWalletPoints.setOnClickListener {
                if (useWalletPoints.isChecked) {
                    val walletBalance = cartData.wallet_balance.toDouble() / 100
                    if (walletBalance > cartData.grand_total.toString().toDouble()) {
                        mainViewModel.applyWallet((cartData.grand_total.toString().toDouble() * 100).toString())
                    } else {
                        mainViewModel.applyWallet(cartData.wallet_balance)
                    }
                } else {
                    mainViewModel.removeWallet()
                }
            }
        }

        checkOutAdapter.setOnShippingOption { sellerId, shippingId ->
            mainViewModel.updateShipping(sellerId, shippingId)
        }

        checkOutAdapter.setOnMessageListener { message, position ->
            cartData.seller_product[position].seller.message = message
        }

        checkOutAdapter.setOnStoreClicked { sellerId ->
            val bundle = Bundle()
            bundle.putString("storeId", sellerId.toString())
            findNavController().navigate(R.id.storeFragment, bundle)
        }

        checkOutAdapter.setOnShopVoucherClickListener { sellerId ->
            val storeData =
                cartData.seller_product.find { it.seller.seller_id.toString() == sellerId }
            if (storeData?.seller?.products?.any {
                    it.cart_selected.toString().toDouble() > 0
                } == false) {
                showToast("Please select any product to apply the voucher")
                return@setOnShopVoucherClickListener
            }
            selectingVoucherSellerId = sellerId
            mainViewModel.getSellerVouchers(sellerId, 1)
        }

        checkOutAdapter.setOnSellerRemoveCoupon { id ->
            mainViewModel.removeCoupon(id.toString(), "seller")
        }


    }

    private fun placeOrder() {
        val jsonObject = JsonObject()
        val sellerArray = JsonObject()

        var sellerIndex = 0


        val someSelectedProductNotDeliverable = checkOutAdapter.differ.currentList.any { seller ->
            seller.seller.products.any { product ->
                product.cart_selected.toString()
                    .toDouble() > 0 && product.check_shipping_availability.toString().toDouble() < 1
            }
        }

        val someSelectedProductOutOfStock: Boolean =
            checkOutAdapter.differ.currentList.filter { it.seller.cart_selected == 1 }
                .any { seller ->
                    seller.seller.products.filter { it.cart_selected == 1 }.any { product ->
                        product.is_out_of_stock == 1
                    }
                }


        if (someSelectedProductNotDeliverable) {
            showToast("Some selected products is not available in your Delivery Address.")
            return
        } else if (someSelectedProductOutOfStock) {
            showToast("Some selected products is Sold Out.")
            return
        }

        cartData.seller_product.forEach { sellerData ->
            val anyProductSelected =
                sellerData.seller.products.any { it.cart_selected.toString().toDouble() > 0 }
            if (anyProductSelected) {

                val sellerObject = JsonObject()
                sellerObject.addProperty("shipping_method", sellerData.shipping_method)
                sellerObject.addProperty("shipping_charge", sellerData.shipping.toString())
                sellerObject.addProperty("seller_id", sellerData.seller.seller_id)
                sellerObject.addProperty("shipping_markup", sellerData.shipping_markup.toString())
                sellerObject.addProperty("total_tax", 0)
                sellerObject.addProperty("commission", 0)
                sellerObject.addProperty("discount_amt", 0)
                sellerObject.addProperty("total_cost", sellerData.seller_subtotal)
                sellerObject.addProperty("is_coupon", sellerData.is_seller_coupon_applied)
                sellerObject.addProperty("packing_charge", 0)
                sellerObject.addProperty("message", sellerData.seller.message ?: "")
                sellerObject.addProperty("shipping_option", sellerData.seller_shipping_option)
                if (sellerData.seller_coupon_data.coupon_id != null) {
                    sellerObject.addProperty("coupon_id", sellerData.seller_coupon_data.coupon_id)
                } else {
                    sellerObject.addProperty("coupon_id", "")
                }


                if (sellerData.seller_coupon_data.discount_type != null) {
                    sellerObject.addProperty(
                        "discount_type",
                        sellerData.seller_coupon_data.discount_type
                    )
                } else {
                    sellerObject.addProperty("discount_type", "")
                }

                if (sellerData.seller_coupon_data.seller_coupon_discount_amt != null) {
                    sellerObject.addProperty(
                        "coupon_discount_amt",
                        sellerData.seller_coupon_data.seller_coupon_discount_amt
                    )
                } else {
                    sellerObject.addProperty("coupon_discount_amt", "")
                }
                sellerArray.add(sellerIndex.toString(), sellerObject)
                sellerIndex++
            }
        }

        jsonObject.add("seller_array", sellerArray)
        jsonObject.addProperty("access_token", sessionManager.token)
        jsonObject.addProperty("lang_id", 1)
        jsonObject.addProperty("is_platform_coupon", cartData.is_platform_coupon_applied)

        if(cartData.platform_coupon_data.platform_discount_type == "shipping_discount"){
            jsonObject.addProperty("platform_discount_amt", cartData.shipping_discount)
        }else{
            jsonObject.addProperty("platform_discount_amt", cartData.platform_voucher_amt)
        }

        if (cartData.platform_coupon_data.coupon_id != null) {

            jsonObject.addProperty("platform_coupon_id", cartData.platform_coupon_data.coupon_id)
        } else {
            jsonObject.addProperty("platform_coupon_id", "")
        }

        if (cartData.platform_coupon_data.discount_type != null) {

            jsonObject.addProperty(
                "platform_discount_type",
                cartData.platform_coupon_data.platform_discount_type
            )
        } else {
            jsonObject.addProperty("platform_discount_type", "")
        }

        jsonObject.addProperty("e_money_amt", cartData.wallet_applied_cash)
        jsonObject.addProperty("address_id", addressId)
        jsonObject.addProperty("reward_id", "")
        jsonObject.addProperty("commission", 0)
        jsonObject.addProperty("reward_amt", "")
        jsonObject.addProperty("device_id", sessionManager.deviceId)
        jsonObject.addProperty("page_url", "https://dev-kangtao.vercel.app/account/checkout")
        jsonObject.addProperty("os_type", "app")
        jsonObject.addProperty("invite_coupon_id", "")
        jsonObject.addProperty("payment_type", 1)
        jsonObject.addProperty("total_amt", cartData.total_offer_cost.toString())
        jsonObject.addProperty("discount_amt", 0)
        jsonObject.addProperty("grand_total", cartData.grand_total.toString())

        val bundle = Bundle()
        bundle.putString("payLoad", jsonObject.toString())
        findNavController().navigate(R.id.paymentMethodFragment, bundle)
    }

    private fun showTermsAndConditions() {
        val appCompatDialog = AppCompatDialog(requireContext())
        val bindingDialog = InternationalTermsAndConditionsPopUpBinding.inflate(layoutInflater)
        appCompatDialog.setContentView(bindingDialog.root)
        appCompatDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        appCompatDialog.window!!.attributes.width = LinearLayout.LayoutParams.MATCH_PARENT
        appCompatDialog.setCancelable(false)


        bindingDialog.description.settings.javaScriptEnabled = true


        bindingDialog.description.loadDataWithBaseURL(null, goodsTermsAndConditions, "text/html", "UTF-8", null)
        bindingDialog.description.webViewClient = WebViewClient()
        bindingDialog.description.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                view!!.context.startActivity(Intent(Intent.ACTION_VIEW, request?.url))
                return true
            }
        }

        bindingDialog.decline.isVisible = true
        bindingDialog.accept.isVisible = true
        bindingDialog.agreeCheckBox.isVisible = true
        if(!appCompatDialog.isShowing) {
            appCompatDialog.show()
        }

        bindingDialog.close.setOnClickListener {
            appCompatDialog.dismiss()
        }

        bindingDialog.decline.setOnClickListener {
            appCompatDialog.dismiss()
        }

        bindingDialog.accept.setOnClickListener {
            if (bindingDialog.agreeCheckBox.isChecked) {
                placeOrder()
                appCompatDialog.dismiss()
            } else {
                showToast("Please check terms and conditions to proceed.")
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.back.id -> findNavController().popBackStack()
            binding.addVoucher.id -> showVoucherBottomSheet()
            binding.removeVoucher.id -> {
                mainViewModel.removeCoupon(
                    cartData.platform_coupon_data.coupon_id.toString(),
                    "platform"
                )
            }

            binding.placeOrder.id -> {
                if(this::cartData.isInitialized) {
                    if (cartData.shipping_customer_type == "international") {
                        showTermsAndConditions()
                    } else {
                        placeOrder()
                    }
                }
            }

            binding.manageAddress.id -> {
                val bundle = Bundle()
                bundle.putString("from", "checkOut")
                findNavController().navigate(R.id.manageAddressFragment, bundle)
            }
        }
    }
}