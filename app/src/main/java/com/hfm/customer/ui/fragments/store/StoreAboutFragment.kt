package com.hfm.customer.ui.fragments.store

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hfm.customer.R
import com.hfm.customer.databinding.FragmentStoreAboutBinding
import com.hfm.customer.ui.fragments.products.productDetails.adapter.VouchersAdapter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class StoreAboutFragment : Fragment(), View.OnClickListener {


    private lateinit var binding: FragmentStoreAboutBinding
    private var currentView: View? = null

    @Inject lateinit var vouchersAdapter: VouchersAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_store_about, container, false)
            binding = FragmentStoreAboutBinding.bind(currentView!!)
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