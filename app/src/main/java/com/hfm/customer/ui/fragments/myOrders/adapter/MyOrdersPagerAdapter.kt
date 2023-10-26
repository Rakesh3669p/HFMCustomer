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
            0 -> MyOrdersListFragment("to_pay")
            1 -> MyOrdersListFragment("to_ship")
            2 -> MyOrdersListFragment("to_receive")
            3 -> MyOrdersListFragment("completed")
            4 -> MyOrdersListFragment("cancelled")
            5 -> BulkOrdersFragment()
            else -> {
                MyOrdersListFragment("to_pay")
            }
        }
    }

    override fun getItemCount(): Int = 6
}