package com.hfm.customer.ui.fragments.store

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.hfm.customer.R
import com.hfm.customer.databinding.FragmentStoreProductListBinding
import com.hfm.customer.ui.fragments.products.productDetails.model.Product
import com.hfm.customer.ui.fragments.products.productList.adapter.ProductListAdapter
import com.hfm.customer.ui.fragments.store.StoreFragment.Companion.categoryIdStore
import com.hfm.customer.ui.fragments.store.adapter.StoreProductCategoryListAdapter
import com.hfm.customer.ui.fragments.store.model.Category
import com.hfm.customer.ui.fragments.store.model.StoreData
import com.hfm.customer.utils.Loader
import com.hfm.customer.utils.NoInternetDialog
import com.hfm.customer.utils.Resource
import com.hfm.customer.utils.initRecyclerView
import com.hfm.customer.utils.initRecyclerViewGrid
import com.hfm.customer.utils.netWorkFailure
import com.hfm.customer.utils.showToast
import com.hfm.customer.viewModel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class StoreProductListFragment(private val storeData: StoreData) : Fragment() {

    private lateinit var binding: FragmentStoreProductListBinding
    private var currentView: View? = null
    private var storeProducts: List<Product> = ArrayList()

    @Inject
    lateinit var productListAdapter: ProductListAdapter

    @Inject
    lateinit var productCategoryListAdapter: StoreProductCategoryListAdapter
    private val mainViewModel: MainViewModel by viewModels()
    private lateinit var appLoader: Loader
    private lateinit var noInternetDialog: NoInternetDialog

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setObserver()
    }

    fun updateData(newStoreData: StoreData) {
       setProducts(newStoreData)
    }
    private fun init() {
        with(binding) {
            appLoader = Loader(requireContext())
            noInternetDialog = NoInternetDialog(requireContext())

            initRecyclerView(requireContext(), categoriesListRv, productCategoryListAdapter, true)
            initRecyclerViewGrid(requireContext(), productListRv, productListAdapter, 2)
            val categories: MutableList<Category> = ArrayList()
            val category = Category(id = 0, "ALL")
            categories.add(category)
            storeData.shop_detail[0].categories.let { categories.addAll(it) }
            storeProducts = storeData.product
            productListAdapter.differ.submitList(storeProducts)
            productCategoryListAdapter.differ.submitList(categories)
        }
    }

    private fun setObserver() {
        mainViewModel.storeDetails.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    if (response.data?.httpcode == 200) {
                        setProducts(response.data.data)
                    } else {
                        showToast(response.data?.status.toString())
                    }
                }

                is Resource.Loading -> appLoader.show()
                is Resource.Error -> apiError(response.message)
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

    private fun setProducts(data: StoreData) {
        initRecyclerView(requireContext(), binding.categoriesListRv, productCategoryListAdapter, true)
        initRecyclerViewGrid(requireContext(), binding.productListRv, productListAdapter, 2)
        val categories: MutableList<Category> = ArrayList()
        val category = Category(id = 0, "ALL")
        categories.add(category)
        data.shop_detail[0].categories.let { categories.addAll(it) }
        storeProducts = data.product
        productListAdapter.differ.submitList(storeProducts)
        productCategoryListAdapter.differ.submitList(categories)
        binding.noData.isVisible = storeProducts.isEmpty()
    }

    private fun setOnClickListener() {
        productListAdapter.setOnProductClickListener {
            val bundle = Bundle().apply { putString("productId", it) }
            findNavController().navigate(R.id.productDetailsFragment, bundle)
        }

        productCategoryListAdapter.setOnSubCategoryClickListener { catId ->
            if (storeData.shop_detail.isNotEmpty()) {
                val categoryId= if(catId==0) "" else catId.toString()
                categoryIdStore = categoryId
                storeData.shop_detail[0].let {
                    mainViewModel.getStoreDetails(
                        it.seller_id.toString(),
                        categoryId
                    )
                }
            }
        }
    }
}