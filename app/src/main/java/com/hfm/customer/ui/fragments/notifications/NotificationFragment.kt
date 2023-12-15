package com.hfm.customer.ui.fragments.notifications

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hfm.customer.MainActivity
import com.hfm.customer.R
import com.hfm.customer.databinding.FragmentNotificationsBinding
import com.hfm.customer.ui.dashBoard.DashBoardActivity
import com.hfm.customer.ui.fragments.notifications.adapter.NotificationAdapter
import com.hfm.customer.ui.fragments.notifications.model.Notification
import com.hfm.customer.ui.fragments.notifications.model.NotificationData
import com.hfm.customer.utils.Loader
import com.hfm.customer.utils.NoInternetDialog
import com.hfm.customer.utils.Resource
import com.hfm.customer.utils.initRecyclerView
import com.hfm.customer.utils.netWorkFailure
import com.hfm.customer.utils.notificationCount
import com.hfm.customer.utils.showToast
import com.hfm.customer.viewModel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class NotificationFragment : Fragment(), View.OnClickListener {

    private val notificationsList: MutableList<Notification> = ArrayList()
    private lateinit var binding: FragmentNotificationsBinding
    private var currentView: View? = null

    @Inject
    lateinit var notificationAdapter: NotificationAdapter
    private lateinit var appLoader: Loader
    private lateinit var noInternetDialog: NoInternetDialog
    private val mainViewModel: MainViewModel by viewModels()
    private var pageNo = 0
    private var isLoading = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_notifications, container, false)
            binding = FragmentNotificationsBinding.bind(currentView!!)
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
        mainViewModel.getNotifications(pageNo = pageNo)
        appLoader = Loader(requireContext())
        noInternetDialog = NoInternetDialog(requireContext())
        noInternetDialog.setOnDismissListener {
            init()
        }
    }

    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val pastVisibleItems = layoutManager.findFirstCompletelyVisibleItemPosition()
            val visibleItemCount = layoutManager.childCount
            val totalItemCount = layoutManager.itemCount

            if (!isLoading && (visibleItemCount + pastVisibleItems) >= totalItemCount) {
                pageNo++
                mainViewModel.getNotifications(pageNo)
                isLoading = true
            }
        }
    }

    private fun setObserver() {
        mainViewModel.notificationViewed.observe(requireActivity()){response->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    if (response.data?.httpcode == 200) {
                        response.data.data?.let {
                            if(it.unread_count!=null) {
                                notificationCount.postValue(response.data.data.unread_count)
                            }
                        }

                    }
                }

                is Resource.Loading -> if (pageNo == 0) appLoader.show()
                is Resource.Error -> apiError(response.message)
            }

        }
        mainViewModel.notifications.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    if (response.data?.httpcode == "200") {
                        setNotificationsData(response.data.data)
                    }
                }

                is Resource.Loading -> if (pageNo == 0) appLoader.show()
                is Resource.Error -> apiError(response.message)
            }
        }
    }

    private fun setNotificationsData(data: NotificationData) {
        isLoading = data.notifications.isEmpty()
        if (pageNo == 0) {
            initRecyclerView(
                requireContext(),
                binding.notificationRv,
                notificationAdapter
            )
            binding.notificationRv.addOnScrollListener(scrollListener)
            notificationsList.addAll(data.notifications)
            notificationAdapter.differ.submitList(notificationsList)
        } else {
            notificationsList.addAll(data.notifications)
            notificationAdapter.differ.submitList(notificationsList)
            notificationAdapter.notifyDataSetChanged()
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
            back.setOnClickListener(this@NotificationFragment)
        }

        notificationAdapter.setOnItemClickListener { position ->
            val notification = notificationAdapter.differ.currentList[position]
            when (notification.app_target_page) {
                "order" -> {
                    when (notification.notify_type) {
                        "quotation_accepted", "bulk_order_requested", "quotation_rejected", "quotation_created" -> {
                            val bundle = Bundle()
                            bundle.putString("from", "bulkOrder")
                            bundle.putString("orderId", notification.ref_id.toString())
                            bundle.putString("saleId", notification.bulk_order_sale_id.toString())
                            mainViewModel.viewedNotification(notificationId = notification.id)
                            findNavController().navigate(R.id.bulkOrderDetailsFragment, bundle)
                        }

                        else -> {
                            val bundle = Bundle()
                            bundle.putString("orderId", notification.order_id)
                            bundle.putString("saleId", notification.ref_id.toString())
                            mainViewModel.viewedNotification(notificationId = notification.id)
                            findNavController().navigate(R.id.orderDetailsFragment, bundle)
                        }
                    }

                }

                "product" -> {
                    val bundle =
                        Bundle().apply { putString("productId", notification.ref_id.toString()) }
                    findNavController().navigate(R.id.productDetailsFragment, bundle)
                    mainViewModel.viewedNotification(notificationId = notification.id)
                }

                "flash_deal" -> {
                    val bundle = Bundle()
                    bundle.putInt("flashSale", 1)
                    findNavController().navigate(R.id.productListFragment, bundle)
                    mainViewModel.viewedNotification(notificationId = notification.id)
                }

                "profile" -> {

                    mainViewModel.viewedNotification(notificationId = notification.id)
                    (activity as DashBoardActivity).toProfile()
                }

                "cart" -> {
                    findNavController().navigate(R.id.cartFragment)
                    mainViewModel.viewedNotification(notificationId = notification.id)
                }

                "support" -> {
                    val bundle = Bundle()
                    bundle.putInt("supportId",notification.ref_id)
                    findNavController().navigate(R.id.supportChatFragment,bundle)
                    mainViewModel.viewedNotification(notificationId = notification.id)

                }

                "new_chat_message","chat" -> {
                    val bundle = Bundle().apply {
                        putString("from", "chatList")
                        putInt("chatId", notification.ref_id)
                    }
                    findNavController().navigate(R.id.chatFragment, bundle)
                    mainViewModel.viewedNotification(notificationId = notification.id)
                }
            }
        }
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            binding.back.id -> findNavController().popBackStack()
        }
    }
}