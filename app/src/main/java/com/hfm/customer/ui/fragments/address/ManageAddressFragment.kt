package com.hfm.customer.ui.fragments.address

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import com.hfm.customer.R
import com.hfm.customer.databinding.FragmentManageAddressBinding
import com.hfm.customer.ui.fragments.address.adapter.ManageAddressAdapter
import com.hfm.customer.ui.fragments.address.model.Address
import com.hfm.customer.utils.Loader
import com.hfm.customer.utils.NoInternetDialog
import com.hfm.customer.utils.Resource
import com.hfm.customer.utils.initRecyclerView
import com.hfm.customer.utils.netWorkFailure
import com.hfm.customer.utils.showToast
import com.hfm.customer.viewModel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ManageAddressFragment : Fragment(), View.OnClickListener {

    private var addressList: List<Address> = ArrayList()

    private lateinit var binding: FragmentManageAddressBinding
    private var currentView: View? = null


    @Inject
    lateinit var manageAddressAdapter: ManageAddressAdapter
    private val mainViewModel: MainViewModel by viewModels()

    private lateinit var noInternetDialog: NoInternetDialog
    private lateinit var appLoader: Loader
    private var from = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_manage_address, container, false)
            binding = FragmentManageAddressBinding.bind(currentView!!)
            init()
            setOnClickListener()
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
        from = arguments?.getString("from").toString()
        initRecyclerView(
            requireContext(),
            binding.manageAddressRv,
            manageAddressAdapter
        )
        val animator = DefaultItemAnimator()
        animator.supportsChangeAnimations = false // Disable default change animations
        binding.manageAddressRv.itemAnimator = animator
        mainViewModel.getAddress()


    }



    private fun setObserver() {
        mainViewModel.address.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    while (appLoader.isShowing){
                        appLoader.dismiss()
                    }
                    if (response.data?.httpcode == "200") {
                        binding.noDataLayout.root.isVisible =
                            response.data.data.address_list.isEmpty()
                        binding.noDataLayout.noDataLbl.text =
                            "No Address Added!\n Please Add your Address"

                        addressList = response.data.data.address_list
                        manageAddressAdapter.differ.submitList(response.data.data.address_list)
                    } else {
                        showToast(response.data?.message.toString())
                    }
                }

                is Resource.Loading -> appLoader.show()
                is Resource.Error -> {
                    appLoader.dismiss()
                    showToast(response.message.toString())
                    if (response.message == netWorkFailure) {
                        noInternetDialog.show()
                    }
                }
            }
        }

        mainViewModel.deleteAddress.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {

                    if (response.data?.httpcode == 200) {
                        mainViewModel.getAddress()
                    } else {
                        showToast(response.data?.message.toString())
                    }
                }

                is Resource.Loading -> appLoader.show()
                is Resource.Error -> {
                    appLoader.dismiss()
                    showToast(response.message.toString())
                    if (response.message == netWorkFailure) {
                        noInternetDialog.show()
                    }
                }
            }
        }

        mainViewModel.defaultAddress.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    if (response.data?.httpcode == 200)
                        mainViewModel.getAddress()
                  else
                        showToast(response.data?.message.toString())
                }

                is Resource.Loading -> appLoader.show()
                is Resource.Error -> {
                    appLoader.dismiss()
                    showToast(response.message.toString())
                    if (response.message == netWorkFailure) {
                        noInternetDialog.show()
                    }
                }
            }
        }
    }


    private fun setOnClickListener() {
        with(binding) {
            back.setOnClickListener(this@ManageAddressFragment)
            addNewAddress.setOnClickListener(this@ManageAddressFragment)
        }
        manageAddressAdapter.setOnDefaultClickListener {addressId->
        mainViewModel.defaultAddress(addressId.toString())
    }

        manageAddressAdapter.setOnDeleteClickListener {addressId->
            mainViewModel.deleteAddress(addressId.toString())
        }

        manageAddressAdapter.setOnEditAddressClickListener {position->
            addressList[position].let {
                val bundle = Bundle().apply {
                    putString("from", "update")
                    putInt("addressId", it.id)
                    putString("addressType", it.address_type)
                    putString("cityId", it.city_id.toString())
                    putString("stateId", it.state_id.toString())
                    putString("countryId", it.country_id.toString())
                    putString("phoneCode", it.country_code.toString())
                    putString("latitude", it.latitude)
                    putString("longitude", it.longitude)
                    putString("customerName", it.name)
                    putString("customerNumber", it.phone)
                    putString("homeFlatNo", it.house.toString())
                    putString("streetBuildingName", it.address2.toString())
                    putString("pinCode", it.pincode.toString())
                    putInt("isDefault", it.is_default)
                }
                findNavController().navigate(R.id.addNewAddressFragment, bundle)
            }
        }
        manageAddressAdapter.setOnAddressClickListener { addressId->
            if(from == "checkOut") {
                findNavController().previousBackStackEntry?.savedStateHandle?.set(
                    "addressId",
                    addressId
                )
                findNavController().popBackStack()
            }


        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.back.id -> findNavController().popBackStack()
            binding.addNewAddress.id -> {
                val bundle = Bundle()
                bundle.putString("from", "new")
                findNavController().navigate(R.id.addNewAddressFragment, bundle)
            }
        }
    }
}