package com.hfm.customer.ui.fragments.wallet

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.hfm.customer.R
import com.hfm.customer.databinding.FragmentWalletBinding
import com.hfm.customer.ui.fragments.wallet.adapter.WalletAdapter
import com.hfm.customer.utils.Loader
import com.hfm.customer.utils.NoInternetDialog
import com.hfm.customer.utils.Resource
import com.hfm.customer.utils.formatToTwoDecimalPlaces
import com.hfm.customer.utils.initRecyclerView
import com.hfm.customer.utils.netWorkFailure
import com.hfm.customer.utils.showToast
import com.hfm.customer.viewModel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.math.roundToInt

@AndroidEntryPoint
class WalletFragment : Fragment(), View.OnClickListener {


    private lateinit var binding: FragmentWalletBinding
    private var currentView: View? = null
    private val mainViewModel:MainViewModel by viewModels()
    @Inject lateinit var walletAdapter: WalletAdapter

    private lateinit var appLoader: Loader
    private lateinit var noInternetDialog: NoInternetDialog

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
        appLoader = Loader(requireContext())
        noInternetDialog = NoInternetDialog(requireContext())
        noInternetDialog.setOnDismissListener { init() }
        mainViewModel.getWallet()
    }



    private fun setObserver() {
        mainViewModel.wallet.observe(viewLifecycleOwner){response->
            when(response){
                is Resource.Success->{
                    appLoader.dismiss()
                    if(response.data?.httpcode=="200"){
                        val pointToRM = response.data.data.total_balance/100
                        binding.balance.text = "${response.data.data.total_balance.roundToInt()} Points (RM ${formatToTwoDecimalPlaces(pointToRM)})"
                        initRecyclerView(requireContext(),binding.walletRv,walletAdapter)
                        walletAdapter.differ.submitList(response.data.data.wallet)
                    }else{
                        showToast(response.data?.message.toString())
                    }
                }
                is Resource.Loading->appLoader.show()
                is Resource.Error->apiError(response.message)
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

    private fun setOnClickListener() {
        with(binding) {
            back.setOnClickListener(this@WalletFragment)
        }
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            binding.back.id -> findNavController().popBackStack()
        }
    }
}

