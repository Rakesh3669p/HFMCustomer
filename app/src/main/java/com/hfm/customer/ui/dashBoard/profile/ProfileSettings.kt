package com.hfm.customer.ui.dashBoard.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.hfm.customer.R
import com.hfm.customer.databinding.FragmentCategoriesBinding
import com.hfm.customer.databinding.FragmentProfileBinding
import com.hfm.customer.databinding.FragmentProfileSettingsBinding
import com.hfm.customer.ui.dashBoard.categories.adapter.CategoriesAdapter
import com.hfm.customer.ui.dashBoard.categories.adapter.CategoryMainAdapter
import com.hfm.customer.utils.showToast
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ProfileSettings : Fragment(), View.OnClickListener {
    private lateinit var binding: FragmentProfileSettingsBinding
    private var currentView: View? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_profile_settings, container, false)
            binding = FragmentProfileSettingsBinding.bind(currentView!!)
            init()
            setObserver()
            setOnClickListener()
        }
        return currentView!!
    }

    private fun init() {

    }

    private fun setObserver() {}

    private fun setOnClickListener() {
        with(binding) {
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
        }
    }
}