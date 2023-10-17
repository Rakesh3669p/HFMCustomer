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
import com.hfm.customer.ui.fragments.products.productList.model.Subcategory
import com.hfm.customer.ui.fragments.store.adapter.StoreProductCategoryListAdapter
import com.hfm.customer.ui.fragments.store.model.Category
import com.hfm.customer.ui.fragments.store.model.StoreData
import com.hfm.customer.utils.initRecyclerView
import com.hfm.customer.utils.initRecyclerViewGrid
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class StoreProductListFragment(private val storeData: StoreData) : Fragment(){

    private lateinit var binding: FragmentStoreProductListBinding
    private var currentView: View? = null

    @Inject lateinit var productListAdapter: ProductListAdapter
    @Inject lateinit var productCategoryListAdapter: StoreProductCategoryListAdapter

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

            val categories:MutableList<Category> = ArrayList()
            val category = Category(id = 0,"ALL")
            categories.add(category)
            storeData.shop_detail[0].categories.let { categories.addAll(it) }
            productListAdapter.differ.submitList(storeData.product)
            productCategoryListAdapter.differ.submitList(categories)
        }
    }

    private fun setObserver() {}

    private fun setOnClickListener() {
        with(binding) {
            productListAdapter.setOnProductClickListener {
                val bundle = Bundle().apply { putString("productId", it) }
                findNavController().navigate(R.id.productDetailsFragment,bundle)
            }
        }
    }
}