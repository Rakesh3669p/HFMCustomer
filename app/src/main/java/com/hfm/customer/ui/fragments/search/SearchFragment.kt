package com.hfm.customer.ui.fragments.search

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.hfm.customer.R
import com.hfm.customer.databinding.FragmentSearchBinding
import com.hfm.customer.ui.fragments.search.adapter.SearchSuggestionsAdapter
import com.hfm.customer.utils.Loader
import com.hfm.customer.utils.NoInternetDialog
import com.hfm.customer.utils.Resource
import com.hfm.customer.utils.SessionManager
import com.hfm.customer.utils.initRecyclerView
import com.hfm.customer.utils.netWorkFailure
import com.hfm.customer.utils.showToast
import com.hfm.customer.viewModel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SearchFragment : Fragment(), View.OnClickListener {

    private lateinit var binding: FragmentSearchBinding
    private var currentView: View? = null
    private val mainViewModel: MainViewModel by viewModels()
    private lateinit var appLoader: Loader
    private lateinit var noInternetDialog: NoInternetDialog

    @Inject
    lateinit var relatedSearchTermAdapter: SearchSuggestionsAdapter
    @Inject lateinit var sessionManager:SessionManager
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_search, container, false)
            binding = FragmentSearchBinding.bind(currentView!!)
            init()
            setObserver()
            setOnClickListener()
        }
        return currentView!!
    }

    override fun onStart() {
        super.onStart()
        val imm = requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.searchBar, 0)
    }

    private fun init() {
        appLoader = Loader(requireContext())
        noInternetDialog = NoInternetDialog(requireContext())
        noInternetDialog.setOnDismissListener { noInternetDialog.dismiss() }
        if(sessionManager.searchPlaceHolder.isNullOrEmpty()){
            binding.searchBar.hint = "Search here.."
        }else{
            binding.searchBar.hint = sessionManager.searchPlaceHolder
        }

        with(binding) {
            binding.clearSearch.isVisible = false
            binding.searchBar.showSoftInputOnFocus = true
            binding.searchBar.requestFocus()

            searchBar.doOnTextChanged { text, _, _, _ ->
                if (text.toString().isNotEmpty()) {
                    binding.clearSearch.isVisible = true
                    mainViewModel.relatedSearchTerms(text.toString())

                } else {
                    binding.clearSearch.isVisible = false
                    binding.searchSuggestionsRv.isVisible = false
                    relatedSearchTermAdapter.differ.submitList(emptyList())
                }
            }

            searchBar.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    if (searchBar.text.toString().isNotEmpty()) {
                        findNavController().popBackStack()
                        val bundle = Bundle()
                        bundle.putString("keyword", searchBar.text.toString())
                        findNavController().navigate(R.id.productListFragment, bundle)
                    }
                    return@setOnEditorActionListener true
                }
                false
            }
        }
    }

    private fun setObserver() {
        mainViewModel.relatedSearchTerms.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    if (response.data?.httpcode == 200) {
                        binding.searchSuggestionsRv.isVisible = true
                        initRecyclerView(
                            requireContext(),
                            binding.searchSuggestionsRv,
                            relatedSearchTermAdapter
                        )
                        relatedSearchTermAdapter.differ.submitList(response.data.data.related_terms)
                        relatedSearchTermAdapter.setOnSearchValue(binding.searchBar.text.toString())

                    } else {
                        binding.searchSuggestionsRv.isVisible = false
                        relatedSearchTermAdapter.differ.submitList(emptyList())
                    }
                }

                is Resource.Loading -> Unit
                is Resource.Error -> apiError(response.message)
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
            back.setOnClickListener(this@SearchFragment)
            clearSearch.setOnClickListener(this@SearchFragment)
            searchFilter.setOnClickListener(this@SearchFragment)
        }
        relatedSearchTermAdapter.setOnItemClickListener {
            val bundle = Bundle()
            bundle.putString("keyword", it)
            findNavController().popBackStack()
            findNavController().navigate(R.id.productListFragment, bundle)
        }
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            binding.back.id -> findNavController().popBackStack()
            binding.searchFilter.id -> findNavController().navigate(R.id.categoriesFragmentHome)
            binding.clearSearch.id -> {
                binding.searchSuggestionsRv.isVisible = false
                relatedSearchTermAdapter.differ.submitList(emptyList())
                binding.searchBar.setText("")
            }

        }
    }

    override fun onPause() {
        val imm = requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.searchBar.windowToken, 0)
        super.onPause()
    }

}