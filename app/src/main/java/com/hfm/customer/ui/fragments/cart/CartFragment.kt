package com.hfm.customer.ui.fragments.cart

import android.annotation.SuppressLint
import android.app.ActionBar
import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.hfm.customer.R
import com.hfm.customer.databinding.BottomSheetVariantsBinding
import com.hfm.customer.databinding.BottomSheetVoucherBinding
import com.hfm.customer.databinding.FragmentCartBinding
import com.hfm.customer.ui.fragments.cart.adapter.CartAdapter
import com.hfm.customer.ui.fragments.cart.adapter.PlatformVoucherAdapter
import com.hfm.customer.ui.fragments.cart.model.CartData
import com.hfm.customer.ui.fragments.cart.model.Coupon
import com.hfm.customer.ui.fragments.products.productDetails.adapter.ProductVariantsAdapter
import com.hfm.customer.ui.fragments.products.productDetails.model.SellerVoucherModel
import com.hfm.customer.ui.fragments.products.productDetails.model.SellerVoucherModelItem
import com.hfm.customer.ui.fragments.products.productDetails.model.Variants
import com.hfm.customer.ui.loginSignUp.LoginActivity
import com.hfm.customer.utils.Loader
import com.hfm.customer.utils.NoInternetDialog
import com.hfm.customer.utils.Resource
import com.hfm.customer.utils.SessionManager
import com.hfm.customer.utils.cartCount
import com.hfm.customer.utils.deductFromWallet
import com.hfm.customer.utils.formatToTwoDecimalPlaces
import com.hfm.customer.utils.initRecyclerView
import com.hfm.customer.utils.netWorkFailure
import com.hfm.customer.utils.showToast
import com.hfm.customer.viewModel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt


@AndroidEntryPoint
class CartFragment : Fragment(), View.OnClickListener {

    companion object {

    }

    private var updateFrom: Boolean = false

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

        mainViewModel.getCart()
        mainViewModel.getPlatFormVouchers(1)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setObserver()
    }


    private fun setObserver() {
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
//                    appLoader.dismiss()
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
                    if (response.data?.httpcode == 200) {
                        if (this::platformVoucherDialog.isInitialized) {
                            platformVoucherDialog.dismiss()
                        }
                        mainViewModel.getCart()
                        showToast("Coupon Applied")
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
                    if (this::platformVoucherDialog.isInitialized) {
                        platformVoucherDialog.dismiss()
                    }
                    if (response.data?.httpcode == 200) {

                        mainViewModel.getCart()
//                        setSelectedSellerCoupon(response.data.data.coupon)
                    } else {
                        showToast(response.data?.message.toString())
                    }
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
                        mainViewModel.getCart()
                    } else {
                        showToast(response.data?.message.toString())
                    }
                }

                is Resource.Loading -> appLoader.show()
                is Resource.Error ->
                    apiError(response.message)
            }
        }


        mainViewModel.updateCartCount.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    if (response.data?.httpcode == 200) {
                        updateFrom = true
                        mainViewModel.getCart()
                    } else {
                        showToast(response.data?.message.toString())
                    }
                }

                is Resource.Loading -> appLoader.show()
                is Resource.Error ->
                    apiError(response.message)
            }
        }
    }


    private fun setCartData(data: CartData) {
        with(binding) {
            cartData = data
            voucherDetailsLayout.isVisible = cartData.is_platform_coupon_applied == 1
            addVoucher.isVisible = cartData.is_platform_coupon_applied == 0
            if (!updateFrom) {
                scrollView.isVisible = false
            }


            initRecyclerView(requireContext(), binding.cartDataRv, cartAdapter)
            val animator: RecyclerView.ItemAnimator? = cartDataRv.itemAnimator
            if (animator is DefaultItemAnimator) {
                animator.supportsChangeAnimations = false
                cartDataRv.itemAnimator = null
            }

            cartAdapter.differ.submitList(data.seller_product)
            totalItems.text = "${data.cart_count} Items"
            selectAll.text = "Select All (${data.cart_count})"

            val productCount = data.seller_product.sumOf { sellerProducts ->
                sellerProducts.seller.products.count { product ->
                    product.cart_selected.toString().toDouble() > 0
                }
            }

            if (productCount > 0) {
                subtotalLbl.text = "Subtotal(${productCount}) Items"
            } else {
                subtotalLbl.text = "Subtotal"
            }

            subtotal.text = "RM ${formatToTwoDecimalPlaces(data.total_offer_cost.toString().toDouble())}"
            shippingAmount.text = "RM ${formatToTwoDecimalPlaces(data.shipping_charges.toString().toDouble())}"
            grandTotal = data.grand_total.toString().toDouble()
            totalAmount.text = formatToTwoDecimalPlaces(grandTotal)

            if(data.total_offer_cost.toString().toDouble()>0){

            }

            if (cartData.seller_voucher_amt > 0) {
                storeVoucher.text = "- RM ${cartData.seller_voucher_amt}"
            } else {
                storeVoucher.text = "RM 0.00"
            }

            if (cartData.platform_voucher_amt > 0) {
                platformVoucher.text = "- RM ${cartData.platform_voucher_amt}"
            } else {
                platformVoucher.text = "RM 0.00"
            }


            if (data.wallet_balance != "false") {
                val pointToRM = data.wallet_balance.toDouble() / 100
                points.text = "${data.wallet_balance.toDouble().roundToInt()} (RM ${
                    formatToTwoDecimalPlaces(pointToRM)
                })"
            } else {
                points.text = "0 (RM 0.00)"
                wallet.text = "RM 0.00"
            }


            val allCartSelectedIsOne = data.seller_product.all { it.seller.cart_selected == 1 }
            selectAll.isChecked = allCartSelectedIsOne

            voucherName.text = cartData.platform_coupon_data.title
            voucherDescription.text =
                "You saved additional RM ${formatToTwoDecimalPlaces(cartData.platform_coupon_data.ofr_amount.toDouble())}"

            lifecycleScope.launch {
                delay(100)
                cartProductDeleted = false
                scrollView.isVisible = true
            }
            updateFrom = false
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
        }

        binding.useWalletPoints.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                if (cartData.wallet_balance != "false") {
                    val walletUpdate = deductFromWallet(
                        cartData.wallet_balance.toDouble(),
                        grandTotal
                    )
                    binding.wallet.text =
                        "- RM ${formatToTwoDecimalPlaces(cartData.wallet_balance.toDouble() - walletUpdate.first)}"
                    binding.totalAmount.text = formatToTwoDecimalPlaces(walletUpdate.second)
                } else {
                    showToast("There are no wallet points to use")
                }
            } else {
                binding.wallet.text = "RM 0.00"
                binding.totalAmount.text =
                    formatToTwoDecimalPlaces(grandTotal)
            }
        }


        cartAdapter.setOnVariantClick { variants, cartId ->
            showVariantsBottomSheet(variants, cartId)
        }

        cartAdapter.setOnAppliedCoupon { _, amount ->
            binding.storeVoucher.text = "- RM $amount"
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
                showToast("please select any product to apply the voucher")
                return@setOnShopVoucherClickListener
            }
            selectingVoucherSellerId = sellerId
            mainViewModel.getSellerVouchers(sellerId)
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
        platformVoucherDialog = BottomSheetDialog(requireActivity(), R.style.MyTransparentBottomSheetDialogTheme)
        platformVoucherDialog.setContentView(platformVoucherBinding.root)
        val bottomSheet: FrameLayout? = platformVoucherDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet)
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
                val imm =
                    requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(voucherCode, InputMethodManager.SHOW_IMPLICIT)
            }

            applyVoucher.setOnClickListener {
                val couponCode = voucherCode.text.toString()
                if (couponCode.isEmpty()) {
                    showToast("Please enter a coupon code to apply")
                }

                mainViewModel.applyPlatFormVouchers(
                    couponCode,
                    cartData.total_offer_cost.toString()
                )
            }
            apply.setOnClickListener {
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

        sellerVoucherAdapter.differ.submitList(sellerVouchers)
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
                val imm =
                    requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(voucherCode, InputMethodManager.SHOW_IMPLICIT)
            }

            applyVoucher.setOnClickListener {
                val sellerData =
                    cartData.seller_product.find { it.seller.seller_id.toString() == selectingVoucherSellerId }
                val couponCode = voucherCode.text.toString()
                if (couponCode.isEmpty()) {
                    showToast("Please enter a coupon code to apply")
                }

                mainViewModel.applySellerVouchers(
                    selectingVoucherSellerId,
                    couponCode,
                    sellerData?.seller_subtotal ?: 0.0
                )
            }

            apply.setOnClickListener {
                val sellerData =
                    cartData.seller_product.find { it.seller.seller_id.toString() == selectingVoucherSellerId }

                mainViewModel.applySellerVouchers(
                    selectingVoucherSellerId,
                    sellerSelectedVoucher?.couponCode ?: "", sellerData?.seller_subtotal ?: 0.0
                )
            }
            cancel.setOnClickListener { platformVoucherDialog.dismiss() }
        }
        lifecycleScope.launch {
            delay(500)
            sellerVoucherBinding.voucherCode.clearFocus()
        }
        platformVoucherDialog.show()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.back.id -> findNavController().popBackStack()
            binding.checkOut.id -> {

                val selectedAnyProduct =
                    cartAdapter.differ.currentList.any { seller ->
                        seller.seller.products.any { product ->
                            product.cart_selected.toString().toDouble() > 0
                        }
                    }

                /*  val someSelectedProductNotDeliverable =
                      cartAdapter.differ.currentList.any { seller ->
                          seller.seller.products.any { product ->
                              product.cart_selected.toString()
                                  .toDouble() > 0 && product.check_shipping_availability.toString()
                                  .toDouble() < 1
                          }
                      }*/

                val notDeliverableProduct = cartAdapter.differ.currentList
                    .flatMap { seller ->
                        seller.seller.products
                            .filter { product ->
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

                val someSelectedProductOutOfStock: Boolean =
                    cartAdapter.differ.currentList.filter { it.seller.cart_selected == 1 }
                        .any { seller ->
                            seller.seller.products.any { product ->
                                product.is_out_of_stock == 1
                            }
                        }

                if (someSelectedProductOutOfStock) {
                    showToast("Some products is Sold Out.")

                } else if (!selectedAnyProduct) {
                    showToast("Please select any product to checkout.")

                } else {
                    findNavController().navigate(R.id.checkOutFragment)
                }
            }

            binding.addVoucher.id -> showVoucherBottomSheet()
            binding.deleteAll.id -> {
                var cartIds = ""
                cartData.seller_product.forEach {
                    it.seller.products.forEach { product ->
                        cartIds = if (cartIds.isEmpty()) {
                            product.cart_id.toString().toDouble().roundToInt().toString()
                        } else {
                            "$cartIds,${product.cart_id.toString().toDouble().roundToInt()}"
                        }
                    }
                }

                mainViewModel.deleterCartProduct(cartIds)
            }

            binding.removeVoucher.id -> mainViewModel.removeCoupon(
                cartData.platform_coupon_data.coupon_id.toString(),
                "platform"
            )

            binding.selectAll.id -> {
                var cartIds = ""
                cartData.seller_product.forEach {
                    it.seller.products.forEach {
                        cartIds = "$cartIds,${it.cart_id.toString().toDouble().roundToInt()}"
                    }
                }
                mainViewModel.selectCart(cartIds, if (binding.selectAll.isChecked) 1 else 0)
            }
        }
    }
    override fun onPause() {
        super.onPause()
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)

    }
}