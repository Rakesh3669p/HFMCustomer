package com.hfm.customer.ui.fragments.store

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.hfm.customer.R
import com.hfm.customer.databinding.FragmentStoreBinding
import com.hfm.customer.ui.fragments.wishlist.adapter.WishListPagerAdapter
import com.hfm.customer.utils.PromotionBanner


class StoreFragment : Fragment() , View.OnClickListener {


    private lateinit var binding: FragmentStoreBinding
    private var currentView: View? = null
    private lateinit var promotionBanner: PromotionBanner
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_store, container, false)
            binding = FragmentStoreBinding.bind(currentView!!)
            init()
            setTabLayoutAndViewPager()
            setObserver()
            setOnClickListener()
        }
        return currentView!!
    }

    private fun init() {
        promotionBanner = PromotionBanner(requireContext(), "")
        promotionBanner.show()
        with(binding) {
        }
    }

    private fun setTabLayoutAndViewPager() {
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Home"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("All Products(60)"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Reviews Ratings(40)"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("About"))


        val fragmentManager = childFragmentManager
        val viewPagerAdapter = WishListPagerAdapter(fragmentManager,lifecycle)
        binding.storeVp.adapter = viewPagerAdapter

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                binding.storeVp.currentItem = tab!!.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

        })


        binding.storeVp.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                binding.tabLayout.selectTab(binding.tabLayout.getTabAt(position))
            }
        })

    }


    private fun setObserver() {}

    private fun setOnClickListener() {
        with(binding) {

        }
    }



    override fun onClick(v: View?) {
        when (v?.id) {
        }
    }
}