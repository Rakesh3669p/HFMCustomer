package com.hfm.customer.ui.fragments.vouchers

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.hfm.customer.R
import com.hfm.customer.databinding.FragmentVouchersBinding
import com.hfm.customer.ui.fragments.cart.model.Coupon
import com.hfm.customer.ui.fragments.vouchers.adapter.ClaimedVouchersAdapter
import com.hfm.customer.ui.loginSignUp.LoginActivity
import com.hfm.customer.utils.Loader
import com.hfm.customer.utils.NoInternetDialog
import com.hfm.customer.utils.Resource
import com.hfm.customer.utils.SessionManager
import com.hfm.customer.utils.initRecyclerView
import com.hfm.customer.utils.netWorkFailure
import com.hfm.customer.utils.showToast
import com.hfm.customer.viewModel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class ClaimedVouchersFragment : Fragment(){
    private var platformVouchers: List<Coupon> = ArrayList()
    private lateinit var binding: FragmentVouchersBinding
    private var currentView: View? = null
    private val mainViewModel: MainViewModel by viewModels()
    private lateinit var appLoader: Loader
    private lateinit var noInternetDialog: NoInternetDialog

    @Inject lateinit var claimedVouchersAdapter: ClaimedVouchersAdapter

    @Inject lateinit var sessionManager: SessionManager
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_vouchers, container, false)
            binding = FragmentVouchersBinding.bind(currentView!!)
            init()
        }
        return currentView!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setObserver()
    }

    private fun init() {
        appLoader = Loader(requireContext())
        noInternetDialog = NoInternetDialog(requireContext())
        noInternetDialog.setOnDismissListener { init() }
        mainViewModel.getClaimedVouchers()
    }

    private fun setObserver() {
        mainViewModel.claimedVouchers.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    if (response.data?.httpcode == 200) {
                        platformVouchers = response.data.data.coupon_list
                        initRecyclerView(requireContext(), binding.vouchersRv, claimedVouchersAdapter)
                        claimedVouchersAdapter.differ.submitList(platformVouchers)
                        binding.noData.root.isVisible = platformVouchers.isEmpty()
                        binding.noData.noDataLbl.text = "No claimed vouchers to show."
                    } else if (response.data?.httpcode == 401) {
                        sessionManager.isLogin = false
                        startActivity(Intent(requireActivity(), LoginActivity::class.java))
                        requireActivity().finish()
                    }
                }

                is Resource.Loading -> appLoader.show()
                is Resource.Error ->
                    apiError(response.message)
            }
        }
    }

    private fun apiError(message: String?) {
        appLoader.dismiss()
        showToast(message.toString())
        if (message == netWorkFailure) {
            noInternetDialog.show()
        }
    }
}