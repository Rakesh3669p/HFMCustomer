package com.hfm.customer.ui.fragments.myOrders.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.hfm.customer.ui.fragments.myOrders.BulkOrdersFragment
import com.hfm.customer.ui.fragments.myOrders.MyOrdersListFragment

class MyOrdersPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> MyOrdersListFragment("pending")
            1 -> MyOrdersListFragment("confirmed")
            2 -> MyOrdersListFragment("progress")
            3 -> MyOrdersListFragment("delivered")
            4 -> MyOrdersListFragment("rejected")
            5 -> BulkOrdersFragment()
            else -> {
                MyOrdersListFragment("progress")
            }
        }
    }

    override fun getItemCount(): Int = 6
}