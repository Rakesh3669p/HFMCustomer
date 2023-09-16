package com.hfm.customer.ui.fragments.address

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
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

    private  var addressList: List<Address> = ArrayList()

    private lateinit var binding: FragmentManageAddressBinding
    private var currentView: View? = null


    @Inject
    lateinit var manageAddressAdapter: ManageAddressAdapter
    private val mainViewModel: MainViewModel by viewModels()

    private lateinit var noInternetDialog: NoInternetDialog
    private lateinit var appLoader: Loader

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_manage_address, container, false)
            binding = FragmentManageAddressBinding.bind(currentView!!)
            init()
            setRecyclerViews()
            setObserver()
            setOnClickListener()
        }
        return currentView!!
    }

    private fun init() {
        appLoader = Loader(requireContext())
        noInternetDialog = NoInternetDialog(requireContext())
        noInternetDialog.setOnDismissListener { init() }
        mainViewModel.getAddress()

    }

    private fun setRecyclerViews() {
        with(binding) {
        }
    }


    private fun setObserver() {
        mainViewModel.address.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    if (response.data?.httpcode == "200") {
                        binding.noDataLayout.root.isVisible = response.data.data.address_list.isEmpty()
                        binding.noDataLayout.noDataLbl.text = "No Address Added!\n Please Add your Address"
                        initRecyclerView(requireContext(), binding.manageAddressRv, manageAddressAdapter)
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
    }


    private fun setOnClickListener() {
        with(binding) {
            back.setOnClickListener(this@ManageAddressFragment)
        }

        manageAddressAdapter.setOnAddressClickListener {position->
            addressList[position]?.let {
                val bundle = Bundle().apply {
                    putString("from","update")
                    putInt("addressId",it.id)
                    putString("addressType",it.address_type)
                    putString("cityId", it.city_id.toString())
                    putString("stateId", it.state_id.toString())
                    putString("countryId", it.country_id.toString())
                    putString("phoneCode",it.country_code.toString())
                    putString("latitude",it.latitude)
                    putString("longitude",it.longitude)
                    putString("customerName",it.name)
                    putString("customerNumber",it.phone)
                    putString("homeFlatNo",it.house.toString())
                    putString("streetBuildingName",it.address2.toString())
                    putString("pinCode",it.pincode.toString())
                }
                findNavController().navigate(R.id.addNewAddressFragment,bundle)
            }

        }
    }
    override fun onClick(v: View?) {
        when (v?.id) {
            binding.back.id -> findNavController().popBackStack()
        }
    }
}