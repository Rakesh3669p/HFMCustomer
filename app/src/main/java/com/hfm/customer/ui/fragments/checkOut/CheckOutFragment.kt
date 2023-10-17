package com.hfm.customer.ui.fragments.checkOut

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.JsonObject
import com.hfm.customer.R
import com.hfm.customer.databinding.BottomSheetStoreVoucherBinding
import com.hfm.customer.databinding.BottomSheetVoucherBinding
import com.hfm.customer.databinding.FragmentCheckoutBinding
import com.hfm.customer.ui.fragments.address.model.Address
import com.hfm.customer.ui.fragments.cart.model.CartData
import com.hfm.customer.ui.fragments.cart.model.SellerProduct
import com.hfm.customer.ui.fragments.checkOut.adapter.CheckOutStoreAdapter
import com.hfm.customer.ui.fragments.checkOut.model.CheckOutData
import com.hfm.customer.ui.fragments.checkOut.model.ShippingOption
import com.hfm.customer.ui.fragments.products.productDetails.adapter.ReviewsAdapter
import com.hfm.customer.utils.Loader
import com.hfm.customer.utils.NoInternetDialog
import com.hfm.customer.utils.Resource
import com.hfm.customer.utils.SessionManager
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
class CheckOutFragment : Fragment() , View.OnClickListener {

    private lateinit var cartData: CartData

    private var grandTotal: Double = 0.0

    private var shippingOptions: List<ShippingOption> = ArrayList()

    private var addressId: Int = 0
    private var addressList: List<Address> = ArrayList()
    private lateinit var binding: FragmentCheckoutBinding
    private var currentView: View? = null

    @Inject lateinit var reviewsAdapter: ReviewsAdapter

    private lateinit var appLoader: Loader
    private lateinit var noInternetDialog: NoInternetDialog
    private val mainViewModel:MainViewModel by viewModels()

    @Inject lateinit var checkOutAdapter: CheckOutStoreAdapter
    @Inject lateinit var sessionManager: SessionManager

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
                    addressId = it?:0
                }

            if(addressId>=0){
                val address = addressList.find { it.id == addressId }
                address?.let { setAddress(it) }
            }
        }
    }
    private fun init() {
        appLoader = Loader(requireContext())
        noInternetDialog = NoInternetDialog(requireContext())
        binding.loader.isVisible = true
        mainViewModel.getAddress()
        mainViewModel.getCheckoutInfo()
    }



    private fun setObserver() {
        mainViewModel.address.observe(viewLifecycleOwner){response->
            when(response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    if(response.data?.httpcode == "200"){
                        addressList = response.data.data.address_list
                        response.data.data.address_list.forEach {
                            if(it.is_default == 1){
                                setAddress(it)
                            }
                        }
                    }
                }
                is Resource.Loading -> appLoader.dismiss()
                is Resource.Error -> apiError(response.message)
            }
        }

        mainViewModel.checkOutInfo.observe(viewLifecycleOwner){response->
            when(response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    if(response.data?.httpcode == 200){
                        mainViewModel.getCart(getDeviceId(requireContext()))
                        shippingOptions = response.data.data.shipping_options
                        setCheckOutData(response.data.data)
                    }else{
                        showToast(response.data?.message.toString())
                    }
                }
                is Resource.Loading -> appLoader.dismiss()
                is Resource.Error -> apiError(response.message)
            }
        }


        mainViewModel.cart.observe(viewLifecycleOwner){response->
            when(response){
                is Resource.Success->{

                    appLoader.dismiss()
                    if(response.data?.httpcode == 200){
                        setProducts(response.data.data)
                    }
                }
                is Resource.Loading->appLoader.show()
                is Resource.Error->apiError(response.message)
            }
        }
    }

    private fun setProducts(cartData: CartData) {

        this.cartData = cartData

        with(binding) {
            initRecyclerView(requireContext(),binding.checkOutRv, checkOutAdapter)

            val checkOutData = mutableListOf<SellerProduct>()

            for (sellerProductItem in cartData.seller_product) {
                if (sellerProductItem.seller.products.any { it.cart_selected.toString().toDouble() > 0 }) {
                    checkOutData.add(sellerProductItem)
                }
            }

            val animator: RecyclerView.ItemAnimator? = checkOutRv.itemAnimator
            if (animator is DefaultItemAnimator) {
                animator.supportsChangeAnimations = false
                checkOutRv.itemAnimator = null
            }

            checkOutAdapter.differ.submitList(checkOutData)

            checkOutAdapter.setShippingOptions(shippingOptions)


            voucherDetailsLayout.isVisible = false
            addVoucher.isVisible = true
            scrollView.isVisible = false


            val productCount = cartData.seller_product.sumOf { sellerProducts ->
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
                "RM ${formatToTwoDecimalPlaces(cartData.total_offer_cost.toString().toDouble())}"
            shippingAmount.text =
                "RM ${formatToTwoDecimalPlaces(cartData.shipping_charges.toString().toDouble())}"
            grandTotal = cartData.grand_total.toString().toDouble()
            totalAmount.text = formatToTwoDecimalPlaces(grandTotal)


            if (cartData.wallet_balance != "false") {
                val pointToRM = cartData.wallet_balance.toDouble() / 100
                points.text = "${cartData.wallet_balance.toDouble().roundToInt()} Points (RM ${formatToTwoDecimalPlaces(pointToRM)})"
            } else {
                points.text = "0 Points (RM 0.00)"
                wallet.text = "RM 0.00"
            }

            lifecycleScope.launch {
                scrollView.isVisible = true
            }
        }

        binding.loader.isVisible = false

    }

    private fun setCheckOutData(data: CheckOutData) {
        val address = data.address.filter { it.is_default_addr == 1 }
    }

    private fun setAddress(address: Address) {
        with(binding){
            addressId = address.id
            customerName.text = address.name.toString()
            customerAddress.text = "${address.address1} ${address.pincode}, ${address.city}, ${address.state}, ${address.country}"
            customerMobile.text = address.phone.toString()
        }
    }

    private fun apiError(message: String?) {
        appLoader.dismiss()
        showToast(message.toString())
        if (message == netWorkFailure) {
            noInternetDialog.show()
        }
    }
    private fun showVoucherBottomSheet() {
        val binding = BottomSheetVoucherBinding.inflate(layoutInflater)
        val sortDialog = BottomSheetDialog(requireActivity(), R.style.MyTransparentBottomSheetDialogTheme)
        sortDialog.setContentView(binding.root)
        sortDialog.show()
    }


    private fun showStoreVoucherBottomSheet() {
        val binding = BottomSheetStoreVoucherBinding.inflate(layoutInflater)
        val sortDialog = BottomSheetDialog(requireActivity(), R.style.MyTransparentBottomSheetDialogTheme)
        sortDialog.setContentView(binding.root)
        sortDialog.show()
    }


    private fun setOnClickListener() {
        with(binding) {
            back.setOnClickListener (this@CheckOutFragment)
            addVoucher.setOnClickListener (this@CheckOutFragment)
            removeVoucher.setOnClickListener (this@CheckOutFragment)
            manageAddress.setOnClickListener (this@CheckOutFragment)
            placeOrder.setOnClickListener (this@CheckOutFragment)
        }

        checkOutAdapter.setOnMessageListener { message, position ->
            cartData.seller_product[position].seller.message = message
        }
    }

    private fun placeOrder(){
        val jsonObject = JsonObject()
        val sellerArray = JsonObject()

        var sellerIndex = 0


        cartData.seller_product.forEach { sellerData ->
            val sellerObject = JsonObject()

            val totalTax = sellerData.seller.products.sumOf { it.total_tax_value.toString().toDouble() }
            val commission = sellerData.seller.products.sumOf { it.commission.toString().toDouble() }
            val discountAmt = sellerData.seller.products.sumOf { it.total_discount_price.toString().toDouble() }
            val totalCost = sellerData.seller.products.sumOf { it.total_discount_price.toString().toDouble() }

            sellerObject.addProperty("shipping_method", sellerData.shipping_method)
            sellerObject.addProperty("shipping_charge", sellerData.shipping.toString())
            sellerObject.addProperty("seller_id", sellerData.seller.seller_id)
            sellerObject.addProperty("shipping_markup", sellerData.shipping_markup.toString())
            sellerObject.addProperty("total_tax", totalTax)
            sellerObject.addProperty("commission", commission)
            sellerObject.addProperty("discount_amt", discountAmt)
            sellerObject.addProperty("total_cost", totalCost)
            sellerObject.addProperty("is_coupon", 0)
            sellerObject.addProperty("coupon_id", "")
            sellerObject.addProperty("discount_type"    , "")
            sellerObject.addProperty("packing_charge", 0)
            sellerObject.addProperty("message", sellerData.seller.message?:"")
            sellerObject.addProperty("shipping_option", 1)
            sellerObject.addProperty("coupon_discount_amt", "")
            sellerArray.add(sellerIndex.toString(), sellerObject)
            sellerIndex++
        }

        jsonObject.add("seller_array", sellerArray)
        jsonObject.addProperty("access_token", sessionManager.token)
        jsonObject.addProperty("lang_id", 1)
        jsonObject.addProperty("is_platform_coupon", 0)
        jsonObject.addProperty("platform_discount_amt", 0)
        jsonObject.addProperty("platform_coupon_id", "")
        jsonObject.addProperty("platform_discount_type", "")
        jsonObject.addProperty("e_money_amt", false)
        jsonObject.addProperty("address_id", addressId)
        jsonObject.addProperty("reward_id", "")
        jsonObject.addProperty("commission", 0)
        jsonObject.addProperty("reward_amt", "")
        jsonObject.addProperty("device_id", getDeviceId(requireContext()))
        jsonObject.addProperty("page_url", "https://dev-kangtao.vercel.app/account/checkout")
        jsonObject.addProperty("os_type", "APP")
        jsonObject.addProperty("invite_coupon_id", "")
        jsonObject.addProperty("payment_type", 1)
        jsonObject.addProperty("total_amt", binding.totalAmount.text.toString())
        jsonObject.addProperty("discount_amt", 0)
        jsonObject.addProperty("grand_total", binding.totalAmount.text.toString())

        val bundle = Bundle()
        bundle.putString("payLoad",jsonObject.toString())
        findNavController().navigate(R.id.paymentMethodFragment,bundle)

    }
    override fun onClick(v: View?) {
        when (v?.id) {
            binding.back.id->findNavController().popBackStack()
            binding.addVoucher.id-> showVoucherBottomSheet()
            binding.removeVoucher.id-> showStoreVoucherBottomSheet()
            binding.placeOrder.id-> {
                placeOrder()
            }
            binding.manageAddress.id-> {
                val bundle = Bundle()
                bundle.putString("from","checkOut")
                findNavController().navigate(R.id.manageAddressFragment,bundle)
            }
        }
    }
}