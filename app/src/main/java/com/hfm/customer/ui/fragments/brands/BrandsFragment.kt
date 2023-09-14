package com.hfm.customer.ui.fragments.brands

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_brands, container, false)
            binding = FragmentBrandsBinding.bind(currentView!!)
            init()
            setObserver()
            setOnClickListener()
        }
        return currentView!!
    }

    private fun init() {
        appLoader = Loader(requireContext())
        alphabetsAdapter.differ.submitList(alphabetList)
        binding.alphabetsRv.apply {
            setHasFixedSize(true)
            layoutManager = FlexboxLayoutManager(requireContext()).apply {
                flexDirection = FlexDirection.ROW
            }
            adapter = alphabetsAdapter
        }
        mainViewModel.getBrandsList()
    }

    private fun setObserver() {
        mainViewModel.homeBrands.observe(viewLifecycleOwner){response->
            when(response){
                is Resource.Success->{
                    appLoader.dismiss()
                    if(response.data?.httpcode == 200){
                        initRecyclerViewGrid(requireContext(),binding.brandsRv,brandsAdapter,2)
                        brandsAdapter.differ.submitList(response.data.data.brands)
                    }
                }

                is Resource.Loading->appLoader.show()

                is Resource.Error->{
                    appLoader.dismiss()
                    showToast(response.message.toString())
                    if(response.message.toString() == netWorkFailure){

                    }
                }
            }
        }
    }



    private fun setOnClickListener() {
        with(binding) {
            back.setOnClickListener(this@BrandsFragment)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.back.id -> findNavController().popBackStack()
        }
    }
}