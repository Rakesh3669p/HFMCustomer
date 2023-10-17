package com.hfm.customer.ui.fragments.commonPage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.hfm.customer.R
import com.hfm.customer.databinding.FragmentCommonPageBinding
import com.hfm.customer.ui.fragments.commonPage.model.Pages
import com.hfm.customer.utils.Loader
import com.hfm.customer.utils.NoInternetDialog
import com.hfm.customer.utils.Resource
import com.hfm.customer.utils.netWorkFailure
import com.hfm.customer.utils.showToast
import com.hfm.customer.viewModel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class CommonPageFragment : Fragment() ,View.OnClickListener{
    private lateinit var binding: FragmentCommonPageBinding
    private var currentView: View? = null
    private val mainViewModel: MainViewModel by viewModels()
    private lateinit var appLoader: Loader
    private lateinit var noInternetDialog: NoInternetDialog
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_common_page, container, false)
            binding = FragmentCommonPageBinding.bind(currentView!!)
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
        noInternetDialog= NoInternetDialog(requireContext())
        noInternetDialog.setOnDismissListener { init() }
        val pageId = arguments?.getInt("pageId")?:0
        mainViewModel.getPageData(pageId)

    }

    private fun setObserver() {
        mainViewModel.pageData.observe(viewLifecycleOwner){response->
            when(response){
                is Resource.Success->{
                    appLoader.dismiss()
                    if(response.data?.httpcode == 200){
                        setPageData(response.data.data.pages)
                    }else{
                        showToast(response.data?.message.toString())
                    }
                }
                is Resource.Loading->appLoader.show()
                is Resource.Error->apiError(response.message)
            }
        }
    }

    private fun setPageData(pages: Pages) {
        with(binding){
            webView.settings.javaScriptEnabled = true
            webView.settings.setSupportZoom(true)
            webView.settings.builtInZoomControls = true
            webView.settings.displayZoomControls = false

            val specificationHtmlContent = pages.content
            toolBarTitle.text = pages.title
            webView.loadDataWithBaseURL(
                null,
                specificationHtmlContent,
                "text/html",
                "UTF-8",
                null
            )
            webView.webViewClient = WebViewClient()
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
            back.setOnClickListener(this@CommonPageFragment)
        }


    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.back.id -> findNavController().popBackStack()
        }
    }
}