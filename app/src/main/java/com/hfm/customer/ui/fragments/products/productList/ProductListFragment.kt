package com.hfm.customer.ui.fragments.products.productList

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.hfm.customer.R
import com.hfm.customer.databinding.BottomSheetFilterBinding
import com.hfm.customer.databinding.BottomSheetSortingBinding
import com.hfm.customer.databinding.FragmentProductListBinding
import com.hfm.customer.ui.dashBoard.home.model.Product
import com.hfm.customer.ui.fragments.products.productList.adapter.FilterProductListBrandsAdapter
import com.hfm.customer.ui.fragments.products.productList.adapter.ProductCategoryListAdapter
import com.hfm.customer.ui.fragments.products.productList.adapter.ProductListAdapter
import com.hfm.customer.utils.Loader
import com.hfm.customer.utils.NoInternetDialog
import com.hfm.customer.utils.Resource
import com.hfm.customer.utils.getDeviceId
import com.hfm.customer.utils.initRecyclerView
import com.hfm.customer.utils.initRecyclerViewGrid
import com.hfm.customer.utils.netWorkFailure
import com.hfm.customer.utils.showToast
import com.hfm.customer.viewModel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class ProductListFragment : Fragment(), View.OnClickListener {

    companion object {
        var selectedBrandFilters: MutableList<Int> = ArrayList()
    }

    private val productList: MutableList<Product> = ArrayList()
    private lateinit var bottomSheetFilterBinding: BottomSheetFilterBinding
    private lateinit var binding: FragmentProductListBinding
    private var currentView: View? = null

    @Inject
    lateinit var productListAdapter: ProductListAdapter

    @Inject
    lateinit var productCategoryListAdapter: ProductCategoryListAdapter

    @Inject
    lateinit var filterBrandAdapter: FilterProductListBrandsAdapter

    private val mainViewModel:MainViewModel by viewModels()

    private lateinit var appLoader:Loader

    private var catId = ""

    private var isLoading = false

    private var pageNo = 0

    private  lateinit var noInternetDialog: NoInternetDialog
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_product_list, container, false)
            binding = FragmentProductListBinding.bind(currentView!!)
            init()
            setObserver()
            setOnClickListener()
        }
        return currentView!!
    }

    private fun init() {
        binding.loader.isVisible = true
        appLoader = Loader(requireContext())
        noInternetDialog = NoInternetDialog(requireContext())
        noInternetDialog.setOnDismissListener {
            init()
        }
        catId = arguments?.getString("catId").toString()
        mainViewModel.getProductList(catId, deviceId = getDeviceId(requireContext()),page = pageNo)

        with(binding) {
            productListRv.apply {
                setHasFixedSize(true)
                layoutManager = StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL)
                adapter = productListAdapter
            }.addOnScrollListener(scrollListener)
        }
    }

    private fun setObserver() {
        mainViewModel.productList.observe(viewLifecycleOwner){response->
            when(response){
                is Resource.Success->{
                    appLoader.dismiss()
                    binding.loader.isVisible = false
                    binding.bottomLoader.isVisible = false
                    if(response.data?.httpcode == 200) {
                        if(pageNo==0) productList.clear()
                        productList.addAll(response.data.data.products)
                        binding.result.text = "${response.data.data.total_products} results for ${response.data.data.subcategory_data.category_breadcrumbs.category.category_name}"
                        productListAdapter.differ.submitList(productList)
                        productListAdapter.notifyDataSetChanged()
                        if(pageNo==0){
                            initRecyclerView(requireContext(),binding.categoriesListRv,productCategoryListAdapter,true)
                            productCategoryListAdapter.differ.submitList(response.data.data.subcategory_data.subcategory)
                        }
                    }else{
                        showToast(response.data?.message.toString())
                    }
                }
                is Resource.Loading-> if(pageNo==0) appLoader.show() else binding.bottomLoader.isVisible = true
                is Resource.Error->{
                    appLoader.dismiss()
                    binding.bottomLoader.isVisible = false
                    showToast(response.message.toString())
                    println(response.message.toString())
                    if(response.message.toString() == netWorkFailure){
                        noInternetDialog.show()
                    }
                }
            }
        }
    }

    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            val recycleLayoutManager = binding.productListRv.layoutManager as StaggeredGridLayoutManager
            var pastVisibleItems = 0
            val visibleItemCount = recycleLayoutManager.childCount
            val totalItemCount = recycleLayoutManager.itemCount
            var firstVisibleItems: IntArray? = null
            firstVisibleItems = recycleLayoutManager.findFirstVisibleItemPositions(firstVisibleItems)
            if(firstVisibleItems != null && firstVisibleItems.isNotEmpty()) {
                pastVisibleItems = firstVisibleItems[0]
            }

            if (!isLoading) {
                if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                    pageNo++
                    mainViewModel.getProductList(catId, deviceId = getDeviceId(requireContext()), page = pageNo)
                    isLoading = true
                }
            }
        }
    }

    private fun setOnClickListener() {
        with(binding) {
            back.setOnClickListener(this@ProductListFragment)
            sort.setOnClickListener(this@ProductListFragment)
            filter.setOnClickListener(this@ProductListFragment)

            filterBrandAdapter.setOnBrandFilterClickListener {
                if (selectedBrandFilters.contains(it)) {
                    selectedBrandFilters.remove(it)
                } else {
                    selectedBrandFilters.add(it)
                }
                filterBrandAdapter.notifyItemChanged(it)
            }

            productListAdapter.setOnProductClickListener {id->
                val bundle = Bundle().apply {
                    putString("productId",id)
                }
                findNavController().navigate(R.id.productDetailsFragment,bundle)
            }
        }
    }

    private fun showSortBottomSheet() {
        val binding = BottomSheetSortingBinding.inflate(layoutInflater)
        val sortDialog = BottomSheetDialog(requireActivity(), R.style.MyTransparentBottomSheetDialogTheme)
        sortDialog.setContentView(binding.root)
        sortDialog.show()
    }

    val chipsData = listOf(
        "Cobzico",
        "Delica",
        "Dragon Fruit Brand",
        "Figsol",
        "Ghee Hiang",
        "Gold choice"
    )

    private fun showFilterBottomSheet() {
        bottomSheetFilterBinding = BottomSheetFilterBinding.inflate(layoutInflater)
        val filterDialog = BottomSheetDialog(requireActivity(), R.style.MyTransparentBottomSheetDialogTheme)
        filterDialog.setContentView(bottomSheetFilterBinding.root)
        filterBrandAdapter.differ.submitList(chipsData)
        bottomSheetFilterBinding.filterBrandRv.apply {
            setHasFixedSize(true)
            layoutManager = FlexboxLayoutManager(requireContext()).apply {
                flexDirection = FlexDirection.ROW
            }
            adapter = filterBrandAdapter
        }

        filterDialog.show()

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.back.id -> findNavController().popBackStack()
            binding.sort.id -> showSortBottomSheet()
            binding.filter.id -> showFilterBottomSheet()
        }
    }
}