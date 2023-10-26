package com.hfm.customer.ui.fragments.store

import android.os.Build
import android.os.Bundle
import android.text.Html
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hfm.customer.R
import com.hfm.customer.databinding.FragmentStoreAboutBinding
import com.hfm.customer.ui.fragments.products.productDetails.adapter.VouchersAdapter
import com.hfm.customer.ui.fragments.store.model.StoreData
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class StoreAboutFragment(private val storeData: StoreData) : Fragment(), View.OnClickListener {


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
            storeData.shop_detail[0].let {
                productsCount.text = it.no_of_products.toString()
                storeRating.text = it.store_rating.toString()
                storeState.text = it.state
                storeCountry.text = it.country
                storeDescription.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Html.fromHtml(it.about, Html.FROM_HTML_MODE_COMPACT)
                } else {
                    Html.fromHtml(it.about)
                }
            }

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