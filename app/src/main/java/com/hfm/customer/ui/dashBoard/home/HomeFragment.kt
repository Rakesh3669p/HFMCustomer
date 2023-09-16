package com.hfm.customer.ui.dashBoard.home

import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.CountDownTimer
import android.text.SpannableStringBuilder
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.PopupMenu.OnDismissListener
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
import com.hfm.customer.ui.dashBoard.DashBoardActivity
import com.hfm.customer.ui.dashBoard.home.adapter.BrandStoreAdapter
import com.hfm.customer.ui.dashBoard.home.adapter.BrandsAdapter
import com.hfm.customer.ui.dashBoard.home.adapter.FactoryAdapter
import com.hfm.customer.ui.dashBoard.home.adapter.FeatureProductsAdapter
import com.hfm.customer.ui.dashBoard.home.adapter.FlashDealAdapter
import com.hfm.customer.ui.dashBoard.home.adapter.HomeMainBannerAdapter
import com.hfm.customer.ui.dashBoard.home.adapter.HomeMainCatAdapter
import com.hfm.customer.ui.dashBoard.home.adapter.TrendingNowAdapter
import com.hfm.customer.ui.dashBoard.home.model.FlashSale
import com.hfm.customer.ui.dashBoard.home.model.Image
import com.hfm.customer.utils.Loader
import com.hfm.customer.utils.NoInternetDialog
import com.hfm.customer.utils.PromotionBanner
import com.hfm.customer.utils.Resource
import com.hfm.customer.utils.initRecyclerView
import com.hfm.customer.utils.isInternetAvailable
import com.hfm.customer.utils.netWorkFailure
import com.hfm.customer.utils.showToast
import com.hfm.customer.viewModel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject


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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_home, container, false)
            binding = FragmentHomeBinding.bind(currentView!!)
            init()
            setObserver()
            setOnClickListener()
        }
        return currentView!!
    }

    private fun init() {
        noInternetDialog = NoInternetDialog(requireContext())
        binding.loader.isVisible = true
        while (noInternetDialog.isShowing) {
            noInternetDialog.dismiss()
        }
        noInternetDialog.setOnDismissListener { init() }
        setMainBanner()
        setPromotionalPopup()
        setAdsRv()
        mainViewModel.getHomePageData()
        appLoader = Loader(requireContext())
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

    private fun setPromotionalPopup() {
        val originalPopup =
            "https://uat.hfm.synuos.com/QA/admin/uploads/storage/app/public/promotion/1/1693369146.png"
        val replacedPopup = originalPopup.replace(
            "https://uat.hfm.synuos.com",
            "http://4.194.191.242"
        )
        promotionBanner = PromotionBanner(requireContext(), replacedPopup)
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
                    homeMainBannerAdapter.differ.submitList(response.data?.data?.main_banner)
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
                    setTimer(response.data?.data?.flash_sale)
                    initRecyclerView(
                        requireContext(),
                        binding.flashDealsProductsRv,
                        flashDealAdapter,
                        true
                    )
                    flashDealAdapter.differ.submitList(response.data?.data?.flash_sale?.products)
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
                    val bannerUrl = response.data?.data?.center_left_banner?.get(0)?.media
                    val originalBannerUrl = bannerUrl.toString()
                    val banner = originalBannerUrl.replace(
                        "https://uat.hfm.synuos.com",
                        "http://4.194.191.242"
                    )
                    binding.secondaryBanner.load(banner)
                }

                is Resource.Loading -> Unit
                is Resource.Error -> {
                    appLoader.dismiss()
                    if (response.message == netWorkFailure) {
                        noInternetDialog.show()
                    }
                }
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
                is Resource.Error -> {
                    appLoader.dismiss()
                    if (response.message == netWorkFailure) {
                        noInternetDialog.show()
                    }
                }
            }
        }
        mainViewModel.homeBottomBanner.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    binding.loader.isVisible = false
                    appLoader.dismiss()
                    with(binding) {

                        val originalBannerLeftUrl =
                            response.data?.data?.bottom_left_banner?.get(0)?.media
                        val bannerLeft = originalBannerLeftUrl?.replace(
                            "https://uat.hfm.synuos.com",
                            "http://4.194.191.242"
                        )

                        val originalBannerRightUrl =
                            response.data?.data?.bottom_right_banner?.get(0)?.media
                        val bannerRight = originalBannerRightUrl?.replace(
                            "https://uat.hfm.synuos.com",
                            "http://4.194.191.242"
                        )



                        referBanner.load(bannerLeft)
                        adBanner.load(bannerRight)
                    }
                }

                is Resource.Loading -> Unit
                is Resource.Error -> {
                    appLoader.dismiss()
                    if (response.message == netWorkFailure) {
                        noInternetDialog.show()
                    }
                }
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
                is Resource.Error -> {
                    appLoader.dismiss()
                    if (response.message == netWorkFailure) {
                        noInternetDialog.show()
                    }
                }
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
                is Resource.Error -> {
                    appLoader.dismiss()
                    if (response.message == netWorkFailure) {
                        noInternetDialog.show()
                    }
                }
            }
        }
        mainViewModel.homeFeatureProducts.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    binding.loader.isVisible = false
                    initRecyclerView(
                        requireContext(),
                        binding.featuresProductRv,
                        featureProductsAdapter,
                        true
                    )
                    featureProductsAdapter.differ.submitList(response.data?.data?.products)
                }

                is Resource.Loading -> Unit
                is Resource.Error -> {
                    appLoader.dismiss()
                    if (response.message == netWorkFailure) {
                        noInternetDialog.show()
                    }
                }
            }
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

            searchFilter.setOnClickListener {
                findNavController().navigate(R.id.categoriesFragmentHome)
            }
        }
        homeMainCatAdapter.setOnCategoryClickListener { data ->
            val bundle = Bundle().apply { putString("catId", data.category_id.toString()) }
            findNavController().navigate(R.id.productListFragment, bundle)
        }

        flashDealAdapter.setOnProductClickListener {
            val bundle = Bundle().apply { putString("productId", it.toString()) }
            findNavController().navigate(R.id.productDetailsFragment, bundle)
        }

        brandStoreAdapter.setOnBrandClickListener {
            val bundle = Bundle().apply { putString("brandId", it.toString()) }
            findNavController().navigate(R.id.productListFragment, bundle)
        }


    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.notification.id -> findNavController().navigate(R.id.notificationFragment)
            binding.officialBrandsViewAll.id -> findNavController().navigate(R.id.brandsFragment)
        }
    }
}
