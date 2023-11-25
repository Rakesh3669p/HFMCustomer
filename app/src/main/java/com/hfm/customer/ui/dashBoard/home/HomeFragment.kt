package com.hfm.customer.ui.dashBoard.home

import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import com.google.android.material.tabs.TabLayoutMediator
import com.hfm.customer.R
import com.hfm.customer.commonModel.BannerData
import com.hfm.customer.commonModel.MainBanner
import com.hfm.customer.commonModel.PromotionPopup
import com.hfm.customer.databinding.FragmentHomeBinding
import com.hfm.customer.ui.dashBoard.home.adapter.BrandStoreAdapter
import com.hfm.customer.ui.dashBoard.home.adapter.BrandsAdapter
import com.hfm.customer.ui.dashBoard.home.adapter.FactoryAdapter
import com.hfm.customer.ui.dashBoard.home.adapter.FeatureProductsAdapter
import com.hfm.customer.ui.dashBoard.home.adapter.FlashDealAdapter
import com.hfm.customer.ui.dashBoard.home.adapter.HomeMainBannerAdapter
import com.hfm.customer.ui.dashBoard.home.adapter.HomeMainCatAdapter
import com.hfm.customer.ui.dashBoard.home.adapter.TrendingNowAdapter
import com.hfm.customer.ui.dashBoard.home.model.AdsImage
import com.hfm.customer.ui.dashBoard.home.model.FlashSale
import com.hfm.customer.ui.loginSignUp.LoginActivity
import com.hfm.customer.utils.Loader
import com.hfm.customer.utils.NoInternetDialog
import com.hfm.customer.utils.PromotionBanner
import com.hfm.customer.utils.Resource
import com.hfm.customer.utils.SessionManager
import com.hfm.customer.utils.cartCount
import com.hfm.customer.utils.hideKeyboard
import com.hfm.customer.utils.initRecyclerView
import com.hfm.customer.utils.loadImage
import com.hfm.customer.utils.netWorkFailure
import com.hfm.customer.utils.notificationCount
import com.hfm.customer.utils.replaceBaseUrl
import com.hfm.customer.utils.showToast
import com.hfm.customer.viewModel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlin.math.roundToInt


@AndroidEntryPoint
class HomeFragment : Fragment(), View.OnClickListener {

    private val mainViewModel: MainViewModel by viewModels()

    @Inject
    lateinit var sessionManager: SessionManager

    // Adapters
    @Inject
    lateinit var homeMainCatAdapter: HomeMainCatAdapter

    @Inject
    lateinit var homeMainBannerAdapter: HomeMainBannerAdapter

    @Inject
    lateinit var brandsAdapter: BrandsAdapter

    @Inject
    lateinit var brandStoreAdapter: BrandStoreAdapter

    @Inject
    lateinit var factoryAdapter: FactoryAdapter

    @Inject
    lateinit var featureProductsAdapter: FeatureProductsAdapter

    @Inject
    lateinit var flashDealAdapter: FlashDealAdapter

    @Inject
    lateinit var trendingNowAdapter: TrendingNowAdapter

    // UI components and other variables
    private lateinit var binding: FragmentHomeBinding
    private lateinit var appLoader: Loader
    private var promotionBanner: PromotionBanner? = null
    private lateinit var noInternetDialog: NoInternetDialog

    private var currentView: View? = null
    private var saleTime: String = ""

    private var handler: Handler = Handler(Looper.getMainLooper())
    private var runnable: Runnable? = null
    private val delay = 2000
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_home, container, false)
            binding = FragmentHomeBinding.bind(currentView!!)
            noInternetDialog = NoInternetDialog(requireContext())
            init()
            setAdsRv()
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
        noInternetDialog.setOnDismissListener { init() }
        noInternetDialog.takeIf { it.isShowing }?.dismiss()
        promotionBanner?.takeIf { it.isShowing }?.dismiss()
        mainViewModel.getHomePageData()
        mainViewModel.getNotifications(0)
        mainViewModel.getCart()
        binding.loader.isVisible = true
        requireActivity().hideKeyboard(binding.root)
    }

    private fun setPromotionalPopup(promotionData: PromotionPopup) {
        mainViewModel.bannerActivity(0,"homepage")
        promotionBanner =
            promotionData.promotion_image?.let { replaceBaseUrl(it) }?.let {
                PromotionBanner(requireContext(),
                    it
                ) {
                    val bundle = Bundle()
                    bundle.putString("catId", promotionData.category)
                    bundle.putString("subCatId", promotionData.sub_category)
                    findNavController().navigate(R.id.productListFragment, bundle)
                    promotionBanner?.dismiss()
                }
            }

        promotionBanner?.show()
    }

    private fun setAdsRv() {
        val imageResources = listOf(
            R.drawable.international_shipping,
            R.drawable.vouchers,
            R.drawable.bulk_purchase,
            R.drawable.partner_offers,
            R.drawable.news_events,
            R.drawable.hfm_contest
        )

        val imageTitles = listOf(
            "International\nshipping",
            "Vouchers",
            "Bulk\nPurchase",
            "Partner\nOffers",
            "News &\nEvents",
            "HFM\nContest"
        )

        val adsImages = imageResources.zip(imageTitles) { resourceId, title ->
            val drawable = ContextCompat.getDrawable(requireContext(), resourceId)
            AdsImage(drawable, title)
        }

        brandsAdapter.differ.submitList(adsImages)
    }

    private fun customPageTransformer(): CompositePageTransformer {
        val compositePageTransformer = CompositePageTransformer()
        compositePageTransformer.addTransformer(MarginPageTransformer((10 * Resources.getSystem().displayMetrics.density).toInt()))
        compositePageTransformer.addTransformer { page, position ->
            val r = 1 - kotlin.math.abs(position)
            page.scaleY = ((0.80f + r * 0.10f))
        }
        return compositePageTransformer
    }

    private fun setObserver() {
        mainViewModel.showHomeLoader.observe(viewLifecycleOwner) { showLoader ->
            if (showLoader) {
                appLoader.show()
            } else {
                appLoader.dismiss()
            }
        }
        mainViewModel.categories.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    binding.loader.isVisible = false
                    initRecyclerView(
                        requireContext(),
                        binding.homeMainCategoriesRv,
                        homeMainCatAdapter,
                        true
                    )
                    homeMainCatAdapter.differ.submitList(response.data?.data?.cat_subcat)
                }

                is Resource.Loading -> Unit
                is Resource.Error -> {
                    if (response.message == netWorkFailure) {
                        noInternetDialog.show()
                    }
                }
            }
        }

        mainViewModel.homeMainBanner.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    binding.loader.isVisible = false
                    response.data?.data?.let {
                        setBanners(it)
                    }
                }

                is Resource.Loading -> Unit
                is Resource.Error -> {
                    if (response.message == netWorkFailure) {
                        noInternetDialog.show()
                    }
                }
            }
        }

        mainViewModel.homeFlashSaleProducts.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    binding.loader.isVisible = false
                    if (response.data?.httpcode == 200) {
                        saleTime = response.data.data.flash_sale.end_time
                        setTimer(response.data.data.flash_sale)
                        initRecyclerView(
                            requireContext(),
                            binding.flashDealsProductsRv,
                            flashDealAdapter,
                            true
                        )

                        binding.flashDealsGroup.isVisible = response.data.data.total_products > 0
                        if (response.data.data.total_products > 0) {
                            flashDealAdapter.differ.submitList(response.data.data.flash_sale.products)
                        }
                    } else {
                        binding.flashDealsGroup.isVisible = false
                    }
                }

                is Resource.Loading -> Unit
                is Resource.Error -> apiError(response.message)
            }
        }

        mainViewModel.homeMiddleBanner.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    binding.loader.isVisible = false
                    if (response.data?.httpcode == 200) {
                        if (response.data.data.center_left_banner.isNotEmpty()) {
                            val imageUrl =
                                replaceBaseUrl(response.data.data.center_left_banner[0].media)
                            binding.secondaryBanner.loadImage(imageUrl)
                            binding.secondaryBanner.setOnClickListener {
                                if (response.data.data.center_left_banner[0].link_type == "product_list") {
                                    val bundle = Bundle()
                                    bundle.putString(
                                        "catId",
                                        response.data.data.center_left_banner[0].category
                                    )
                                    bundle.putString(
                                        "subCatId",
                                        response.data.data.center_left_banner[0].subcategory_id
                                    )
                                    findNavController().navigate(R.id.productListFragment, bundle)
                                } else {
                                    val bundle = Bundle().apply {
                                        putString(
                                            "productId",
                                            response.data.data.center_left_banner[0].product_id
                                        )
                                    }
                                    findNavController().navigate(
                                        R.id.productDetailsFragment,
                                        bundle
                                    )
                                }
                            }
                        } else {
                            binding.secondaryBanner.isVisible = false
                        }
                    }
                }

                is Resource.Loading -> Unit
                is Resource.Error -> apiError(response.message)
            }
        }

        mainViewModel.homeWholeSaleProducts.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    binding.loader.isVisible = false
                    initRecyclerView(requireContext(), binding.factoryDealsRv, factoryAdapter, true)
                    factoryAdapter.differ.submitList(response.data?.data?.flash_sale?.products)
                }

                is Resource.Loading -> Unit
                is Resource.Error -> apiError(response.message)
            }
        }

        mainViewModel.homeBrands.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    binding.loader.isVisible = false
                    initRecyclerView(requireContext(), binding.brandsRv, brandStoreAdapter, true)
                    brandStoreAdapter.differ.submitList(response.data?.data?.brands)
                }

                is Resource.Loading -> Unit
                is Resource.Error -> apiError(response.message)
            }
        }
        mainViewModel.homeTrendingNow.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    binding.loader.isVisible = false
                    initRecyclerView(
                        requireContext(),
                        binding.trendingNowRv,
                        trendingNowAdapter,
                        true
                    )
                    trendingNowAdapter.differ.submitList(response.data?.data?.events)
                }

                is Resource.Loading -> Unit
                is Resource.Error -> apiError(response.message)
            }
        }
        mainViewModel.homeFeatureProducts.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    binding.loader.isVisible = false
                    response.data?.let {
                        if (it.httpcode.toString().toDouble().roundToInt() == 200) {
                            with(binding) {
                                featuresProductLbl.isVisible = it.data.products.isNotEmpty()
                                featuresProductViewAll.isVisible = it.data.products.isNotEmpty()
                                initRecyclerView(
                                    requireContext(),
                                    binding.featuresProductRv,
                                    featureProductsAdapter,
                                    true
                                )
                                featureProductsAdapter.differ.submitList(it.data.products)
                            }
                        }
                    }
                }

                is Resource.Loading -> Unit
                is Resource.Error -> apiError(response.message)
            }
        }
        mainViewModel.notifications.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    if (response.data?.httpcode == "200") {
                        val unreadCount = response.data.data.unread_count
                        notificationCount.postValue(unreadCount)
                        /*binding.notificationCountBg.isVisible = unreadCount > 0
                        binding.notificationCount.isVisible = unreadCount > 0
                        binding.notificationCount.text = unreadCount.toString()*/
                    } else {
                        binding.notificationCountBg.isVisible = false
                        binding.notificationCount.isVisible = false
                    }
                }

                is Resource.Loading -> Unit
                is Resource.Error -> apiError(response.message)
            }
        }

        mainViewModel.cart.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    if (response.data?.httpcode == 200)
                        cartCount.postValue(response.data.data.cart_count)
                }

                is Resource.Loading -> Unit
                is Resource.Error -> apiError(response.message)
            }
        }

        cartCount.observe(requireActivity()) { count ->
            binding.cartCountBg.isVisible = count > 0
            binding.cartCount.isVisible = count > 0
            binding.cartCount.text = count.toString()

        }

        notificationCount.observe(requireActivity()) { count ->
            binding.notificationCountBg.isVisible = count > 0
            binding.notificationCount.isVisible = count > 0
            binding.notificationCount.text = count.toString()

        }
    }

    private fun setBanners(bannerData: BannerData) {
        with(binding) {
            initRecyclerView(requireContext(), adsRv, brandsAdapter, true)
            homeMainBanner.apply {
                clipChildren = false
                clipToPadding = false
                offscreenPageLimit = 3
                homeMainBanner.setPageTransformer(customPageTransformer())
                adapter = homeMainBannerAdapter
                currentItem = 1
            }
            homeMainBannerAdapter.differ.submitList(bannerData.app_top_banner)
            val tabLayoutMediator = TabLayoutMediator(indicator, homeMainBanner, true) { _, _ -> }
            tabLayoutMediator.attach()
            homeMainBanner.currentItem = 1
            homeMainBannerAdapter.setOnItemClickListener { catId, subCatId, productId, linkType ->
                if (linkType == "product_list") {
                    val bundle = Bundle()
                    bundle.putString("catId", catId)
                    bundle.putString("subCatId", subCatId)
                    findNavController().navigate(R.id.productListFragment, bundle)
                } else {
                    val bundle = Bundle().apply { putString("productId", productId) }
                    findNavController().navigate(R.id.productDetailsFragment, bundle)
                }
            }
            startCarousel(bannerData.app_top_banner)

            bannerData.app_bottom_banner.forEachIndexed { index, bottomBanner ->
                when (index) {
                    0 -> {
                        referBanner.isVisible = true
                        referBanner.loadImage(replaceBaseUrl(bottomBanner.media))
                        referBanner.setOnClickListener {
                            if (bottomBanner.link_type == "product_list") {
                                val bundle = Bundle()
                                bundle.putString("catId", bottomBanner.category)
                                bundle.putString("subCatId", bottomBanner.sub_category)
                                findNavController().navigate(R.id.productListFragment, bundle)
                            } else {
                                val bundle = Bundle().apply {
                                    putString(
                                        "productId",
                                        bottomBanner.product_id
                                    )
                                }
                                findNavController().navigate(R.id.productDetailsFragment, bundle)
                            }
                        }
                    }

                    1 -> {
                        adBanner.isVisible = true
                        adBanner.loadImage(replaceBaseUrl(bottomBanner.media))
                        adBanner.setOnClickListener {
                            if (bottomBanner.link_type == "product_list") {
                                val bundle = Bundle()
                                bundle.putString("catId", bottomBanner.category)
                                bundle.putString("subCatId", bottomBanner.sub_category)
                                findNavController().navigate(R.id.productListFragment, bundle)
                            } else {
                                val bundle = Bundle().apply {
                                    putString(
                                        "productId",
                                        bottomBanner.product_id
                                    )
                                }
                                findNavController().navigate(R.id.productDetailsFragment, bundle)
                            }
                        }
                    }
                }
            }
        }

        if (!bannerData.promotion_popup.promotion_image.isNullOrEmpty() && bannerData.promotion_popup.promo_visibility==1) {
            setPromotionalPopup(bannerData.promotion_popup)
        }

        sessionManager.searchPlaceHolder = bannerData.search_placeholder_text
        binding.searchBar.text =
            if (sessionManager.searchPlaceHolder.isEmpty()) "Search here.." else bannerData.search_placeholder_text
    }


    private fun startCarousel(appTopBanner: List<MainBanner>) {
        runnable = Runnable {
            val currentItem = binding.homeMainBanner.currentItem
            val itemCount = appTopBanner.size

            val nextItem = if (currentItem == itemCount - 1) 0 else currentItem + 1
            binding.homeMainBanner.setCurrentItem(nextItem, true)

            handler.postDelayed(runnable!!, delay.toLong())
        }
        handler.postDelayed(runnable!!, 2000)
    }

    private fun setTimer(flashSale: FlashSale?) {
        if (flashSale?.end_time.isNullOrEmpty()) return
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val endTimeString = flashSale?.end_time.toString()
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
        binding.flashDealsTimer.day.text = String.format(Locale.getDefault(), "%02d", days)
        binding.flashDealsTimer.hour.text = String.format(Locale.getDefault(), "%02d", hours)
        binding.flashDealsTimer.minutes.text = String.format(Locale.getDefault(), "%02d", minutes)
        binding.flashDealsTimer.seconds.text = String.format(Locale.getDefault(), "%02d", seconds)
    }

    private fun setOnClickListener() {
        with(binding) {
            notification.setOnClickListener(this@HomeFragment)
            officialBrandsViewAll.setOnClickListener(this@HomeFragment)
            cart.setOnClickListener(this@HomeFragment)
            notification.setOnClickListener(this@HomeFragment)
            searchBar.setOnClickListener(this@HomeFragment)
            searchFilter.setOnClickListener(this@HomeFragment)
            factoryDealsViewAll.setOnClickListener(this@HomeFragment)
            flashDealsViewAll.setOnClickListener(this@HomeFragment)
            featuresProductViewAll.setOnClickListener(this@HomeFragment)
            searchFilter.setOnClickListener(this@HomeFragment)
            secondaryBanner.setOnClickListener(this@HomeFragment)
        }

        homeMainCatAdapter.setOnCategoryClickListener { data ->
            val bundle = Bundle().apply { putString("catId", data.category_id.toString()) }
            findNavController().navigate(R.id.productListFragment, bundle)
        }

        featureProductsAdapter.setOnCategoryClickListener { data ->
            val bundle = Bundle().apply { putString("productId", data.product_id.toString()) }
            findNavController().navigate(R.id.productDetailsFragment, bundle)
        }

        flashDealAdapter.setOnProductClickListener {
            val bundle = Bundle().apply { putString("productId", it.toString()) }
            findNavController().navigate(R.id.productDetailsFragment, bundle)
        }

        factoryAdapter.setOnProductClickListener {
            val bundle = Bundle().apply { putString("productId", it.toString()) }
            findNavController().navigate(R.id.productDetailsFragment, bundle)
        }

        brandStoreAdapter.setOnBrandClickListener {
            val bundle = Bundle().apply { putString("brandId", it.toString()) }
            findNavController().navigate(R.id.productListFragment, bundle)
        }

        trendingNowAdapter.setOnCategoryClickListener { catId, subCatId ,productId,linkType->
            if(linkType=="product_list"){
                val bundle = Bundle()
                bundle.putString("catId", catId)
                bundle.putString("subCatId", subCatId)
                findNavController().navigate(R.id.productListFragment, bundle)
            }else{
                val bundle = Bundle().apply {
                    putString(
                        "productId",
                        productId
                    )
                }
                findNavController().navigate(R.id.productDetailsFragment, bundle)
            }

        }

        brandsAdapter.setOnItemClickListener { position ->
            when (position) {
                0 -> {
                    val bundle = Bundle()
                    bundle.putInt("pageId", 6)
                    findNavController().navigate(R.id.commonPageFragment, bundle)
                }

                1 -> {
                    if (!sessionManager.isLogin) {
                        showToast("Please login first")
                        startActivity(Intent(requireActivity(), LoginActivity::class.java))
                        requireActivity().finish()
                        return@setOnItemClickListener
                    }
                    findNavController().navigate(R.id.vouchersFragment)
                }

                2 -> {
                    val bundle = Bundle()
                    bundle.putInt("wholeSale", 1)
                    findNavController().navigate(R.id.productListFragment, bundle)
                }

                3 -> {
                    val bundle = Bundle()
                    bundle.putString("title", "Partner Offers")
                    bundle.putInt("pageId", 7)
                    findNavController().navigate(R.id.blogsFragment, bundle)
                }

                4 -> {
                    val bundle = Bundle()
                    bundle.putString("title", "News & Events")
                    bundle.putInt("pageId", 5)
                    findNavController().navigate(R.id.blogsFragment, bundle)
                }

                5 -> {
                    val bundle = Bundle()
                    bundle.putString("title", "HFM Contest")
                    bundle.putInt("pageId", 6)
                    findNavController().navigate(R.id.blogsFragment, bundle)
                }
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

    private fun navigateToFragments(id:Int){
        if (sessionManager.isLogin) {
            findNavController().navigate(id)
        } else {
            startActivity(Intent(requireActivity(), LoginActivity::class.java))
            requireActivity().finish()
        }
    }
    override fun onClick(v: View?) {
        when (v?.id) {
            binding.notification.id -> navigateToFragments(R.id.notificationFragment)
            binding.officialBrandsViewAll.id -> findNavController().navigate(R.id.brandsFragment)
            binding.cart.id -> navigateToFragments(R.id.cartFragment)
            binding.searchFilter.id -> findNavController().navigate(R.id.categoriesFragmentHome)

            binding.searchBar.id -> {
                val bundle = Bundle()
                bundle.putString("searchHint", binding.searchBar.text.toString())
                findNavController().navigate(R.id.searchFragment, bundle)
            }

            binding.factoryDealsViewAll.id -> {
                val bundle = Bundle()
                bundle.putInt("wholeSale", 1)
                findNavController().navigate(R.id.productListFragment, bundle)
            }

            binding.featuresProductViewAll.id -> {
                val bundle = Bundle()
                findNavController().navigate(R.id.productListFragment, bundle)
            }

            binding.flashDealsViewAll.id -> {
                val bundle = Bundle()
                bundle.putInt("flashSale", 1)
                bundle.putString("endTime", saleTime)
                findNavController().navigate(R.id.productListFragment, bundle)
            }
        }
    }
}
