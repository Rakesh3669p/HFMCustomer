package com.hfm.customer.ui.fragments.myOrders

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.hfm.customer.R
import com.hfm.customer.databinding.FragmentBulkOrdersBinding
import com.hfm.customer.ui.fragments.myOrders.adapter.BulkOrdersAdapter
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
class BulkOrdersFragment : Fragment(){
    private lateinit var binding: FragmentBulkOrdersBinding
    private var currentView: View? = null
    private val mainViewModel:MainViewModel by viewModels()
    @Inject lateinit var bulkOrderAdapter:  BulkOrdersAdapter
    private lateinit var appLoader:Loader
    private lateinit var noInternetDialog:NoInternetDialog
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
        noInternetDialog = NoInternetDialog(requireContext())
        noInternetDialog.setOnDismissListener { init() }
        mainViewModel.getBulkOrders(pageNo)

    }

    private fun setObserver() {
        mainViewModel.bulkOrderList.observe(viewLifecycleOwner){response->
            when(response){
                is Resource.Success->{
                    while (appLoader.isShowing) {
                        appLoader.dismiss()
                    }
                    if(response.data?.httpcode == 200){
                        initRecyclerView(requireContext(),binding.bulkOrdersRv,bulkOrderAdapter)
                        bulkOrderAdapter.differ.submitList(response.data.data.bulkrequest_order_details)
                    }else{
                        showToast(response.data?.message.toString())
                    }

                }
                is Resource.Loading->appLoader.show()
                is Resource.Error->apiError(response.message)
            }
        }
    }

    private fun apiError(message: String?) {
        appLoader.dismiss()
        showToast(message.toString())
        if (message == netWorkFailure) {
            noInternetDialog.show()
        }
    }
    private fun setOnClickListener() {
        bulkOrderAdapter.setOnOrderClickListener {
            val bundle = Bundle()
            bundle.putString("orderId",it.order_id)
            bundle.putString("saleId", it.sale_id.toString())
            findNavController().navigate(R.id.orderDetailsFragment, bundle)
        }
    }
}