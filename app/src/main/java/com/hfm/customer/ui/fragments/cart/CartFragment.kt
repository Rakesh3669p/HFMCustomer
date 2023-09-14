package com.hfm.customer.ui.fragments.cart

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.tabs.TabLayout
import com.hfm.customer.R
import com.hfm.customer.databinding.BottomSheetSortingBinding
import com.hfm.customer.databinding.BottomSheetVoucherBinding
import com.hfm.customer.databinding.FragmentCartBinding
import com.hfm.customer.databinding.FragmentStoreBinding
import com.hfm.customer.ui.fragments.wishlist.adapter.WishListPagerAdapter


class CartFragment : Fragment(), View.OnClickListener {


    private lateinit var binding: FragmentCartBinding
    private var currentView: View? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_cart, container, false)
            binding = FragmentCartBinding.bind(currentView!!)
            init()
            setObserver()
            setOnClickListener()
        }
        return currentView!!
    }

    private fun init() {
        with(binding) {}
    }


    private fun setObserver() {}

    private fun setOnClickListener() {
        with(binding) {
            back.setOnClickListener(this@CartFragment)
            checkOut.setOnClickListener(this@CartFragment)
            addVoucher.setOnClickListener(this@CartFragment)
        }
    }

    private fun showVoucherBottomSheet() {
        val binding = BottomSheetVoucherBinding.inflate(layoutInflater)
        val sortDialog = BottomSheetDialog(requireActivity(), R.style.MyTransparentBottomSheetDialogTheme)
        sortDialog.setContentView(binding.root)
        sortDialog.show()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.back.id -> findNavController().popBackStack()
            binding.checkOut.id -> findNavController().navigate(R.id.checkOutFragment)
            binding.addVoucher.id -> showVoucherBottomSheet()
        }
    }
}