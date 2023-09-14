package com.hfm.customer.ui.fragments.checkOut

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
import com.hfm.customer.ui.fragments.products.productDetails.adapter.ReviewsAdapter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CheckOutFragment : Fragment() , View.OnClickListener {

    private lateinit var binding: FragmentCheckoutBinding
    private var currentView: View? = null

    @Inject lateinit var reviewsAdapter: ReviewsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_checkout, container, false)
            binding = FragmentCheckoutBinding.bind(currentView!!)
            init()
            setRecyclerViews()
            setObserver()
            setOnClickListener()
        }
        return currentView!!
    }

    private fun init() {}

    private fun setRecyclerViews() {
        with(binding){
        }
    }


    private fun setObserver() {}

    private fun showVoucherBottomSheet() {
        val binding = BottomSheetVoucherBinding.inflate(layoutInflater)
        val sortDialog = BottomSheetDialog(requireActivity(), R.style.MyTransparentBottomSheetDialogTheme)
        sortDialog.setContentView(binding.root)
        sortDialog.show()
    }


    private fun showStoreVoucherBottomSheet() {
        val binding = BottomSheetStoreVoucherBinding.inflate(layoutInflater)
        val sortDialog = BottomSheetDialog(requireActivity(), R.style.MyTransparentBottomSheetDialogTheme)
        sortDialog.setContentView(binding.root)
        sortDialog.show()
    }


    private fun setOnClickListener() {
        with(binding) {
            back.setOnClickListener (this@CheckOutFragment)
            addVoucher.setOnClickListener (this@CheckOutFragment)
            removeVoucher.setOnClickListener (this@CheckOutFragment)
            manageAddress.setOnClickListener (this@CheckOutFragment)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.back.id->findNavController().popBackStack()
            binding.addVoucher.id-> showVoucherBottomSheet()
            binding.removeVoucher.id-> showStoreVoucherBottomSheet()
            binding.manageAddress.id-> findNavController().navigate(R.id.manageAddressFragment)
        }
    }
}