package com.hfm.customer.ui.fragments.products.productList

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.hfm.customer.R
import com.hfm.customer.databinding.BottomSheetFilterBinding
import com.hfm.customer.databinding.BottomSheetSortingBinding
import com.hfm.customer.databinding.FragmentProductListBinding
import com.hfm.customer.ui.fragments.products.productDetails.model.Product
import com.hfm.customer.ui.fragments.products.productList.adapter.FilterProductListBrandsAdapter
import com.hfm.customer.ui.fragments.products.productList.adapter.ProductCategoryListAdapter
import com.hfm.customer.ui.fragments.products.productList.adapter.ProductListAdapter
import com.hfm.customer.utils.Loader
import com.hfm.customer.utils.NoInternetDialog
import com.hfm.customer.utils.Resource
import com.hfm.customer.utils.getDeviceId
import com.hfm.customer.utils.initRecyclerView
import com.hfm.customer.utils.netWorkFailure
import com.hfm.customer.utils.showToast
import com.hfm.customer.viewModel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.math.roundToInt


@AndroidEntryPoint
class ProductListFragment : Fragment(), View.OnClickListener {
    companion object {
        var selectedBrandFilters: MutableList<Int> = ArrayList()
    }

    @Inject
    lateinit var productListAdapter: ProductListAdapter
    @Inject
    lateinit var productCategoryListAdapter: ProductCategoryListAdapter
    @Inject
    lateinit var filterBrandAdapter: FilterProductListBrandsAdapter

    private var catId = ""
    private var subCatId = ""
    private var brandId = ""
    private var search = ""
    private var popular: String = "1"
    private var newest: String = ""
    private var lowToHigh: String = ""
    private var highToLow: String = ""
    private var maxPrice: String = ""
    private var minPrice: String = ""
    private var deviceId: String = ""
    private val productList: MutableList<Product> = ArrayList()
    private lateinit var bottomSheetFilterBinding: BottomSheetFilterBinding
    private lateinit var binding: FragmentProductListBinding
    private var currentView: View? = null
    private val mainViewModel: MainViewModel by viewModels()
    private lateinit var appLoader: Loader
    private var isLoading = false
    private var pageNo = 0

    private lateinit var noInternetDialog: NoInternetDialog
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
        deviceId = getDeviceId(requireContext())
        noInternetDialog = NoInternetDialog(requireContext())
        noInternetDialog.setOnDismissListener {
            init()
        }
        catId = arguments?.getString("catId") ?: ""
        brandId = arguments?.getString("brandId") ?: ""
        makeProductListApiCall()

        with(binding) {
            productListRv.apply {
                setHasFixedSize(true)
                layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
                adapter = productListAdapter
            }.addOnScrollListener(scrollListener)
        }
    }

    private fun makeProductListApiCall() {
        mainViewModel.getProductList(
            catId = catId,
            subCatId = subCatId,
            brandId = brandId,
            maxPrice = maxPrice,
            minPrice = minPrice,
            lowToHigh = lowToHigh,
            highToLow = highToLow,
            popular = popular,
            latest = newest,
            search = search,
            deviceId = deviceId,
            page = pageNo,
        )
    }

    private fun setObserver() {
        mainViewModel.productList.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    binding.loader.isVisible = false
                    binding.bottomLoader.isVisible = false
                    if (response.data?.httpcode == 200) {
                        if (pageNo == 0) productList.clear()
                        isLoading = response.data.data.products.isEmpty()
                        productList.addAll(response.data.data.products)
                        if(response.data.data.products.isNotEmpty()) {
                            binding.result.text =
                                "${response.data.data.total_products} results for ${response.data.data.products[0].category_name.toString()}"
                        }
                        productListAdapter.differ.submitList(productList)
                        productListAdapter.notifyDataSetChanged()
                        if (pageNo == 0) {
                            if (response.data.data.subcategory_data.subcategory != null && response.data.data.subcategory_data.subcategory.isNotEmpty()) {
                                initRecyclerView(
                                    requireContext(),
                                    binding.categoriesListRv,
                                    productCategoryListAdapter,
                                    true
                                )
                                productCategoryListAdapter.differ.submitList(response.data.data.subcategory_data.subcategory)
                            }
                        }
                    } else {
                        showToast(response.data?.message.toString())
                    }
                }

                is Resource.Loading -> if (pageNo == 0) appLoader.show() else binding.bottomLoader.isVisible =
                    true

                is Resource.Error -> {
                    appLoader.dismiss()
                    binding.bottomLoader.isVisible = false
                    showToast(response.message.toString())
                    println(response.message.toString())
                    if (response.message.toString() == netWorkFailure) {
                        noInternetDialog.show()
                    }
                }
            }
        }
    }

    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            val recycleLayoutManager =
                binding.productListRv.layoutManager as StaggeredGridLayoutManager
            var pastVisibleItems = 0
            val visibleItemCount = recycleLayoutManager.childCount
            val totalItemCount = recycleLayoutManager.itemCount
            var firstVisibleItems: IntArray? = null
            firstVisibleItems =
                recycleLayoutManager.findFirstVisibleItemPositions(firstVisibleItems)
            if (firstVisibleItems != null && firstVisibleItems.isNotEmpty()) {
                pastVisibleItems = firstVisibleItems[0]
            }

            if (!isLoading) {
                if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                    pageNo++
                    makeProductListApiCall()
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

            productCategoryListAdapter.setOnSubCategoryClickListener {
                pageNo = 0
                subCatId = it.toString()
                makeProductListApiCall()
            }

            filterBrandAdapter.setOnBrandFilterClickListener {
                if (selectedBrandFilters.contains(it)) {
                    selectedBrandFilters.remove(it)
                } else {
                    selectedBrandFilters.add(it)
                }
                filterBrandAdapter.notifyItemChanged(it)
            }
            productListAdapter.setOnProductClickListener { id ->
                val bundle = Bundle().apply { putString("productId", id) }
                findNavController().navigate(R.id.productDetailsFragment, bundle)
            }
        }
    }

    private fun showSortBottomSheet() {
        val sortBinding = BottomSheetSortingBinding.inflate(layoutInflater)
        val sortDialog =
            BottomSheetDialog(requireActivity(), R.style.MyTransparentBottomSheetDialogTheme)
        sortDialog.setContentView(sortBinding.root)

        with(sortBinding) {
            popularityRadioButton.setOnClickListener {
                popular = "1"
                newest = ""
                lowToHigh = ""
                highToLow = ""
                pageNo = 0
                makeProductListApiCall()
                sortDialog.dismiss()
            }

            newestRadioButton.setOnClickListener {
                popular = ""
                newest = "1"
                lowToHigh = ""
                highToLow = ""
                pageNo = 0
                makeProductListApiCall()
                sortDialog.dismiss()
            }
            lowPriceRadioButton.setOnClickListener {
                popular = ""
                newest = ""
                lowToHigh = "1"
                highToLow = ""
                pageNo = 0
                mainViewModel.getProductList(
                    catId,
                    lowToHigh = "1",
                    deviceId = deviceId,
                    page = pageNo
                )
                sortDialog.dismiss()
            }

            highPriceRadioButton.setOnClickListener {
                popular = ""
                newest = ""
                lowToHigh = ""
                highToLow = "1"
                pageNo = 0
                mainViewModel.getProductList(
                    catId,
                    highToLow = "1",
                    deviceId = deviceId,
                    page = pageNo
                )
                sortDialog.dismiss()
            }
        }
        sortBinding.popularityRadioButton.isChecked = popular.isNotEmpty()
        sortBinding.newestRadioButton.isChecked = newest.isNotEmpty()
        sortBinding.lowPriceRadioButton.isChecked = lowToHigh.isNotEmpty()
        sortBinding.highPriceRadioButton.isChecked = highToLow.isNotEmpty()
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
        val filterDialog =
            BottomSheetDialog(requireActivity(), R.style.MyTransparentBottomSheetDialogTheme)
        filterDialog.setContentView(bottomSheetFilterBinding.root)
        filterBrandAdapter.differ.submitList(chipsData)
        with(bottomSheetFilterBinding) {
            filterBrandRv.apply {
                setHasFixedSize(true)
                layoutManager = FlexboxLayoutManager(requireContext()).apply {
                    flexDirection = FlexDirection.ROW
                }
                adapter = filterBrandAdapter
            }

            rangeSlider.addOnChangeListener { slider, _, fromUser ->
                if (fromUser) {
                    min.setText(slider.values[0].roundToInt().toString())
                    max.setText(slider.values[1].roundToInt().toString())
                }
            }

            apply.setOnClickListener {
                pageNo = 0
                minPrice = min.text.toString().trim()
                maxPrice = max.text.toString().trim()
                makeProductListApiCall()
                filterDialog.dismiss()
            }
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