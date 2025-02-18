package com.hfm.customer.ui.fragments.referral

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
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
import com.hfm.customer.utils.loadImage
import com.hfm.customer.utils.netWorkFailure
import com.hfm.customer.utils.replaceBaseUrl
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
    private var referralData: ReferralData? = null
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
                            referralData = response.data.data
                            setReferral()
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

    private fun setReferral() {
        with(binding) {
//            referralImage.load()
            if (referralData?.referral?.isNotEmpty() == true) {
                referralData?.referral!![0].let {


                    // Assuming 'it' is an object with a property 'referral_points'
                    val referralPoints = it.referral_points

                    val inviteMessage = "Invite Friends,\nget $referralPoints points"
                    val spannableString = SpannableString(inviteMessage)
                    val startIndex = inviteMessage.indexOf(referralPoints)
                    val endIndex = inviteMessage.length
                    val boldTypeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
                    spannableString.setSpan(
                        StyleSpan(boldTypeface.style),
                        startIndex,
                        endIndex,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )

                    rewardsImageText.text = spannableString


                    referralImage.load(replaceBaseUrl(it.image)) {
                        placeholder(R.drawable.referral_image)
                        error(R.drawable.referral_image)
                    }
                    couponCode.text = it.ref_code
                    one.text =
                        "1. Offer Valid on order of RM ${formatToTwoDecimalPlaces(it.ord_min_amount)} and above"
                    two.text = "2. Maximum invitations: ${it.max_invitations} Friends"
                    val thirdPoint =
                        if (it.referral_rewards_expiry.isNotEmpty() && it.referral_rewards_expiry.toInt() > 1) {
                            "3. Rewards cash expires in ${it.referral_rewards_expiry} days from date issued"
                        } else {
                            "3. Rewards cash expires in ${it.referral_rewards_expiry} day from date issued"
                        }
                    three.text = thirdPoint
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
            copy.setOnClickListener(this@ReferralFragment)
            shareButton.setOnClickListener(this@ReferralFragment)
        }
    }

    private fun shareReferral() {
        var couponCode = ""
        var points = ""
        var amount = ""
        if (referralData?.referral?.isNotEmpty() == true) {
            referralData?.referral!![0].let {
                couponCode = it.ref_code
                points = it.referral_points
                amount = "(RM ${formatToTwoDecimalPlaces(it.referral_points_rm.toDouble())} OFF)"
            }

        }

        val content =
            "Sharing is Saving! I wanted to share a special referral code with you from www.halalfoodmaster.com. Use the code $couponCode when you sign up as new customer to get $points points $amount on your first purchase! Click here \"https://onelink.to/hz6kxn\" to register now."

        val intent = Intent(Intent.ACTION_SEND)
        intent.putExtra(Intent.EXTRA_TEXT, content)
        intent.type = "text/plain"
        startActivity(Intent.createChooser(intent, "Share Via"))
    }

    private fun copyCode() {
        val couponCode = binding.couponCode.text.trim()
        val clipboardManager =
            requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("text", couponCode)
        clipboardManager.setPrimaryClip(clipData)
        showToast("Referral code copied")
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.back.id -> findNavController().popBackStack()
            binding.copy.id -> copyCode()
            binding.shareButton.id -> shareReferral()
        }
    }


}