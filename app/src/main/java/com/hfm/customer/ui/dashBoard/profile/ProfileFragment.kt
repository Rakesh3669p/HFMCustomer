package com.hfm.customer.ui.dashBoard.profile

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import coil.ImageLoader
import coil.load
import coil.request.ImageRequest
import coil.util.DebugLogger
import com.hfm.customer.R
import com.hfm.customer.databinding.FragmentCategoriesBinding
import com.hfm.customer.databinding.FragmentProfileBinding
import com.hfm.customer.ui.dashBoard.categories.adapter.CategoriesAdapter
import com.hfm.customer.ui.dashBoard.categories.adapter.CategoryMainAdapter
import com.hfm.customer.ui.dashBoard.profile.model.ProfileData
import com.hfm.customer.ui.loginSignUp.LoginActivity
import com.hfm.customer.utils.Loader
import com.hfm.customer.utils.Resource
import com.hfm.customer.utils.SessionManager
import com.hfm.customer.utils.netWorkFailure
import com.hfm.customer.utils.showToast
import com.hfm.customer.viewModel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ProfileFragment : Fragment(), View.OnClickListener {
    private lateinit var binding: FragmentProfileBinding
    private var currentView: View? = null
    @Inject
    lateinit var sessionManager: SessionManager

    private val mainViewModel:MainViewModel by viewModels()
    private lateinit var appLoader: Loader

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_profile, container, false)
            binding = FragmentProfileBinding.bind(currentView!!)
            init()
            setObserver()
            setOnClickListener()
        }
        return currentView!!
    }

    private fun init() {
        appLoader = Loader(requireContext())
        mainViewModel.getProfile()
    }

    private fun setObserver() {
        mainViewModel.profile.observe(viewLifecycleOwner){response->
            when(response){
                is Resource.Success->{
                    appLoader.dismiss()
                    if(response.data?.httpcode=="200"){
                        setProfile(response.data.data)
                    }else{
                        showToast(response.data?.message.toString())
                    }
                }
                is Resource.Loading->appLoader.show()
                is Resource.Error->{
                    appLoader.dismiss()
                    showToast(response.message.toString())
                    if(response.message.toString()== netWorkFailure){

                    }
                }
            }

        }
    }

    @SuppressLint("SetTextI18n")
    private fun setProfile(data: ProfileData) {
        with(binding){
            data.profile[0].let {
                val imageOriginal = it.profile_image
                val imageReplaced =
                    imageOriginal.replace("https://uat.hfm.synuos.com", "http://4.194.191.242")

                val imageLoader = ImageLoader.Builder(requireContext())
                    .logger(DebugLogger())
                    .build()

                 val request = ImageRequest.Builder(requireContext())
                    .data(imageReplaced)
                    .target(profileImage)
                    .placeholder(R.drawable.user)
                    .error(R.drawable.user)
                    .build()
                lifecycleScope.launch {
                    imageLoader.execute(request)
                }
                userName.text = "${it.first_name} ${it.last_name}"
                if(it.customer_type !=null) {
                    userType.text = "Customer Type: ${it.customer_type.uppercase()}"
                }
            }
        }
    }

    private fun setOnClickListener() {
        with(binding) {
            manageAddress.setOnClickListener(this@ProfileFragment)
            wishList.setOnClickListener(this@ProfileFragment)
            wallet.setOnClickListener(this@ProfileFragment)
            voucher.setOnClickListener(this@ProfileFragment)
            chat.setOnClickListener(this@ProfileFragment)
            support.setOnClickListener(this@ProfileFragment)
            referral.setOnClickListener(this@ProfileFragment)
            logout.setOnClickListener(this@ProfileFragment)
            basicDetailsBg.setOnClickListener(this@ProfileFragment)
            toPay.setOnClickListener(this@ProfileFragment)
            viewAllLbl.setOnClickListener(this@ProfileFragment)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.manageAddress.id -> findNavController().navigate(R.id.manageAddressFragment)
            binding.wishList.id -> findNavController().navigate(R.id.wishListFragment)
            binding.wallet.id -> findNavController().navigate(R.id.walletFragment)
            binding.voucher.id -> findNavController().navigate(R.id.vouchersFragment)
            binding.chat.id -> findNavController().navigate(R.id.chatUsersFragment)
            binding.support.id -> findNavController().navigate(R.id.supportFragment)
            binding.referral.id -> showToast("Under Construction")
            binding.logout.id -> {
                sessionManager.isLogin = false
                startActivity(Intent(requireContext(), LoginActivity::class.java))
            }

            binding.basicDetailsBg.id -> findNavController().navigate(R.id.profileSettingsFragment)
            binding.toPay.id -> findNavController().navigate(R.id.paymentMethodFragment)
            binding.viewAllLbl.id -> findNavController().navigate(R.id.myOrdersFragment)
        }
    }
}