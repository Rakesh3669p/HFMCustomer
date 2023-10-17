package com.hfm.customer.ui.dashBoard.categories

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
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
import javax.inject.Inject

@AndroidEntryPoint
class CategoriesFragment : Fragment() {

    private var catId: Int = -1
    private lateinit var mainCategoryData: List<CatSubcat>
    private lateinit var binding: FragmentCategoriesBinding
    private var currentView: View? = null

    @Inject
    lateinit var categoryMainAdapter: CategoryMainAdapter

    @Inject
    lateinit var categoriesAdapter: CategoriesAdapter

    private lateinit var loader: Loader
    private lateinit var noInnternetDialog: NoInternetDialog
    private val mainViewModel: MainViewModel by activityViewModels()

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

    private fun init() {
        loader = Loader(requireContext())
        noInnternetDialog = NoInternetDialog(requireContext())
        noInnternetDialog.setOnDismissListener { init() }
        mainViewModel.getCategories()
        with(binding) {
            initRecyclerView(requireContext(), categoriesMainRv, categoryMainAdapter)
            initRecyclerViewGrid(requireContext(), categoriesRv, categoriesAdapter, 3)
        }
    }

    private fun setObserver() {

        mainViewModel.categories.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    loader.dismiss()
                    if (response.data?.status == "success") {
                        response.data.data.cat_subcat.let { setCategories(it) }
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
        categoryMainAdapter.differ.submitList(mainCategoryData)
        categoriesAdapter.differ.submitList(mainCategoryData[0].subcategory)
        catId = mainCategoryData[0].category_id
    }

    private fun setOnClickListener() {

        categoryMainAdapter.setOnMainCategoryClickListener { position ->
            binding.categoryName.text = mainCategoryData[position].category_name
            catId = mainCategoryData[position].category_id
            categoriesAdapter.differ.submitList(mainCategoryData[position].subcategory)
        }
        categoriesAdapter.setOnCategoryClickListener {subCatId->
            val bundle = Bundle()
            bundle.putString("catId", catId.toString())
            bundle.putString("subCatId", subCatId.toString())
            findNavController().navigate(R.id.productListFragment, bundle)
        }
    }
}