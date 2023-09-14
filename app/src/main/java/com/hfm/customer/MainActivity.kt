package com.hfm.customer

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.hfm.customer.databinding.ActivityMainBinding
import com.hfm.customer.ui.dashBoard.DashBoardActivity
import com.hfm.customer.utils.SessionManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    @Inject
    lateinit var sessionManager: SessionManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_HFMCustomer)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }
}