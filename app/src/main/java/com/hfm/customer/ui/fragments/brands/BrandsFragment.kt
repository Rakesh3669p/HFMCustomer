package com.hfm.customer.ui.fragments.brands

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.hfm.customer.R
import com.hfm.customer.databinding.FragmentBrandsBinding
import com.hfm.customer.databinding.FragmentBulkOrdersBinding
import com.hfm.customer.ui.dashBoard.home.adapter.BrandsAdapter
import com.hfm.customer.ui.fragments.brands.adapter.BrandStoreSingleAdapter
import com.hfm.customer.ui.fragments.brands.adapter.BrandsAlphabetsAdapter
import com.hfm.customer.ui.fragments.myOrders.adapter.BulkOrdersAdapter
import com.hfm.customer.utils.Loader
import com.hfm.customer.utils.NoInternetDialog
import com.hfm.customer.utils.Resource
import com.hfm.customer.utils.initRecyclerView
import com.hfm.customer.utils.initRecyclerViewGrid
import com.hfm.customer.utils.netWorkFailure
import com.hfm.customer.utils.showToast
import com.hfm.customer.viewModel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

val alphabetList = listOf(
    "#","A", "B", "C", "D", "E", "F", "G", "H", "I", "J",
    "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T",
    "U", "V", "W", "X", "Y", "Z"
)

@AndroidEntryPoint
class BrandsFragment : Fragment() ,View.OnClickListener{
    private lateinit var binding: FragmentBrandsBinding
    private var currentView: View? = null
    private val mainViewModel: MainViewModel by viewModels()
    @Inject lateinit var alphabetsAdapter: BrandsAlphabetsAdapter
    @Inject lateinit var brandsAdapter: BrandStoreSingleAdapter
    private lateinit var appLoader: Loader
    private lateinit var noInternetDialog: NoInternetDialog
    var alphabet = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_brands, container, false)
            binding = FragmentBrandsBinding.bind(currentView!!)
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
        alphabetsAdapter.differ.submitList(alphabetList)
        binding.alphabetsRv.apply {
            setHasFixedSize(true)
            layoutManager = FlexboxLayoutManager(requireContext()).apply {
                flexDirection = FlexDirection.ROW
            }
            adapter = alphabetsAdapter
        }
        mainViewModel.getBrands(alphabet)
    }

    private fun setObserver() {
        mainViewModel.brands.observe(viewLifecycleOwner){response->
            when(response){
                is Resource.Success->{
                    appLoader.dismiss()
                    if(response.data?.httpcode == 200){
                        initRecyclerViewGrid(requireContext(),binding.brandsRv,brandsAdapter,2)
                        brandsAdapter.differ.submitList(response.data.data.brands)
                        binding.noData.isVisible = response.data.data.brands.isEmpty()
                    }else if(response.data?.httpcode == 404){
                        binding.noData.isVisible = true
                        brandsAdapter.differ.submitList(emptyList())
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
            back.setOnClickListener(this@BrandsFragment)
            search.setOnClickListener(this@BrandsFragment)
        }

        alphabetsAdapter.setOnItemClickListener {selectedAlphabet->
            alphabet = if(selectedAlphabet=="#") "" else selectedAlphabet
            mainViewModel.getBrands(alphabet)
        }

        brandsAdapter.setOnBrandClickListener {
            val bundle = Bundle().apply { putString("brandId", it.toString()) }
            findNavController().navigate(R.id.productListFragment, bundle)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.back.id -> findNavController().popBackStack()
            binding.search.id -> findNavController().navigate(R.id.searchFragment)
        }
    }
}