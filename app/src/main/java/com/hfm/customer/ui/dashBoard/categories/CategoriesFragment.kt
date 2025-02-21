package com.hfm.customer.ui.dashBoard.categories

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.hfm.customer.R
import com.hfm.customer.commonModel.CatSubcat
import com.hfm.customer.databinding.FragmentCategoriesBinding
import com.hfm.customer.ui.dashBoard.categories.adapter.CategoriesAdapter
import com.hfm.customer.ui.dashBoard.categories.adapter.CategoryMainAdapter
import com.hfm.customer.utils.Loader
import com.hfm.customer.utils.NoInternetDialog
import com.hfm.customer.utils.Resource
import com.hfm.customer.utils.initRecyclerView
import com.hfm.customer.utils.initRecyclerViewGrid
import com.hfm.customer.utils.showToast
import com.hfm.customer.viewModel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class CategoriesFragment : Fragment() {

    private var initialized: Int = 0
    private var selectedPosition: Int = 0
    private var catId: Int = -1
    private var mainCategoryData: List<CatSubcat> = ArrayList()
    private lateinit var binding: FragmentCategoriesBinding
    private var currentView: View? = null

    @Inject
    lateinit var categoryMainAdapter: CategoryMainAdapter

    @Inject
    lateinit var categoriesAdapter: CategoriesAdapter

    private lateinit var loader: Loader
    private lateinit var noInnternetDialog: NoInternetDialog
    private val mainViewModel: MainViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            initialized = savedInstanceState.getInt("initialized")
            selectedPosition = savedInstanceState.getInt("position")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_categories, container, false)
            binding = FragmentCategoriesBinding.bind(currentView!!)
            init()
            setObserver()
            setOnClickListener()
        }
        return currentView!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setObserver()
//               categoryMainAdapter.setSelectionPosition(selectedPosition)
//               currentView?.findViewById<TextView>(R.id.categoryName)?.text = mainCategoryData[selectedPosition].category_name
//               catId = mainCategoryData[selectedPosition].category_id
//               categoriesAdapter.differ.submitList(mainCategoryData[selectedPosition].subcategory)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt("initialized", 1)
        outState.putInt("position", selectedPosition)
        setCategories(mainCategoryData)
        super.onSaveInstanceState(outState)
    }


    private fun init() {
        loader = Loader(requireContext())
        noInnternetDialog = NoInternetDialog(requireContext())
        noInnternetDialog.setOnDismissListener { init() }
        mainViewModel.getCategories()
        with(binding) {
            initRecyclerView(requireContext(), categoriesMainRv, categoryMainAdapter)
            initRecyclerViewGrid(requireContext(), categoriesRv, categoriesAdapter, 3)
        }

        if(selectedPosition==0){
            binding.categoriesMainRv.post {
                binding.categoriesMainRv.scrollToPosition(0)
            }

        }
    }

    private fun setObserver() {

        mainViewModel.categories.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    loader.dismiss()
                    if (response.data?.status == "success") {
                        response.data.data.cat_subcat.let {

                            setCategories(it)
                        }
                    }
                }

                is Resource.Loading -> Unit
                is Resource.Error -> {
                    loader.dismiss()
                    showToast(response.message.toString())
                }
            }
        }
    }

    private fun setCategories(categoryData: List<CatSubcat>) {
        mainCategoryData = categoryData
        catId = mainCategoryData[selectedPosition].category_id
        binding.categoryName.text = mainCategoryData[selectedPosition].category_name
        categoryMainAdapter.setSelectionPosition(selectedPosition)
        categoryMainAdapter.differ.submitList(mainCategoryData)
        categoriesAdapter.differ.submitList(mainCategoryData[selectedPosition].subcategory)

        if(selectedPosition==0){
            binding.categoriesMainRv.post {
                binding.categoriesMainRv.scrollToPosition(0)
            }

        }
    }

    override fun onResume() {
        super.onResume()
//        selectedPosition = 0
        if(mainCategoryData.isNotEmpty()) {
            setCategories(mainCategoryData)
        }
    }

    private fun setOnClickListener() {

        categoryMainAdapter.setOnMainCategoryClickListener { position ->
            selectedPosition = position
            binding.categoryName.text = mainCategoryData[position].category_name
            catId = mainCategoryData[position].category_id
            categoriesAdapter.differ.submitList(mainCategoryData[position].subcategory)
        }
        categoriesAdapter.setOnCategoryClickListener { subCatId ->
            val bundle = Bundle()
            bundle.putString("catId", catId.toString())
            bundle.putString("subCatId", subCatId.toString())
            findNavController().navigate(R.id.productListFragment, bundle)
        }
    }
}