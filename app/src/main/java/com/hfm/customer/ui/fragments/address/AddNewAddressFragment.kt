package com.hfm.customer.ui.fragments.address

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.hfm.customer.R
import com.hfm.customer.databinding.BottomSheetFilterBinding
import com.hfm.customer.databinding.BottomSheetSortingBinding
import com.hfm.customer.databinding.FragmentProductListBinding
import com.hfm.customer.ui.fragments.products.productList.adapter.FilterProductListBrandsAdapter
import com.hfm.customer.ui.fragments.products.productList.adapter.ProductCategoryListAdapter
import com.hfm.customer.ui.fragments.products.productList.adapter.ProductListAdapter
import com.hfm.customer.utils.initRecyclerView
import com.hfm.customer.utils.initRecyclerViewGrid
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class AddNewAddressFragment : Fragment(), View.OnClickListener {
    private lateinit var binding: FragmentProductListBinding
    private var currentView: View? = null

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
        with(binding) {
        }
    }

    private fun setObserver() {}

    private fun setOnClickListener() {
        with(binding) {
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
        }
    }
}