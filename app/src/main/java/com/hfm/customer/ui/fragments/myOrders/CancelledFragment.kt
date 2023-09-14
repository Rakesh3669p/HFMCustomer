package com.hfm.customer.ui.fragments.myOrders

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
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
import com.hfm.customer.databinding.FragmentProductListBinding
import com.hfm.customer.ui.fragments.myOrders.adapter.CancelledAdapter
import com.hfm.customer.ui.fragments.products.productList.adapter.FilterProductListBrandsAdapter
import com.hfm.customer.ui.fragments.products.productList.adapter.ProductCategoryListAdapter
import com.hfm.customer.ui.fragments.products.productList.adapter.ProductListAdapter
import com.hfm.customer.ui.fragments.wishlist.adapter.WishListPagerAdapter
import com.hfm.customer.utils.initRecyclerView
import com.hfm.customer.utils.initRecyclerViewGrid
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class CancelledFragment : Fragment(), View.OnClickListener {
    private lateinit var binding: FragmentBulkOrdersBinding
    private var currentView: View? = null
    @Inject lateinit var cancelledAdapter: CancelledAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_bulk_orders, container, false)
            binding = FragmentBulkOrdersBinding.bind(currentView!!)
            init()
            setObserver()
            setOnClickListener()
        }
        return currentView!!
    }

    private fun init() {
        with(binding) {
            initRecyclerView(requireContext(),binding.bulkOrdersRv,cancelledAdapter)
        }
    }

    private fun setObserver() {}

    private fun setOnClickListener() {
        with(binding) {
        }

        cancelledAdapter.setOnOrderClickListener {
            findNavController().navigate(R.id.orderDetailsFragment)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
        }
    }
}