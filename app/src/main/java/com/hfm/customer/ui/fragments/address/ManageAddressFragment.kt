package com.hfm.customer.ui.fragments.address

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.hfm.customer.R
import com.hfm.customer.databinding.BottomSheetStoreVoucherBinding
import com.hfm.customer.databinding.BottomSheetVoucherBinding
import com.hfm.customer.databinding.FragmentCheckoutBinding
import com.hfm.customer.databinding.FragmentCheckoutFaqBinding
import com.hfm.customer.databinding.FragmentManageAddressBinding
import com.hfm.customer.ui.fragments.address.adapter.ManageAddressAdapter
import com.hfm.customer.ui.fragments.products.productDetails.adapter.ReviewsAdapter
import com.hfm.customer.utils.initRecyclerView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ManageAddressFragment : Fragment() , View.OnClickListener {

    private lateinit var binding: FragmentManageAddressBinding
    private var currentView: View? = null


    @Inject lateinit var manageAddressAdapter: ManageAddressAdapter

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
        initRecyclerView(requireContext(),binding.manageAddressRv,manageAddressAdapter)

    }

    private fun setRecyclerViews() {
        with(binding){
        }
    }


    private fun setObserver() {}


    private fun setOnClickListener() {
        with(binding) {
            back.setOnClickListener (this@ManageAddressFragment)
        }
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            binding.back.id->findNavController().popBackStack()
        }
    }
}