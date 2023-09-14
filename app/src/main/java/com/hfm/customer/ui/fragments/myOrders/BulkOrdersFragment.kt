package com.hfm.customer.ui.fragments.myOrders

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
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
import com.hfm.customer.ui.fragments.myOrders.adapter.BulkOrdersAdapter
import com.hfm.customer.ui.fragments.products.productList.adapter.FilterProductListBrandsAdapter
import com.hfm.customer.ui.fragments.products.productList.adapter.ProductCategoryListAdapter
import com.hfm.customer.ui.fragments.products.productList.adapter.ProductListAdapter
import com.hfm.customer.ui.fragments.wishlist.adapter.WishListPagerAdapter
import com.hfm.customer.utils.Loader
import com.hfm.customer.utils.Resource
import com.hfm.customer.utils.initRecyclerView
import com.hfm.customer.utils.initRecyclerViewGrid
import com.hfm.customer.utils.netWorkFailure
import com.hfm.customer.utils.showToast
import com.hfm.customer.viewModel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import dagger.multibindings.IntKey
import javax.inject.Inject


@AndroidEntryPoint
class BulkOrdersFragment : Fragment(), View.OnClickListener {
    private lateinit var binding: FragmentBulkOrdersBinding
    private var currentView: View? = null
    private val mainViewModel:MainViewModel by viewModels()
    @Inject lateinit var bulkOrderAdapter:  BulkOrdersAdapter
    private lateinit var appLoader:Loader
    var pageNo = 0
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
        appLoader = Loader(requireContext())
            mainViewModel.getBulkOrders(pageNo)

    }

    private fun setObserver() {
        mainViewModel.bulkOrderList.observe(viewLifecycleOwner){response->
            when(response){
                is Resource.Success->{
                    appLoader.dismiss()
                    if(response.data?.httpcode == 200){
                        initRecyclerView(requireContext(),binding.bulkOrdersRv,bulkOrderAdapter)
                        bulkOrderAdapter.differ.submitList(response.data.data.bulkrequest_order_details)
                    }else{
                        showToast(response.data?.message.toString())
                    }

                }
                is Resource.Loading->appLoader.show()
                is Resource.Error->{
                    appLoader.dismiss()
                    showToast(response.message.toString())
                    if(response.message.toString() == netWorkFailure){

                    }
                }
            }
        }
    }



    private fun setOnClickListener() {
        with(binding) {
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
        }
    }
}