package com.hfm.customer.ui.fragments.products.productList

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.CountDownTimer
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
import com.hfm.customer.ui.dashBoard.home.model.Brand
import com.hfm.customer.ui.fragments.products.productDetails.model.Product

import com.hfm.customer.ui.fragments.products.productList.adapter.FilterProductListBrandsAdapter
import com.hfm.customer.ui.fragments.products.productList.adapter.ProductCategoryListAdapter
import com.hfm.customer.ui.fragments.products.productList.adapter.ProductListAdapter
import com.hfm.customer.ui.fragments.products.productList.model.ProductListData
import com.hfm.customer.ui.fragments.products.productList.model.Subcategory
import com.hfm.customer.utils.Loader
import com.hfm.customer.utils.NoInternetDialog
import com.hfm.customer.utils.Resource
import com.hfm.customer.utils.SessionManager
import com.hfm.customer.utils.initRecyclerView
import com.hfm.customer.utils.netWorkFailure
import com.hfm.customer.utils.showToast
import com.hfm.customer.viewModel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlin.math.roundToInt

@AndroidEntryPoint
class ProductListFragment : Fragment(), View.OnClickListener {

    companion object {
        var selectedBrandFilters: MutableList<Int> = ArrayList()
    }

    @Inject
    lateinit var sessionManager: SessionManager

    private val productList: MutableList<Product> = ArrayList()
    private var brands: List<Brand> = ArrayList()

    private var catId = ""
    private var subCatId = ""
    private var brandId = ""
    private var search = ""
    private var popular: String = ""
    private var newest: String = ""
    private var lowToHigh: String = ""
    private var highToLow: String = ""
    private var maxPrice: String = ""
    private var minPrice: String = ""
    private var wholeSale: Int = 0
    private var flashSale: Int = 0


    @Inject
    lateinit var productListAdapter: ProductListAdapter

    @Inject
    lateinit var productCategoryListAdapter: ProductCategoryListAdapter

    @Inject
    lateinit var filterBrandAdapter: FilterProductListBrandsAdapter

    private lateinit var bottomSheetFilterBinding: BottomSheetFilterBinding
    private lateinit var binding: FragmentProductListBinding
    private var currentView: View? = null

    private val mainViewModel: MainViewModel by viewModels()

    private lateinit var appLoader: Loader
    private lateinit var noInternetDialog: NoInternetDialog

    private var isLoading = false
    private var pageNo = 0
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_product_list, container, false)
            binding = FragmentProductListBinding.bind(currentView!!)
            init()

        }
        return currentView!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setObserver()
        setOnClickListener()
    }

    private fun init() {
        binding.loader.isVisible = true
        appLoader = Loader(requireContext())
        noInternetDialog = NoInternetDialog(requireContext())
        noInternetDialog.setOnDismissListener {
            init()
        }
        catId = arguments?.getString("catId") ?: ""
        subCatId = arguments?.getString("subCatId") ?: ""
        brandId = arguments?.getString("brandId") ?: ""
        search = arguments?.getString("keyword") ?: ""
        wholeSale = arguments?.getInt("wholeSale") ?: 0
        flashSale = arguments?.getInt("flashSale") ?: 0
        val endTime = arguments?.getString("endTime")?:""
        makeProductListApiCall()
        mainViewModel.getBrandsList()

        with(binding) {
            productListRv.apply {
                setHasFixedSize(true)
                layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
                adapter = productListAdapter
            }.addOnScrollListener(scrollListener)
        }

        if(flashSale == 1 && endTime.isNotEmpty()){
            binding.flashDealsGroup.isVisible = true
            setTimer(endTime)
        }
    }

    private fun setTimer(endTime: String) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val endTimeString = endTime
        val endTime = dateFormat.parse(endTimeString) ?: Date()
        val currentTime = Date()
        val timeDifference = endTime.time - currentTime.time
        val countdownTimer = object : CountDownTimer(timeDifference, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                updateCountdownText(millisUntilFinished)
            }
            override fun onFinish() { updateCountdownText(0) }
        }
        countdownTimer.start()
    }

    private fun updateCountdownText(millisUntilFinished: Long) {
        val days = millisUntilFinished / (1000 * 60 * 60 * 24)
        val hours = (millisUntilFinished % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60)
        val minutes = (millisUntilFinished % (1000 * 60 * 60)) / (1000 * 60)
        val seconds = (millisUntilFinished % (1000 * 60)) / 1000
        binding.flashDealsTime.day.text = String.format(Locale.getDefault(), "%02d", days)
        binding.flashDealsTime.hour.text = String.format(Locale.getDefault(), "%02d", hours)
        binding.flashDealsTime.minutes.text = String.format(Locale.getDefault(), "%02d", minutes)
        binding.flashDealsTime.seconds.text = String.format(Locale.getDefault(), "%02d", seconds)
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
            deviceId = sessionManager.deviceId,
            page = pageNo,
            wholeSale = wholeSale,
            flashSale = flashSale,

            )
    }

    private fun setObserver() {
        mainViewModel.productList.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    binding.loader.isVisible = false
                    binding.bottomLoader.isVisible = false
                    if (response.data?.httpcode == 200) setProductData(response.data.data) else showToast(
                        response.data?.message.toString()
                    )
                }

                is Resource.Loading -> if (pageNo == 0) appLoader.show() else binding.bottomLoader.isVisible =
                    true

                is Resource.Error -> apiError(response.message)
            }
        }

        mainViewModel.homeBrands.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    if (response.data?.httpcode == 200) {
                        brands = response.data.data.brands
                    }
                }

                is Resource.Loading -> Unit
                is Resource.Error -> apiError(response.message)
            }
        }
    }

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    private fun setProductData(data: ProductListData) {
        if (pageNo == 0) {
            productList.clear()
            if (!data.subcategory_data.subcategory.isNullOrEmpty()) {
                val subCategories: MutableList<Subcategory> = ArrayList()
                val subcategory = Subcategory(id = 0, subcategory_name = "ALL")
                subCategories.add(subcategory)
                subCategories.addAll(data.subcategory_data.subcategory)
                initRecyclerView(
                    requireContext(),
                    binding.categoriesListRv,
                    productCategoryListAdapter,
                    true
                )
                productCategoryListAdapter.differ.submitList(subCategories)
            }
        }

        isLoading = data.products.isEmpty()
        productList.addAll(data.products)
        binding.noData.root.isVisible = productList.isEmpty()
        productListAdapter.differ.submitList(productList)
        productListAdapter.notifyDataSetChanged()

        if (data.products.isNotEmpty()) {
            binding.result.text =
                "${data.total_products} results for ${data.products[0].category_name}"
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
            search.setOnClickListener(this@ProductListFragment)

            productCategoryListAdapter.setOnSubCategoryClickListener {
                pageNo = 0
                subCatId = if (it == 0) {
                    ""
                } else {
                    it.toString()
                }
                makeProductListApiCall()
            }
            filterBrandAdapter.setOnBrandFilterClickListener { it, adapterPosition ->
                if (selectedBrandFilters.contains(it)) {
                    selectedBrandFilters.remove(it)
                } else {
                    selectedBrandFilters.add(it)
                }
                filterBrandAdapter.notifyItemChanged(adapterPosition)
                brandId = ""
                selectedBrandFilters.forEach {
                    brandId = "$brandId,$it"
                }
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
                    deviceId = sessionManager.deviceId,
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
                    deviceId = sessionManager.deviceId,
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

    private fun showFilterBottomSheet() {
        bottomSheetFilterBinding = BottomSheetFilterBinding.inflate(layoutInflater)
        val filterDialog =
            BottomSheetDialog(requireActivity(), R.style.MyTransparentBottomSheetDialogTheme)
        filterDialog.setContentView(bottomSheetFilterBinding.root)
        filterBrandAdapter.differ.submitList(brands)
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
            cancel.setOnClickListener {
                filterDialog.dismiss()
            }
        }

        filterDialog.show()
    }

    private fun apiError(message: String?) {
        appLoader.dismiss()
        binding.bottomLoader.isVisible = false
        showToast(message.toString())
        if (message == netWorkFailure) {
            noInternetDialog.show()
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.back.id -> findNavController().popBackStack()
            binding.sort.id -> showSortBottomSheet()
            binding.filter.id -> showFilterBottomSheet()
            binding.search.id -> findNavController().navigate(R.id.searchFragment)
        }
    }
}