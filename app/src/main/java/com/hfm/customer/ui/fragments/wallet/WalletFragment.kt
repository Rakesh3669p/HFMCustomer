package com.hfm.customer.ui.fragments.wallet

import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.hfm.customer.R
import com.hfm.customer.databinding.FragmentWalletBinding
import com.hfm.customer.ui.fragments.wallet.adapter.WalletAdapter
import com.hfm.customer.ui.fragments.wallet.model.WalletData
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
                        setData(response.data.data)

                    }else{
                        showToast(response.data?.message.toString())
                    }
                }
                is Resource.Loading->appLoader.show()
                is Resource.Error->apiError(response.message)
            }

        }
    }

    private fun setData(data: WalletData) {

        if(data.total_balance<=0){
            binding.noData.root.isVisible = true
            binding.noData.noDataLbl.text = getString(R.string.no_points_record_found_lbl)
            return
        }else{
            binding.noData.root.isVisible = false
        }

        binding.balanceLbl.text = getString(R.string.current_balance_lbl)
        binding.balance.text = "${data.total_balance} Points (RM ${formatToTwoDecimalPlaces(data.wallet_cash)})"

        val redColor = ContextCompat.getColor(requireContext(), R.color.red)
        val spannableString = SpannableString("Notice: Your ${data.recent_expire.amount} referral will expire on ${data.recent_expire.date}")

// Set red color span for amount
        val redColorSpanAmount = ForegroundColorSpan(redColor)
        spannableString.setSpan(
            redColorSpanAmount,
            spannableString.indexOf(data.recent_expire.amount),
            spannableString.indexOf(data.recent_expire.amount) + data.recent_expire.amount.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

// Set bold span for amount
        val boldSpanAmount = StyleSpan(Typeface.BOLD)
        spannableString.setSpan(
            boldSpanAmount,
            spannableString.indexOf(data.recent_expire.amount),
            spannableString.indexOf(data.recent_expire.amount) + data.recent_expire.amount.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

// Set red color span for date
        val redColorSpanDate = ForegroundColorSpan(redColor)
        spannableString.setSpan(
            redColorSpanDate,
            spannableString.indexOf(data.recent_expire.date),
            spannableString.indexOf(data.recent_expire.date) + data.recent_expire.date.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

// Set bold span for date
        val boldSpanDate = StyleSpan(Typeface.BOLD)
        spannableString.setSpan(
            boldSpanDate,
            spannableString.indexOf(data.recent_expire.date),
            spannableString.indexOf(data.recent_expire.date) + data.recent_expire.date.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        binding.referralExpire.text = spannableString

        binding.referralExpire.isVisible = !data.recent_expire.amount.isEmpty()
        initRecyclerView(requireContext(),binding.walletRv,walletAdapter)
        walletAdapter.differ.submitList(data.wallet)
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

