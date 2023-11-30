package com.hfm.customer.ui.fragments.referral

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import coil.load
import com.hfm.customer.R
import com.hfm.customer.databinding.FragmentReferalBinding
import com.hfm.customer.utils.Loader
import com.hfm.customer.utils.NoInternetDialog
import com.hfm.customer.utils.Resource
import com.hfm.customer.utils.formatToTwoDecimalPlaces
import com.hfm.customer.utils.netWorkFailure
import com.hfm.customer.utils.showToast
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
        mainViewModel.getReferral()
    }

    private fun setObserver() {
        mainViewModel.referrals.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    when (response.data?.httpcode) {
                        "200" -> {
                            setReferral(response.data.data)
                        }
                        "400" -> {

                        }
                        else -> {

                        }
                    }
                }
                is Resource.Loading -> appLoader.show()
                is Resource.Error -> apiError(response.message)
            }
        }
    }

    private fun setReferral(data: ReferralData) {
        with(binding){
//            referralImage.load()

            if(data.refferal.isNotEmpty()){
                data.refferal[0].let {
                    couponCode.text = it.ref_code
                    one.text = "1. Offer Valid on order of RM ${formatToTwoDecimalPlaces(it.ord_min_amount)} and above"
                    two.text = "2. Maximum invitations: ${it.max_invitation} Friends"
                    three.text = "3. Rewards cash expires in ${it.referral_rewards_expiry} days from date issued"
                }
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
            back.setOnClickListener(this@ReferralFragment)
            share.setOnClickListener(this@ReferralFragment)
        }
    }

    private fun shareReferral() {

        val content = "\uD83C\uDF89 Unlock Exciting Offers with Our Referral Code! \uD83D\uDE80\n" +
                "\n" +
                "Hey there!\n" +
                "\n" +
                "Ready for an amazing experience? Use my referral code [YourReferralCode] when you sign up for [App Name] and unlock a world of exciting offers and exclusive perks! \uD83C\uDF1F\n" +
                "\n" +
                "Why join [App Name]?\n" +
                "\n" +
                "✨ [Highlight a key feature or benefit]\n" +
                "✨ [Another key feature or benefit]\n" +
                "✨ [Any special promotions or discounts]\n" +
                "\n" +
                "How to get started:\n" +
                "\n" +
                "Download [App Name] from the [App Store/Google Play].\n" +
                "Sign up using my referral code: [YourReferralCode].\n" +
                "Enjoy the perks and benefits that come with being part of our community!\n" +
                "Hurry, the fun is just a click away! \uD83D\uDE80 Let's make your [App Name] experience unforgettable together!\n" +
                "\n" +
                "[App Store Link] | [Google Play Link]\n" +
                "\n" +
                "Happy exploring! \uD83C\uDF08"

        val intent = Intent(Intent.ACTION_SEND)
        intent.putExtra(Intent.EXTRA_TEXT, content)
        intent.type = "text/plain"
        startActivity(Intent.createChooser(intent, "Share Via"))
    }
    override fun onClick(v: View?) {
        when (v?.id) {
            binding.back.id -> findNavController().popBackStack()
            binding.share.id -> shareReferral()
        }
    }
}