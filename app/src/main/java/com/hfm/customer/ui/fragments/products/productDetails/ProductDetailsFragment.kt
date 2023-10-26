package com.hfm.customer.ui.fragments.products.productDetails

import android.annotation.SuppressLint
import android.app.ActionBar.LayoutParams
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.content.Context
import android.graphics.Point
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Html
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.WebViewClient
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.util.TypedValueCompat.dpToPx
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.MergingMediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.navigation.fragment.findNavController
import coil.load
import com.google.android.material.bottomsheet.BottomSheetBehavior
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
import com.hfm.customer.ui.fragments.products.productDetails.model.Image
import com.hfm.customer.ui.fragments.products.productDetails.model.ProductData
import com.hfm.customer.utils.Loader
import com.hfm.customer.utils.NoInternetDialog
import com.hfm.customer.utils.Resource
import com.hfm.customer.utils.cartCount
import com.hfm.customer.utils.extractYouTubeVideoId
import com.hfm.customer.utils.formatToTwoDecimalPlaces
import com.hfm.customer.utils.initRecyclerView
import com.hfm.customer.utils.makeGone
import com.hfm.customer.utils.makeInvisible
import com.hfm.customer.utils.makeVisible
import com.hfm.customer.utils.netWorkFailure
import com.hfm.customer.utils.replaceBaseUrl
import com.hfm.customer.utils.showToast
import com.hfm.customer.viewModel.MainViewModel
import com.maxrave.kotlinyoutubeextractor.State
import com.maxrave.kotlinyoutubeextractor.YTExtractor
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.IOException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlin.math.roundToInt


@SuppressLint("UnsafeOptInUsageError")
@AndroidEntryPoint
class ProductDetailsFragment : Fragment(), View.OnClickListener {

    private lateinit var exoPause: ImageView
    private lateinit var exoPlay: ImageView

    private var selectedVariant: String = ""

    private var qty: Int = 0
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

    private lateinit var noInternetDialog: NoInternetDialog

    private val videoIcon =
        "https://png.pngtree.com/png-vector/20190215/ourmid/pngtree-play-video-icon-graphic-design-template-vector-png-image_530837.jpg"


    private val videoPlayer: ExoPlayer by lazy {
        ExoPlayer.Builder(requireContext()).build().apply {
            setAudioAttributes(audioAttributes, true)
            pauseAtEndOfMediaItems = true
            setHandleAudioBecomingNoisy(true)
        }
    }

    private lateinit var dataSourceFactory: DefaultDataSource.Factory

    private lateinit var appLoader: Loader
    private var cartId: String = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)

        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_product_detail, container, false)
            binding = FragmentProductDetailBinding.bind(currentView!!)
            init()
            setOnClickListener()
        }
        return currentView!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setObserver()
    }

    private fun init() {
        appLoader = Loader(requireContext())
        noInternetDialog = NoInternetDialog(requireContext())
        dataSourceFactory = DefaultDataSource.Factory(requireContext())
        binding.productVideo.clipToOutline = true
        binding.productVideo.player = videoPlayer
        exoPlay = binding.productVideo.findViewById(androidx.media3.ui.R.id.exo_play)
        exoPause = binding.productVideo.findViewById(androidx.media3.ui.R.id.exo_pause)
        noInternetDialog.setOnDismissListener { init() }
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
                is Resource.Error -> apiError(response.message)
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
                        binding.voucherListCv.isVisible = response.data.isNotEmpty()
                    } else {
                        binding.voucherListCv.isVisible = false
                    }
                }

                is Resource.Loading -> {}
                is Resource.Error -> apiError(response.message)
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
                is Resource.Error -> apiError(response.message)
            }
        }

        mainViewModel.sendBulkOrderRequest.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    if (response.data?.httpcode == 200) {
                        bulkOrderDialog.dismiss()
                        val bundle = Bundle()
                        bundle.putString("from","bulkOrders")
                        findNavController().navigate(R.id.action_productDetailsFragment_to_myOrdersFragment,bundle)
                    } else showToast(response.data?.message.toString())
                }

                is Resource.Loading -> appLoader.show()
                is Resource.Error -> apiError(response.message)
            }
        }


        mainViewModel.addToWishList.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    if (response.data?.httpcode == 200) {
                        binding.addToWishlist.setImageResource(R.drawable.like_active)
                        productData.product.in_wishlist = 1
                    } else {
                        binding.addToWishlist.setImageResource(R.drawable.ic_fav)
                    }
                }

                is Resource.Loading -> Unit
                is Resource.Error -> apiError(response.message)
            }
        }

        mainViewModel.removeFromWishList.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    if (response.data?.httpcode == 200) {
                        binding.addToWishlist.setImageResource(R.drawable.ic_fav)
                        productData.product.in_wishlist = 0
                    } else {
                        binding.addToWishlist.setImageResource(R.drawable.ic_fav_fill)
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
                    if (response.data?.httpcode == 200) {
                        showToast(response.data.message)
                        binding.addToCartMain.makeInvisible()
                        binding.cartCount.makeVisible()
                        binding.qty.text = "1"
                        qty = 1
                        cartId = response.data.data.cart_id.toString()
                        cartCount.postValue(cartCount.value?.plus(1) ?: 1)
                    } else {
                        showToast(response.data?.message.toString())
                    }
                }

                is Resource.Loading -> appLoader.show()
                is Resource.Error -> apiError(response.message)
            }
        }

        mainViewModel.checkAvailability.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    if (response.data?.httpcode == 200) {
                        if (response.data.status == "error") {
                            binding.estimateDeliveryDate.isVisible = true
                            binding.estimateDeliveryDate.text = "Not Available"

                        } else {
                            binding.estimateDeliveryDate.isVisible = true
                            val responseMessage = response.data.message
                            val spannableString =
                                SpannableString("Estimated delivery between $responseMessage")
                            val redColorSpan = ForegroundColorSpan(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.red
                                )
                            )
                            spannableString.setSpan(
                                redColorSpan,
                                spannableString.indexOf(responseMessage),
                                spannableString.indexOf(responseMessage) + responseMessage.length,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                            binding.estimateDeliveryDate.text = spannableString

                        }
                    } else {
                        showToast(response.data?.message.toString())
                    }
                }

                is Resource.Loading -> appLoader.show()
                is Resource.Error -> apiError(response.message)
            }
        }
        mainViewModel.addToCartMultiple.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    binding.addToCart.text = "Added to Cart"
                    binding.addToCartMain.makeInvisible()
                    binding.cartCount.makeVisible()
                    binding.qty.text = "1"
                    cartCount.postValue(cartCount.value?.plus(1) ?: 1)
                }

                is Resource.Loading -> appLoader.show()
                is Resource.Error -> apiError(response.message)
            }
        }


        mainViewModel.updateCartCount.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()

                    binding.addToCartMain.makeInvisible()
                    binding.cartCount.makeVisible()
                    binding.qty.text = qty.toString()
                }

                is Resource.Loading -> appLoader.show()
                is Resource.Error -> apiError(response.message)
            }
        }

        mainViewModel.deleterCartProduct.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()

                    binding.addToCartMain.makeVisible()
                    binding.cartCount.makeInvisible()
                    binding.qty.text = "0"
                    cartCount.postValue(cartCount.value?.minus(1) ?: 1)
                }

                is Resource.Loading -> appLoader.show()
                is Resource.Error -> apiError(response.message)
            }
        }

        cartCount.observe(requireActivity()) { count ->
            binding.topBarCartCount.text = count.toString()
        }
    }


    private fun apiError(message: String?) {
        appLoader.dismiss()
        showToast(message.toString())
        if (message == netWorkFailure) {
            noInternetDialog.show()
        }
    }

    @SuppressLint("SetJavaScriptEnabled", "SetTextI18n")
    private fun setProductDetails(productData: ProductData) {
        this.productData = productData

        if(productData.customer_addr!=null){
            if(!productData.customer_addr.pincode.isNullOrEmpty()){
                binding.pinCode.setText(productData.customer_addr.pincode)
                mainViewModel.checkAvailability(productData.product.product_id.toString(), productData.customer_addr.pincode)
            }
        }
        initRecyclerView(requireContext(), binding.productsImagesRv, productsImagesAdapter, true)

        val productImages: MutableList<Image> = ArrayList()
        productData.product.image?.let { productImages.addAll(it) }
        val image = Image(videoIcon, "")

        if (productData.product.video?.isNotEmpty() == true) {
            productImages.add(image)
        }

        productsImagesAdapter.differ.submitList(productImages)
        mainViewModel.getSellerVouchers(sellerId = productData.product.seller_id.toString())

        with(binding) {
            if (productData.product.image?.isNotEmpty() == true) {
                productImage.load(replaceBaseUrl(productData.product.image[0].image)){
                    placeholder(R.drawable.logo)
                    
                }
            }
            productName.text = productData.product.product_name
            ratingBar.rating = productData.product.rating.toString().toFloat()
            ratingCount.text =
                productData.product.rating.toString().toDouble().roundToInt().toString()
            brand.text = "Brand: ${productData.product.brand_name}"

            ratingDetails.text = "(${
                productData.product.rating.toString().toDouble().roundToInt()
            } ratings & ${
                productData.product.total_reviews.toString().toDouble().roundToInt()
            } reviews)"

            reviewCv.isVisible = productData.product.rating.toString().toDouble() > 0
            reviewRatingBar.rating = productData.product.rating.toString().toFloat()
            reviewRatingCount.text = productData.product.rating.toString().toDouble().roundToInt().toString()
            reviewLbl.text = "Customer Reviews (${productData.product.total_reviews.toString().toDouble().roundToInt()})"
            reviewRatingDetails.text = "${productData.product.total_reviews.toString().toDouble().roundToInt()} Reviews"


            if (productData.product.offer_price != null && productData.product.offer_price.toString() != "false") {
                productPrice.text = "RM ${
                    formatToTwoDecimalPlaces(
                        productData.product.offer_price.toString().toDouble()
                    )
                }"
            }


            productListingPrice.text = "NP: RM ${
                formatToTwoDecimalPlaces(
                    productData.product.actual_price.toString().toDouble()
                )
            }"

            addToWishlist.setImageResource(if (productData.product.in_wishlist == 1) R.drawable.like_active else R.drawable.ic_fav)
            if (productData.seller_info.isNotEmpty()) {
                productData.seller_info[0].let {
                    val imageOriginal = it.logo
                    val imageReplaced =
                        imageOriginal.replace("https://uat.hfm.synuos.com", "http://4.194.191.242")
                    storeImage.load(imageReplaced){
                        placeholder(R.drawable.logo)
                        
                    }
                    storeName.text = it.store_name
                    /*chatResponse.text = "${
                        formatToTwoDecimalPlaces(
                            it.postive_review.toDouble()
                        ).toDouble().roundToInt()
                    } % Positive ${it.followers} followers"*/
                }
            }



            binding.descriptionWebView.settings.javaScriptEnabled = true
            val htmlContent = productData.product.long_description
            val descriptionContent = Html.fromHtml(htmlContent).toString().replace("\\s+".toRegex(), "")
            descriptionCv.isVisible =descriptionContent.isNotEmpty()
            descriptionWebView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
            descriptionWebView.webViewClient = WebViewClient()

            val measuredHeight = binding.descriptionWebView.measuredHeight
            binding.viewMore.isVisible = measuredHeight > 120
            binding.viewMoreArrow.isVisible = measuredHeight > 120



            if (productData.product.specification == "false" || productData.product.specification.isEmpty()) {
                specificationsCv.isVisible = false
                specificationsWebView.isVisible = false
                specificationsDivider.isVisible = false
            } else {
                specificationsCv.isVisible = true
                specificationsWebView.isVisible = true
                specificationsDivider.isVisible = true
                binding.specificationsWebView.settings.javaScriptEnabled = true
                val specificationHtmlContent = productData.product.specification
                val specificationContent = Html.fromHtml(specificationHtmlContent).toString().replace("\\s+".toRegex(), "")
                specificationsCv.isVisible =specificationContent.isNotEmpty()
                specificationsWebView.loadDataWithBaseURL(
                    null,
                    specificationHtmlContent,
                    "text/html",
                    "UTF-8",
                    null
                )
                specificationsWebView.webViewClient = WebViewClient()
            }

            with(frequentlyBoughtItems.frequentlyBoughtItem1) {
                if (productData.product.image?.isNotEmpty() == true) {
                    val imageOriginal = productData.product.image[0].image
                    val imageReplaced =
                        imageOriginal.replace("https://uat.hfm.synuos.com", "http://4.194.191.242")
                    productImage.load(imageReplaced){
                        placeholder(R.drawable.logo)
                        
                    }
                }
                productName.text = productData.product.product_name
                productPrice.text = "RM ${
                    formatToTwoDecimalPlaces(
                        productData.product.actual_price.toString().toDouble()
                    )
                }"
            }

            if (productData.cross_selling_products.isEmpty()) {
                frequentProducts.isVisible = false
            } else {
                frequentProducts.isVisible = true
                with(frequentlyBoughtItems.frequentlyBoughtItem2) {
                    productData.cross_selling_products[0].let {
                        if (it.image.isNotEmpty()) {
                            val imageOriginal = it.image[0].image
                            val imageReplaced = imageOriginal.replace(
                                "https://uat.hfm.synuos.com",
                                "http://4.194.191.242"
                            )
                            productImage.load(imageReplaced){
                                placeholder(R.drawable.logo)
                                
                            }
                        }
                        productName.text = it.product_name
                        if(it.actual_price!=null)
                        productPrice.text =
                            "RM ${formatToTwoDecimalPlaces(it.actual_price.toString().toDouble())}"

                        total.text = "RM ${
                            formatToTwoDecimalPlaces(
                                productData.product.actual_price.toString()
                                    .toDouble() + it.actual_price.toString()
                                    .toDouble()
                            )
                        }"
                    }
                }
            }
            if (productData.product.is_out_of_stock != null) {
                soldOut.isVisible = productData.product.is_out_of_stock.toString().toBoolean()
            }

            initRecyclerView(requireContext(), productsVariantsRv, productsVariantsAdapter, true)
            if (productData.varaiants_list.isNotEmpty()) {
                selectedVariant = productData.varaiants_list[0].pro_id.toString()
                soldOut.isVisible = productData.varaiants_list[0].is_out_of_stock.toString().toBoolean()
                if (productData.varaiants_list[0].offer_price.toString()
                        .isNotEmpty() && productData.varaiants_list[0].offer_price.toString() != "false"
                ) {
                    binding.productListingPrice.isVisible = true
                    binding.productPrice.text = "RM ${
                        formatToTwoDecimalPlaces(
                            productData.varaiants_list[0].offer_price.toString().toDouble()
                        )
                    }"
                    binding.productListingPrice.text = "NP: RM${
                        formatToTwoDecimalPlaces(
                            productData.varaiants_list[0].actual_price.toString().toDouble()
                        )
                    }"
                } else {
                    binding.productListingPrice.isVisible = false
                    binding.productPrice.text = "RM ${
                        formatToTwoDecimalPlaces(
                            productData.varaiants_list[0].actual_price.toString().toDouble()
                        )
                    }"
                    binding.productListingPrice.text = "NP: RM ${
                        formatToTwoDecimalPlaces(
                            productData.varaiants_list[0].actual_price.toString().toDouble()
                        )
                    }"
                }

                productData.varaiants_list[0].isSelected = true
                productsVariantsAdapter.differ.submitList(productData.varaiants_list)
            }

            initRecyclerView(requireContext(), relatedRv, relativeProductListAdapter, true)
            relativeProductListAdapter.differ.submitList(productData.other_products)
            if (productData.product.offer_name == "Flash Sale") {
                flashDealOrOutOfStock.isVisible = true
                setTimer(productData.product.end_time)
            }


            pinCode.clearFocus()
            binding.loader.isVisible = false
        }
    }

    private fun setTimer(endTime: String) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val endTimeString = endTime
        val endTime = dateFormat.parse(endTimeString) ?: Date()
        val currentTime = Date()
        val timeDifference = endTime.time - currentTime.time
        val countdownTimer = object : CountDownTimer(timeDifference, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                updateCountdownText(millisUntilFinished)
            }

            override fun onFinish() {
                updateCountdownText(0)
            }
        }
        countdownTimer.start()
    }

    private fun updateCountdownText(millisUntilFinished: Long) {
        val days = millisUntilFinished / (1000 * 60 * 60 * 24)
        val hours = (millisUntilFinished % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60)
        val minutes = (millisUntilFinished % (1000 * 60 * 60)) / (1000 * 60)
        val seconds = (millisUntilFinished % (1000 * 60)) / 1000
        binding.flashDealOrOutOfStock.text = "Flash Deals Ends In: ${
            String.format(
                Locale.getDefault(),
                "%02d:%02d:%02d:%02d",
                days,
                hours,
                minutes,
                seconds
            )
        }"
    }


    private fun showBulkOrderBottomSheet() {
        if (!this::productData.isInitialized) return
        var unitOfMeasures = ""
        bottomSheetBinding = BottomSheetBulkOrderBinding.inflate(layoutInflater)
        bulkOrderDialog =
            BottomSheetDialog(requireActivity(), R.style.MyTransparentBottomSheetDialogTheme)

        bulkOrderDialog.setContentView(bottomSheetBinding.root)


        val windowManager =
            requireActivity().getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        val screenHeight: Int = size.y
        val desiredHeight = (screenHeight * 0.85).toInt() // Adjust the fraction as needed


        with(bottomSheetBinding) {
            val layoutParams = scrollView.layoutParams
            layoutParams.width = LayoutParams.MATCH_PARENT
            layoutParams.height = desiredHeight
            scrollView.layoutParams = layoutParams
            val bottomSheet: FrameLayout? = bulkOrderDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet)
            if (bottomSheet != null) {
                BottomSheetBehavior.from(bottomSheet).state = BottomSheetBehavior.STATE_EXPANDED
            }
            productName.text = productData.product.product_name
            val imageOriginal = productData.product.image?.get(0)?.image
            val imageReplaced =
                imageOriginal?.replace("https://uat.hfm.synuos.com", "http://4.194.191.242")
            productImage.load(imageReplaced){
                placeholder(R.drawable.logo)
                
            }
            name.setText("${profileData.first_name} ${profileData.last_name}")
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
                ArrayAdapter<String>(
                    requireContext(),
                    R.layout.spinner_text_units_of_measurement,
                    items
                )
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
                    currentDate.monthValue-1,
                    currentDate.dayOfMonth
                )
                datePickerDialog.datePicker.minDate = System.currentTimeMillis()-1000
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

    private var dateSetListener =
        OnDateSetListener { _, year, month, dayOfMonth ->
            bottomSheetBinding.date.text = "$dayOfMonth-$month-$year"
        }


    private fun playVideo() {
        val yt =
            YTExtractor(con = requireActivity(), CACHING = false, LOGGING = true, retryCount = 3)
        lifecycleScope.launch {
            yt.extract(extractYouTubeVideoId(productData.product.video.toString()).toString())
            if (yt.state == State.SUCCESS) {
                yt.getYTFiles().let {
                    val videoMedia = it?.get(135)?.url.toString()
                    val audioMedia = it?.get(251)?.url.toString()
                    startVideo(videoMedia, audioMedia)
                }
            }
        }
    }

    private fun startVideo(videoMedia: String, audioMedia: String) {
        val videoMediaItem: MediaItem = MediaItem.fromUri(videoMedia)
        val audioMediaItem: MediaItem = MediaItem.fromUri(audioMedia)
        val videoMediaSource =
            ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(videoMediaItem)
        val audioMediaSource =
            ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(audioMediaItem)
        val mergedSource = MergingMediaSource(videoMediaSource, audioMediaSource)

        val currentMediaItem: MediaItem? = videoPlayer.currentMediaItem
        if (currentMediaItem == null || currentMediaItem != mergedSource.mediaItem) {
            try {
                videoPlayer.stop()
                videoPlayer.setMediaSource(mergedSource)
                videoPlayer.prepare()
                videoPlayer.addListener(playerListener)
                videoPlayer.playWhenReady = true
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } else {
            togglePlayPause()
        }
    }


    private fun togglePlayPause() {
        if (videoPlayer.isPlaying) {
            videoPlayer.pause()
        } else {
            if (videoPlayer.currentPosition >= videoPlayer.duration) {
                videoPlayer.seekTo(0)
            }
            videoPlayer.play()
        }
    }


    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            super.onIsPlayingChanged(isPlaying)
            if (isPlaying) {
                exoPlay.isVisible = false
                exoPause.isVisible = true
            } else {
                exoPlay.isVisible = true
                exoPause.isVisible = false
            }
        }
    }


    private fun setOnClickListener() {
        with(binding) {
            back.setOnClickListener(this@ProductDetailsFragment)
            viewMore.setOnClickListener(this@ProductDetailsFragment)
            addToCartMain.setOnClickListener(this@ProductDetailsFragment)
            addToCart.setOnClickListener(this@ProductDetailsFragment)
            increaseQty.setOnClickListener(this@ProductDetailsFragment)
            decreaseQty.setOnClickListener(this@ProductDetailsFragment)
            checkPinCode.setOnClickListener(this@ProductDetailsFragment)
            viewAllReviews.setOnClickListener(this@ProductDetailsFragment)
            bulkOrder.setOnClickListener(this@ProductDetailsFragment)
            visitStore.setOnClickListener(this@ProductDetailsFragment)
            storeIcon.setOnClickListener(this@ProductDetailsFragment)
            cart.setOnClickListener(this@ProductDetailsFragment)
            addToWishlist.setOnClickListener(this@ProductDetailsFragment)
            chat.setOnClickListener(this@ProductDetailsFragment)

            exoPlay.setOnClickListener {
                videoPlayer.play()
            }

            exoPause.setOnClickListener {
                videoPlayer.pause()
            }
        }


        productsImagesAdapter.setOnImageClickListener { data ->
            if (data == videoIcon) {
                binding.productVideo.isVisible = true
                playVideo()
            } else {

                videoPlayer.pause()
                binding.productVideo.isVisible = false
                binding.productImage.load(data){
                    placeholder(R.drawable.logo)
                    
                }
            }
        }

        relativeProductListAdapter.setOnProductClickListener { id ->
            val bundle = Bundle().apply {
                putString("productId", id)
            }
            findNavController().navigate(R.id.productDetailsFragment, bundle)
        }

        productsVariantsAdapter.setOnVariantsClickListener { position ->

            binding.addToCartMain.isVisible = true
            binding.cartCount.isVisible = false

            selectedVariant = productData.varaiants_list[position].pro_id.toString()
            binding.soldOut.isVisible = productData.varaiants_list[position].is_out_of_stock.toString().toBoolean()
            if (productData.varaiants_list[position].offer_price.toString()
                    .isNotEmpty() && productData.varaiants_list[position].offer_price.toString() != "false"
            ) {
                binding.productListingPrice.isVisible = true
                binding.productPrice.text = "RM ${
                    formatToTwoDecimalPlaces(
                        productData.varaiants_list[position].offer_price.toString().toDouble()
                    )
                }"
                binding.productListingPrice.text = "RM ${
                    formatToTwoDecimalPlaces(
                        productData.varaiants_list[position].actual_price.toString().toDouble()
                    )
                }"
            } else {
                binding.productListingPrice.isVisible = false
                binding.productPrice.text = "RM ${
                    formatToTwoDecimalPlaces(
                        productData.varaiants_list[position].actual_price.toString().toDouble()
                    )
                }"
                binding.productListingPrice.text = "RM ${
                    formatToTwoDecimalPlaces(
                        productData.varaiants_list[position].actual_price.toString().toDouble()
                    )
                }"
            }

        }
    }


    private var isExpanded = false
    override fun onClick(v: View?) {
        when (v?.id) {
            binding.back.id -> findNavController().popBackStack()
            binding.cart.id -> findNavController().navigate(R.id.cartFragment)
            binding.chat.id -> {

                productData.seller_info[0].let { sellerDetail ->
                    val chatId =
                        if (sellerDetail.chat_id.isNullOrEmpty()) 0 else sellerDetail.chat_id.toInt()
                    val bundle = Bundle()
                    bundle.putString("from", "chatList")
                    bundle.putString("storeName", binding.storeName.text.toString())
                    bundle.putString("sellerId", sellerDetail.seller_id.toString())
                    bundle.putString("saleId", "")
                    bundle.putInt("chatId", chatId)
                    findNavController().navigate(R.id.chatFragment, bundle)
                }
            }

            binding.addToCart.id -> {
                if (binding.addToCart.text == "Added to Cart") {
                    showToast("Already added to cart")
                    return
                }

                val productIds = selectedVariant.ifEmpty {
                    "${productData.product.product_id},${productData.cross_selling_products[0].product_id}"
                }
                mainViewModel.addToCartMultiple(productIds, "1")
            }

            binding.addToCartMain.id -> {
                if (selectedVariant.isNotEmpty()) {
                    mainViewModel.addToCart(selectedVariant, "1")
                } else {
                    mainViewModel.addToCart(productData.product.product_id.toString(), "1")

                }
            }

            binding.increaseQty.id -> {
                qty = binding.qty.text.toString().toInt() + 1
                mainViewModel.updateCartQty(cartId = cartId, qty = qty)
            }


            binding.decreaseQty.id -> {
                qty = binding.qty.text.toString().toInt() - 1
                if (qty < 1) {
                    mainViewModel.deleterCartProduct(cartId = cartId)
                } else {
                    mainViewModel.updateCartQty(cartId = cartId, qty = qty)
                }

                binding.qty.text = "${binding.qty.text.toString().toInt() - 1}"
                if (binding.qty.text.toString().toInt() <= 0) {
                    binding.addToCartMain.makeVisible()
                    binding.cartCount.makeGone()
                }
            }


            binding.viewAllReviews.id -> {
                showToast("Under Construction")
//                findNavController().navigate(R.id.customerRatingFragment)
            }

            binding.bulkOrder.id -> showBulkOrderBottomSheet()

            binding.visitStore.id -> {
                val bundle = Bundle()
                bundle.putString("storeId", productData.seller_info[0].seller_id.toString())
                findNavController().navigate(R.id.storeFragment, bundle)
            }

            binding.storeIcon.id -> {
                val bundle = Bundle()
                bundle.putString("storeId", productData.seller_info[0].seller_id.toString())
                findNavController().navigate(R.id.storeFragment, bundle)
            }

            binding.checkPinCode.id -> {
                val pinCode = binding.pinCode.text.toString()
                if (pinCode.isEmpty()) {
                    showToast("Please Enter a valid pincode.")
                    return
                }
                mainViewModel.checkAvailability(productData.product.product_id.toString(), pinCode)
            }

            binding.viewMore.id -> {
                val webViewLayoutParams = binding.descriptionWebView.layoutParams
                val measuredHeight = binding.descriptionWebView.measuredHeight
                val maxExpandedHeight = dpToPx(
                    240f,
                    resources.displayMetrics
                ) // Define the maximum expanded height in pixels

                if (measuredHeight > maxExpandedHeight.roundToInt()) {
                    webViewLayoutParams.height = maxExpandedHeight.roundToInt()
                    binding.viewMore.text = getString(R.string.view_more_lbl)
                    binding.viewMoreArrow.rotation = 0f
                } else {
                    webViewLayoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                    binding.viewMore.text = getString(R.string.view_less_lbl)
                    binding.viewMoreArrow.rotation = 180f
                }
                binding.descriptionWebView.layoutParams = webViewLayoutParams
            }

            binding.cart.id -> findNavController().navigate(R.id.cartFragment)
            binding.addToWishlist.id -> {
                if (productData.product.in_wishlist == 1) {
                    mainViewModel.removeFromWishList(productData.product.product_id.toString())
                    binding.addToWishlist.setImageResource(R.drawable.ic_fav)
                } else {
                    mainViewModel.addToWishList(productData.product.product_id.toString())
                    binding.addToWishlist.setImageResource(R.drawable.like_active)
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        videoPlayer.pause()
    }
}