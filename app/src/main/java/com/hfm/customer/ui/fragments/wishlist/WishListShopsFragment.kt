package com.hfm.customer.ui.fragments.wishlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.hfm.customer.R
import com.hfm.customer.databinding.FragmentWishlistShopsBinding
import com.hfm.customer.ui.fragments.wishlist.adapter.WishlistShopAdapter
import com.hfm.customer.utils.initRecyclerView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class WishListShopsFragment : Fragment(), View.OnClickListener {

    private lateinit var binding: FragmentWishlistShopsBinding
    private var currentView: View? = null

    @Inject lateinit var wishlistShopAdapter: WishlistShopAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_wishlist_shops, container, false)
            binding = FragmentWishlistShopsBinding.bind(currentView!!)
            init()
            setObserver()
            setOnClickListener()
        }
        return currentView!!
    }

    private fun init() {
        with(binding) {
            initRecyclerView(requireContext(),productListRv,wishlistShopAdapter)
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