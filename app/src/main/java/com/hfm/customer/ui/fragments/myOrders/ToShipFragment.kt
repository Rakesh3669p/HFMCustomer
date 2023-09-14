package com.hfm.customer.ui.fragments.myOrders

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.hfm.customer.R
import com.hfm.customer.databinding.FragmentBulkOrdersBinding
import com.hfm.customer.ui.fragments.myOrders.adapter.BulkOrdersAdapter
import com.hfm.customer.ui.fragments.myOrders.adapter.ToShipAdapter
import com.hfm.customer.utils.Loader
import com.hfm.customer.utils.Resource
import com.hfm.customer.utils.initRecyclerView
import com.hfm.customer.utils.netWorkFailure
import com.hfm.customer.utils.showToast
import com.hfm.customer.viewModel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class ToShipFragment : Fragment(), View.OnClickListener {
    private lateinit var binding: FragmentBulkOrdersBinding
    private var currentView: View? = null
    private val mainViewModel: MainViewModel by viewModels()
    @Inject
    lateinit var bulkOrderAdapter: BulkOrdersAdapter
    private lateinit var appLoader: Loader
    var pageNo = 0
    @Inject lateinit var toShipAdapter: ToShipAdapter
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
        initRecyclerView(requireContext(),binding.bulkOrdersRv,toShipAdapter)
    }

    private fun setObserver() {

    }


    private fun setOnClickListener() {
        with(binding) {
        }

        bulkOrderAdapter.setOnOrderClickListener {  }
    }


    override fun onClick(v: View?) {
        when (v?.id) {
        }
    }
}