package com.hfm.customer.ui.fragments.blogs

import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import coil.load
import coil.transform.CircleCropTransformation
import com.hfm.customer.R
import com.hfm.customer.databinding.FragmentBlogsDetailsBinding
import com.hfm.customer.ui.fragments.blogs.model.BlogDetailsData
import com.hfm.customer.utils.Loader
import com.hfm.customer.utils.NoInternetDialog
import com.hfm.customer.utils.Resource
import com.hfm.customer.utils.initRecyclerView
import com.hfm.customer.utils.netWorkFailure
import com.hfm.customer.utils.replaceBaseUrl
import com.hfm.customer.utils.showToast
import com.hfm.customer.viewModel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class BlogsDetailsFragment : Fragment() ,View.OnClickListener{
    private lateinit var binding: FragmentBlogsDetailsBinding
    private var currentView: View? = null
    private val mainViewModel: MainViewModel by viewModels()
    private lateinit var appLoader: Loader
    private lateinit var noInternetDialog: NoInternetDialog
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_blogs_details, container, false)
            binding = FragmentBlogsDetailsBinding.bind(currentView!!)
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
        val id = arguments?.getInt("id")?:0
        mainViewModel.getBlogsDetails(id)

    }

    private fun setObserver() {
        mainViewModel.blogsDetails.observe(viewLifecycleOwner){response->
            when(response){
                is Resource.Success->{
                    appLoader.dismiss()
                    if(response.data?.httpcode == 200){
                        setBlogData(response.data.data)
                    }else{
                        showToast(response.data?.message.toString())
                    }
                }
                is Resource.Loading->appLoader.show()
                is Resource.Error->apiError(response.message)
            }
        }
    }

    private fun setBlogData(data: BlogDetailsData) {
        with(binding){
            blogImage.load(replaceBaseUrl(data.post.image)){
                placeholder(R.drawable.logo)
                
            }
            blogAuthor.text = data.post.author
            blogTitle.text = data.post.blog_title
            blogDesc.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Html.fromHtml(data.post.content, Html.FROM_HTML_MODE_COMPACT)
            } else {
                Html.fromHtml(data.post.content)
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
            back.setOnClickListener(this@BlogsDetailsFragment)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.back.id -> findNavController().popBackStack()
        }
    }
}