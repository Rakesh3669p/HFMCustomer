package com.hfm.customer.ui.fragments.vouchers

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
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
import com.hfm.customer.databinding.AppLoaderBinding
import com.hfm.customer.databinding.FragmentVoucherPagerBinding
import com.hfm.customer.databinding.FragmentVouchersBinding
import com.hfm.customer.databinding.FragmentWalletBinding
import com.hfm.customer.ui.fragments.cart.model.Coupon
import com.hfm.customer.ui.fragments.products.productDetails.model.SellerVoucherModel
import com.hfm.customer.ui.fragments.vouchers.adapter.VouchersAdapter
import com.hfm.customer.ui.fragments.vouchers.adapter.VouchersPagerAdapter
import com.hfm.customer.ui.fragments.wishlist.adapter.WishListPagerAdapter
import com.hfm.customer.ui.loginSignUp.LoginActivity
import com.hfm.customer.utils.Loader
import com.hfm.customer.utils.NoInternetDialog
import com.hfm.customer.utils.Resource
import com.hfm.customer.utils.SessionManager
import com.hfm.customer.utils.initRecyclerView
import com.hfm.customer.utils.netWorkFailure
import com.hfm.customer.utils.showToast
import com.hfm.customer.viewModel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class VouchersPagerFragment : Fragment(), View.OnClickListener {


    private var platformVouchers: List<Coupon> = ArrayList()

    private lateinit var binding: FragmentVoucherPagerBinding
    private var currentView: View? = null
    private val mainViewModel: MainViewModel by viewModels()
    private lateinit var appLoader: Loader
    private lateinit var noInternetDialog: NoInternetDialog

    @Inject
    lateinit var vouchersAdapter: VouchersAdapter

    @Inject
    lateinit var sessionManager: SessionManager
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_voucher_pager, container, false)
            binding = FragmentVoucherPagerBinding.bind(currentView!!)
            init()
            setOnClickListener()
        }
        return currentView!!
    }

    private fun init(){
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Platform Vouchers"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Seller Vouchers"))

        val fragmentManager = childFragmentManager
        val viewPagerAdapter = VouchersPagerAdapter(fragmentManager,lifecycle)
        binding.vouchersVp.isSaveEnabled = false
        binding.vouchersVp.adapter = viewPagerAdapter
        binding.vouchersVp.isUserInputEnabled = false

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                binding.vouchersVp.currentItem = tab!!.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })


        binding.vouchersVp.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                binding.tabLayout.selectTab(binding.tabLayout.getTabAt(position))
            }
        })
    }



    private fun setOnClickListener() {
        with(binding) {
            back.setOnClickListener(this@VouchersPagerFragment)
        }

    }


    override fun onClick(v: View?) {
        when (v?.id) {
            binding.back.id -> findNavController().popBackStack()
        }
    }
}