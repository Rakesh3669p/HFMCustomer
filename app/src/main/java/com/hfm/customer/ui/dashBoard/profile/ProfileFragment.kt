package com.hfm.customer.ui.dashBoard.profile

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import coil.ImageLoader
import coil.load
import coil.request.ImageRequest
import coil.util.DebugLogger
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.hfm.customer.BuildConfig
import com.hfm.customer.R
import com.hfm.customer.databinding.FragmentCategoriesBinding
import com.hfm.customer.databinding.FragmentProfileBinding
import com.hfm.customer.ui.dashBoard.categories.adapter.CategoriesAdapter
import com.hfm.customer.ui.dashBoard.categories.adapter.CategoryMainAdapter
import com.hfm.customer.ui.dashBoard.profile.model.ProfileData
import com.hfm.customer.ui.loginSignUp.LoginActivity
import com.hfm.customer.utils.Loader
import com.hfm.customer.utils.NoInternetDialog
import com.hfm.customer.utils.Resource
import com.hfm.customer.utils.SessionManager
import com.hfm.customer.utils.business
import com.hfm.customer.utils.netWorkFailure
import com.hfm.customer.utils.showToast
import com.hfm.customer.viewModel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ProfileFragment : Fragment(), View.OnClickListener {
    private var customerType: String = ""
    private lateinit var binding: FragmentProfileBinding
    private var currentView: View? = null
    @Inject
    lateinit var sessionManager: SessionManager

    private val mainViewModel:MainViewModel by activityViewModels()
    private lateinit var appLoader: Loader
    private lateinit var noInternetDialog: NoInternetDialog


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        if(!sessionManager.isLogin){
            startActivity(Intent(requireActivity(), LoginActivity::class.java))
            requireActivity().finish()
            return currentView
        }

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
        binding.loader.isVisible = true
        appLoader = Loader(requireContext())
        noInternetDialog = NoInternetDialog(requireContext())
        binding.logout.isVisible  = sessionManager.isLogin
        binding.version.text = "Version ${BuildConfig.VERSION_NAME}"
        mainViewModel.getProfile()
    }

    private fun setObserver() {
        mainViewModel.profile.observe(viewLifecycleOwner){response->
            when(response){
                is Resource.Success->{
                    binding.loader.isVisible = false
                    appLoader.dismiss()
                    if(response.data?.httpcode=="200"){
                        setProfile(response.data.data)
                    }else{
                        showToast(response.data?.message.toString())
                    }
                }
                is Resource.Loading->Unit
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
                userName.text = "${it.first_name} ${it.last_name?:""}"
                if(it.customer_type !=null) {
                    customerType = it.customer_type.uppercase()
                    userType.text = "Customer Type: ${customerType.uppercase()}"
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
            toShip.setOnClickListener(this@ProfileFragment)
            toReceive.setOnClickListener(this@ProfileFragment)
            bulkOrders.setOnClickListener(this@ProfileFragment)
            viewAllLbl.setOnClickListener(this@ProfileFragment)
            search.setOnClickListener(this@ProfileFragment)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.manageAddress.id -> findNavController().navigate(R.id.manageAddressFragment)
            binding.wishList.id -> findNavController().navigate(R.id.wishListFragment)
            binding.wallet.id -> findNavController().navigate(R.id.walletFragment)
            binding.voucher.id -> findNavController().navigate(R.id.vouchersFragment)
            binding.chat.id -> findNavController().navigate(R.id.chatUsersFragment2)
            binding.support.id -> findNavController().navigate(R.id.supportFragment)
            binding.referral.id -> findNavController().navigate(R.id.referralFragment)
            binding.search.id ->  findNavController().navigate(R.id.searchFragment)
            binding.logout.id -> {
                when (sessionManager.loginType) {

                    "F" -> {
                        try {
                            LoginManager.getInstance().logOut()
                        } catch (_: Exception) {
                        }
                    }

                    "G" -> {
                        try {
                            val gso =
                                GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                    .build()
                            val googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
                            googleSignInClient.signOut()
                        } catch (_: Exception) {
                        }
                    }

                }
                sessionManager.isLogin = false
                startActivity(Intent(requireContext(), LoginActivity::class.java))
                requireActivity().finish()
            }

            binding.basicDetailsBg.id -> {
                if(customerType == business){
                    findNavController().navigate(R.id.profileSettingsBusiness)
                }else {
                    findNavController().navigate(R.id.profileSettingsFragment)
                }
            }
            binding.toPay.id -> {
                val bundle = Bundle()
                bundle.putString("from","toPay")
                findNavController().navigate(R.id.myOrdersFragment,bundle)
            }

            binding.toShip.id -> {
                val bundle = Bundle()
                bundle.putString("from","toShip")
                findNavController().navigate(R.id.myOrdersFragment,bundle)
            }

            binding.toReceive.id -> {
                val bundle = Bundle()
                bundle.putString("from","toReceive")
                findNavController().navigate(R.id.myOrdersFragment,bundle)
            }

            binding.bulkOrders.id -> {
                val bundle = Bundle()
                bundle.putString("from","bulkOrders")
                findNavController().navigate(R.id.myOrdersFragment,bundle)
            }

            binding.viewAllLbl.id -> findNavController().navigate(R.id.myOrdersFragment)
        }
    }
}