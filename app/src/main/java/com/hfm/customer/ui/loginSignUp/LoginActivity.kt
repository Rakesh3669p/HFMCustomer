package com.hfm.customer.ui.loginSignUp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.viewModels
import com.hfm.customer.R
import com.hfm.customer.databinding.ActivityLoginBinding
import com.hfm.customer.ui.dashBoard.DashBoardActivity
import com.hfm.customer.utils.SessionManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    @Inject lateinit var sessionManager: SessionManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (sessionManager.isLogin) {
            startActivity(Intent(this, DashBoardActivity::class.java)).apply { finish() }
        }
    }
}