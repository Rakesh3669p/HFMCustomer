package com.hfm.customer

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.hfm.customer.databinding.ActivityMainBinding
import com.hfm.customer.ui.dashBoard.DashBoardActivity
import com.hfm.customer.utils.SessionManager
import com.hfm.customer.utils.getDeviceIdInternal
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    @Inject
    lateinit var sessionManager: SessionManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sessionManager.deviceId = getDeviceIdInternal(this)
    }
}