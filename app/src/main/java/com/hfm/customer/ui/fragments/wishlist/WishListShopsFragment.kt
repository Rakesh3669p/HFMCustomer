package com.hfm.customer.ui.fragments.wishlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.hfm.customer.R
import com.hfm.customer.databinding.FragmentWishlistShopsBinding
import com.hfm.customer.ui.fragments.wishlist.adapter.WishlistShopAdapter
import com.hfm.customer.ui.fragments.wishlist.model.StoreFavouriteData
import com.hfm.customer.utils.Loader
import com.hfm.customer.utils.NoInternetDialog
import com.hfm.customer.utils.Resource
import com.hfm.customer.utils.initRecyclerView
import com.hfm.customer.utils.netWorkFailure
import com.hfm.customer.utils.showToast
import com.hfm.customer.viewModel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class WishListShopsFragment : Fragment() {

    private lateinit var binding: FragmentWishlistShopsBinding
    private var currentView: View? = null

    @Inject
    lateinit var wishlistShopAdapter: WishlistShopAdapter
    private lateinit var appLoader: Loader
    private lateinit var noInternetDialog: NoInternetDialog
    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_wishlist_shops, container, false)
            binding = FragmentWishlistShopsBinding.bind(currentView!!)
            init()
            setOnClickListener()
        }
        return currentView!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setObserver()
    }

    private fun init() {
        appLoader = Loader(requireContext())
        noInternetDialog = NoInternetDialog(requireContext())
        noInternetDialog.setOnDismissListener { init() }
        mainViewModel.followedShops()

    }

    private fun setObserver() {
        mainViewModel.followedShops.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    if (response.data?.httpcode == "200") {
                        setFavStores(response.data.data)
                    } else {
                        binding.noDataLayout.root.isVisible = true
                        showToast(response.data?.message.toString())
                    }
                }

                is Resource.Loading -> appLoader.show()
                is Resource.Error -> apiError(response.message)
            }
        }

        mainViewModel.unFollowShop.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    mainViewModel.followedShops()
                }

                is Resource.Loading -> appLoader.show()
                is Resource.Error -> apiError(response.message)
            }
        }
    }

    private fun setFavStores(data: StoreFavouriteData) {
        binding.noDataLayout.root.isVisible = data.favourite_list.isEmpty()
        initRecyclerView(requireContext(), binding.productListRv, wishlistShopAdapter)
        wishlistShopAdapter.differ.submitList(data.favourite_list)
    }

    private fun apiError(message: String?) {
        appLoader.dismiss()
        showToast(message.toString())
        if (message == netWorkFailure) {
            noInternetDialog.show()
        }
    }

    private fun setOnClickListener() {

        wishlistShopAdapter.setOnUnFollowClickListener {
            mainViewModel.unFollowShop(it.toString())
        }

        wishlistShopAdapter.setOnChatClickListener { data ->
            val bundle = Bundle()
            bundle.putString("from", "chatList")
            bundle.putString("storeName", data.store_name)
            bundle.putString("sellerId", data.seller_id.toString())
            bundle.putString("saleId", "")
            bundle.putInt("chatId", data.chat_id ?: 0)
            findNavController().navigate(R.id.chatFragment, bundle)
        }
        wishlistShopAdapter.setOnItemClickListener { sellerId ->
            val bundle = Bundle()
            bundle.putString("storeId", sellerId.toString())
            findNavController().navigate(R.id.storeFragment, bundle)
        }
    }
}