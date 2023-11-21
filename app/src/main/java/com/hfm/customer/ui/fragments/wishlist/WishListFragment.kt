package com.hfm.customer.ui.fragments.wishlist

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.hfm.customer.R
import com.hfm.customer.databinding.FragmentStoreBinding
import com.hfm.customer.databinding.FragmentWishlistBinding
import com.hfm.customer.ui.fragments.wishlist.adapter.WishListPagerAdapter
import com.hfm.customer.ui.fragments.wishlist.adapter.WishlistShopAdapter
import com.hfm.customer.viewModel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class WishListFragment : Fragment() , View.OnClickListener {


    private lateinit var binding: FragmentWishlistBinding
    private var currentView: View? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_wishlist, container, false)
            binding = FragmentWishlistBinding.bind(currentView!!)
            init()
            setTabLayoutAndViewPager()
            setOnClickListener()
        }
        return currentView!!
    }

    private fun init() {
    }

    private fun setTabLayoutAndViewPager() {
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Products"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Shops"))

        val fragmentManager = childFragmentManager
        val viewPagerAdapter = WishListPagerAdapter(fragmentManager,lifecycle)
        binding.storeVp.isSaveEnabled = false
        binding.storeVp.adapter = viewPagerAdapter
        binding.storeVp.isUserInputEnabled = false

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                binding.storeVp.currentItem = tab!!.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })


        binding.storeVp.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                binding.tabLayout.selectTab(binding.tabLayout.getTabAt(position))
            }
        })
    }

    private fun setOnClickListener() {
        with(binding) {
            back.setOnClickListener(this@WishListFragment)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.back.id -> findNavController().popBackStack()
        }
    }
}