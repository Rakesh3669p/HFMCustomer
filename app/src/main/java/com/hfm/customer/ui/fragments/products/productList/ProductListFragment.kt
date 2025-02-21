package com.hfm.customer.ui.fragments.products.productList

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.view.marginBottom
import androidx.core.view.updateLayoutParams
import androidx.core.widget.doOnTextChanged
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
import com.hfm.customer.ui.fragments.brands.model.Brand
import com.hfm.customer.ui.fragments.products.productDetails.model.Product
import com.hfm.customer.ui.fragments.products.productList.adapter.FilterProductListBrandsAdapter
import com.hfm.customer.ui.fragments.products.productList.adapter.ProductCategoryListAdapter
import com.hfm.customer.ui.fragments.products.productList.adapter.ProductListAdapter
import com.hfm.customer.ui.fragments.products.productList.model.ProductListData
import com.hfm.customer.ui.fragments.products.productList.model.Subcategory
import com.hfm.customer.ui.fragments.store.StoreFragment
import com.hfm.customer.utils.Loader
import com.hfm.customer.utils.NoInternetDialog
import com.hfm.customer.utils.Resource
import com.hfm.customer.utils.SessionManager
import com.hfm.customer.utils.initRecyclerView
import com.hfm.customer.utils.makeInvisible
import com.hfm.customer.utils.makeVisible
import com.hfm.customer.utils.netWorkFailure
import com.hfm.customer.utils.showToast
import com.hfm.customer.viewModel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlin.math.roundToInt


@Suppress("CAST_NEVER_SUCCEEDS")
@AndroidEntryPoint
class ProductListFragment : Fragment(), View.OnClickListener {

    companion object {
        var selectedBrandFilters: MutableList<Int> = ArrayList()
    }


    private lateinit var filterDialog: BottomSheetDialog
    private var showProductsRandomly: Boolean = false

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
    private var feature: Int = 0


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
    var initial =true
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
        feature = arguments?.getInt("feature") ?: 0

        showProductsRandomly =
            catId.isEmpty() && subCatId.isEmpty() && brandId.isEmpty() && search.isEmpty() && wholeSale == 0 && flashSale == 0 && feature == 0

        makeProductListApiCall()
        mainViewModel.getBrands()

        if (showProductsRandomly) {
//            binding.result.makeInvisible()
            binding.bottomLoader.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = 120
            }
            binding.category.makeInvisible()
        }


        with(binding) {
            productListRv.apply {
                setHasFixedSize(true)
                layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
                adapter = productListAdapter
            }.addOnScrollListener(scrollListener)
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

            override fun onFinish() {
                updateCountdownText(0)
            }
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
            catId = if (brandId.isEmpty()) catId else "",
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
            feature = feature,
            random = if (showProductsRandomly) 1 else 0,
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
                        setProductData(response.data.data)

                    } else showToast(
                        response.data?.message.toString()
                    )
                }

                is Resource.Loading -> if (pageNo == 0) appLoader.show() else binding.bottomLoader.isVisible = true

                is Resource.Error -> apiError(response.message)
            }
        }

        mainViewModel.brands.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    if (response.data?.httpcode == 200) {
                        brands = response.data.data.brands
                        if (this::filterDialog.isInitialized && filterDialog.isShowing) {
                            filterBrandAdapter.differ.submitList(brands)
                        }
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
            if (flashSale == 1 && !data.end_time.isNullOrEmpty()) {
                binding.flashDealsGroup.isVisible = true
                setTimer(data.end_time)
            }
            productList.clear()
            if (data.subcategory_data != null) {
                if (!data.subcategory_data.subcategory.isNullOrEmpty()) {
                    val subCategories: MutableList<Subcategory> = ArrayList()
                    val subcategory = Subcategory(id = 0, subcategory_name = "All")
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


        }

        isLoading = data.products.isEmpty()
        /*if (showProductsRandomly) {
            productList.addAll(data.products.shuffled())
        } else {
        }*/
        productList.addAll(data.products)
        if (pageNo == 0 && productList.isEmpty()) {
            binding.result.makeInvisible()
            binding.category.makeInvisible()
        } else {
            binding.result.makeVisible()
            binding.category.makeVisible()
        }



        binding.noData.root.isVisible = productList.isEmpty()
        binding.noData.noDataLbl.text = "No Products Found"
        if(binding.noData.root.isVisible){
            binding.sort.isVisible = !initial
            binding.filter.isVisible = !initial
        }
        productListAdapter.differ.submitList(productList)
        productListAdapter.notifyDataSetChanged()


        if (data.products.isNotEmpty()) {
            if (wholeSale == 1) {
                binding.result.text = "${data.total_products} results for "
                binding.category.text = "Factory Direct WholeSale"
            } else if (flashSale == 1) {
                binding.result.text = "${data.total_products} results for "
                binding.category.text = "Flash Deal"
            } else if (feature == 1) {
                binding.result.text = "${data.total_products} results for "
                binding.category.text = "Featured Products"
            } else {
                binding.result.text = "${data.total_products} results for "
                binding.category.text = "${data.products[0].category_name}"
            }
        }

        if (showProductsRandomly) {
            binding.result.text = "${data.total_products} results"
            binding.category.makeInvisible()
        }
        initial = false
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
                    brandId = if (brandId.isEmpty()) "$it" else "$brandId,$it"
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
        sortDialog.setCanceledOnTouchOutside(true)
        sortDialog.setCancelable(true)
        with(sortBinding) {
            popularityRadioButton.setOnClickListener {
                sortDialog.dismiss()
                popular = "1"
                newest = ""
                lowToHigh = ""
                highToLow = ""
                pageNo = 0
                makeProductListApiCall()

            }

            newestRadioButton.setOnClickListener {
                sortDialog.dismiss()
                popular = ""
                newest = "1"
                lowToHigh = ""
                highToLow = ""
                pageNo = 0
                makeProductListApiCall()

            }
            lowPriceRadioButton.setOnClickListener {
                sortDialog.dismiss()
                popular = ""
                newest = ""
                lowToHigh = "1"
                highToLow = ""
                pageNo = 0
                mainViewModel.getProductList(
                    catId,
                    lowToHigh = "1",
                    deviceId = sessionManager.deviceId,
                    page = pageNo,
                    random = if (showProductsRandomly) 1 else 0
                )

            }

            highPriceRadioButton.setOnClickListener {
                sortDialog.dismiss()
                popular = ""
                newest = ""
                lowToHigh = ""
                highToLow = "1"
                pageNo = 0
                mainViewModel.getProductList(
                    catId,
                    highToLow = "1",
                    deviceId = sessionManager.deviceId,
                    page = pageNo,
                    random = if (showProductsRandomly) 1 else 0
                )

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
        filterDialog =
            BottomSheetDialog(requireActivity(), R.style.MyTransparentBottomSheetDialogTheme)
        filterDialog.setContentView(bottomSheetFilterBinding.root)
        filterDialog.setCanceledOnTouchOutside(true)
        filterDialog.setCancelable(true)

        filterBrandAdapter.differ.submitList(brands)
        with(bottomSheetFilterBinding) {
            filterBrandRv.apply {
                setHasFixedSize(true)
                layoutManager = FlexboxLayoutManager(requireContext()).apply {
                    flexDirection = FlexDirection.ROW
                }
                adapter = filterBrandAdapter
            }

            clearSearch.setOnClickListener {
                brandSearch.setText("")
            }

            brandSearch.doOnTextChanged { text, _, _, _ ->
                clearSearch.isVisible = !text.isNullOrEmpty()
                val searchValue = brandSearch.text.toString().lowercase()
                if (text.toString().isNotEmpty()) {
                    mainViewModel.getBrands(name="1",filterName = searchValue)
                } else if (text.isNullOrEmpty()) {
                    mainViewModel.getBrands(name="1",filterName = "")
                }

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

    override fun onDestroy() {
        selectedBrandFilters.clear()
        super.onDestroy()
    }
}