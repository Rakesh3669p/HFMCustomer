package com.hfm.customer.ui.fragments.noInternet

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.tabs.TabLayout
import com.hfm.customer.R
import com.hfm.customer.databinding.BottomSheetFilterBinding
import com.hfm.customer.databinding.BottomSheetSortingBinding
import com.hfm.customer.databinding.FragmentBulkOrdersBinding
import com.hfm.customer.databinding.FragmentMyOrdersBinding
import com.hfm.customer.databinding.FragmentNoInternetBinding
import com.hfm.customer.databinding.FragmentProductListBinding
import com.hfm.customer.ui.dashBoard.DashBoardActivity
import com.hfm.customer.ui.fragments.myOrders.adapter.BulkOrdersAdapter
import com.hfm.customer.ui.fragments.products.productList.adapter.FilterProductListBrandsAdapter
import com.hfm.customer.ui.fragments.products.productList.adapter.ProductCategoryListAdapter
import com.hfm.customer.ui.fragments.products.productList.adapter.ProductListAdapter
import com.hfm.customer.ui.fragments.wishlist.adapter.WishListPagerAdapter
import com.hfm.customer.utils.Loader
import com.hfm.customer.utils.Resource
import com.hfm.customer.utils.initRecyclerView
import com.hfm.customer.utils.initRecyclerViewGrid
import com.hfm.customer.utils.isInternetAvailable
import com.hfm.customer.utils.netWorkFailure
import com.hfm.customer.utils.showToast
import com.hfm.customer.viewModel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import dagger.multibindings.IntKey
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class NoInternetFragment : Fragment() {
    private var currentFragment: Int? = null
    private lateinit var binding: FragmentNoInternetBinding
    private var currentView: View? = null
    private lateinit var appLoader: Loader
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_no_internet, container, false)
            binding = FragmentNoInternetBinding.bind(currentView!!)
            init()
            setOnClickListener()
        }
        return currentView!!
    }

    private fun init() {
        appLoader = Loader(requireContext())
        currentFragment = arguments?.getInt("fragment")
    }


    private fun setOnClickListener() {
        with(binding) {
            retry.setOnClickListener {
                appLoader.show()
                lifecycleScope.launch {
                    delay(1000)
                    appLoader.dismiss()
                    if (isInternetAvailable(requireContext())) {
                        findNavController().popBackStack()
                        if (currentFragment != null) {
                            currentFragment?.let {
                                findNavController().navigate(it)
                            }
                        } else {
                            startActivity(Intent(requireActivity(), DashBoardActivity::class.java))
                            requireActivity().finish()
                        }
                    } else {
                        showToast("No Internet")
                    }
                }
            }
        }
    }
}