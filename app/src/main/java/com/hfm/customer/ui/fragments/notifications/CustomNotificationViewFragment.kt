package com.hfm.customer.ui.fragments.notifications

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hfm.customer.MainActivity
import com.hfm.customer.R
import com.hfm.customer.databinding.FragmentCustomNotificationsViewBinding
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
class CustomNotificationViewFragment : Fragment(), View.OnClickListener {

    private lateinit var binding: FragmentCustomNotificationsViewBinding
    private var currentView: View? = null

    private lateinit var appLoader: Loader
    private lateinit var noInternetDialog: NoInternetDialog
    private var redirection = ""
    private var title = ""
    private var description = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_custom_notifications_view, container, false)
            binding = FragmentCustomNotificationsViewBinding.bind(currentView!!)
            init()
            setOnClickListener()
        }
        return currentView!!
    }


    private fun init() {
        appLoader = Loader(requireContext())
        noInternetDialog = NoInternetDialog(requireContext())
        noInternetDialog.setOnDismissListener {
            init()
        }

        redirection = arguments?.getString("redirection")?:""
        title = arguments?.getString("title")?:""
        description = arguments?.getString("description")?:""
        with(binding) {
            notificationTitle.text = title
            notificationWv.settings.javaScriptEnabled = true
            notificationWv.loadDataWithBaseURL(
                null,
                description,
                "text/html",
                "UTF-8",
                null
            )
            notificationWv.webViewClient = WebViewClient()
            notificationWv.webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    view!!.context.startActivity(Intent(Intent.ACTION_VIEW, request?.url))
                    return true
                }
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

    private fun setRedirection() {
        when(redirection){
            "notifications"->findNavController().popBackStack()
            "shocking-sales","flash_sale"-> {
                val bundle = Bundle()
                bundle.putInt("flashSale", 1)
                findNavController().navigate(R.id.productListFragment, bundle)
            }
            "coupons"->{
                findNavController().navigate(R.id.vouchersFragment)
            }
        }
    }

    private fun setOnClickListener() {
        with(binding) {
            back.setOnClickListener(this@CustomNotificationViewFragment)
            view.setOnClickListener(this@CustomNotificationViewFragment)
        }
    }
    override fun onClick(v: View?) {
        when (v?.id) {
            binding.back.id -> findNavController().popBackStack()
            binding.view.id -> setRedirection()
        }
    }


}