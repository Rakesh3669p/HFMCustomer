package com.hfm.customer.ui.dashBoard

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.PersistableBundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI.setupWithNavController
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.ktx.isFlexibleUpdateAllowed
import com.google.android.play.core.ktx.isImmediateUpdateAllowed
import com.google.firebase.messaging.FirebaseMessaging
import com.hfm.customer.BuildConfig
import com.hfm.customer.R
import com.hfm.customer.databinding.ActivityDashBoardBinding
import com.hfm.customer.ui.loginSignUp.LoginActivity
import com.hfm.customer.utils.Resource
import com.hfm.customer.utils.SessionManager
import com.hfm.customer.utils.getDeviceIdInternal
import com.hfm.customer.utils.showToast
import com.hfm.customer.viewModel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import javax.inject.Inject


private val MY_REQUEST_CODE: Int = 87945

@AndroidEntryPoint
class DashBoardActivity : AppCompatActivity() {
    private var doubleBackToExitPressedOnce = false
    private val DOUBLE_CLICK_EXIT_DELAY = 2000
    private lateinit var binding: ActivityDashBoardBinding
    private lateinit var navHostFragment: NavHostFragment
    private lateinit var navController: NavController

    @Inject
    lateinit var sessionManager: SessionManager
    private val mainViewModel: MainViewModel by viewModels()
    private lateinit var appUpdateManager: AppUpdateManager
    private var updateType = AppUpdateType.FLEXIBLE
    private var isCriticalUpdate: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_HFMCustomer)
        binding = ActivityDashBoardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setObserver()
        init()
    }

    private fun init() {
        appUpdateManager = AppUpdateManagerFactory.create(applicationContext)
        navController = findNavController(R.id.nav_host_fragment_content_main)
        navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        setupWithNavController(binding.bottomNavigationView, navHostFragment.navController)

        navController.addOnDestinationChangedListener(destinationListener)
        sessionManager.deviceId = getDeviceIdInternal(this)
        mainViewModel.getAppUpdate()
        val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
        mainViewModel.checkLogin()
        updateFCMToken()

        intent?.getStringExtra("appTargetPage")?.takeIf { it.isNotEmpty() }?.let {
            notificationsHandling(intent)
        }
        if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED){

        } else {
            if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)){
                Snackbar.make(
                    findViewById(android.R.id.content),
                    "Please allow notification permission",
                    Snackbar.LENGTH_INDEFINITE
                ).setAction("Enable Notification") {
                    val intent = Intent().apply {
                        action = android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        data = Uri.fromParts("package", packageName, null)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    startActivity(intent)
                }.setActionTextColor(ContextCompat.getColor(this, R.color.red))
                    .setDuration(4000).show()
            } else {
                // first request or forever denied case
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }

    }

    private fun updateFCMToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener {
            if (it.isSuccessful) {
                mainViewModel.updateDeviceToken(it.result)
            }
        }
    }


    private fun setObserver() {

        mainViewModel.checkLogin.observe(this) { response ->
            when (response) {
                is Resource.Success -> {
                    response.data?.let {
                        if (it.httpcode == 401) {
                            sessionManager.isLogin = false
                        }
                    }
                }

                is Resource.Loading -> Unit
                is Resource.Error -> Unit
            }
        }

        mainViewModel.checkAppUpdate.observe(this) { response ->
            when (response) {
                is Resource.Success -> {
                    response.data?.let {
                        if (it.status) {
                            isCriticalUpdate = it.data.critical_update == 1
                            appUpdate(it.data.critical_update)
                        }
                    }
                }

                is Resource.Loading -> Unit
                is Resource.Error -> {
                    isCriticalUpdate = false
                    appUpdate(0)
                }
            }
        }

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


    private fun setFlexibleUpdate() {
        try {
            if (updateType == AppUpdateType.FLEXIBLE) {
                appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
                    if (info.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                        appUpdateManager.startUpdateFlowForResult(
                            info, updateType, this, MY_REQUEST_CODE
                        )
                    }
                }
            }
        } catch (_: IllegalArgumentException) {
        }
    }

    private fun appUpdate(type: Int) {
        /** updateType 0 for Immediate
         *  1 for Flexible
         */

        if (type == 0) appUpdateManager.registerListener(installStateUpdateListener)
        updateType = if (type == 1) AppUpdateType.IMMEDIATE else AppUpdateType.FLEXIBLE
        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            val isUpdateAvailable = info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
            val isUpdateAllowed = when (updateType) {
                AppUpdateType.FLEXIBLE -> info.isFlexibleUpdateAllowed
                AppUpdateType.IMMEDIATE -> info.isImmediateUpdateAllowed
                else -> false
            }
            if (isUpdateAvailable && isUpdateAllowed) {
                appUpdateManager.startUpdateFlowForResult(info, updateType, this, MY_REQUEST_CODE)
            }
        }
    }


    private val installStateUpdateListener = InstallStateUpdatedListener { state ->
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            showToast("Downloaded Successfully Restarting app in 5 seconds.")
            lifecycleScope.launch {
                delay(5000)
                appUpdateManager.completeUpdate()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == MY_REQUEST_CODE) {
            if (resultCode != RESULT_OK) {
                showToast("Update flow failed! Result code: $resultCode $data ")
                Log.e("MY_APP", "Update flow failed! Result code: $resultCode")
                if (isCriticalUpdate) {
                    appUpdate(1)
                }
            } else {
                Log.e("MY_APP", "Update flow Success! Result code: $resultCode")
            }
        }
    }



    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.getStringExtra("notificationFrom").toString().isNotEmpty()) {
            lifecycleScope.launch {
                notificationsHandling(intent)
            }
        }
    }

    private fun notificationsHandling(intent: Intent?) {
        if(!sessionManager.isLogin){
            startActivity(Intent(this@DashBoardActivity,LoginActivity::class.java))
            return
        }
        val appTargetPage = intent?.getStringExtra("appTargetPage").toString()
        val notifyType = intent?.getStringExtra("notify_type").toString()
        val refId = intent?.getStringExtra("ref_id").toString()
        val refLink = intent?.getStringExtra("ref_link").toString()
        val bulkOrderSaleId = intent?.getStringExtra("bulk_order_sale_id").toString()
        val orderId = intent?.getStringExtra("order_id").toString()
        val id = intent?.getStringExtra("id").toString()
        val title = intent?.getStringExtra("title").toString()
        val description = intent?.getStringExtra("description").toString()

        when (appTargetPage) {
            "order","bulk_order" -> {
                when (notifyType) {
                    "quotation_accepted", "bulk_order_requested", "quotation_rejected", "quotation_created" -> {
                        val bundle = Bundle()
                        bundle.putString("from", "bulkOrder")
                        bundle.putString("orderId", refId)
                        bundle.putString("saleId", bulkOrderSaleId)
//                        mainViewModel.viewedNotification(id.toInt())
                        navController.navigate(R.id.bulkOrderDetailsFragment, bundle)
                    }

                    else -> {
                        val bundle = Bundle()
                        bundle.putString("orderId", orderId)
                        bundle.putString("saleId", refId)
//                        mainViewModel.viewedNotification(id.toInt())
                        navController.navigate(R.id.orderDetailsFragment, bundle)
                    }
                }
            }

            "product" -> {
                val bundle =
                    Bundle().apply { putString("productId", refId) }
                navController.navigate(R.id.productDetailsFragment, bundle)
//                mainViewModel.viewedNotification(id.toInt())
            }

            "flash_deal" -> {
                val bundle = Bundle()
                bundle.putInt("flashSale", 1)
                navController.navigate(R.id.productListFragment, bundle)
//                mainViewModel.viewedNotification(id.toInt())
            }

            "profile" -> {
//                mainViewModel.viewedNotification(id.toInt())
                binding.bottomNavigationView.selectedItemId = R.id.profileFragment
            }

            "cart" -> {
                navController.navigate(R.id.cartFragment)
//                mainViewModel.viewedNotification(id.toInt())
            }

            "support" -> {
                val bundle = Bundle()
                bundle.putInt("supportId",refId.toInt())
                navController.navigate(R.id.supportChatFragment,bundle)
//                mainViewModel.viewedNotification(id.toInt())

            }

            "new_chat_message","chat" -> {
                val bundle = Bundle().apply {
                    putString("from", "chatList")
                    putInt("chatId", refId.toInt())
                }
                navController.navigate(R.id.chatFragment, bundle)
//                mainViewModel.viewedNotification(id.toInt())
            }

            "notification_detail" -> {
                val bundle = Bundle().apply {
                    putString("redirection", refLink)
                    putString("title", title)
                    putString("description", description)
                }
                navController.navigate(R.id.customNotificationViewFragment, bundle)
//                mainViewModel.viewedNotification(notificationId = id.toInt())
            }
        }


    }

    fun toProfile() {
        navController.popBackStack()
        binding.bottomNavigationView.selectedItemId = R.id.profileFragment
    }

    override fun onResume() {
        super.onResume()
        setFlexibleUpdate()
    }

    override fun onBackPressed() {
        if (navController.currentDestination?.id == R.id.homeFragment) {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed()
                return
            }
            this.doubleBackToExitPressedOnce = true
            Toast.makeText(this, "Click back again to exit", Toast.LENGTH_SHORT).show()
            Handler(Looper.getMainLooper()).postDelayed(
                { doubleBackToExitPressedOnce = false },
                DOUBLE_CLICK_EXIT_DELAY.toLong()
            )
        } else {
            navController.popBackStack()
        }
    }

    val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {

            } else {

            }
        }
}