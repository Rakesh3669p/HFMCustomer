package com.hfm.customer.ui.fragments.myOrders

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.hfm.customer.R
import com.hfm.customer.databinding.FragmentMyOrdersBinding
import com.hfm.customer.ui.fragments.myOrders.adapter.MyOrdersPagerAdapter
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MyOrdersFragment : Fragment(), View.OnClickListener {
    private lateinit var binding: FragmentMyOrdersBinding
    private var currentView: View? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_my_orders, container, false)
            binding = FragmentMyOrdersBinding.bind(currentView!!)
            init()
            setTabLayoutAndViewPager()
            setObserver()
            setOnClickListener()
        }
        return currentView!!
    }

    private fun init() {

    }

    private fun setObserver() {}

    private fun setTabLayoutAndViewPager() {
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("To Pay"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("To Ship"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("To Receive"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Completed"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Cancelled"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Bulk Order Requests"))


        val fragmentManager = childFragmentManager
        val viewPagerAdapter = MyOrdersPagerAdapter(fragmentManager,lifecycle)
        binding.ordersVp.offscreenPageLimit = ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT
        binding.ordersVp.adapter = viewPagerAdapter
        binding.ordersVp.isUserInputEnabled = false
        binding.ordersVp.isSaveEnabled = false

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                binding.ordersVp.currentItem = tab!!.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })


        binding.ordersVp.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                binding.tabLayout.selectTab(binding.tabLayout.getTabAt(position))
            }
        })


        when(arguments?.getString("from")){
            "toPay"->binding.ordersVp.currentItem =0
            "toShip"->binding.ordersVp.currentItem =1
            "toReceive"->binding.ordersVp.currentItem =2
            "cancelled"->binding.ordersVp.currentItem =4
            "bulkOrders"->binding.ordersVp.currentItem =5
        }

        Handler(Looper.getMainLooper()).postDelayed({ binding.tabLayout.getTabAt(binding.ordersVp.currentItem)?.select() }, 100)

    }


    private fun setOnClickListener() {
        with(binding) {
            back.setOnClickListener(this@MyOrdersFragment)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.back.id -> findNavController().popBackStack()
        }
    }
}