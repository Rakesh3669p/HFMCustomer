package com.hfm.customer.ui.loginSignUp.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.hfm.customer.ui.loginSignUp.register.BusinessRegisterFragment
import com.hfm.customer.ui.loginSignUp.register.NormalRegisterFragment


class LoginAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle,

    ) : FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> NormalRegisterFragment()
            1 -> BusinessRegisterFragment()
            else -> {
                NormalRegisterFragment()
            }
        }
    }

    override fun getItemCount(): Int {
        return 2
    }
}
