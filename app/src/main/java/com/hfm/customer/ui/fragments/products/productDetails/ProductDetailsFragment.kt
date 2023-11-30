package com.hfm.customer.ui.fragments.products.productDetails

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Html
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.core.content.ContextCompat
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
import com.hfm.customer.R
import com.hfm.customer.commonModel.RatingReviewsData
import com.hfm.customer.databinding.DialogQtyPopupBinding
import com.hfm.customer.databinding.FragmentProductDetailBinding
import com.hfm.customer.databinding.ReviewMediaImagesBinding
import com.hfm.customer.databinding.ReviewMediaVideoBinding
import com.hfm.customer.ui.dashBoard.profile.model.Profile
import com.hfm.customer.ui.fragments.products.productDetails.adapter.ComboProductsAdapter
import com.hfm.customer.ui.fragments.products.productDetails.adapter.ProductImagesAdapter
import com.hfm.customer.ui.fragments.products.productDetails.adapter.ProductVariantsAdapter
import com.hfm.customer.ui.fragments.products.productDetails.adapter.RelativeProductListAdapter
import com.hfm.customer.ui.fragments.products.productDetails.adapter.ReviewsAdapter
import com.hfm.customer.ui.fragments.products.productDetails.adapter.SpecsAdapter
import com.hfm.customer.ui.fragments.products.productDetails.adapter.VouchersAdapter
import com.hfm.customer.ui.fragments.products.productDetails.model.Image
import com.hfm.customer.ui.fragments.products.productDetails.model.ProductData
import com.hfm.customer.ui.loginSignUp.LoginActivity
import com.hfm.customer.utils.Loader
import com.hfm.customer.utils.NoInternetDialog
import com.hfm.customer.utils.Resource
import com.hfm.customer.utils.SessionManager
import com.hfm.customer.utils.cartCount
import com.hfm.customer.utils.extractYouTubeVideoId
import com.hfm.customer.utils.formatToTwoDecimalPlaces
import com.hfm.customer.utils.initRecyclerView
import com.hfm.customer.utils.loadImage
import com.hfm.customer.utils.makeGone
import com.hfm.customer.utils.makeInvisible
import com.hfm.customer.utils.makeVisible
import com.hfm.customer.utils.moveToLogin
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
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlin.math.roundToInt

@Suppress("DEPRECATION")
@SuppressLint("UnsafeOptInUsageError", "SetTextI18n,SetJavaScriptEnabled")
@AndroidEntryPoint
class ProductDetailsFragment : Fragment(), View.OnClickListener {

    private var addedQty: String = "0"
    private lateinit var bulkOrderBottomSheetFragment: BulkOrderBottomSheet
    private lateinit var exoPause: ImageView
    private lateinit var exoPlay: ImageView
    private lateinit var exoBuffer: ProgressBar
    private var selectedVariant: String = ""
    private var qty: Int = 0

    private lateinit var productData: ProductData
    private lateinit var profileData: Profile
    private lateinit var binding: FragmentProductDetailBinding
    private var currentView: View? = null

    @Inject
    lateinit var sessionManager: SessionManager

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
    lateinit var comboAdapter: ComboProductsAdapter

    @Inject
    lateinit var specsAdapter: SpecsAdapter

    private val mainViewModel: MainViewModel by viewModels()

    private lateinit var noInternetDialog: NoInternetDialog

    private var descriptionMeasuredHeight = 0

    private val videoIcon =
        "https://png.pngtree.com/png-vector/20190215/ourmid/pngtree-play-video-icon-graphic-design-template-vector-png-image_530837.jpg"


    private val videoPlayer: ExoPlayer by lazy {
        ExoPlayer.Builder(requireContext()).build().apply {
            setAudioAttributes(audioAttributes, true)
            pauseAtEndOfMediaItems = true
            setHandleAudioBecomingNoisy(true)
        }
    }

    private val reviewPlayer: ExoPlayer by lazy {
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
        exoBuffer = binding.productVideo.findViewById(androidx.media3.ui.R.id.exo_buffering)
        noInternetDialog.setOnDismissListener { init() }
        binding.loader.isVisible = true
        val productId = arguments?.getString("productId").toString()

        mainViewModel.getProductDetails(productId)
        mainViewModel.getProfile()
        mainViewModel.getProductReview(productId = productId, limit = 4)

        binding.pinCode.setOnFocusChangeListener { _, _ ->
            scrollToView(binding.pinCode)
        }
    }

    private fun scrollToView(view: View) {
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        val viewY = location[1]
        val screenHeight: Int = binding.root.height
        val scrollY: Int = binding.scrollView.scrollY
        val newScrollY = scrollY + (viewY - screenHeight / 2)
        binding.scrollView.scrollTo(0, newScrollY)
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

        mainViewModel.productReview.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    if (response.data?.httpcode == 200) {
                        setReviewData(response.data.data)
                    }
                }

                is Resource.Loading -> Unit
                is Resource.Error -> apiError(response.message)
            }
        }


        mainViewModel.sellerVouchers.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    if (response.data?.httpcode == 200) {
                        binding.voucherListCv.isVisible = true
                        initRecyclerView(
                            requireContext(),
                            binding.vouchersRv,
                            vouchersAdapter,
                            true
                        )
                        vouchersAdapter.differ.submitList(response.data.data.coupon_list)
                        binding.voucherListCv.isVisible =
                            response.data.data.coupon_list.isNotEmpty()
                        binding.vouchers.isVisible = true
                        binding.vouchers.text =
                            "${response.data.data.coupon_list.size} Vouchers Available"
                    } else {
                        binding.vouchers.isVisible = false
                        binding.voucherListCv.isVisible = false
                    }
                }

                is Resource.Loading -> Unit
                is Resource.Error -> apiError(response.message)
            }
        }
        mainViewModel.productDetails.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    if (response.data?.httpcode == 200) {
                        productData = response.data.data
                        setProductDetails()
                    } else showToast(response.data?.message.toString())
                }

                is Resource.Loading -> appLoader.show()
                is Resource.Error -> apiError(response.message)
            }
        }

        mainViewModel.sendBulkOrderRequest.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    if (this::bulkOrderBottomSheetFragment.isInitialized) {
                        bulkOrderBottomSheetFragment.dismiss()
                    }
                    if (response.data?.httpcode == 200) {
                        val message = "Your bulk order request has successfully submitted"
                        showToast(message)
                        val bundle = Bundle()
                        bundle.putString("from", "bulkOrders")
                        findNavController().navigate(
                            R.id.action_productDetailsFragment_to_myOrdersFragment,
                            bundle
                        )
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
                    when (response.data?.httpcode) {
                        200 -> {
                            binding.addToWishlist.setImageResource(R.drawable.like_active)
                            productData.product.in_wishlist = 1
                            showToast("Product added to favourites")
                        }

                        401 -> {
                            sessionManager.isLogin = false
                            startActivity(Intent(requireActivity(), LoginActivity::class.java))
                            requireActivity().finish()
                        }

                        else -> {
                            showToast(response.data?.message.toString())
                            binding.addToWishlist.setImageResource(R.drawable.ic_fav)
                        }
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

                    when (response.data?.httpcode) {
                        200 -> {
                            binding.addToWishlist.setImageResource(R.drawable.ic_fav)
                            productData.product.in_wishlist = 0
                            showToast("Product removed from favourites")
                        }

                        401 -> {
                            sessionManager.isLogin = false
                            startActivity(Intent(requireActivity(), LoginActivity::class.java))
                            requireActivity().finish()
                        }

                        else -> {
                            showToast(response.data?.message.toString())
                            binding.addToWishlist.setImageResource(R.drawable.ic_fav_fill)
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
                    when (response.data?.httpcode) {
                        200 -> {
                            showToast(response.data.message)
                            binding.addToCartMain.makeInvisible()
                            binding.cartCount.makeVisible()
                            binding.qty.text = addedQty
                            qty = addedQty.toInt()
                            cartId = response.data.data.cart_id.toString()
                            cartCount.postValue(response.data.data.cart_count)
                        }

                        401 -> {
                            sessionManager.isLogin = false
                            startActivity(Intent(requireActivity(), LoginActivity::class.java))
                            requireActivity().finish()
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

        mainViewModel.checkAvailability.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    if (response.data?.httpcode == 200) {
                        binding.internationalLbl.isVisible = false
                        if (response.data.status == "error") {
                            binding.estimateDeliveryDate.isVisible = true
                            binding.estimateDeliveryDate.text = getString(R.string.notAvailable)

                        } else {
                            binding.estimateDeliveryDate.isVisible = true
                            val responseMessage = response.data.message
                            if (responseMessage == "Not Available") {

                                binding.estimateDeliveryDate.isVisible = true
                                binding.estimateDeliveryDate.text = getString(R.string.notAvailable)
                                binding.estimateDeliveryDate.setTextColor(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        R.color.red
                                    )
                                )
                                binding.estimateDeliveryDate.setTextColor(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        R.color.red
                                    )
                                )
                            } else {


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

                                val boldSpan = StyleSpan(Typeface.BOLD)

                                spannableString.setSpan(
                                    boldSpan,
                                    spannableString.indexOf(responseMessage),
                                    spannableString.indexOf(responseMessage) + responseMessage.length,
                                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                                )
                                binding.estimateDeliveryDate.text = spannableString
                            }
                        }
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
                    if (response.data?.httpcode == 200) {
                        binding.addToCartMain.makeInvisible()
                        binding.cartCount.makeVisible()
                        binding.qty.text = "1"
                        cartCount.postValue(response.data.cart_count)
                    } else if (response.data?.httpcode == 401) {
                        sessionManager.isLogin = false
                        startActivity(Intent(requireActivity(), LoginActivity::class.java))
                        requireActivity().finish()
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
                            binding.addToCartMain.makeInvisible()
                            binding.cartCount.makeVisible()
                            binding.qty.text = qty.toString()
                        }

                        400 -> showToast(response.data.message)
                        401 -> {
                            sessionManager.isLogin = false
                            startActivity(Intent(requireActivity(), LoginActivity::class.java))
                            requireActivity().finish()
                        }
                    }
                }

                is Resource.Loading -> appLoader.show()
                is Resource.Error -> apiError(response.message)
            }
        }

        mainViewModel.deleterCartProduct.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    when (response.data?.httpcode) {
                        200 -> {
                            binding.addToCartMain.makeVisible()
                            binding.cartCount.makeInvisible()
                            binding.qty.text = "0"
                            cartCount.postValue(cartCount.value?.minus(1) ?: 1)
                        }

                        401 -> {
                            sessionManager.isLogin = false
                            startActivity(Intent(requireActivity(), LoginActivity::class.java))
                            requireActivity().finish()
                        }

                        else -> {

                        }
                    }
                }

                is Resource.Loading -> appLoader.show()
                is Resource.Error -> apiError(response.message)
            }
        }

        cartCount.observe(requireActivity()) { count ->
            binding.topBarCartCount.isVisible = count > 0
            binding.topBarCartCount.text = count.toString()
        }
    }

    private fun setReviewData(data: RatingReviewsData) {
        if (data.review.isNotEmpty()) {
            initRecyclerView(requireContext(), binding.reviewsRv, reviewsAdapter)
            reviewsAdapter.differ.submitList(data.review)
            with(binding) {
                fiveStarCount.text = data.rate_range.FiveStars.toString()
                fourStarCount.text = data.rate_range.FourStars.toString()
                threeStarCount.text = data.rate_range.ThreeStars.toString()
                twoStarCount.text = data.rate_range.TwoStars.toString()
                oneStarCount.text = data.rate_range.OneStar.toString()

                oneStarSlider.value =
                    ((data.rate_range.OneStar.toDouble() / data.rate_range.All.toDouble()) * 100).toFloat()
                twoStarSlider.value =
                    ((data.rate_range.TwoStars.toDouble() / data.rate_range.All.toDouble()) * 100).toFloat()
                threeStarSlider.value =
                    ((data.rate_range.ThreeStars.toDouble() / data.rate_range.All.toDouble()) * 100).toFloat()
                fourStarSlider.value =
                    ((data.rate_range.FourStars.toDouble() / data.rate_range.All.toDouble()) * 100).toFloat()
                fiveStarSlider.value =
                    ((data.rate_range.FiveStars.toDouble() / data.rate_range.All.toDouble()) * 100).toFloat()
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

    @SuppressLint("SetJavaScriptEnabled", "SetTextI18n", "ObsoleteSdkInt")
    private fun setProductDetails() {
        mainViewModel.getSellerVouchers(sellerId = productData.product.seller_id.toString(), 0)
        setComboProducts()
        setProductAvailability()

        with(binding) {
            initRecyclerView(requireContext(), productsImagesRv, productsImagesAdapter, true)
            initRecyclerView(requireContext(), productsVariantsRv, productsVariantsAdapter, true)
            initRecyclerView(requireContext(), relatedRv, relativeProductListAdapter, true)

            val productImages: MutableList<Image> = ArrayList()
            productData.product.image?.let { productImages.addAll(it) }
            val image = Image(videoIcon, "")
            if (productData.product.video?.isNotEmpty() == true) productImages.add(image)
            productsImagesAdapter.differ.submitList(productImages)

            if (productData.product.image != null && productData.product.image?.isNotEmpty() == true) productImage.loadImage(
                replaceBaseUrl(productData.product.image!![0].image)
            )

            soldOut.isVisible = productData.product.is_out_of_stock == 1

            productName.text = productData.product.product_name
            ratingBar.rating = productData.product.rating.toString().toFloat()
            ratingCount.text = productData.product.rating.toString().toDouble().toString()
            brand.text = "Brand: ${productData.product.brand_name}"

            val rating = productData.product.rating.toString().toDoubleOrNull()?.roundToInt() ?: 0
            val totalReviews =
                productData.product.total_reviews.toString().toDoubleOrNull()?.roundToInt() ?: 0
            ratingDetails.text = "($rating ratings & $totalReviews reviews)"

//            reviewCv.isVisible = productData.product.rating.toString().toDouble() > 0
            rateProduct.isVisible = productData.product.rating.toString()
                .toDouble() <= 0 && productData.product.isPurchased == 1 && productData.product.review_submitted == 0
            viewAllReviews.isVisible =
                !rateProduct.isVisible && productData.product.rating.toString().toDouble() > 0

            reviewRatingBar.rating = productData.product.rating.toString().toFloat()
            reviewRatingCount.text = productData.product.rating.toString().toDouble().toString()
            reviewLbl.text = "Customer Reviews ($totalReviews)"
            reviewRatingDetails.text = "$totalReviews Reviews"


            val priceToShow = productData.product.offer_price.toString().toDoubleOrNull()
                ?: productData.product.actual_price.toString().toDoubleOrNull() ?: 0.0
            productPrice.text = "RM ${formatToTwoDecimalPlaces(priceToShow)}"

            // Set visibility of productListingPrice
            productListingPrice.isVisible =
                productData.product.offer_price != null && productData.product.offer_price.toString() != "false"

            val actualPrice = productData.product.actual_price.toString().toDoubleOrNull()
            productListingPrice.text = "NP: RM ${formatToTwoDecimalPlaces(actualPrice?:0.0)}"

            addToWishlist.setImageResource(if (productData.product.in_wishlist == 1) R.drawable.like_active else R.drawable.ic_fav)

            if (productData.seller_info.isNotEmpty()) {
                productData.seller_info[0].let {
                    storeImage.loadImage(replaceBaseUrl(it.logo))
                    storeName.text = it.store_name
                    chatResponse.text =
                        "${formatToTwoDecimalPlaces(it.postive_review.toDouble()).toDouble()}% Positive ${it.followers} followers"
                }
            }

            setDescriptionAndSpecification()

            if (productData.varaiants_list.isNotEmpty()) {
                val selectedVariantData = productData.varaiants_list[0]
                selectedVariant = selectedVariantData.pro_id.toString()
                soldOut.isVisible = selectedVariantData.is_out_of_stock.toString().toDouble() > 0
                val offerPrice = selectedVariantData.offer_price?.toDoubleOrNull()
                val variantActualPrice = selectedVariantData.actual_price

                productListingPrice.isVisible = offerPrice != null && offerPrice > 0.0
                productPrice.text =
                    "RM ${formatToTwoDecimalPlaces(offerPrice ?: variantActualPrice)}"
                productListingPrice.text = "NP: RM ${formatToTwoDecimalPlaces(variantActualPrice)}"
                selectedVariantData.isSelected = true
                productsVariantsAdapter.differ.submitList(productData.varaiants_list)
            }

            if (productData.product.offer_name == "Flash Sale") {
                flashDealOrOutOfStock.isVisible = true
                setTimer(productData.product.end_time)
            }

            relativeProductListAdapter.differ.submitList(productData.other_products)
            pinCode.clearFocus()
            binding.loader.isVisible = false
        }
        setFrequentlyBoughtProducts()
    }


    private fun setDescriptionAndSpecification() {
        with(binding) {
            descriptionWebView.settings.javaScriptEnabled = true
            val htmlContent = productData.product.long_description

            val descriptionContent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Html.fromHtml(htmlContent, Html.FROM_HTML_MODE_COMPACT)
            } else {
                Html.fromHtml(htmlContent)
            }

            descriptionCv.isVisible =
                !(htmlContent == "false" || htmlContent.isEmpty() || descriptionContent.isEmpty())

            descriptionWebView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
            descriptionWebView.webViewClient = WebViewClient()
            val webViewLayoutParams = binding.descriptionWebView.layoutParams

            descriptionWebView.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    view?.postDelayed({
                        descriptionMeasuredHeight = binding.descriptionWebView.measuredHeight
                        if (descriptionMeasuredHeight < 209) {
                            webViewLayoutParams.height = descriptionMeasuredHeight
                        } else if (descriptionMeasuredHeight > 210) {
                            webViewLayoutParams.height = 210
                        }
                        binding.descriptionWebView.layoutParams = webViewLayoutParams
                        binding.viewMore.isVisible = descriptionMeasuredHeight >= 210
                        binding.viewMoreArrow.isVisible = descriptionMeasuredHeight >= 210
                    }, 500)
                }

                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                }
            }

            val specHtmlContent = productData.product.specification
            val specContent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Html.fromHtml(specHtmlContent, Html.FROM_HTML_MODE_COMPACT)
            } else {
                Html.fromHtml(specHtmlContent)
            }

            if (productData.product.specification == "false" || productData.product.specification.isEmpty() || specContent.isEmpty()) {
                specificationsCv.isVisible = false
                specificationsWebView.isVisible = false
                specificationsDivider.isVisible = false
            } else {
                specificationsCv.isVisible = true
                specificationsWebView.isVisible = true
                specificationsDivider.isVisible = true
                binding.specificationsWebView.settings.javaScriptEnabled = true
                val specificationHtmlContent = productData.product.specification
                val specificationContent =
                    Html.fromHtml(specificationHtmlContent).toString().replace("\\s+".toRegex(), "")
                specificationsCv.isVisible = specificationContent.isNotEmpty()
                specificationsWebView.loadDataWithBaseURL(
                    null,
                    specificationHtmlContent,
                    "text/html",
                    "UTF-8",
                    null
                )
                specificationsWebView.webViewClient = WebViewClient()
            }
        }

    }

    private fun setComboProducts() {
        with(binding) {
            itemsInComboLbl.isVisible = productData.product.combo_products.isNullOrEmpty() != true
            itemsInComboCount.isVisible = productData.product.combo_products.isNullOrEmpty() != true
            comboRv.isVisible = productData.product.combo_products.isNullOrEmpty() != true
            initRecyclerView(requireContext(), comboRv, comboAdapter)

            if (!productData.product.combo_products.isNullOrEmpty()) {
                itemsInComboCount.text = productData.product.combo_products.size.toString()
                comboAdapter.differ.submitList(productData.product.combo_products)
            }

            comboAdapter.setOnItemClickListener { id ->
                val bundle = Bundle().apply { putString("productId", id.toString()) }
                findNavController().navigate(R.id.productDetailsFragment, bundle)
            }
        }
    }

    private fun setFrequentlyBoughtProducts() {
        if (productData.cross_selling_products.isEmpty()) return

        with(binding.frequentlyBoughtItems.frequentlyBoughtItem1) {
            if (productData.product.image?.isNotEmpty() == true) {
                productImage.loadImage(productData.product.image!![0].image)
            }
            productName.text = productData.product.product_name
            productPrice.text = binding.productPrice.text.toString()
            wholeSaleLbl.isVisible = false
            frozenLbl.isVisible = false
            saveLbl.isVisible = false
        }

        if (productData.cross_selling_products.isEmpty()) {
            binding.frequentProducts.isVisible = false
        } else {

            binding.frequentProducts.isVisible = true
            with(binding.frequentlyBoughtItems.frequentlyBoughtItem2) {

                productData.cross_selling_products.getOrNull(0)?.let { crossSellingProduct ->
                    with(crossSellingProduct) {

                        if (crossSellingProduct.image.isNotEmpty()) {
                            productImage.loadImage(replaceBaseUrl(crossSellingProduct.image[0].image))
                        }



                        productName.text = product_name


                        val offerPrice = crossSellingProduct.offer_price.toString().toDoubleOrNull()
                        val actualPrice = crossSellingProduct.actual_price.toString().toDouble()

                        val displayPrice = when {
                            offerPrice != null && offerPrice != 0.0 -> {
                                binding.productListingPrice.isVisible = true
                                offerPrice
                            }

                            else -> {
                                binding.productListingPrice.isVisible = false
                                actualPrice
                            }
                        }

                        productPrice.text = "RM ${formatToTwoDecimalPlaces(displayPrice)}"

                        val mainProductPrice =
                            productData.product.offer_price.toString().toDoubleOrNull()
                                ?: productData.product.actual_price.toString().toDoubleOrNull()
                                ?: 0.0
                        val totalFrequentAmount = mainProductPrice + displayPrice
                        binding.total.text = "RM ${formatToTwoDecimalPlaces(totalFrequentAmount)}"
                    }
                }
                wholeSaleLbl.isVisible = false
                frozenLbl.isVisible = false
                saveLbl.isVisible = false
            }
        }
    }


    private fun setProductAvailability() {
        productData.customer_addr?.let {
            binding.pinCode.setText(it.pincode)
            if (it.country_id != "132") {
                binding.internationalLbl.isVisible = true
                val spannableString = SpannableString(getString(R.string.shippingEnquiry))
                val redColorSpan =
                    ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.red))

                spannableString.setSpan(
                    redColorSpan,
                    spannableString.indexOf("support."),
                    spannableString.indexOf("support.") + "support.".length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                spannableString.setSpan(
                    UnderlineSpan(),
                    spannableString.indexOf("support."),
                    spannableString.indexOf("support.") + "support.".length - 1,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                val boldSpan = StyleSpan(Typeface.BOLD)
                spannableString.setSpan(
                    boldSpan,
                    spannableString.indexOf("support."),
                    spannableString.indexOf("support.") + "support.".length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                binding.internationalLbl.text = spannableString


            } else {
                binding.internationalLbl.isVisible = false
                if (!it.pincode.isNullOrEmpty()) {
                    mainViewModel.checkAvailability(
                        productData.product.product_id.toString(),
                        it.pincode
                    )
                }
            }
        }
    }

    private fun setTimer(endTime: String) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val saleEndTime = dateFormat.parse(endTime) ?: Date()
        val currentTime = Date()
        val timeDifference = saleEndTime.time - currentTime.time
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

    private fun playVideo() {
        exoBuffer.isVisible = true
        exoPlay.isVisible = false
        exoPause.isVisible = false
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
                exoBuffer.isVisible = false
                exoPlay.isVisible = false
                exoPause.isVisible = true
            } else {
                exoBuffer.isVisible = true
                exoPlay.isVisible = true
                exoPause.isVisible = false
            }
        }
    }


    @SuppressLint("SetTextI18n")
    private fun setOnClickListener() {
        with(binding) {
            back.setOnClickListener(this@ProductDetailsFragment)
            viewMore.setOnClickListener(this@ProductDetailsFragment)
            viewMoreArrow.setOnClickListener(this@ProductDetailsFragment)
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
            chat.setOnClickListener(this@ProductDetailsFragment)
            internationalLbl.setOnClickListener(this@ProductDetailsFragment)
            rateProduct.setOnClickListener(this@ProductDetailsFragment)

            exoPlay.setOnClickListener {
                videoPlayer.play()
            }

            exoPause.setOnClickListener {
                videoPlayer.pause()
            }
        }

        vouchersAdapter.setOnItemClickListener {position->
            if(sessionManager.isLogin){
                val couponCode = vouchersAdapter.differ.currentList[position].couponCode
                val clipboardManager =
                    requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clipData = ClipData.newPlainText("text", couponCode)
                clipboardManager.setPrimaryClip(clipData)
                showToast("Coupon code copied")
            }else{
                requireActivity().moveToLogin(sessionManager)

            }
        }

        reviewsAdapter.setOnImageClickListener { images, index -> showImageDialog(images, index) }
        reviewsAdapter.setOnVideoClickListener { video -> showVideoDialog(video) }

        productsImagesAdapter.setOnImageClickListener { data ->
            if (data == videoIcon) {
                binding.productVideo.isVisible = true
                playVideo()
            } else {

                videoPlayer.pause()
                binding.productVideo.isVisible = false
                binding.productImage.loadImage(data)
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
            binding.soldOut.isVisible =
                productData.varaiants_list[position].is_out_of_stock.toString().toDouble() > 0
            if (!productData.varaiants_list[position].offer_price.isNullOrEmpty() && productData.varaiants_list[position].offer_price != "false") {
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

    private fun showImageDialog(images: List<String>, index: Int) {
        var currentIndex = index
        val appCompatDialog = Dialog(requireContext())
        val bindingDialog = ReviewMediaImagesBinding.inflate(layoutInflater)
        appCompatDialog.setContentView(bindingDialog.root)
        appCompatDialog.setCancelable(true)

        bindingDialog.productImage.loadImage(replaceBaseUrl(images[currentIndex]))
        bindingDialog.right.setOnClickListener {

            if (currentIndex < images.size - 1) {
                currentIndex++
                bindingDialog.productImage.loadImage(replaceBaseUrl(images[currentIndex]))
            }
        }

        bindingDialog.left.setOnClickListener {
            if (currentIndex > 0) {
                currentIndex--
                bindingDialog.productImage.loadImage(replaceBaseUrl(images[currentIndex]))
            }
        }

        bindingDialog.close.setOnClickListener { appCompatDialog.dismiss() }
        appCompatDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        appCompatDialog.show()
    }

    private fun showVideoDialog(video: String) {
        val appCompatDialog = Dialog(requireContext())
        val bindingDialog = ReviewMediaVideoBinding.inflate(layoutInflater)
        appCompatDialog.setContentView(bindingDialog.root)
        appCompatDialog.setCancelable(true)

        bindingDialog.reviewVideo.clipToOutline = true
        bindingDialog.reviewVideo.player = reviewPlayer
        val exoPlay: ImageView =
            bindingDialog.reviewVideo.findViewById(androidx.media3.ui.R.id.exo_play)
        val exoPause: ImageView =
            bindingDialog.reviewVideo.findViewById(androidx.media3.ui.R.id.exo_pause)
        val exoBuffer: ProgressBar =
            bindingDialog.reviewVideo.findViewById(androidx.media3.ui.R.id.exo_buffering)
        exoPlay.setOnClickListener {
            playReviewVideo(video)
        }

        exoPause.setOnClickListener {
            reviewPlayer.pause()
        }

        playReviewVideo(video)

        reviewPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                super.onIsPlayingChanged(isPlaying)
                if (isPlaying) {
                    exoBuffer.isVisible = false
                    exoPlay.isVisible = false
                    exoPause.isVisible = true
                } else {
                    exoBuffer.isVisible = true
                    exoPlay.isVisible = true
                    exoPause.isVisible = false
                }
            }
        })


        bindingDialog.close.setOnClickListener { appCompatDialog.dismiss() }
        appCompatDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        appCompatDialog.show()
    }

    private fun showCartQtyDialog() {
        if (!sessionManager.isLogin) {
            showToast("Please login first")
            startActivity(Intent(requireActivity(), LoginActivity::class.java))
            requireActivity().finish()
            return
        }

        val appCompatDialog = Dialog(requireContext())
        val bindingDialog = DialogQtyPopupBinding.inflate(layoutInflater)
        appCompatDialog.setContentView(bindingDialog.root)
        appCompatDialog.setCancelable(true)

        bindingDialog.apply.setOnClickListener {
            addedQty = bindingDialog.qty.text.toString().trim()
            if (addedQty.isEmpty()) {
                showToast("Please enter your required quantity")
                return@setOnClickListener
            }
            addToCart(addedQty)
            appCompatDialog.dismiss()

        }
        bindingDialog.cancel.setOnClickListener { appCompatDialog.dismiss() }
        appCompatDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        appCompatDialog.show()
    }

    private fun playReviewVideo(video: String) {
        val mediaItem: MediaItem = MediaItem.fromUri(video)
        val mediaSource =
            ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem)
        val currentMediaItem: MediaItem? = reviewPlayer.currentMediaItem
        if (currentMediaItem == null || currentMediaItem != mediaSource.mediaItem) {
            try {
                reviewPlayer.stop()
                reviewPlayer.setMediaSource(mediaSource)
                reviewPlayer.prepare()
                reviewPlayer.playWhenReady = true
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } else {
            toggleReviewPlayPause()
        }
    }

    private fun redirectToChat() {
        if (sessionManager.isLogin) {
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
        } else {
            showToast("Please login first")
            startActivity(Intent(requireActivity(), LoginActivity::class.java))
            requireActivity().finish()
        }
    }

    private fun toggleReviewPlayPause() {
        if (reviewPlayer.isPlaying) {
            reviewPlayer.pause()
        } else {
            if (reviewPlayer.currentPosition >= reviewPlayer.duration) {
                reviewPlayer.seekTo(0)
            }
            reviewPlayer.play()
        }
    }

    private fun addToCart(qty: String) {
        if (!sessionManager.isLogin) {
            showToast("Please login first")
            startActivity(Intent(requireActivity(), LoginActivity::class.java))
            requireActivity().finish()
            return
        }

        if (selectedVariant.isNotEmpty()) {
            mainViewModel.addToCart(selectedVariant, qty)
        } else {
            mainViewModel.addToCart(productData.product.product_id.toString(), qty)

        }
    }

    private fun addToCartFrequentlyBought() {
        if (!sessionManager.isLogin) {
            showToast("Please login first")
            startActivity(Intent(requireActivity(), LoginActivity::class.java))
            requireActivity().finish()
            return
        }
        val productIds = selectedVariant.ifEmpty {
            "${productData.product.product_id},${productData.cross_selling_products[0].product_id}"
        }

        mainViewModel.addToCartMultiple(productIds, "1")

    }

    private fun increaseQty() {
        qty = binding.qty.text.toString().toInt() + 1
        mainViewModel.updateCartQty(cartId = cartId, qty = qty)
    }

    private fun decreaseQty() {
        val currentQty = binding.qty.text.toString().toInt()
        val updatedQty = currentQty - 1
        if (updatedQty < 1) {
            mainViewModel.deleterCartProduct(cartId = cartId)
            binding.addToCartMain.makeVisible()
            binding.cartCount.makeGone()
        } else {
            mainViewModel.updateCartQty(cartId = cartId, qty = updatedQty)
            binding.qty.text = updatedQty.toString()
        }
    }

    private fun bulkOrder() {
        if (!sessionManager.isLogin) {
            showToast("Please login first")
            startActivity(Intent(requireActivity(), LoginActivity::class.java))
            requireActivity().finish()
            return
        }

        if (!this::profileData.isInitialized) {
            return
        }
        bulkOrderBottomSheetFragment = BulkOrderBottomSheet(
            profileData,
            productData.product
        ) { _, name, email, qty, phone, dateNeeded, deliveryAddress, remarks ->

            val proId = selectedVariant.ifEmpty { productData.product.product_id.toString() }
            mainViewModel.sendBulkOrderRequest(
                proId,
                name,
                email,
                qty,
                phone,
                dateNeeded,
                deliveryAddress,
                remarks,
                1
            )

        }
        bulkOrderBottomSheetFragment.show(childFragmentManager, "BSDialogFragment")
    }

    private fun viewAllReviews() {
        val bundle = Bundle()
        bundle.putString("productId", productData.product.product_id.toString())
        bundle.putInt("purchased", productData.product.isPurchased)
        bundle.putInt("reviewed", productData.product.review_submitted)
        findNavController().navigate(R.id.customerRatingFragment, bundle)
    }

    private fun visitStore() {
        val bundle = Bundle()
        bundle.putString("storeId", productData.seller_info[0].seller_id.toString())
        findNavController().navigate(R.id.storeFragment, bundle)
    }

    private fun checkPinCode() {
        binding.pinCode.clearFocus()
        val pinCode = binding.pinCode.text.toString()
        if (pinCode.isEmpty()) {
            showToast("Please enter a valid postcode.")
            return
        }
        mainViewModel.checkAvailability(productData.product.product_id.toString(), pinCode)
    }

    private fun descViewMore() {
        val webViewLayoutParams = binding.descriptionWebView.layoutParams
        val isViewLess = binding.viewMore.text == getString(R.string.view_less_lbl)
        if (isViewLess) {
            webViewLayoutParams.height = 210
            binding.viewMore.text = getString(R.string.view_more_lbl)
            binding.viewMoreArrow.rotation = 0f
        } else {
            webViewLayoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            binding.viewMore.text = getString(R.string.view_less_lbl)
            binding.viewMoreArrow.rotation = 180f
        }
        binding.descriptionWebView.layoutParams = webViewLayoutParams
    }

    private fun addToWishList() {
        if (sessionManager.isLogin) {
            if (productData.product.in_wishlist == 1) {
                mainViewModel.removeFromWishList(productData.product.product_id.toString())
                binding.addToWishlist.setImageResource(R.drawable.ic_fav)
            } else {
                mainViewModel.addToWishList(productData.product.product_id.toString())
                binding.addToWishlist.setImageResource(R.drawable.like_active)
            }
        } else {
            showToast("Please login first")
            startActivity(Intent(requireActivity(), LoginActivity::class.java))
            requireActivity().finish()
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.back.id -> findNavController().popBackStack()
            binding.pinCode.id -> scrollToView(binding.pinCode)
            binding.cart.id -> findNavController().navigate(R.id.cartFragment)
            binding.chat.id -> redirectToChat()
            binding.addToCart.id -> addToCartFrequentlyBought()
            binding.addToCartMain.id -> showCartQtyDialog()
            binding.increaseQty.id -> increaseQty()
            binding.decreaseQty.id -> decreaseQty()
            binding.viewAllReviews.id -> viewAllReviews()
            binding.bulkOrder.id -> bulkOrder()
            binding.visitStore.id -> visitStore()
            binding.storeIcon.id -> visitStore()
            binding.checkPinCode.id -> checkPinCode()
            binding.viewMore.id -> descViewMore()
            binding.viewMoreArrow.id -> descViewMore()
            binding.cart.id -> findNavController().navigate(R.id.cartFragment)
            binding.addToWishlist.id -> addToWishList()
            R.id.internationalLbl -> findNavController().navigate(R.id.supportFragment)
            R.id.rateProduct -> {
                val bundle = Bundle()
                bundle.putString("from", "productDetails")
                bundle.putString("productId", productData.product.product_id.toString())

                findNavController().navigate(R.id.submitReviewFragment, bundle)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        videoPlayer.pause()
        reviewPlayer.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        videoPlayer.release()
        reviewPlayer.release()
    }
}
