package com.hfm.customer.ui.fragments.cart

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView
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
import com.hfm.customer.utils.Loader
import com.hfm.customer.utils.NoInternetDialog
import com.hfm.customer.utils.Resource
import com.hfm.customer.utils.SessionManager
import com.hfm.customer.utils.deductFromWallet
import com.hfm.customer.utils.formatToTwoDecimalPlaces
import com.hfm.customer.utils.getDeviceId
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

    companion object{

    }

    private var platFormVoucherDiscount: Double = 0.0

    private var grandTotal: Double = 0.0
    private var changingVariant: Boolean = false
    private var variantId: String = ""

    private lateinit var cartData: CartData

    private var selectingVoucherSellerId: String = ""
    private var sellerSelectedVoucher: SellerVoucherModelItem? = null
    private var platFormSelectedVoucher: SellerVoucherModelItem? = null

    private var cartProductDeleted: Boolean = false
    private lateinit var platformVoucherDialog: BottomSheetDialog
    private lateinit var variantsDialog: BottomSheetDialog

    private var platformVouchers: SellerVoucherModel? = null
    private var sellerVouchers: SellerVoucherModel? = null


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

        mainViewModel.getCart(getDeviceId(requireContext()))
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
                    mainViewModel.getCart(getDeviceId(requireContext()))
                }

                is Resource.Loading -> Unit
                is Resource.Error -> apiError(response.message)
            }
        }

        mainViewModel.selectCart.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    mainViewModel.getCart(getDeviceId(requireContext()))
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
                    if (response.data?.httpcode == 200) {
                        binding.noDataFound.root.isVisible = false
                        setCartData(response.data.data)
                    } else {
                        binding.noDataFound.root.isVisible = true
                        binding.noDataFound.noDataLbl.text = "No Products Added.."
                        showToast(response.data?.message.toString())
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
                    appLoader.dismiss()
                    platformVouchers = response.data
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
                    sellerVouchers = response.data
                    showSellerVoucherBottomSheet()
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
                            mainViewModel.getCart(getDeviceId(requireContext()))
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
                        setSelectedCoupon(response.data.data.coupon)
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
                    if (response.data?.httpcode == 200) {
                        if (this::platformVoucherDialog.isInitialized) {
                            platformVoucherDialog.dismiss()
                        }
                        setSelectedSellerCoupon(response.data.data.coupon)
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
                        mainViewModel.getCart(getDeviceId(requireContext()))
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

    private fun setSelectedSellerCoupon(coupon: Coupon) {

        val index =
            cartData.seller_product.indexOfFirst { it.seller.seller_id.toString() == selectingVoucherSellerId }
        cartAdapter.setCouponApplied(index, coupon)

        cartData.seller_product[index].seller.coupon = coupon
        cartAdapter.differ.submitList(cartData.seller_product)
        setCartData(cartData)
        lifecycleScope.launch {
            delay(100)

            binding.scrollView.isVisible = false
            delay(100)
            binding.scrollView.isVisible = true
        }
        platformVoucherDialog.dismiss()

    }

    private fun setSelectedCoupon(coupon: Coupon) {
        with(binding) {

            voucherDetailsLayout.isVisible = true
            addVoucher.isVisible = false
            voucherName.text = coupon.offer


            if (coupon.offer_value_in == "percentage") {
                platFormVoucherDiscount =
                    (coupon.offer_value.toDouble() / 100) * binding.totalAmount.text.toString()
                        .toDouble()
                val finalAmount = formatToTwoDecimalPlaces(
                    binding.totalAmount.text.toString().toDouble() - platFormVoucherDiscount
                ).toDouble()
                binding.platformVoucher.text =
                    "- RM ${formatToTwoDecimalPlaces(platFormVoucherDiscount)}"
                binding.totalAmount.text = finalAmount.toString()
                voucherDescription.text =
                    "You saved additional RM ${formatToTwoDecimalPlaces(platFormVoucherDiscount)}"

            } else {
                platFormVoucherDiscount = coupon.offer_value_cal
                val finalAmount = formatToTwoDecimalPlaces(
                    binding.totalAmount.text.toString().toDouble() - coupon.offer_value.toDouble()
                ).toDouble()
                binding.platformVoucher.text =
                    "- RM ${formatToTwoDecimalPlaces(coupon.offer_value.toDouble())}"
                binding.totalAmount.text = finalAmount.toString()
                voucherDescription.text = "You saved additional RM ${coupon.offer_value_cal}"
            }

        }
    }

    private fun setCartData(data: CartData) {
        with(binding) {
            cartData = data
            voucherDetailsLayout.isVisible = false
            addVoucher.isVisible = true
            scrollView.isVisible = false

            initRecyclerView(requireContext(), binding.cartDataRv, cartAdapter)
            val animator: RecyclerView.ItemAnimator? = cartDataRv.itemAnimator
            if (animator is DefaultItemAnimator) {
                animator.supportsChangeAnimations = false
                cartDataRv.itemAnimator = null
            }

            cartAdapter.differ.submitList(data.seller_product)
            totalItems.text = "${data.cart_count} Items"
            selectAll.text = "Select All (${data.cart_count})"

            val productCount = data.seller_product.sumOf{ sellerProducts ->
                sellerProducts.seller.products.count { product ->
                    product.cart_selected.toString().toDouble() > 0
                }
            }

            if(productCount>0){
                subtotalLbl.text = "Subtotal(${productCount}) Items"
            }else{
                subtotalLbl.text = "Subtotal"
            }

            subtotal.text =
                "RM ${formatToTwoDecimalPlaces(data.total_offer_cost.toString().toDouble())}"
            shippingAmount.text =
                "RM ${formatToTwoDecimalPlaces(data.shipping_charges.toString().toDouble())}"
            grandTotal = data.grand_total.toString().toDouble()
            totalAmount.text = formatToTwoDecimalPlaces(grandTotal)


            if (data.wallet_balance != "false") {
                val pointToRM = data.wallet_balance.toDouble() / 100
                points.text = "${data.wallet_balance.toDouble().roundToInt()} Points (RM ${formatToTwoDecimalPlaces(pointToRM)})"
            } else {
                points.text = "0 Points (RM 0.00)"
                wallet.text = "RM 0.00"
            }


            val allCartSelectedIsOne = data.seller_product.all { it.seller.cart_selected == 1 }
            selectAll.isChecked = allCartSelectedIsOne

            lifecycleScope.launch {
                cartProductDeleted = false
                scrollView.isVisible = true
            }
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

        cartAdapter.setOnAppliedCoupon { status, amount ->
            binding.storeVoucher.text = "- RM $amount"
            val finalAmount = formatToTwoDecimalPlaces(grandTotal - amount.toDouble())
            binding.totalAmount.text = finalAmount


        }


        cartAdapter.setOnSellerRemoveCoupon { amount ->
            binding.totalAmount.text =
                formatToTwoDecimalPlaces(binding.totalAmount.text.toString().toDouble() + amount)
            binding.storeVoucher.text = "RM 0.00"
            lifecycleScope.launch {
                delay(100)
                binding.scrollView.isVisible = false
                delay(100)
                binding.scrollView.isVisible = true

            }
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
        platformVoucherDialog =
            BottomSheetDialog(requireActivity(), R.style.MyTransparentBottomSheetDialogTheme)
        platformVoucherDialog.setContentView(platformVoucherBinding.root)
        initRecyclerView(
            requireContext(),
            platformVoucherBinding.vouchersRv,
            platformVoucherAdapter
        )

        platformVoucherAdapter.setOnItemClickListener { position ->
            platFormSelectedVoucher = platformVouchers?.get(position)
        }

        with(platformVoucherBinding) {
            requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)

            voucherCode.setOnClickListener {
                val imm =
                    requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(voucherCode, InputMethodManager.SHOW_IMPLICIT)
            }

            apply.setOnClickListener {
                mainViewModel.applyPlatFormVouchers(platFormSelectedVoucher?.coupon_code ?: "")
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
        initRecyclerView(requireContext(), sellerVoucherBinding.vouchersRv, sellerVoucherAdapter)
        sellerVoucherAdapter.differ.submitList(sellerVouchers)
        sellerVoucherAdapter.setOnItemClickListener { position ->
            sellerSelectedVoucher = sellerVouchers?.get(position)
        }

        with(sellerVoucherBinding) {
            requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)

            voucherCode.setOnClickListener {
                val imm =
                    requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(voucherCode, InputMethodManager.SHOW_IMPLICIT)
            }

            apply.setOnClickListener {
                val sellerData =
                    cartData.seller_product.find { it.seller.seller_id.toString() == selectingVoucherSellerId }


                val sellerCartAmount = sellerData?.seller?.products
                    ?.filter { it.cart_selected.toString().toDouble() > 0 }
                    ?.sumOf { it.total_discount_price.toString().toDouble() } ?: 0.0

                try {
                    if (sellerCartAmount < sellerSelectedVoucher?.minimum_purchase?.toDouble()!!) {
                        showToast("Voucher cannot be applied on the amount..")
                        return@setOnClickListener
                    }
                } catch (_: Exception) {
                }

                mainViewModel.applySellerVouchers(
                    selectingVoucherSellerId,
                    sellerSelectedVoucher?.coupon_code ?: ""
                )
            }
            cancel.setOnClickListener { platformVoucherDialog.dismiss() }
        }

        platformVoucherDialog.show()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.back.id -> findNavController().popBackStack()
            binding.checkOut.id -> {
                var someSelectedProductNotDeliverable = false
                var someSelectedProductOutOfStock = false
                cartAdapter.differ.currentList.forEach {
                    it.seller.products.forEach {
                        if (it.cart_selected.toString().toDouble() > 0) {
                            if(it.check_shipping_availability.toString().toDouble() < 1) {
                                someSelectedProductNotDeliverable =
                                    it.check_shipping_availability.toString().toDouble() < 1
                            }
                            if(it.is_out_of_stock) {
                                someSelectedProductOutOfStock = it.is_out_of_stock
                            }
                        }
                    }
                }

                if (someSelectedProductNotDeliverable) {
                    showToast("Some selected products is not available in your Delivery Address.")
                } else if(someSelectedProductOutOfStock){
                    showToast("Some selected products is Sold Out.")

                }else {
                    findNavController().navigate(R.id.checkOutFragment)
                }
            }

            binding.addVoucher.id -> showVoucherBottomSheet()
            binding.removeVoucher.id -> {
                binding.voucherDetailsLayout.isVisible = false
                binding.addVoucher.isVisible = true
                binding.platformVoucher.text = "RM 0.00"
                binding.totalAmount.text = formatToTwoDecimalPlaces(
                    binding.totalAmount.text.toString().toDouble() + platFormVoucherDiscount
                )
            }

            binding.selectAll.id -> {
                var cartIds = ""
                cartData.seller_product.forEach {
                    it.seller.products.forEach {
                        cartIds = "$cartIds,${it.cart_id}"
                    }
                }
                mainViewModel.selectCart(cartIds, if (binding.selectAll.isChecked) 1 else 0)
            }
        }
    }
}