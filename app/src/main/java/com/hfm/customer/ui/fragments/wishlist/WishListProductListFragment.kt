package com.hfm.customer.ui.fragments.wishlist

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.hfm.customer.R
import com.hfm.customer.databinding.FragmentWishlistProductListBinding
import com.hfm.customer.ui.fragments.products.productList.adapter.ProductListAdapter
import com.hfm.customer.utils.Loader
import com.hfm.customer.utils.NoInternetDialog
import com.hfm.customer.utils.Resource
import com.hfm.customer.utils.initRecyclerViewGrid
import com.hfm.customer.utils.netWorkFailure
import com.hfm.customer.utils.showToast
import com.hfm.customer.viewModel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class WishListProductListFragment : Fragment() {

    private lateinit var binding: FragmentWishlistProductListBinding
    private var currentView: View? = null
    private val mainViewModel: MainViewModel by viewModels()

    @Inject lateinit var productListAdapter: ProductListAdapter

    private lateinit var noInternetDialog: NoInternetDialog
    private lateinit var appLoader: Loader
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
        appLoader = Loader(requireContext())
        noInternetDialog = NoInternetDialog(requireContext())
        noInternetDialog.setOnDismissListener { init() }
        initRecyclerViewGrid(requireContext(),binding.wishListRv,productListAdapter,2)
        mainViewModel.getWishListProducts()
    }

    @SuppressLint("SetTextI18n")
    private fun setObserver() {
        mainViewModel.wishListProducts.observe(viewLifecycleOwner){response->
            when(response){
                is Resource.Success->{
                    appLoader.dismiss()
                    if(response.data?.httpcode=="200"){
                        binding.noDataLayout.root.isVisible = response.data.data.wishlist.isEmpty()
                        binding.noDataLayout.noDataLbl.text = "No favourite products found!"
                        productListAdapter.differ.submitList(response.data.data.wishlist)
                    }else{
                        showToast(response.data?.message.toString())
                    }
                }
                is Resource.Loading-> appLoader.show()
                is Resource.Error->{
                    appLoader.dismiss()
                    showToast(response.message.toString())
                    if(response.message == netWorkFailure){
                        noInternetDialog.show()
                    }
                }
            }
        }
    }

    private fun setOnClickListener() {
            productListAdapter.setOnProductClickListener {
                val bundle = Bundle().apply { putString("productId", it) }
                findNavController().navigate(R.id.productDetailsFragment,bundle)
            }
    }
}