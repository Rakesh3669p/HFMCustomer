package com.hfm.customer.ui.dashBoard.home

import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.SpannableStringBuilder
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
        noInternetDialog.setOnDismissListener {
            init()
        }
//        promotionBanner = PromotionBanner(requireContext(), "")
//        promotionBanner.show()
        val imageBanners: MutableList<Image> = ArrayList()
        imageBanners.add(
            Image(
                "https://images.unsplash.com/photo-1692855676269-487641edd584?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxlZGl0b3JpYWwtZmVlZHwxMHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=500&q=60",
                ""
            )
        )
        imageBanners.add(
            Image(
                "https://images.unsplash.com/photo-1692607431253-8225c0e89f7d?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxlZGl0b3JpYWwtZmVlZHwyMHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=500&q=60",
                ""
            )
        )
        imageBanners.add(
            Image(
                "https://images.unsplash.com/photo-1691760300575-6cce80be7541?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxlZGl0b3JpYWwtZmVlZHwzOXx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=500&q=60",
                ""
            )
        )
        imageBanners.add(
            Image(
                "https://images.unsplash.com/photo-1691760300575-6cce80be7541?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxlZGl0b3JpYWwtZmVlZHwzOXx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=500&q=60",
                ""
            )
        )
        imageBanners.add(
            Image(
                "https://images.unsplash.com/photo-1692607431253-8225c0e89f7d?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxlZGl0b3JpYWwtZmVlZHwyMHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=500&q=60",
                ""
            )
        )
        imageBanners.add(
            Image(
                "https://plus.unsplash.com/premium_photo-1692007370455-cafc274e0796?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxlZGl0b3JpYWwtZmVlZHw0N3x8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=500&q=60",
                ""
            )
        )
        imageBanners.add(
            Image(
                "https://images.unsplash.com/photo-1692855676269-487641edd584?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxlZGl0b3JpYWwtZmVlZHwxMHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=500&q=60",
                ""
            )
        )


        val adsImages: MutableList<AdsImage> = ArrayList()
        adsImages.add(
            AdsImage(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.international_shipping
                ), "International shipping"
            )
        )
        adsImages.add(
            AdsImage(
                ContextCompat.getDrawable(requireContext(), R.drawable.vouchers),
                "Vouchers"
            )
        )
        adsImages.add(
            AdsImage(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.bulk_purchase
                ), "Bulk Purchase"
            )
        )
        adsImages.add(
            AdsImage(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.partner_offers
                ), "Partner Offers"
            )
        )
        adsImages.add(
            AdsImage(
                ContextCompat.getDrawable(requireContext(), R.drawable.news_events),
                "News Events"
            )
        )
        adsImages.add(
            AdsImage(
                ContextCompat.getDrawable(requireContext(), R.drawable.hfm_contest),
                "HFM Contest"
            )
        )
        brandsAdapter.differ.submitList(adsImages)

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


        mainViewModel.getHomePageData()
        appLoader = Loader(requireContext())
        appLoader.show()


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

    private fun setOnClickListener() {
        with(binding) {
            notification.setOnClickListener(this@HomeFragment)
            officialBrandsViewAll.setOnClickListener(this@HomeFragment)
        }

        homeMainCatAdapter.setOnCategoryClickListener { data ->
            val bundle = Bundle().apply { putString("catId", data.category_id.toString()) }
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
