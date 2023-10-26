package com.hfm.customer.ui.loginSignUp.register

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.hfm.customer.R
import com.hfm.customer.databinding.FragmentLoginBinding
import com.hfm.customer.databinding.FragmentRegisterBinding
import com.hfm.customer.ui.loginSignUp.LoginSignUpViewModel
import com.hfm.customer.ui.loginSignUp.adapter.LoginAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterFragment : Fragment(), View.OnClickListener {

    private lateinit var binding: FragmentRegisterBinding
    private var currentView: View? = null
    private val loginSignUpViewModel: LoginSignUpViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_register, container, false)
            binding = FragmentRegisterBinding.bind(currentView!!)
            init()
            setObserver()
            setOnClickListener()
        }
        return currentView!!
    }

    private fun init() {
        with(binding.viewPager) {
            val viewPagerAdapter = LoginAdapter(childFragmentManager, lifecycle)
            adapter = viewPagerAdapter
            isUserInputEnabled = false
            isSaveEnabled = false
        }
    }

    private fun setObserver() {}

    private fun setOnClickListener() {
        with(binding) {
            normalRadioButton.setOnClickListener(this@RegisterFragment)
            businessRadioButton.setOnClickListener(this@RegisterFragment)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.normalRadioButton.id -> {
                binding.normalRadioButton.isChecked = true
                binding.businessRadioButton.isChecked = false
                binding.viewPager.setCurrentItem(0, false)
            }

            binding.businessRadioButton.id -> {
                binding.businessRadioButton.isChecked = true
                binding.normalRadioButton.isChecked = false
                binding.viewPager.setCurrentItem(1, false)
            }
        }
    }
}