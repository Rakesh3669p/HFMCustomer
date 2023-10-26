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
import androidx.fragment.app.Fragment
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


@AndroidEntryPoint
class IPay88 : Fragment() {

    private lateinit var binding: FragmentPaymentGatewayBinding
    private var currentView: View? = null
    private lateinit var appLoader: Loader
    private lateinit var noInternetDialog: NoInternetDialog

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

        val orderId = arguments?.getString("orderId")
        val amount = arguments?.getString("amount")
        with(binding) {
            paymentGateWayWebView.loadUrl("https://uat.hfm.synuos.com/UAT/admin/ipay/request?order_id=$orderId&amount=$amount&os_type=app")
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
        appCompatDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        appCompatDialog.setCancelable(false)
        bindingDialog.ok.setOnClickListener {
            appCompatDialog.dismiss()
            findNavController().navigate(R.id.action_online_paymentFragment_to_myOrdersFragment)
        }
        appCompatDialog.show()


    }

    private fun showFailedDialog() {
        val appCompatDialog = Dialog(requireContext())
        val bindingDialog = DialogueOrderSuccessBinding.inflate(layoutInflater)
        appCompatDialog.setContentView(bindingDialog.root)
        appCompatDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        appCompatDialog.setCancelable(false)
        bindingDialog.title.text = "Failed!"
        bindingDialog.desc.text = "Unable to make Payment,\n Please try again.."
        bindingDialog.ok.setOnClickListener {
            appCompatDialog.dismiss()
            findNavController().navigate(R.id.action_online_paymentFragment_to_myOrdersFragment)
        }
        appCompatDialog.show()

    }


}

class JSBridge() {
    @JavascriptInterface
    fun showMessageInNative(message: String) {
        println("got Message $message")
    }
}