package com.hfm.customer.ui.fragments.store.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.hfm.customer.ui.fragments.store.StoreAboutFragment
import com.hfm.customer.ui.fragments.store.StoreHomeFragment
import com.hfm.customer.ui.fragments.store.StoreProductListFragment
import com.hfm.customer.ui.fragments.store.StoreRatingFragment
import com.hfm.customer.ui.fragments.store.model.StoreData
import com.hfm.customer.ui.fragments.store.model.StoreProductData

class StorePagerAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle,
    var storeData: StoreData
) :
    FragmentStateAdapter(fragmentManager, lifecycle) {
    private val fragments: MutableMap<Int, Fragment> = mutableMapOf()
    override fun createFragment(position: Int): Fragment {
        val fragment = when (position) {
            0 -> StoreHomeFragment(storeData)
            1 -> StoreProductListFragment(storeData)
            2 -> StoreRatingFragment(storeData)
            3 -> StoreAboutFragment(storeData)
            else -> {
                StoreHomeFragment(storeData)
            }
        }
        fragments[position] = fragment
        return fragment
    }

    override fun getItemCount(): Int = 4

    fun getFragment(position: Int): Fragment? {
        return fragments[position]
    }
}