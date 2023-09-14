package com.hfm.customer.ui.fragments.myOrders.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.hfm.customer.ui.fragments.myOrders.BulkOrdersFragment
import com.hfm.customer.ui.fragments.myOrders.CancelledFragment
import com.hfm.customer.ui.fragments.myOrders.CompletedFragment
import com.hfm.customer.ui.fragments.myOrders.ToPayFragment
import com.hfm.customer.ui.fragments.myOrders.ToReceiveFragment
import com.hfm.customer.ui.fragments.myOrders.ToShipFragment
import com.hfm.customer.ui.fragments.store.StoreAboutFragment
import com.hfm.customer.ui.fragments.store.StoreHomeFragment
import com.hfm.customer.ui.fragments.store.StoreProductListFragment
import com.hfm.customer.ui.fragments.store.StoreRatingFragment

class MyOrdersPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ToPayFragment()
            1 -> ToShipFragment()
            2 -> ToReceiveFragment()
            3 -> CompletedFragment()
            4 -> CancelledFragment()
            5 -> BulkOrdersFragment()
            else -> {
                StoreHomeFragment()
            }
        }
    }

    override fun getItemCount(): Int = 6
}