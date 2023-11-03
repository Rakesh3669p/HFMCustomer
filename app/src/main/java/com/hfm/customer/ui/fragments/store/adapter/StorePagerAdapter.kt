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

class StorePagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle, var storeData: StoreData) :
    FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> StoreHomeFragment(storeData)
            1 -> StoreProductListFragment(storeData)
            2 -> StoreRatingFragment(storeData)
            3 -> StoreAboutFragment(storeData)
            else -> {
                StoreHomeFragment(storeData)
            }
        }
    }

    override fun getItemCount(): Int = 4
}