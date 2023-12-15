package com.hfm.customer.ui.fragments.cart

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
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
import com.hfm.customer.R
import com.hfm.customer.databinding.BottomSheetVariantsBinding
import com.hfm.customer.databinding.BottomSheetVoucherBinding
import com.hfm.customer.databinding.FragmentCartBinding
import com.hfm.customer.ui.fragments.address.model.Address
import com.hfm.customer.ui.fragments.cart.adapter.CartAdapter
import com.hfm.customer.ui.fragments.cart.adapter.PlatformVoucherAdapter
import com.hfm.customer.ui.fragments.cart.model.CartData
import com.hfm.customer.ui.fragments.cart.model.Coupon
import com.hfm.customer.ui.fragments.products.productDetails.adapter.ProductVariantsAdapter
import com.hfm.customer.ui.fragments.products.productDetails.model.Variants
import com.hfm.customer.ui.loginSignUp.LoginActivity
import com.hfm.customer.utils.Loader
import com.hfm.customer.utils.NoInternetDialog
import com.hfm.customer.utils.Resource
import com.hfm.customer.utils.SessionManager
import com.hfm.customer.utils.cartCount
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

@SuppressLint("SetTextI18n")
@AndroidEntryPoint
class CartFragment : Fragment(), View.OnClickListener {

    private var grandTotal: Double = 0.0
    private var changingVariant: Boolean = false
    private var variantId: String = ""

    private lateinit var cartData: CartData

    private var selectingVoucherSellerId: String = ""
    private var sellerSelectedVoucher: Coupon? = null
    private var platFormSelectedVoucher: Coupon? = null

    private var cartProductDeleted: Boolean = false
    private lateinit var platformVoucherDialog: BottomSheetDialog
    private lateinit var variantsDialog: BottomSheetDialog

    private var platformVouchers: List<Coupon> = ArrayList()
    private var sellerVouchers: List<Coupon> = ArrayList()
    private var addressId: Int = 0
    private var addressList: List<Address> = ArrayList()


    private lateinit var binding: FragmentCartBinding
    private var currentView: View? = null

    @Inject
    lateinit var sessionManager: SessionManager
    private val mainViewModel: MainViewModel by viewModels()
    private lateinit var appLoader: Loader
    private lateinit var noInternetDialog: NoInternetDialog

    @Inject
    lateinit var cartAdapter: CartAdapter

    @Inject
    lateinit var variantsAdapter: ProductVariantsAdapter

    @Inject
    lateinit var platformVoucherAdapter: PlatformVoucherAdapter

    @Inject
    lateinit var sellerVoucherAdapter: PlatformVoucherAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)

        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_cart, container, false)
            currentView = inflater.inflate(R.layout.fragment_cart, container, false)
            binding = FragmentCartBinding.bind(currentView!!)
            init()
            setOnClickListener()
        }
        return currentView!!
    }

    private fun init() {
        appLoader = Loader(requireContext())
        noInternetDialog = NoInternetDialog(requireContext())
        binding.loader.isVisible = true
        noInternetDialog.setOnDismissListener {
            init()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setObserver()
        mainViewModel.getAddress()
        mainViewModel.getCart()
        mainViewModel.getPlatFormVouchers(1)


        lifecycleScope.launch {
            /*delay(500)
            findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Int>("addressId")
                ?.observe(viewLifecycleOwner) {
                    addressId = it ?: 0
                }

            if (addressId >= 0) {
                val address = addressList.find { it.id == addressId }
                address?.let {
                    setAddress(it)
                    mainViewModel.getCart()
                }
            }*/
        }
    }

    private fun setObserver() {
        mainViewModel.address.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    if (response.data?.httpcode == "200") {
                        addressList = response.data.data.address_list
                        binding.manageAddressGroup.isVisible = addressList.isNotEmpty()
                        binding.addAddressGroup.isVisible = addressList.isEmpty()
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
        mainViewModel.addToCart.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    variantsDialog.dismiss()
                    mainViewModel.getCart()
                }

                is Resource.Loading -> Unit
                is Resource.Error -> apiError(response.message)
            }
        }

        mainViewModel.selectCart.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    mainViewModel.getCart()
                }

                is Resource.Loading -> Unit
                is Resource.Error -> apiError(response.message)
            }
        }

        mainViewModel.cart.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    binding.loader.isVisible = false

                    when (response.data?.httpcode) {
                        200 -> {
                            binding.noDataFound.root.isVisible = false
                            setCartData(response.data.data)
                            cartCount.postValue(response.data.data.cart_count)
                        }

                        401 -> {
                            sessionManager.isLogin = false
                            startActivity(Intent(requireActivity(), LoginActivity::class.java))
                            requireActivity().finish()
                        }

                        else -> {
                            cartCount.postValue(0)
                            binding.noDataFound.root.isVisible = true
                            binding.noDataFound.noDataLbl.text = "No Products Added.."
                            showToast(response.data?.message.toString())
                        }
                    }
                }

                is Resource.Loading -> appLoader.show()
                is Resource.Error ->
                    apiError(response.message)
            }
        }

        mainViewModel.platformVouchers.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    if (response.data?.httpcode == 200) {
                        platformVouchers = response.data.data.coupon_list
                    }
                }

                is Resource.Loading -> appLoader.show()
                is Resource.Error ->
                    apiError(response.message)
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
                is Resource.Error ->
                    apiError(response.message)
            }
        }

        mainViewModel.deleterCartProduct.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    if (response.data?.httpcode == 200) {
                        cartCount.postValue(response.data.data.cart_count)
                        cartProductDeleted = true
                        if (changingVariant) {
                            mainViewModel.addToCart(variantId, "1")
                        } else {
                            changingVariant = false
                            mainViewModel.getCart()
                        }
                    } else {
                        showToast(response.data?.message.toString())
                    }
                }

                is Resource.Loading -> appLoader.show()
                is Resource.Error ->
                    apiError(response.message)
            }
        }

        mainViewModel.applyPlatformVoucher.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    if (this::platformVoucherDialog.isInitialized) {
                        platformVoucherDialog.dismiss()
                    }
                    when (response.data?.httpcode) {
                        200 -> {
                            if(response.data.status == "success"){
                                mainViewModel.getCart()
                                showToast("Voucher Applied")
                            }else{
                                showToastLong(response.data.message.toString())
                            }

                        }

                        401 -> {}
                        else -> {
                            showToast(response.data?.message.toString())
                        }
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
                    if (this::platformVoucherDialog.isInitialized) {
                        platformVoucherDialog.dismiss()
                    }

                    when (response.data?.httpcode) {
                        200 -> {
                            if(response.data.status == "success"){
                                mainViewModel.getCart()
                                showToast("Voucher Applied")
                            }else{
                                showToastLong(response.data.message.toString())
                            }
                        }


                        401 -> {}
                        else -> {
                            showToast(response.data?.message.toString())
                        }
                    }
                }

                is Resource.Loading -> appLoader.show()
                is Resource.Error -> apiError(response.message)
            }
        }
        mainViewModel.removeCoupon.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    if (response.data?.httpcode == 200) {
                        mainViewModel.getCart()
                    } else {
                        showToast(response.data?.message.toString())
                    }
                }

                is Resource.Loading -> appLoader.show()
                is Resource.Error -> apiError(response.message)
            }
        }


        mainViewModel.updateCartCount.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    when (response.data?.httpcode) {
                        200 -> {
                            mainViewModel.getCart()
                        }

                        401 -> {}
                        else -> {
                            showToast(response.data?.message.toString())
                        }
                    }
                }

                is Resource.Loading -> appLoader.show()
                is Resource.Error -> apiError(response.message)
            }
        }
    }

    private fun setAddress(address: Address) {

        with(binding) {
            addressId = address.id
            customerName.text = address.name.toString()
            customerAddress.text =
                "${address.house},${address.street},\n${address.city}, ${address.state}, ${address.country}, ${address.pincode}"
            customerMobile.text = address.phone.toString()
        }
    }

    private fun setCartData(data: CartData) {
        binding.toolBarTitle.setOnClickListener {
            binding.root.postInvalidate()
        }

        if (data.wallet_text.isNotEmpty()) {
            showToastLong(data.wallet_text)
        }

        if (data.platform_coupon_text.isNotEmpty()) {
            showToastLong(data.platform_coupon_text)
        }





        with(binding) {
            cartData = data
            voucherDetailsLayout.isVisible = cartData.is_platform_coupon_applied == 1
            addVoucher.isVisible = cartData.is_platform_coupon_applied == 0
            binding.cartDataRv.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = cartAdapter
            }

            val animator: RecyclerView.ItemAnimator? = cartDataRv.itemAnimator
            if (animator is DefaultItemAnimator) {
                animator.supportsChangeAnimations = false
                cartDataRv.itemAnimator = null
            }
            cartAdapter.setActivity(requireActivity())
            cartAdapter.differ.submitList(cartData.seller_product)
            cartAdapter.notifyDataSetChanged()
            totalItems.text = "${cartData.cart_count} Items"
            selectAll.text = "Select All (${cartData.cart_count})"

            val productCount = cartData.seller_product.sumOf { sellerProducts ->
                sellerProducts.seller.products.count { product ->
                    product.cart_selected.toString().toDouble() > 0
                }
            }

            if (productCount > 0) {
                subtotalLbl.text = "Subtotal(${productCount} Items)"
            } else {
                subtotalLbl.text = "Subtotal"
            }

            subtotal.text = "RM ${formatToTwoDecimalPlaces(cartData.total_offer_cost.toString().toDouble())}"
            shippingAmount.text = "RM ${formatToTwoDecimalPlaces(cartData.shipping_charges.toString().toDouble())}"
            grandTotal = cartData.grand_total.toString().toDouble()
            totalAmount.text = formatToTwoDecimalPlaces(grandTotal)

            if (cartData.shipping_discount > 0) {
                shippingDiscount.text =
                    "RM -${formatToTwoDecimalPlaces(cartData.shipping_discount)}"
            } else {
                shippingDiscount.text = "RM 0.00"
            }

            if (cartData.seller_voucher_amt > 0) {
                storeVoucher.text = "RM -${formatToTwoDecimalPlaces(cartData.seller_voucher_amt)}"
            } else {
                storeVoucher.text = "RM 0.00"
            }

            if (cartData.platform_voucher_amt > 0) {
                platformVoucher.text = "RM -${cartData.platform_voucher_amt}"
            } else {
                platformVoucher.text = "RM 0.00"
            }

            if (cartData.wallet_balance != "false") {
                points.text = "${cartData.wallet_balance.toDouble()
                } (RM ${formatToTwoDecimalPlaces(cartData.wallet_cash.toDouble())})"
            } else {
                points.text = "0 (RM 0.00)"
                wallet.text = "RM 0.00"
            }

            useWalletPoints.isChecked = cartData.is_wallet_applied == 1
            if (useWalletPoints.isChecked) {
                wallet.text =
                    "RM -${formatToTwoDecimalPlaces(cartData.wallet_applied_cash.toDouble())}"
            } else {
                wallet.text = "RM 0.00"
            }

            val allCartSelectedIsOne = cartData.seller_product.all { it.seller.cart_selected == 1 }
            selectAll.isChecked = allCartSelectedIsOne

            voucherName.text = cartData.platform_coupon_data.title
            if (data.platform_coupon_data.is_free_shipping == 1) {
                voucherDescription.text = "Free shipping Voucher applied"
            } else {
                voucherDescription.text = "You saved additional RM ${formatToTwoDecimalPlaces(data.platform_coupon_data.platform_discount_amount)}"
            }

            binding.scrollView.invalidate()
            binding.cartDataRv.invalidate()
            binding.root.invalidate()
        }
    }

    private fun apiError(message: String?) {
        appLoader.dismiss()
        showToast(message.toString())
        if (message == netWorkFailure) {
            noInternetDialog.show()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setOnClickListener() {
        with(binding) {
            back.setOnClickListener(this@CartFragment)
            checkOut.setOnClickListener(this@CartFragment)
            addVoucher.setOnClickListener(this@CartFragment)
            removeVoucher.setOnClickListener(this@CartFragment)
            deleteAll.setOnClickListener(this@CartFragment)
            selectAll.setOnClickListener(this@CartFragment)
            manageAddress.setOnClickListener(this@CartFragment)
            addNewAddress.setOnClickListener(this@CartFragment)
        }

        binding.useWalletPoints.setOnClickListener {
            if (binding.useWalletPoints.isChecked) {
                if (cartData.wallet_cash.toDouble() > cartData.grand_total.toString().toDouble()) {
                    mainViewModel.applyWallet(
                        (cartData.grand_total.toString().toDouble() * 100).toString()
                    )
                } else {
                    mainViewModel.applyWallet(cartData.wallet_balance)
                }

            } else {
                mainViewModel.removeWallet()
            }
        }


        cartAdapter.setOnVariantClick { variants, cartId ->
            showVariantsBottomSheet(variants, cartId)
        }

        cartAdapter.setOnAppliedCoupon { _, amount ->
            binding.storeVoucher.text = "RM -$amount"
            val finalAmount = formatToTwoDecimalPlaces(grandTotal - amount.toDouble())
            binding.totalAmount.text = finalAmount
        }


        cartAdapter.setOnSellerRemoveCoupon { couponId ->
            mainViewModel.removeCoupon(couponId = couponId.toString(), "seller")
        }

        cartAdapter.setOnCartSelectionClickListener { cartId, status ->
            mainViewModel.selectCart(cartId, if (status) 1 else 0)
        }

        cartAdapter.setOnDeleteClickListener { cartId ->
            mainViewModel.deleterCartProduct(cartId)
        }

        cartAdapter.setOnDeleteClickListener { cartId ->
            mainViewModel.deleterCartProduct(cartId)
        }

        cartAdapter.setOnStoreClicked { sellerId ->
            val bundle = Bundle()
            bundle.putString("storeId", sellerId.toString())
            findNavController().navigate(R.id.storeFragment, bundle)
        }

        cartAdapter.setOnQtyChangeListener { cartId, qty ->
            if (qty.toInt() <= 0) {
                mainViewModel.deleterCartProduct(cartId)
            } else {
                mainViewModel.updateCartQty(cartId, qty = qty.toInt())
            }
        }

        cartAdapter.setOnShopVoucherClickListener { sellerId ->
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
    }

    private fun showVariantsBottomSheet(variants: List<Variants>, cartId: String) {

        val variantsBinding = BottomSheetVariantsBinding.inflate(layoutInflater)
        variantsDialog =
            BottomSheetDialog(requireActivity(), R.style.MyTransparentBottomSheetDialogTheme)
        variantsDialog.setContentView(variantsBinding.root)
        initRecyclerView(requireContext(), variantsBinding.variantsRv, variantsAdapter, true)
        variantsAdapter.differ.submitList(variants)

        variantsAdapter.setOnVariantsClickListener { position ->
            variantId = variants[position].pro_id.toString()
        }

        with(variantsBinding) {
            confirm.setOnClickListener {
                changingVariant = true
                mainViewModel.deleterCartProduct(cartId)
            }
            cancel.setOnClickListener { variantsDialog.dismiss() }
        }

        variantsDialog.show()
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
                    return@setOnClickListener
                }

                mainViewModel.applyPlatFormVouchers(
                    couponCode,
                    cartData.total_offer_cost.toString()
                )
            }

            apply.setOnClickListener {
                if (platFormSelectedVoucher?.couponCode.isNullOrEmpty()) {
                    showToast("Please enter a coupon code or select to apply")
                    return@setOnClickListener
                }
                mainViewModel.applyPlatFormVouchers(
                    platFormSelectedVoucher?.couponCode ?: "", cartData.total_offer_cost.toString()
                )
            }

            cancel.setOnClickListener { platformVoucherDialog.dismiss() }
        }

        platformVoucherAdapter.differ.submitList(platformVouchers)

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
            titleLbl.text = "Select Shop Vouchers"

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
                    sellerData?.seller_subtotal ?: 0.0,
                    sellerData?.shipping ?: 0.0
                )
            }



            apply.setOnClickListener {
                val sellerData =
                    cartData.seller_product.find { it.seller.seller_id.toString() == selectingVoucherSellerId }
                val couponCode = sellerSelectedVoucher?.couponCode.toString()

                if (couponCode.isEmpty()) {
                    showToast("Please enter a coupon code or select to apply")
                    return@setOnClickListener
                }

                mainViewModel.applySellerVouchers(
                    selectingVoucherSellerId,
                    couponCode,
                    sellerData?.seller_subtotal ?: 0.0, sellerData?.shipping ?: 0.0
                )
            }
            cancel.setOnClickListener { platformVoucherDialog.dismiss() }
        }
        sellerVoucherAdapter.differ.submitList(sellerVouchers)

        platformVoucherDialog.show()
    }

    private fun validateAndCheckOut() {
        val selectedAnyProduct = cartAdapter.differ.currentList.any { seller ->
            seller.seller.products.any { product ->
                product.cart_selected.toString().toDouble() > 0
            }
        }

        val notDeliverableProduct = cartAdapter.differ.currentList
            .flatMap { seller ->
                seller.seller.products.filter { product ->
                    product.cart_selected.toString()
                        .toDouble() > 0 && product.check_shipping_availability.toString()
                        .toDouble() < 1
                }
            }
            .firstOrNull { it.check_shipping_availability.toString().toDouble() == 0.0 }

        if (notDeliverableProduct != null) {
            showToast(notDeliverableProduct.check_shipping_availability_text)
            return
        }

        val someSelectedProductOutOfStock = cartAdapter.differ.currentList
            .any { seller ->
                seller.seller.products.any { product ->
                    product.is_out_of_stock == 1 && seller.seller.cart_selected == 1
                }
            }

        if (someSelectedProductOutOfStock) {
            showToast("Some products are Sold Out.")
        } else if (!selectedAnyProduct) {
            showToast("Please select any product to checkout.")
        } else {
            findNavController().navigate(R.id.checkOutFragment)
        }
    }

    private fun deleteCart() {
        val cartIds = cartData.seller_product.flatMap { it.seller.products }
            .filter { it.cart_selected.toString().toDouble() > 0 || it.is_out_of_stock > 0 }
            .map { it.cart_id.toString().toDouble().roundToInt() }
            .joinToString(",")
        if (cartIds.isEmpty()) {
            showToast("Please select the product")
        } else {
            mainViewModel.deleterCartProduct(cartIds)
        }
    }

    private fun selectCart() {
        val cartIds = cartData.seller_product.flatMap { it.seller.products }
            .joinToString(",") { it.cart_id.toString().toDouble().roundToInt().toString() }
        val selectAll = if (binding.selectAll.isChecked) 1 else 0
        mainViewModel.selectCart(cartIds, selectAll)
    }

    private fun removePlatformVoucher() {
        mainViewModel.removeCoupon(cartData.platform_coupon_data.coupon_id.toString(), "platform")
    }

    private fun manageAddress() {
        val bundle = Bundle()
        bundle.putString("from", "checkOut")
        findNavController().navigate(R.id.manageAddressFragment, bundle)
    }

    private fun addNewAddress() {
        val bundle = Bundle()
        bundle.putString("from", "cart")
        findNavController().navigate(R.id.addNewAddressFragment, bundle)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.back.id -> findNavController().popBackStack()
            binding.checkOut.id -> validateAndCheckOut()
            binding.addVoucher.id -> showVoucherBottomSheet()
            binding.deleteAll.id -> deleteCart()
            binding.removeVoucher.id -> removePlatformVoucher()
            binding.selectAll.id -> selectCart()
            binding.manageAddress.id -> manageAddress()
            binding.addNewAddress.id -> addNewAddress()
        }
    }


    override fun onPause() {
        super.onPause()
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
    }
}