package com.hfm.customer.ui.dashBoard.home

import android.content.Intent
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.CountDownTimer
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import coil.load
import com.google.android.material.tabs.TabLayoutMediator
import com.hfm.customer.R
import com.hfm.customer.databinding.FragmentHomeBinding
import com.hfm.customer.ui.dashBoard.home.adapter.BrandStoreAdapter
import com.hfm.customer.ui.dashBoard.home.adapter.BrandsAdapter
import com.hfm.customer.ui.dashBoard.home.adapter.FactoryAdapter
import com.hfm.customer.ui.dashBoard.home.adapter.FeatureProductsAdapter
import com.hfm.customer.ui.dashBoard.home.adapter.FlashDealAdapter
import com.hfm.customer.ui.dashBoard.home.adapter.HomeMainBannerAdapter
import com.hfm.customer.ui.dashBoard.home.adapter.HomeMainCatAdapter
import com.hfm.customer.ui.dashBoard.home.adapter.TrendingNowAdapter
import com.hfm.customer.ui.dashBoard.home.model.FlashSale
import com.hfm.customer.ui.loginSignUp.LoginActivity
import com.hfm.customer.utils.Loader
import com.hfm.customer.utils.NoInternetDialog
import com.hfm.customer.utils.PromotionBanner
import com.hfm.customer.utils.Resource
import com.hfm.customer.utils.SessionManager
import com.hfm.customer.utils.cartCount
import com.hfm.customer.utils.initRecyclerView
import com.hfm.customer.utils.netWorkFailure
import com.hfm.customer.utils.replaceBaseUrl
import com.hfm.customer.utils.showToast
import com.hfm.customer.viewModel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlin.math.roundToInt

data class AdsImage(
    val image: Drawable?,
    val name: String
)

@AndroidEntryPoint
class HomeFragment : Fragment(), View.OnClickListener {
    private lateinit var binding: FragmentHomeBinding
    private var currentView: View? = null

    private val mainViewModel: MainViewModel by activityViewModels()

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

    private lateinit var appLoader: Loader
    private lateinit var promotionBanner: PromotionBanner
    private lateinit var noInternetDialog: NoInternetDialog
    @Inject lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_home, container, false)
            binding = FragmentHomeBinding.bind(currentView!!)
            noInternetDialog = NoInternetDialog(requireContext())
            init()
            setObserver()
            setOnClickListener()
        }
        return currentView!!
    }

    private fun init() {
        appLoader = Loader(requireContext())
        binding.loader.isVisible = true
        if(noInternetDialog.isShowing){ noInternetDialog.dismiss() }
        noInternetDialog.setOnDismissListener { init() }
        setMainBanner()
        if(this::promotionBanner.isInitialized){
            if(promotionBanner.isShowing) promotionBanner.dismiss()
        }

        setAdsRv()
        mainViewModel.apply {
            getHomePageData()
            getNotifications()
            getCart()
        }
        appLoader.show()
    }

    private fun setMainBanner() {
        binding.apply {
            initRecyclerView(requireContext(), adsRv, brandsAdapter, true)
            homeMainBanner.apply {
                clipChildren = false
                clipToPadding = false
                offscreenPageLimit = 3
                homeMainBanner.setPageTransformer(customPageTransformer())
                adapter = homeMainBannerAdapter
                currentItem = 1
            }
            val tabLayoutMediator =
                TabLayoutMediator(binding.indicator, binding.homeMainBanner, true) { _, _ -> }
            tabLayoutMediator.attach()
        }
    }

    private fun setPromotionalPopup(promotionImage: String) {
        promotionBanner = PromotionBanner(requireContext(), replaceBaseUrl(promotionImage))
        promotionBanner.show()
    }

    private fun setAdsRv() {
        val adsImages: MutableList<AdsImage> = ArrayList()
        val imageResources = listOf(
            R.drawable.international_shipping,
            R.drawable.vouchers,
            R.drawable.bulk_purchase,
            R.drawable.partner_offers,
            R.drawable.news_events,
            R.drawable.hfm_contest
        )

        val imageTitles = listOf(
            "International shipping",
            "Vouchers",
            "Bulk Purchase",
            "Partner Offers",
            "News Events",
            "HFM Contest"
        )

        for (i in imageResources.indices) {
            val drawable = ContextCompat.getDrawable(requireContext(), imageResources[i])
            val title = imageTitles[i]
            adsImages.add(AdsImage(drawable, title))
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

        mainViewModel.categories.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    binding.loader.isVisible = false
                    appLoader.dismiss()
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
                    appLoader.dismiss()
                    response.data?.data?.let {

                        homeMainBannerAdapter.differ.submitList(it.app_top_banner)
                        if(it.promotion_popup!=null && !it.promotion_popup?.promotion_image.isNullOrEmpty())
                            setPromotionalPopup(response.data.data.promotion_popup.promotion_image)
                        if(it.search_placeholder_text.isNullOrEmpty()){
                            binding.searchBar.text = "Search here.."
                        }else{
                            binding.searchBar.text = it.search_placeholder_text
                        }
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
                    appLoader.dismiss()
                    if(response.data?.httpcode == 200) {
                        setTimer(response.data.data.flash_sale)
                        initRecyclerView(
                            requireContext(),
                            binding.flashDealsProductsRv,
                            flashDealAdapter,
                            true
                        )

                        binding.flashDealsGroup.isVisible = response.data.data.total_products>0
                        if(response.data.data.total_products>0) {
                            flashDealAdapter.differ.submitList(response.data.data.flash_sale.products)
                        }
                    }else{
                        binding.flashDealsGroup.isVisible = false
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

        mainViewModel.homeMiddleBanner.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    binding.loader.isVisible = false
                    binding.secondaryBanner.load(replaceBaseUrl(response.data?.data?.center_left_banner?.get(0)?.media.toString()))
                }
                is Resource.Loading -> Unit
                is Resource.Error -> apiError(response.message)
            }
        }

        mainViewModel.homeWholeSaleProducts.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    binding.loader.isVisible = false
                    appLoader.dismiss()
                    initRecyclerView(requireContext(), binding.factoryDealsRv, factoryAdapter, true)
                    factoryAdapter.differ.submitList(response.data?.data?.flash_sale?.products)
                }
                is Resource.Loading -> Unit
                is Resource.Error -> apiError(response.message)
            }
        }

        mainViewModel.homeBottomBanner.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    binding.loader.isVisible = false
                    appLoader.dismiss()
                    with(binding) {
                        referBanner.load(replaceBaseUrl(response.data?.data?.bottom_left_banner?.get(0)?.media.toString()))
                        adBanner.load(replaceBaseUrl(response.data?.data?.bottom_right_banner?.get(0)?.media.toString()))
                    }
                }
                is Resource.Loading -> Unit
                is Resource.Error -> apiError(response.message)
            }
        }
        mainViewModel.homeBrands.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    binding.loader.isVisible = false
                    appLoader.dismiss()
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
                    appLoader.dismiss()
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
                    appLoader.dismiss()
                    binding.loader.isVisible = false
                    response.data?.let {
                        if(it.httpcode.toString().toDouble().roundToInt() == 200){
                            with(binding){
                                featuresProductLbl.isVisible = it.data.products.isNotEmpty()
                                featuresProductViewAll.isVisible = it.data.products.isNotEmpty()
                                initRecyclerView(requireContext(), binding.featuresProductRv, featureProductsAdapter, true)
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
                    appLoader.dismiss()
                    binding.notificationCount.text = response.data?.data?.notification_count.toString()
                }
                is Resource.Loading -> Unit
                is Resource.Error -> apiError(response.message)
            }
        }
        mainViewModel.cart.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    cartCount.postValue(response.data?.data?.cart_count)
                }
                is Resource.Loading -> Unit
                is Resource.Error -> apiError(response.message)
            }
        }

        cartCount.observe(requireActivity()){ count->
            binding.cartCount.text = count.toString()
        }
    }

    private fun setTimer(flashSale: FlashSale?) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val endTimeString = flashSale?.end_time.toString()
        val endTime = dateFormat.parse(endTimeString) ?: Date()
        val currentTime = Date()
        val timeDifference = endTime.time - currentTime.time
        val countdownTimer = object : CountDownTimer(timeDifference, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                updateCountdownText(millisUntilFinished)
            }
            override fun onFinish() { updateCountdownText(0) }
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
            searchFilter.setOnClickListener(this@HomeFragment)
        }

        homeMainCatAdapter.setOnCategoryClickListener { data ->
            val bundle = Bundle().apply { putString("catId", data.category_id.toString()) }
            findNavController().navigate(R.id.productListFragment, bundle)
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

        trendingNowAdapter.setOnCategoryClickListener {
            val bundle = Bundle()
            bundle.putInt("wholeSale",1)
            findNavController().navigate(R.id.productListFragment,bundle)
        }

        brandsAdapter.setOnItemClickListener {position->
            when(position) {
                0 -> {
                    val bundle = Bundle()
                    bundle.putInt("pageId", 6)
                    findNavController().navigate(R.id.commonPageFragment, bundle)
                }

                1 -> findNavController().navigate(R.id.vouchersFragment)
                2 -> {
                    val bundle = Bundle()
                    bundle.putInt("wholeSale", 1)
                    findNavController().navigate(R.id.productListFragment, bundle)
                }

                3 -> {
                    val bundle = Bundle()
                    bundle.putInt("pageId", 16)
                    findNavController().navigate(R.id.commonPageFragment, bundle)
                }

                4 -> {
                    findNavController().navigate(R.id.blogsFragment)
                }

                5 -> {
                    val bundle = Bundle()
                    bundle.putInt("pageId", 8)
                    findNavController().navigate(R.id.commonPageFragment, bundle)
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
    override fun onClick(v: View?) {
        when (v?.id) {
            binding.notification.id -> {
                if(sessionManager.isLogin) {
                    findNavController().navigate(R.id.notificationFragment)
                }else {
                    startActivity(Intent(requireActivity(), LoginActivity::class.java))
                    requireActivity().finish()
                }
            }
            binding.officialBrandsViewAll.id -> findNavController().navigate(R.id.brandsFragment)
            binding.cart.id -> {
                if(sessionManager.isLogin) {
                    findNavController().navigate(R.id.cartFragment)
                }else {
                    startActivity(Intent(requireActivity(), LoginActivity::class.java))
                    requireActivity().finish()
                }
            }
            binding.searchBar.id -> {
                val bundle = Bundle()
                bundle.putString("searchHint",binding.searchBar.hint.toString())
                findNavController().navigate(R.id.searchFragment,bundle)
            }
            binding.searchFilter.id -> findNavController().navigate(R.id.categoriesFragmentHome)
            binding.factoryDealsViewAll.id -> {
                val bundle = Bundle()
                bundle.putInt("wholeSale",1)
                findNavController().navigate(R.id.productListFragment,bundle)
            }
            binding.flashDealsViewAll.id -> {
                val bundle = Bundle()
                bundle.putInt("wholeSale",1)
                findNavController().navigate(R.id.productListFragment,bundle)
            }
        }
    }
}
