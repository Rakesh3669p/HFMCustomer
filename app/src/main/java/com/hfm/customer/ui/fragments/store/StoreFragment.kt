package com.hfm.customer.ui.fragments.store

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.hfm.customer.R
import com.hfm.customer.databinding.FragmentStoreBinding
import com.hfm.customer.ui.fragments.store.adapter.StorePagerAdapter
import com.hfm.customer.ui.fragments.store.model.StoreData
import com.hfm.customer.ui.loginSignUp.LoginActivity
import com.hfm.customer.utils.Loader
import com.hfm.customer.utils.NoInternetDialog
import com.hfm.customer.utils.PromotionBanner
import com.hfm.customer.utils.Resource
import com.hfm.customer.utils.SessionManager
import com.hfm.customer.utils.clearSearch
import com.hfm.customer.utils.formatToTwoDecimalPlaces
import com.hfm.customer.utils.loadImage
import com.hfm.customer.utils.netWorkFailure
import com.hfm.customer.utils.replaceBaseUrl
import com.hfm.customer.utils.showToast
import com.hfm.customer.viewModel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.math.roundToInt

@AndroidEntryPoint
class StoreFragment : Fragment(), View.OnClickListener {

    companion object {
        var searchValue = ""
        var categoryIdStore = ""
        var subCatId = ""
    }

    private var typeSearched: Boolean = false
    private lateinit var viewPagerAdapter: StorePagerAdapter

    private lateinit var storeData: StoreData

    private var storeId: String = ""
    private lateinit var binding: FragmentStoreBinding
    private var currentView: View? = null
    private lateinit var promotionBanner: PromotionBanner
    private val mainViewModel: MainViewModel by viewModels()
    private lateinit var noInternetDialog: NoInternetDialog
    private lateinit var appLoader: Loader

    @Inject
    lateinit var sessionManager: SessionManager
    private var storeDetailsAlreadySetted = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_store, container, false)
            binding = FragmentStoreBinding.bind(currentView!!)
            init()
            setOnClickListener()
        }
        return currentView!!
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE and WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setObserver()
        setSearchView()

        arguments?.let { storeId = it.getString("storeId").toString() }
        mainViewModel.getStoreDetails(storeId)
    }

    private fun setSearchView() {


        binding.search.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchValue = binding.search.text.toString().lowercase()
                if (this::viewPagerAdapter.isInitialized) {
                    mainViewModel.getStoreProducts(
                        storeId.toDouble().roundToInt(),
                        searchValue,
                        categoryIdStore
                    )
                }

                return@setOnEditorActionListener true
            }
            false
        }
        binding.search.doOnTextChanged { text, _, _, _ ->
            binding.clearSearch.isVisible = !text.isNullOrEmpty()
            searchValue = binding.search.text.toString().lowercase()
            if (text.toString().length > 2) {
                mainViewModel.getStoreProducts(
                    storeId.toDouble().roundToInt(),
                    searchValue,
                    categoryIdStore
                )
                typeSearched = true
            } else if (text.isNullOrEmpty()) {

                mainViewModel.getStoreProducts(
                    storeId.toDouble().roundToInt(),
                    searchValue,
                    categoryIdStore
                )
            }

        }

    }


    private fun init() {
        binding.storeDataGroup.isVisible = false
        appLoader = Loader(requireContext())
        noInternetDialog = NoInternetDialog(requireContext())
        mainViewModel.getProfile()
    }


    private fun clearSearchValue() {
        searchValue = ""
        binding.search.setText("")
    }

    private fun setTabLayoutAndViewPager(data: StoreData) {
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Home"))
        binding.tabLayout.addTab(
            binding.tabLayout.newTab().setText("All Products(${data.total_products})")
        )
        binding.tabLayout.addTab(
            binding.tabLayout.newTab()
                .setText("Reviews Ratings(${data.shop_detail[0].store_prd_rating})")
        )
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("About"))


        val fragmentManager = childFragmentManager
        viewPagerAdapter = StorePagerAdapter(fragmentManager, lifecycle, data)
        binding.storeVp.adapter = viewPagerAdapter
        binding.storeVp.isSaveEnabled = false
        binding.storeVp.isUserInputEnabled = false
        binding.storeVp.currentItem = 1
        binding.tabLayout.selectTab(binding.tabLayout.getTabAt(1))

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                binding.storeVp.currentItem = tab!!.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

        })


        binding.storeVp.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                binding.tabLayout.selectTab(binding.tabLayout.getTabAt(position))
            }
        })

    }


    private fun setObserver() {

        clearSearch.observe(viewLifecycleOwner) {
            if (it) clearSearchValue()
        }

        mainViewModel.storeDetails.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    if (response.data?.httpcode == 200) {
                        storeData = response.data.data
                        if (!storeDetailsAlreadySetted) {
                            setStoreDetails()
                        }
                    } else {
                        showToast(response.data?.status.toString())
                    }
                }

                is Resource.Loading -> if (!storeDetailsAlreadySetted)
                    appLoader.show()

                is Resource.Error -> apiError(response.message)
            }
        }

        mainViewModel.storeProducts.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
//                    appLoader.dismiss()
                    if (response.data?.httpcode == 200) {
                        storeData.product = response.data.data.product
                        val productListFragment = viewPagerAdapter.getFragment(1) as? StoreProductListFragment
                        productListFragment?.updateData(storeData)
                        binding.storeVp.currentItem = 1


                    } else {
                        showToast(response.data?.status.toString())
                    }
                }

                is Resource.Loading -> Unit
                is Resource.Error -> apiError(response.message)
            }
        }

        mainViewModel.followShop.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    when (response.data?.httpcode) {
                        200 -> {
                            storeData.shop_detail[0].is_following = 1
                            binding.follow.text =
                                if (storeData.shop_detail[0].is_following == 0) "Follow" else "Following"
                        }

                        401 -> {
                            sessionManager.isLogin = false
                            startActivity(Intent(requireActivity(), LoginActivity::class.java))
                            requireActivity().finish()
                        }

                        else -> {
                            showToast(response.data?.status.toString())
                        }
                    }
                }

                is Resource.Loading -> Unit
                is Resource.Error -> apiError(response.message)
            }
        }

        mainViewModel.unFollowShop.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    if (response.data?.httpcode == 200) {
                        storeData.shop_detail[0].is_following = 0
                        binding.follow.text =
                            if (storeData.shop_detail[0].is_following == 0) "Follow" else "Following"


                    } else {
                        showToast(response.data?.status.toString())
                    }
                }

                is Resource.Loading -> Unit
                is Resource.Error -> apiError(response.message)
            }
        }

    }


    private fun setStoreDetails() {
        storeDetailsAlreadySetted = true
        with(binding) {
            if (storeData.shop_detail.isNotEmpty()) {
                storeData.shop_detail[0].let { shopDetail ->
                    promotionBanner = PromotionBanner(
                        requireContext(),
                        replaceBaseUrl(shopDetail.promotion_image)
                    ) {
                        promotionBanner.dismiss()
                        if (shopDetail.promotion_product_id > 0) {
                            val bundle = Bundle().apply {
                                putString(
                                    "productId",
                                    shopDetail.promotion_product_id.toString()
                                )
                            }
                            findNavController().navigate(R.id.productDetailsFragment, bundle)
                        } else {

                            mainViewModel.getStoreProducts(
                                storeId.toDouble().roundToInt(),
                                shopDetail.promotion_category,
                                shopDetail.promotion_sub_category
                            )
                        }

                    }

                    if (!shopDetail.promotion_image.isNullOrEmpty() && shopDetail.promo_visibility == 1) {
                        promotionBanner.show()
                        mainViewModel.bannerActivity(sellerId = shopDetail.seller_id?:0, "seller")
                    }

                    storeName.text = shopDetail.store_name
                    storeImage.loadImage(replaceBaseUrl(shopDetail.logo))
                    storeBanner.loadImage(replaceBaseUrl(shopDetail.banner))
                    val followersAndChat = "${
                        formatToTwoDecimalPlaces(
                            shopDetail.postive_review.toString().toDouble()
                        )
                    }% Positive | ${shopDetail.followers} followers"

                    storeFollowers.text = followersAndChat
                    follow.text = if (shopDetail.is_following == 0) "Follow" else "Following"

                    val white = ContextCompat.getColor(requireContext(), R.color.white)
                    val black = ContextCompat.getColor(requireContext(), R.color.black)
                    if (shopDetail.banner.isNotEmpty()) {
                        storeName.setTextColor(white)
                        storeFollowers.setTextColor(white)
                        val drawable = ColorDrawable(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.blackTrans200
                            )
                        )
                        storeBanner.foreground = drawable
                    } else {
                        storeName.setTextColor(black)
                        storeFollowers.setTextColor(black)

                    }
                }

            }
        }
        binding.storeDataGroup.isVisible = true
        setTabLayoutAndViewPager(storeData)
    }

    private fun apiError(message: String?) {
        appLoader.dismiss()
        showToast(message.toString())
        println("Error: $message")
        if (message == netWorkFailure) {
            noInternetDialog.show()
        }
    }

    private fun followStore() {
        if (!sessionManager.isLogin) {
            showToast("Please login first")
            startActivity(Intent(requireActivity(), LoginActivity::class.java))
            requireActivity().finish()
            return
        }

        if (storeData.shop_detail[0].is_following == 1) {
            mainViewModel.unFollowShop(storeId)
        } else {

            mainViewModel.followShop(storeId)
        }
    }

    private fun setOnClickListener() {
        with(binding) {
            follow.setOnClickListener(this@StoreFragment)
            chat.setOnClickListener(this@StoreFragment)
            back.setOnClickListener(this@StoreFragment)
            clearSearch.setOnClickListener(this@StoreFragment)
        }
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            binding.back.id -> findNavController().popBackStack()
            binding.clearSearch.id -> {
                binding.search.setText("")
                mainViewModel.getStoreProducts(
                    storeId.toDouble().roundToInt(),
                    "",
                    categoryIdStore
                )
            }

            binding.follow.id -> followStore()
            binding.chat.id -> {

                if (!sessionManager.isLogin) {
                    showToast("Please login first")
                    startActivity(Intent(requireActivity(), LoginActivity::class.java))
                    requireActivity().finish()
                    return
                }

                storeData.shop_detail[0].let { shopDetail ->
                    val chatId =
                        if (shopDetail.chat_id.isNullOrEmpty()) 0 else shopDetail.chat_id.toInt()
                    val bundle = Bundle()
                    bundle.putString("from", "chatList")
                    bundle.putString("storeName", binding.storeName.text.toString())
                    bundle.putString("sellerId", shopDetail.seller_id.toString())
                    bundle.putString("saleId", "")
                    bundle.putInt("chatId", chatId)
                    findNavController().navigate(R.id.chatFragment, bundle)
                }
            }
        }
    }
}