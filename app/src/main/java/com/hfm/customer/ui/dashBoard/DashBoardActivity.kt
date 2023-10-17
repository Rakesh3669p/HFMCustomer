package com.hfm.customer.ui.dashBoard

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI.setupWithNavController
import com.hfm.customer.R
import com.hfm.customer.databinding.ActivityDashBoardBinding
import com.hfm.customer.databinding.ActivityMainBinding
import com.hfm.customer.utils.showToast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DashBoardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashBoardBinding
    private lateinit var navHostFragment: NavHostFragment
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_HFMCustomer)
        binding = ActivityDashBoardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
    }

    private fun init() {
        navController = findNavController(R.id.nav_host_fragment_content_main)
        navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        setupWithNavController(binding.bottomNavigationView, navHostFragment.navController)
        navController.addOnDestinationChangedListener(destinationListener)

    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    private val destinationListener =
        NavController.OnDestinationChangedListener { _, destination, _ ->
            val validFragmentIds = listOf(
                R.id.homeFragment,
                R.id.categoriesFragment,
                R.id.profileFragment,
                R.id.exploreFragment,
                R.id.chatUsersFragment
            )
            val checkable = destination.id in validFragmentIds

            for (i in 0..4) {
                binding.bottomNavigationView.menu.getItem(i).isCheckable = checkable
            }
            val validFragmentBottomNavIds = listOf(
                R.id.homeFragment,
                R.id.categoriesFragment,
                R.id.profileFragment,
                R.id.categoriesFragment,
                R.id.exploreFragment,
                R.id.chatUsersFragment
            )
            val showBottomNavBar = destination.id in validFragmentBottomNavIds
            binding.bottomNavigationView.isVisible = showBottomNavBar
            binding.divider.isVisible = showBottomNavBar
        }
}