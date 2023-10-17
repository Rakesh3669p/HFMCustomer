package com.hfm.customer.ui.fragments.blogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import coil.load
import com.hfm.customer.R
import com.hfm.customer.databinding.FragmentBlogsBinding
import com.hfm.customer.databinding.FragmentCommonPageBinding
import com.hfm.customer.ui.fragments.blogs.adapter.BlogsAdapter
import com.hfm.customer.ui.fragments.blogs.model.Blogs
import com.hfm.customer.ui.fragments.commonPage.model.Pages
import com.hfm.customer.utils.Loader
import com.hfm.customer.utils.NoInternetDialog
import com.hfm.customer.utils.Resource
import com.hfm.customer.utils.initRecyclerView
import com.hfm.customer.utils.netWorkFailure
import com.hfm.customer.utils.replaceBaseUrl
import com.hfm.customer.utils.showToast
import com.hfm.customer.viewModel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class BlogsFragment : Fragment() ,View.OnClickListener{
    private lateinit var binding: FragmentBlogsBinding
    private var currentView: View? = null
    private val mainViewModel: MainViewModel by viewModels()
    private lateinit var appLoader: Loader
    private lateinit var noInternetDialog: NoInternetDialog
    @Inject lateinit var blogsAdapter: BlogsAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_blogs, container, false)
            binding = FragmentBlogsBinding.bind(currentView!!)
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

        mainViewModel.getBlogs()

    }

    private fun setObserver() {
        mainViewModel.blogsList.observe(viewLifecycleOwner){response->
            when(response){
                is Resource.Success->{
                    appLoader.dismiss()
                    if(response.data?.httpcode == 200){
                        setBlogsList(response.data.data.list)
                    }else{
                        showToast(response.data?.message.toString())
                    }
                }
                is Resource.Loading->appLoader.show()
                is Resource.Error->apiError(response.message)
            }
        }
    }

    private fun setBlogsList(list: List<Blogs>) {
        with(binding){
            val blogsList :MutableList<Blogs> = ArrayList()
            blogsList.addAll(list)
            initRecyclerView(requireContext(),blogsRv,blogsAdapter)
            blogsList.removeAt(0)
            blogsAdapter.differ.submitList(blogsList)
            val bundle = Bundle()
            if(list.isNotEmpty()) {
                list[0]?.let {
                    blogImage.load(replaceBaseUrl(it.image) )
                    blogAuthor.text = it.author
                    blogTitle.text = it.blog_title

                    bundle.putInt("id",it.blog_id)
                }
            }

            blogImage.setOnClickListener { findNavController().navigate(R.id.blogsDetailsFragment,bundle) }
            blogAuthor.setOnClickListener { findNavController().navigate(R.id.blogsDetailsFragment,bundle) }
            blogTitle.setOnClickListener { findNavController().navigate(R.id.blogsDetailsFragment,bundle) }
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
            back.setOnClickListener(this@BlogsFragment)
        }

        blogsAdapter.setOnBlogClickListener {
            val bundle = Bundle()
            bundle.putInt("id",it)
            findNavController().navigate(R.id.blogsDetailsFragment,bundle)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.back.id -> findNavController().popBackStack()
        }
    }
}