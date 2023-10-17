package com.hfm.customer.ui.fragments.myOrders

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.hfm.customer.R
import com.hfm.customer.databinding.FragmentMyAllOrdersBinding
import com.hfm.customer.ui.fragments.myOrders.adapter.MyAllOrdersAdapter
import com.hfm.customer.ui.fragments.myOrders.model.MyOrdersData
import com.hfm.customer.ui.fragments.myOrders.model.Purchase
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
class MyAllOrdersFragment : Fragment() ,View.OnClickListener{

    private var from: String = ""

    private var myOrders: List<Purchase> = ArrayList()

    private lateinit var binding: FragmentMyAllOrdersBinding
    private var currentView: View? = null
    private val mainViewModel: MainViewModel by viewModels()
    @Inject
    lateinit var myAllOrdersAdapter: MyAllOrdersAdapter
    private lateinit var appLoader: Loader
    private lateinit var noInternetDialog: NoInternetDialog
    var pageNo = 0
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_my_all_orders, container, false)
            binding = FragmentMyAllOrdersBinding.bind(currentView!!)
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
        mainViewModel.getMyOrders("",pageNo)
        from = arguments?.getString("from").toString()


    }

    private fun setObserver() {
        mainViewModel.myOrders.observe(viewLifecycleOwner){response->
            when(response){
                is Resource.Success->{
                    appLoader.dismiss()
                    if(response.data?.httpcode == "200"){
                        binding.noDataLayout.root.isVisible = false

                        setMyOrders(response.data.data)
                    }else{
                        binding.noDataLayout.root.isVisible = true
                        showToast(response.data?.message.toString())
                    }
                }
                is Resource.Loading->appLoader.show()
                is Resource.Error->apiError(response.message)
            }
        }
    }

    private fun setMyOrders(data: MyOrdersData) {
        myOrders = data.purchase
        initRecyclerView(requireContext(),binding.bulkOrdersRv,myAllOrdersAdapter)
        myAllOrdersAdapter.differ.submitList(data.purchase)
        myAllOrdersAdapter.setFrom(from)
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
            back.setOnClickListener(this@MyAllOrdersFragment)
        }

        myAllOrdersAdapter.setOnOrderClickListener { position->
            val chatId = if(myOrders[position].chat_id.isNotEmpty()) myOrders[position].chat_id.toInt() else 0
            val bundle = Bundle()
            bundle.putString("orderId",myOrders[position].order_id)
            bundle.putString("orderDateTime","${myOrders[position].order_date} | ${myOrders[position].order_time}")
            bundle.putString("storeName", myOrders[position].store_name)
            bundle.putString("orderAmount",myOrders[position].grand_total.toString())
            bundle.putString("itemsCount",myOrders[position].products.size.toString())
            bundle.putInt("chatId", chatId)
            bundle.putString("saleId",myOrders[position].sale_id.toString())
            bundle.putString("sellerId",myOrders[position].seller_id.toString())
            if(from == "chat"){
                findNavController().navigate(R.id.action_myAllOrders_to_chat,bundle)
            }else{
                findNavController().navigate(R.id.action_myAllOrders_to_supportFragment,bundle)
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.back.id->findNavController().popBackStack()
        }
    }
}