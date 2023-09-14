package com.hfm.customer.ui.fragments.store

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hfm.customer.R
import com.hfm.customer.databinding.FragmentStoreRatingBinding
import com.hfm.customer.ui.fragments.products.productDetails.adapter.ReviewsAdapter
import com.hfm.customer.utils.initRecyclerView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class StoreRatingFragment : Fragment() , View.OnClickListener {

    private lateinit var binding: FragmentStoreRatingBinding
    private var currentView: View? = null

    @Inject lateinit var reviewsAdapter: ReviewsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_store_rating, container, false)
            binding = FragmentStoreRatingBinding.bind(currentView!!)
            init()
            setRecyclerViews()
            setObserver()
            setOnClickListener()
        }
        return currentView!!
    }

    private fun init() {}

    private fun setRecyclerViews() {
        with(binding){
            initRecyclerView(requireContext(), reviewsRv, reviewsAdapter)
        }
    }


    private fun setObserver() {}

    private fun setOnClickListener() {
        with(binding) {
        }
    }


    override fun onClick(v: View?) {
        when (v?.id) { }
    }
}