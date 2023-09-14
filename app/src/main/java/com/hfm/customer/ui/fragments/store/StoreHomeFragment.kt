package com.hfm.customer.ui.fragments.store

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.tabs.TabLayoutMediator
import com.hfm.customer.R
import com.hfm.customer.databinding.FragmentStoreHomeBinding
import com.hfm.customer.ui.fragments.products.productDetails.adapter.VouchersAdapter
import com.hfm.customer.ui.fragments.store.adapter.StoreBannerAdapter
import com.hfm.customer.utils.initRecyclerView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class StoreHomeFragment : Fragment(), View.OnClickListener {


    private lateinit var binding: FragmentStoreHomeBinding
    private var currentView: View? = null

    @Inject lateinit var bannerAdapter: StoreBannerAdapter
    @Inject lateinit var vouchersAdapter: VouchersAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_store_home, container, false)
            binding = FragmentStoreHomeBinding.bind(currentView!!)
            init()
            setObserver()
            setBanner()
            setOnClickListener()
        }
        return currentView!!
    }


    private fun init() {
        with(binding) {
            initRecyclerView(requireContext(),binding.vouchersRv,vouchersAdapter,true)
        }
    }

    private fun setBanner() {



        val banners = listOf(
            "https://foodtank.com/wp-content/uploads/2021/09/gemma-stpjHJGqZyw-unsplash.jpg",
            "https://www.tastingtable.com/img/gallery/is-grocery-store-produce-washed-before-it-reaches-shelves/l-intro-1655496657.jpg",
            "https://www.explorencr.com/wp-content/uploads/2019/08/Super-Market-in-Noida.jpg",
            "https://foodtank.com/wp-content/uploads/2021/09/gemma-stpjHJGqZyw-unsplash.jpg",
            "https://www.tastingtable.com/img/gallery/is-grocery-store-produce-washed-before-it-reaches-shelves/l-intro-1655496657.jpg",
            "https://www.explorencr.com/wp-content/uploads/2019/08/Super-Market-in-Noida.jpg",
            "https://foodtank.com/wp-content/uploads/2021/09/gemma-stpjHJGqZyw-unsplash.jpg",
            "https://www.tastingtable.com/img/gallery/is-grocery-store-produce-washed-before-it-reaches-shelves/l-intro-1655496657.jpg",
            "https://www.explorencr.com/wp-content/uploads/2019/08/Super-Market-in-Noida.jpg"
        )

        bannerAdapter.differ.submitList(banners)
        binding.banner.adapter = bannerAdapter
        val tabLayoutMediator = TabLayoutMediator(binding.bannerDots, binding.banner, true) { _, _ -> }
        tabLayoutMediator.attach()

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