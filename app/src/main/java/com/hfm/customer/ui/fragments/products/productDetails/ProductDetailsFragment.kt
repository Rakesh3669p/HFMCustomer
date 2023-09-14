package com.hfm.customer.ui.fragments.products.productDetails

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebViewClient
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import coil.load
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.hfm.customer.R
import com.hfm.customer.databinding.BottomSheetBulkOrderBinding
import com.hfm.customer.databinding.FragmentProductDetailBinding
import com.hfm.customer.ui.dashBoard.profile.model.Profile
import com.hfm.customer.ui.fragments.products.productDetails.adapter.ProductImagesAdapter
import com.hfm.customer.ui.fragments.products.productDetails.adapter.ProductVariantsAdapter
import com.hfm.customer.ui.fragments.products.productDetails.adapter.RelativeProductListAdapter
import com.hfm.customer.ui.fragments.products.productDetails.adapter.ReviewsAdapter
import com.hfm.customer.ui.fragments.products.productDetails.adapter.SpecsAdapter
import com.hfm.customer.ui.fragments.products.productDetails.adapter.VouchersAdapter
import com.hfm.customer.ui.fragments.products.productDetails.model.ProductData
import com.hfm.customer.utils.Loader
import com.hfm.customer.utils.Resource
import com.hfm.customer.utils.initRecyclerView
import com.hfm.customer.utils.makeGone
import com.hfm.customer.utils.makeInvisible
import com.hfm.customer.utils.makeVisible
import com.hfm.customer.utils.netWorkFailure
import com.hfm.customer.utils.showToast
import com.hfm.customer.viewModel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import javax.inject.Inject


@AndroidEntryPoint
class ProductDetailsFragment : Fragment(), View.OnClickListener {

    private lateinit var bulkOrderDialog: BottomSheetDialog
    private lateinit var bottomSheetBinding: BottomSheetBulkOrderBinding
    private lateinit var productData: ProductData
    private lateinit var profileData: Profile

    private lateinit var binding: FragmentProductDetailBinding
    private var currentView: View? = null

    @Inject
    lateinit var productsImagesAdapter: ProductImagesAdapter

    @Inject
    lateinit var productsVariantsAdapter: ProductVariantsAdapter

    @Inject
    lateinit var relativeProductListAdapter: RelativeProductListAdapter

    @Inject
    lateinit var vouchersAdapter: VouchersAdapter

    @Inject
    lateinit var reviewsAdapter: ReviewsAdapter

    @Inject
    lateinit var specsAdapter: SpecsAdapter

    private val mainViewModel: MainViewModel by viewModels()

    private lateinit var appLoader: Loader
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_product_detail, container, false)
            binding = FragmentProductDetailBinding.bind(currentView!!)
            init()
            setObserver()
            setOnClickListener()
        }
        return currentView!!
    }


    private fun init() {
        appLoader = Loader(requireContext())
        binding.loader.isVisible = true
        val productId = arguments?.getString("productId").toString()
        mainViewModel.getProductDetails(productId)
        mainViewModel.getProfile()
    }


    private fun setObserver() {

        mainViewModel.profile.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    if (response.data?.httpcode == "200") {
                        profileData = response.data.data.profile[0]
                    } else {
                        showToast(response.data?.message.toString())
                    }
                }

                is Resource.Loading -> Unit
                is Resource.Error -> {
                    appLoader.dismiss()
                    showToast(response.message.toString())
                    if (response.message.toString() == netWorkFailure) {

                    }
                }
            }

        }


        mainViewModel.sellerVouchers.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    if (response.data?.isNotEmpty() == true) {
                        binding.voucherListCv.isVisible = true
                        initRecyclerView(
                            requireContext(),
                            binding.vouchersRv,
                            vouchersAdapter,
                            true
                        )
                        vouchersAdapter.differ.submitList(response.data)
                    } else {
                        binding.voucherListCv.isVisible = false
                    }
                }

                is Resource.Loading -> {}
                is Resource.Error -> {
                    showToast(response.message.toString())
                    if (response.message.toString() == netWorkFailure) {

                    }

                }
            }
        }
        mainViewModel.productDetails.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    if (response.data?.httpcode == 200) setProductDetails(response.data.data)
                    else showToast(response.data?.message.toString())
                }

                is Resource.Loading -> appLoader.show()
                is Resource.Error -> {
                    appLoader.dismiss()
                    showToast(response.message.toString())
                    if (response.message.toString() == netWorkFailure) {

                    }
                }
            }
        }

        mainViewModel.sendBulkOrderRequest.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    if (response.data?.httpcode == 200) {
                        bulkOrderDialog.dismiss()
                        findNavController().navigate(R.id.action_productDetailsFragment_to_myOrdersFragment)
                    }
                    else showToast(response.data?.message.toString())
                }

                is Resource.Loading -> appLoader.show()
                is Resource.Error -> {
                    appLoader.dismiss()
                    showToast(response.message.toString())
                    if (response.message.toString() == netWorkFailure) {

                    }
                }
            }
        }
    }

    private fun setProductDetails(productData: ProductData) {
        this.productData = productData
        initRecyclerView(requireContext(), binding.productsImagesRv, productsImagesAdapter, true)
        productsImagesAdapter.differ.submitList(productData.product.image)
        mainViewModel.getSellerVouchers(sellerId = productData.product.seller_id.toString())
        val imageOriginal = productData.product.image[0].image
        val imageReplaced =
            imageOriginal.replace("https://uat.hfm.synuos.com", "http://4.194.191.242")

        with(binding) {
            productImage.load(imageReplaced)
            productName.text = productData.product.product_name
            ratingBar.rating = productData.product.rating.toString().toFloat()
            ratingCount.text = productData.product.rating.toString()

            ratingDetails.text =
                "(${productData.product.rating} ratings & ${productData.product.total_reviews} reviews)"
            productPrice.text = "RM ${productData.product.offer_price}"
            productListingPrice.text = "NP: RM ${productData.product.actual_price}"

            if (productData.seller_info.isNotEmpty()) {
                productData.seller_info[0].let {
                    val imageOriginal = it.logo
                    val imageReplaced =
                        imageOriginal.replace("https://uat.hfm.synuos.com", "http://4.194.191.242")
                    storeImage.load(imageReplaced)
                    storeName.text = it.store_name
                }

            }

            reviewRatingBar.rating = productData.product.rating.toString().toFloat()
            reviewRatingCount.text = productData.product.rating.toString()
            reviewRatingDetails.text = "${productData.product.total_reviews} reviews)"

            binding.descriptionWebView.settings.javaScriptEnabled =
                true // Enable JavaScript if needed
            val htmlContent = productData.product.long_description
            descriptionWebView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
            descriptionWebView.webViewClient = WebViewClient()

            if (productData.product.specification == "false") {
                specificationsCv.isVisible = false
                specificationsWebView.isVisible = false
                specificationsDivider.isVisible = false
            } else {
                specificationsCv.isVisible = true
                specificationsWebView.isVisible = true
                specificationsDivider.isVisible = true
                binding.specificationsWebView.settings.javaScriptEnabled =
                    true // Enable JavaScript if needed
                val specificationHtmlContent = productData.product.long_description
                specificationsWebView.loadDataWithBaseURL(
                    null,
                    specificationHtmlContent,
                    "text/html",
                    "UTF-8",
                    null
                )
                specificationsWebView.webViewClient = WebViewClient()

            }

            with(frequentlyBoughtItem1) {

                productImage.load(productData.product.image[0].image)
                productName.text = productData.product.product_name
                productPrice.text = "RM ${productData.product.product_name}"
            }

            if (productData.cross_selling_products.isEmpty()) {
                frequentProducts.isVisible = false
                frequentlyBoughtItem1.root.isVisible = false
                frequentlyBoughtItem2.root.isVisible = false
            } else {
                frequentProducts.isVisible = true
                frequentlyBoughtItem1.root.isVisible = true
                frequentlyBoughtItem2.root.isVisible = true
                with(frequentlyBoughtItem2) {
                    productData.cross_selling_products[0].let {
                        if (it.image.isNotEmpty()) {
                            productImage.load(it.image[0].image)
                        }

                        productName.text = it.product_name
                        productPrice.text = "RM ${it.product_name}"
                    }
                }
            }

//            initRecyclerView(requireContext(), productsVariantsRv, productsVariantsAdapter, true)
//            productsVariantsAdapter.differ.submitList(productData.varaiants_list)

            initRecyclerView(requireContext(), relatedRv, relativeProductListAdapter, true)
            relativeProductListAdapter.differ.submitList(productData.other_products)


//            initRecyclerView(requireContext(), reviewsRv, reviewsAdapter)
//            initRecyclerView(requireContext(), specificationsRv, specsAdapter)

            binding.loader.isVisible = false
        }
    }

    private fun setOnClickListener() {
        with(binding) {
            back.setOnClickListener(this@ProductDetailsFragment)
            addToCartMain.setOnClickListener(this@ProductDetailsFragment)
            increaseQty.setOnClickListener(this@ProductDetailsFragment)
            decreaseQty.setOnClickListener(this@ProductDetailsFragment)
            checkPinCode.setOnClickListener(this@ProductDetailsFragment)
            viewAllReviews.setOnClickListener(this@ProductDetailsFragment)
            bulkOrder.setOnClickListener(this@ProductDetailsFragment)
            visitStore.setOnClickListener(this@ProductDetailsFragment)
            cart.setOnClickListener(this@ProductDetailsFragment)
        }

        productsImagesAdapter.setOnImageClickListener { data ->
            binding.productImage.load(data)
        }

        relativeProductListAdapter.setOnProductClickListener { id ->
            val bundle = Bundle().apply {
                putString("productId", id)
            }
            findNavController().navigate(R.id.productDetailsFragment, bundle)
        }
    }


    private fun showBulkOrderBottomSheet() {
        var unitOfMeasures = ""
        bottomSheetBinding = BottomSheetBulkOrderBinding.inflate(layoutInflater)
        bulkOrderDialog =
            BottomSheetDialog(requireActivity(), R.style.MyTransparentBottomSheetDialogTheme)
        bulkOrderDialog.setContentView(bottomSheetBinding.root)
        with(bottomSheetBinding) {
            productName.text = productData.product.product_name
            val imageOriginal = productData.product.image[0].image
            val imageReplaced =
                imageOriginal.replace("https://uat.hfm.synuos.com", "http://4.194.191.242")
            productImage.load(imageReplaced)
            name.setText("${profileData.first_name} ${profileData.last_name}")
            contact.setText(profileData.phone)
            email.setText(profileData.email)
            address.setText(profileData.address1)


            // Define an array of items
            val items = arrayOf(
                "Units of Measurement",
                "Pieces",
                "Packs",
                "Sets",
                "Numbers",
                "Kilograms",
                "Boxes",
                "Outers",
                "Carton"
            )

            val adapter: ArrayAdapter<String> =
                ArrayAdapter<String>(requireContext(), R.layout.spinner_text_units_of_measurement, items)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)


            unitsSpinner.adapter = adapter
            unitsSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parentView: AdapterView<*>,
                    selectedItemView: View,
                    position: Int,
                    id: Long
                ) {
                    unitOfMeasures = parentView.getItemAtPosition(position) as String

                }

                override fun onNothingSelected(parentView: AdapterView<*>?) {
                    // Do nothing here
                }
            }
            val currentDate = LocalDate.now()


            date.setOnClickListener {
                val datePickerDialog = DatePickerDialog(
                    requireContext(),
                    dateSetListener,
                    currentDate.year,
                    currentDate.monthValue,
                    currentDate.dayOfMonth
                )
                datePickerDialog.show()
            }

            send.setOnClickListener {

                val name = name.text.toString()
                val email = email.text.toString()
                val qty = qty.text.toString()
                val phone = contact.text.toString()
                val date = date.text.toString()
                val deliveryAddress = address.text.toString()
                val remarks = remarks.text.toString()

                if (name.isEmpty()) {
                    showToast("Please Enter a valid Name")
                    return@setOnClickListener
                }

                if (email.isEmpty()) {
                    showToast("Please Enter a valid Email")
                    return@setOnClickListener
                }
                if (qty.isEmpty() || qty.toInt() < 1) {
                    showToast("Quantity should be more than 0")
                    return@setOnClickListener
                }

                if (unitOfMeasures.isEmpty() || unitOfMeasures == "Units of Measurement") {
                    showToast("Please Select Unit of Measures")
                    return@setOnClickListener
                }

                if (phone.isEmpty()) {
                    showToast("Please Enter a valid phone")
                    return@setOnClickListener
                }
                if (date.isEmpty()) {
                    showToast("Please select valid date")
                    return@setOnClickListener
                }

                if (deliveryAddress.isEmpty()) {
                    showToast("Please Enter a valid deliveryAddress")
                    return@setOnClickListener
                }

                mainViewModel.sendBulkOrderRequest(
                    productData.product.product_id.toString(),
                    name,
                    email,
                    qty,
                    phone,
                    date,
                    deliveryAddress,
                    remarks,
                    1
                )
            }
            cancel.setOnClickListener {
                bulkOrderDialog.dismiss()
            }
        }
        bulkOrderDialog.show()
    }

    var dateSetListener =
        OnDateSetListener { _, year, month, dayOfMonth ->
            bottomSheetBinding.date.text = "$dayOfMonth-$month-$year"
        }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.back.id -> findNavController().popBackStack()


            binding.addToCartMain.id -> {
                binding.addToCartMain.makeInvisible()
                binding.cartCount.makeVisible()
                binding.qty.text = "1"
            }

            binding.increaseQty.id -> binding.qty.text =
                "${binding.qty.text.toString().toInt() + 1}"

            binding.decreaseQty.id -> {
                binding.qty.text = "${binding.qty.text.toString().toInt() - 1}"
                if (binding.qty.text.toString().toInt() <= 0) {
                    binding.addToCartMain.makeVisible()
                    binding.cartCount.makeGone()
                }
            }

            binding.checkPinCode.id -> binding.estimateDeliveryDate.makeVisible()

            binding.viewAllReviews.id -> findNavController().navigate(R.id.customerRatingFragment)

            binding.bulkOrder.id -> showBulkOrderBottomSheet()

            binding.visitStore.id -> findNavController().navigate(R.id.storeFragment)

            binding.cart.id -> findNavController().navigate(R.id.cartFragment)
        }
    }
}