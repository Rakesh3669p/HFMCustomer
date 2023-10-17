package com.hfm.customer.ui.fragments.support

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.hfm.customer.R
import com.hfm.customer.databinding.FragmentSupportBinding
import com.hfm.customer.ui.fragments.support.model.SupportTickets
import com.hfm.customer.ui.fragments.support.adapter.SupportAdapter
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
class SupportFragment : Fragment(), View.OnClickListener {

    private val supportTickets: MutableList<SupportTickets> = ArrayList()
    private lateinit var binding: FragmentSupportBinding
    private var currentView: View? = null

    @Inject
    lateinit var supportAdapter: SupportAdapter

    private val mainViewModel:MainViewModel by viewModels()
    private lateinit var appLoader: Loader
    private lateinit var noInternetDialog: NoInternetDialog

    private var pageNo:Int = 0
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
        appLoader = Loader(requireContext())
        noInternetDialog = NoInternetDialog(requireContext())
        noInternetDialog.setOnDismissListener { init() }
        mainViewModel.getSupportTickets(pageNo = pageNo)
    }

    private fun setObserver() {
        mainViewModel.supportTickets.observe(viewLifecycleOwner){response->
            when(response){
                is Resource.Success->{
                    appLoader.dismiss()
                    if(response.data?.httpcode==200){
                        if(pageNo == 0) supportTickets.clear()
                        supportTickets.addAll(response.data.data.list)
                        initRecyclerView(requireContext(), binding.supportRv, supportAdapter)
                        supportAdapter.differ.submitList(supportTickets)
                        binding.noSupportTickets.isVisible = supportTickets.isEmpty()
                        binding.createNewTicket2.isVisible = supportTickets.isNotEmpty()

                    }else{
                        binding.noSupportTickets.isVisible = true
                        binding.createNewTicket2.isVisible = supportTickets.isNotEmpty()
                        showToast(response.data?.message.toString())
                    }


                }
                is Resource.Loading->appLoader.show()
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
    private fun setOnClickListener() {
        with(binding) {
            back.setOnClickListener(this@SupportFragment)
            createNewTicket.setOnClickListener(this@SupportFragment)
            createNewTicket2.setOnClickListener(this@SupportFragment)
        }
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            binding.createNewTicket.id -> findNavController().navigate(R.id.createSupportTicketFragment)
            binding.createNewTicket2.id -> findNavController().navigate(R.id.createSupportTicketFragment)
            binding.back.id -> findNavController().popBackStack()

        }
    }
}