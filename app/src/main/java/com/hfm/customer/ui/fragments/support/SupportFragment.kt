package com.hfm.customer.ui.fragments.support

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.hfm.customer.R
import com.hfm.customer.databinding.BottomSheetBulkOrderBinding
import com.hfm.customer.databinding.BottomSheetCreateTicketBinding
import com.hfm.customer.databinding.FragmentSupportBinding
import com.hfm.customer.ui.fragments.vouchers.adapter.SupportAdapter
import com.hfm.customer.ui.fragments.vouchers.adapter.VouchersAdapter
import com.hfm.customer.utils.initRecyclerView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class SupportFragment : Fragment(), View.OnClickListener {

    private lateinit var binding: FragmentSupportBinding
    private var currentView: View? = null

    @Inject
    lateinit var supportAdapter: SupportAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_support, container, false)
            binding = FragmentSupportBinding.bind(currentView!!)
            init()
            setObserver()
            setOnClickListener()
        }
        return currentView!!
    }


    private fun init() {
        with(binding) {
            initRecyclerView(requireContext(), supportRv, supportAdapter)
        }
    }


    private fun setObserver() {}

    private fun showBulkOrderBottomSheet() {
        val binding = BottomSheetCreateTicketBinding.inflate(layoutInflater)
        val sortDialog =
            BottomSheetDialog(requireActivity(), R.style.MyTransparentBottomSheetDialogTheme)
        sortDialog.setContentView(binding.root)
        sortDialog.show()
    }

    private fun setOnClickListener() {
        with(binding) {
            createNewTicket.setOnClickListener(this@SupportFragment)
        }
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            binding.createNewTicket.id -> showBulkOrderBottomSheet()
        }
    }
}