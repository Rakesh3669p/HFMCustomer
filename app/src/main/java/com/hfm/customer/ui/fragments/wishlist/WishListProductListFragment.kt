package com.hfm.customer.ui.fragments.wishlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.hfm.customer.R
import com.hfm.customer.databinding.FragmentWishlistProductListBinding
import com.hfm.customer.ui.fragments.products.productList.adapter.ProductListAdapter
import com.hfm.customer.utils.initRecyclerViewGrid
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class WishListProductListFragment : Fragment(), View.OnClickListener {

    private lateinit var binding: FragmentWishlistProductListBinding
    private var currentView: View? = null

    @Inject lateinit var productListAdapter: ProductListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_wishlist_product_list, container, false)
            binding = FragmentWishlistProductListBinding.bind(currentView!!)
            init()
            setObserver()
            setOnClickListener()
        }
        return currentView!!
    }

    private fun init() {
        with(binding) {
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