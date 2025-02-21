package com.hfm.customer.ui.fragments.payment.paymentGateway

import android.app.Dialog
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.JsResult
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import com.hfm.customer.R
import com.hfm.customer.databinding.DialogueMediaPickupBinding
import com.hfm.customer.databinding.DialogueOrderSuccessBinding
import com.hfm.customer.databinding.FragmentPaymentGatewayBinding
import com.hfm.customer.utils.Loader
import com.hfm.customer.utils.NoInternetDialog
import com.hfm.customer.utils.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@AndroidEntryPoint
class IPay88 : Fragment() {

    private lateinit var binding: FragmentPaymentGatewayBinding
    private var currentView: View? = null
    private lateinit var appLoader: Loader
    private lateinit var noInternetDialog: NoInternetDialog
    private var orderId= ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_payment_gateway, container, false)
            binding = FragmentPaymentGatewayBinding.bind(currentView!!)
            init()
        }
        return currentView!!
    }


    private fun init() {
        appLoader = Loader(requireContext())
        noInternetDialog = NoInternetDialog(requireContext())
        noInternetDialog.setOnDismissListener { init() }

        orderId = arguments?.getString("orderId").toString()
        val amount = arguments?.getString("amount")
        val paymentUrl = arguments?.getString("paymentUrl").toString()
        with(binding) {
            paymentGateWayWebView.loadUrl(paymentUrl)
            paymentGateWayWebView.settings.javaScriptEnabled = true

              paymentGateWayWebView.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    appLoader.dismiss()
                    if (url.toString().contains("status=success")) {
                        showSuccessDialog()
                    } else if (url.toString().contains("status=failed")) {
                        showFailedDialog()
                    }
                }

                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    appLoader.show()
                }
            }

            paymentGateWayWebView.webChromeClient = object : WebChromeClient() {
                override fun onJsAlert(
                    view: WebView,
                    url: String,
                    message: String,
                    result: JsResult
                ): Boolean {
                    return super.onJsAlert(view, url, message, result)
                }
            }

            paymentGateWayWebView.addJavascriptInterface(JSBridge(), "JSBridge")
        }
    }

    private fun showSuccessDialog() {
        val appCompatDialog = Dialog(requireContext())
        val bindingDialog = DialogueOrderSuccessBinding.inflate(layoutInflater)
        appCompatDialog.setContentView(bindingDialog.root)
        appCompatDialog.window!!.attributes.width = LinearLayout.LayoutParams.MATCH_PARENT
        appCompatDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        appCompatDialog.setCancelable(false)
        bindingDialog.title.text = "Thank you for placing order.\nYour order id is $orderId"
        bindingDialog.desc.text = "We will notify you regarding the status of the order."
        bindingDialog.ok.setOnClickListener {
            appCompatDialog.dismiss()
            val bundle = Bundle()
            bundle.putString("from","toPay")
            findNavController().navigate(R.id.action_online_paymentFragment_to_myOrdersFragment,bundle)
        }
        appCompatDialog.show()
    }

    private fun showFailedDialog() {
        val appCompatDialog = Dialog(requireContext())
        val bindingDialog = DialogueOrderSuccessBinding.inflate(layoutInflater)
        appCompatDialog.setContentView(bindingDialog.root)
        appCompatDialog.window!!.attributes.width = LinearLayout.LayoutParams.MATCH_PARENT
        appCompatDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        appCompatDialog.setCancelable(false)
        bindingDialog.title.text = "Failed!"
        bindingDialog.desc.text = "Unable to make Payment,\n Please try again.."
        bindingDialog.ok.setOnClickListener {
            appCompatDialog.dismiss()
            findNavController().navigate(R.id.action_online_paymentFragment_to_homeFragment)
        }
        appCompatDialog.show()

        lifecycleScope.launch {
            delay(3000)
            if(appCompatDialog.isShowing){
                appCompatDialog.dismiss()
                val bundle = Bundle()
                bundle.putString("from","cancelled")
                findNavController().navigate(R.id.action_online_paymentFragment_to_myOrdersFragment,bundle)
            }
        }

    }
}

class JSBridge() {
    @JavascriptInterface
    fun showMessageInNative(message: String) {
        println("got Message $message")
    }
}