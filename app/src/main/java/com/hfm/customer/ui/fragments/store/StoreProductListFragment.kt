package com.hfm.customer.ui.fragments.store

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.hfm.customer.R
import com.hfm.customer.databinding.FragmentStoreProductListBinding
import com.hfm.customer.ui.fragments.products.productList.adapter.ProductCategoryListAdapter
import com.hfm.customer.ui.fragments.products.productList.adapter.ProductListAdapter
import com.hfm.customer.utils.initRecyclerView
import com.hfm.customer.utils.initRecyclerViewGrid
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class StoreProductListFragment : Fragment(), View.OnClickListener {

    private lateinit var binding: FragmentStoreProductListBinding
    private var currentView: View? = null

    @Inject lateinit var productListAdapter: ProductListAdapter
    @Inject lateinit var productCategoryListAdapter: ProductCategoryListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_store_product_list, container, false)
            binding = FragmentStoreProductListBinding.bind(currentView!!)
            init()
            setObserver()
            setOnClickListener()
        }
        return currentView!!
    }

    private fun init() {
        with(binding) {
            initRecyclerView(requireContext(),categoriesListRv,productCategoryListAdapter,true)
            initRecyclerViewGrid(requireContext(),productListRv,productListAdapter,2)
        }
    }

    private fun setObserver() {}

    private fun setOnClickListener() {
        with(binding) {
            productListAdapter.setOnProductClickListener {
                findNavController().navigate(R.id.productDetailsFragment)
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
        }
    }
}