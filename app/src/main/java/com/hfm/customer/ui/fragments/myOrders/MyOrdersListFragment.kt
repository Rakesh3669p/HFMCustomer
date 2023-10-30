package com.hfm.customer.ui.fragments.myOrders

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.hfm.customer.R
import com.hfm.customer.databinding.FragmentBulkOrdersBinding
import com.hfm.customer.ui.fragments.myOrders.adapter.MyOrdersListAdapter
import com.hfm.customer.ui.fragments.myOrders.model.MyOrdersData
import com.hfm.customer.ui.fragments.myOrders.model.Purchase
import com.hfm.customer.ui.fragments.products.productDetails.model.Product
import com.hfm.customer.utils.Loader
import com.hfm.customer.utils.NoInternetDialog
import com.hfm.customer.utils.Resource
import com.hfm.customer.utils.initRecyclerView
import com.hfm.customer.utils.netWorkFailure
import com.hfm.customer.utils.showToast
import com.hfm.customer.viewModel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MyOrdersListFragment(private val orderStatus:String) : Fragment() {
    private val purchases: MutableList<Purchase> = ArrayList()
    private lateinit var binding: FragmentBulkOrdersBinding
    private var currentView: View? = null
    private val mainViewModel: MainViewModel by viewModels()
    @Inject
    lateinit var myOrdersListAdapter: MyOrdersListAdapter
    private lateinit var appLoader: Loader
    private lateinit var noInternetDialog: NoInternetDialog
    var pageNo = 0
    private var isLoading = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_bulk_orders, container, false)
            binding = FragmentBulkOrdersBinding.bind(currentView!!)
            init()
        }
        return currentView!!
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setObserver()
    }

    private fun init() {
        appLoader = Loader(requireContext())
        noInternetDialog = NoInternetDialog(requireContext())
        noInternetDialog.setOnDismissListener { init() }
        mainViewModel.getMyOrders(orderStatus,pageNo )
    }

    private fun setObserver() {
        mainViewModel.myOrders.observe(viewLifecycleOwner){response->
            when(response){
                is Resource.Success->{
                    binding.bottomLoader.isVisible = false
                    if(response.data?.httpcode == "200"){
                        binding.noDataLayout.root.isVisible = false
                        isLoading = response.data.data.purchase.isEmpty()
                        setMyOrders(response.data.data)
                    }else{
                        binding.noDataLayout.root.isVisible = purchases.isEmpty()
                    }
                }
                is Resource.Loading-> if(pageNo==0) Unit else binding.bottomLoader.isVisible = true
                is Resource.Error->apiError(response.message)
            }
        }
    }

    private fun setMyOrders(data: MyOrdersData) {
        if(pageNo==0) {
            initRecyclerView(requireContext(), binding.bulkOrdersRv, myOrdersListAdapter)
            binding.bulkOrdersRv.addOnScrollListener(scrollListener)
        }
        if(data.purchase.isNotEmpty()) {
            purchases.addAll(data.purchase)
        }
        myOrdersListAdapter.differ.submitList(purchases)
        myOrdersListAdapter.notifyDataSetChanged()
        myOrdersListAdapter.setPaymentStatus(orderStatus)

        myOrdersListAdapter.setOnOrderClickListener {
            val bundle = Bundle()
            bundle.putString("orderId",it.order_id)
            bundle.putString("saleId", it.sale_id.toString())
            findNavController().navigate(R.id.orderDetailsFragment, bundle)
        }
    }

    private fun apiError(message: String?) {
        binding.bottomLoader.isVisible = false
        appLoader.dismiss()
        showToast(message.toString())
        if (message == netWorkFailure) {
            noInternetDialog.show()
        }
    }

    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val pastVisibleItems = layoutManager.findFirstCompletelyVisibleItemPosition()
            val visibleItemCount = layoutManager.childCount
            val totalItemCount = layoutManager.itemCount

            if (!isLoading && (visibleItemCount + pastVisibleItems) >= totalItemCount) {
                pageNo++
                mainViewModel.getMyOrders(orderStatus, pageNo)
                isLoading = true
            }
        }
    }
}