package com.hfm.customer.ui.fragments.wallet

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hfm.customer.R
import com.hfm.customer.databinding.FragmentWalletBinding
import com.hfm.customer.ui.fragments.vouchers.adapter.VouchersAdapter
import com.hfm.customer.utils.initRecyclerView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class WalletFragment : Fragment(), View.OnClickListener {


    private lateinit var binding: FragmentWalletBinding
    private var currentView: View? = null

    @Inject lateinit var walletAdapter: VouchersAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_wallet, container, false)
            binding = FragmentWalletBinding.bind(currentView!!)
            init()
            setObserver()
            setOnClickListener()
        }
        return currentView!!
    }


    private fun init() {
        with(binding) {
            initRecyclerView(requireContext(),walletRv,walletAdapter)
        }
    }



    private fun setObserver() {}

    private fun setOnClickListener() {
        with(binding) {

        }
    }


    override fun onClick(v: View?) {
        when (v?.id) {
        }
    }
}