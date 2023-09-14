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
import com.hfm.customer.databinding.FragmentNotificationsBinding
import com.hfm.customer.databinding.FragmentSupportBinding
import com.hfm.customer.ui.fragments.notifications.adapter.NotificationAdapter
import com.hfm.customer.ui.fragments.vouchers.adapter.SupportAdapter
import com.hfm.customer.ui.fragments.vouchers.adapter.VouchersAdapter
import com.hfm.customer.utils.initRecyclerView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class NotificationFragment : Fragment(), View.OnClickListener {

    private lateinit var binding: FragmentNotificationsBinding
    private var currentView: View? = null

    @Inject
    lateinit var notificationAdapter: NotificationAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_notifications, container, false)
            binding = FragmentNotificationsBinding.bind(currentView!!)
            init()
            setObserver()
            setOnClickListener()
        }
        return currentView!!
    }


    private fun init() {
        with(binding) {
            initRecyclerView(requireContext(), notificationRv, notificationAdapter)
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
        }
    }


    override fun onClick(v: View?) {
        when (v?.id) {
        }
    }
}