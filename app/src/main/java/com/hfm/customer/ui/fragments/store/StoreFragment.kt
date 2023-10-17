package com.hfm.customer.ui.fragments.store

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import coil.load
import com.google.android.material.tabs.TabLayout
import com.hfm.customer.R
import com.hfm.customer.databinding.FragmentStoreBinding
import com.hfm.customer.ui.fragments.store.adapter.StorePagerAdapter
import com.hfm.customer.ui.fragments.store.model.StoreData
import com.hfm.customer.utils.Loader
import com.hfm.customer.utils.NoInternetDialog
import com.hfm.customer.utils.PromotionBanner
import com.hfm.customer.utils.Resource
import com.hfm.customer.utils.formatToTwoDecimalPlaces
import com.hfm.customer.utils.getDeviceId
import com.hfm.customer.utils.netWorkFailure
import com.hfm.customer.utils.replaceBaseUrl
import com.hfm.customer.utils.showToast
import com.hfm.customer.viewModel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StoreFragment : Fragment(), View.OnClickListener {

    private lateinit var storeData: StoreData

    private  var storeId: String = ""
    private lateinit var binding: FragmentStoreBinding
    private var currentView: View? = null
    private lateinit var promotionBanner: PromotionBanner
    private val mainViewModel: MainViewModel by viewModels()
    private lateinit var noInternetDialog: NoInternetDialog
    private lateinit var appLoader: Loader

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setObserver()
    }
    private fun init() {
        binding.storeDataGroup.isVisible = false
        appLoader = Loader(requireContext())
        noInternetDialog = NoInternetDialog(requireContext())


        arguments?.let {
            storeId = it.getString("storeId").toString()
        }

        mainViewModel.getStoreDetails(storeId, getDeviceId(requireContext()))
        mainViewModel.getProfile()
    }



    private fun setTabLayoutAndViewPager(data: StoreData) {
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Home"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("All Products(${data.total_products})"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Reviews Ratings(0)"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("About"))


        val fragmentManager = childFragmentManager
        val viewPagerAdapter = StorePagerAdapter(fragmentManager, lifecycle,data)
        binding.storeVp.adapter = viewPagerAdapter
        binding.storeVp.isSaveEnabled = false

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
        mainViewModel.storeDetails.observe(viewLifecycleOwner){response->
            when(response){
                is Resource.Success->{
                    appLoader.dismiss()
                    if(response.data?.httpcode== 200){
                        storeData = response.data.data
                        setStoreDetails()
                    }else{
                        showToast(response.data?.status.toString())
                    }
                }
                is Resource.Loading->appLoader.show()
                is Resource.Error->apiError(response.message)
            }
        }

        mainViewModel.followShop.observe(viewLifecycleOwner){response->
            when(response){
                is Resource.Success->{
                    appLoader.dismiss()
                    if(response.data?.httpcode== 200){
                        storeData.shop_detail[0].is_following = 1
                        binding.follow.text = if(storeData.shop_detail[0].is_following==0)"Follow" else "Following"
                    }else{
                        showToast(response.data?.status.toString())
                    }
                }
                is Resource.Loading->Unit
                is Resource.Error->apiError(response.message)
            }
        }

        mainViewModel.unFollowShop.observe(viewLifecycleOwner){response->
            when(response){
                is Resource.Success->{
                    appLoader.dismiss()
                    if(response.data?.httpcode== 200){
                        storeData.shop_detail[0].is_following = 0
                        binding.follow.text = if(storeData.shop_detail[0].is_following==0)"Follow" else "Following"


                    }else{
                        showToast(response.data?.status.toString())
                    }
                }
                is Resource.Loading->Unit
                is Resource.Error->apiError(response.message)
            }
        }

    }

    private fun setStoreDetails() {

        with(binding){
            if(storeData.shop_detail.isNotEmpty()) {
                storeData.shop_detail[0].let { shopDetail ->
                    promotionBanner = PromotionBanner(requireContext(), replaceBaseUrl(shopDetail.promotion_image))
                    promotionBanner.show()

                    storeName.text = shopDetail.store_name
                    storeImage.load(replaceBaseUrl(shopDetail.logo))
                    storeBanner.load(replaceBaseUrl(shopDetail.banner))
                    storeFollowers.text = "${formatToTwoDecimalPlaces(shopDetail.postive_review.toString().toDouble())} % Positive ${shopDetail.followers} followers"
                    follow.text = if(shopDetail.is_following==0)"Follow" else "Following"

                    val white = ContextCompat.getColor(requireContext(),R.color.white)
                    val black = ContextCompat.getColor(requireContext(),R.color.black)
                    if(shopDetail.banner.isNotEmpty()){
                        storeName.setTextColor(white)
                        storeFollowers.setTextColor(white)
                        val drawable = ColorDrawable(ContextCompat.getColor(requireContext(), R.color.blackTrans200))
                        storeBanner.foreground = drawable
                    }else{
                        storeName.setTextColor(black)
                        storeFollowers.setTextColor(black)

                    }
                }
                setTabLayoutAndViewPager(storeData)

            }
        }
        binding.storeDataGroup.isVisible = true
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
        if(storeData.shop_detail[0].is_following == 1){
            mainViewModel.unFollowShop(storeId)
        }else{

            mainViewModel.followShop(storeId)
        }
    }
    private fun setOnClickListener() {
        with(binding) {
            follow.setOnClickListener(this@StoreFragment)
            chat.setOnClickListener(this@StoreFragment)
            back.setOnClickListener(this@StoreFragment)
        }
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            binding.back.id->findNavController().popBackStack()
            binding.follow.id->followStore()
            binding.chat.id-> {
                storeData.shop_detail[0].let { shopDetail ->
                    val bundle = Bundle()
                    bundle.putString("from","chatList")
                    bundle.putString("storeName", binding.storeName.text.toString())
                    bundle.putString("sellerId", shopDetail.seller_id.toString())
                    bundle.putString("saleId", "")
                    bundle.putInt("chatId", shopDetail.chat_id.toInt())
                    findNavController().navigate(R.id.chatFragment, bundle)
                }
            }
        }
    }
}