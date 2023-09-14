package com.hfm.customer.ui.fragments.referral

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.hfm.customer.R
import com.hfm.customer.databinding.FragmentProductListBinding
import com.hfm.customer.databinding.FragmentReferalBinding
import com.hfm.customer.utils.Loader
import com.hfm.customer.utils.NoInternetDialog
import com.hfm.customer.viewModel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ReferralFragment : Fragment(), View.OnClickListener {

    private lateinit var binding: FragmentReferalBinding
    private var currentView: View? = null
    private val mainViewModel: MainViewModel by viewModels()
    private lateinit var appLoader: Loader
    private lateinit var noInternetDialog: NoInternetDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_referal, container, false)
            binding = FragmentReferalBinding.bind(currentView!!)
            init()
            setObserver()
            setOnClickListener()
        }
        return currentView!!
    }

    private fun init() {
        appLoader = Loader(requireContext())
        noInternetDialog = NoInternetDialog(requireContext())
        noInternetDialog.setOnDismissListener { noInternetDialog.dismiss() }
    }

    private fun setObserver() {
    }


    private fun setOnClickListener() {
        with(binding) {
            back.setOnClickListener(this@ReferralFragment)

        }
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            binding.back.id -> findNavController().popBackStack()
        }
    }
}