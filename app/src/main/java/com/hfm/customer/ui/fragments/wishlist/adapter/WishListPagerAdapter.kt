package com.hfm.customer.ui.fragments.wishlist.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.hfm.customer.ui.fragments.store.StoreAboutFragment
import com.hfm.customer.ui.fragments.store.StoreHomeFragment
import com.hfm.customer.ui.fragments.store.StoreProductListFragment
import com.hfm.customer.ui.fragments.store.StoreRatingFragment
import com.hfm.customer.ui.fragments.wishlist.WishListProductListFragment
import com.hfm.customer.ui.fragments.wishlist.WishListShopsFragment

class WishListPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> WishListProductListFragment()
            1 -> WishListShopsFragment()
            else -> {
                WishListProductListFragment()
            }
        }
    }

    override fun getItemCount(): Int = 2
}