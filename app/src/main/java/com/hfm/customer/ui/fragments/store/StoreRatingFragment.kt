package com.hfm.customer.ui.fragments.store

import android.content.res.ColorStateList
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hfm.customer.R
import com.hfm.customer.commonModel.Review
import com.hfm.customer.databinding.FragmentStoreRatingBinding
import com.hfm.customer.ui.fragments.products.productDetails.adapter.ReviewsAdapter
import com.hfm.customer.ui.fragments.store.model.StoreData
import com.hfm.customer.ui.fragments.store.model.StoreReviewsData
import com.hfm.customer.utils.Loader
import com.hfm.customer.utils.NoInternetDialog
import com.hfm.customer.utils.Resource
import com.hfm.customer.utils.initRecyclerView
import com.hfm.customer.utils.makeGone
import com.hfm.customer.utils.makeVisible
import com.hfm.customer.utils.netWorkFailure
import com.hfm.customer.utils.showToast
import com.hfm.customer.viewModel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class StoreRatingFragment(private val storeData: StoreData) : Fragment(), View.OnClickListener {

    private lateinit var binding: FragmentStoreRatingBinding
    private var currentView: View? = null
    @Inject lateinit var reviewsAdapter: ReviewsAdapter
    private val reviews: MutableList<Review> = ArrayList()
    private lateinit var appLoader: Loader
    private lateinit var noInternetDialog: NoInternetDialog
    private val mainViewModel: MainViewModel by viewModels()
    private var sellerId: Int = 0
    private var ratings = ""
    private var media = ""
    private var isLoading = false

    private var pageNo = 0
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_store_rating, container, false)
            binding = FragmentStoreRatingBinding.bind(currentView!!)
            init()
            setObserver()
            setOnClickListener()
        }
        return currentView!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setObserver()
        sellerId = storeData.shop_detail[0].seller_id?:0
        mainViewModel.getStoreReviews(
            sellerId = storeData.shop_detail[0].seller_id?:0,
            ratings,
            pageNo
        )
    }

    private fun init() {
        binding.loader.makeVisible()
        appLoader = Loader(requireContext())
        noInternetDialog = NoInternetDialog(requireContext())
        initRecyclerView(requireContext(), binding.reviewsRv, reviewsAdapter)
        binding.reviewsRv.addOnScrollListener(scrollListener)
    }

    private fun setObserver() {
        mainViewModel.storeReview.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    binding.loader.makeGone()
                    if (response.data?.httpcode == "200") {
                        binding.noDataFound.root.isVisible = false
                        setReviews(response.data.data)
                    } else {
                        binding.noDataFound.root.isVisible = true
                        binding.noDataFound.noDataLbl.text = "No Reviews Yet."
                        showToast(response.data?.message.toString())
                    }
                }

                is Resource.Loading -> if(pageNo==0)appLoader.show()
                is Resource.Error -> apiError(response.message)
            }
        }
    }

    private fun setReviews(data: StoreReviewsData) {
        if (pageNo == 0) {
            initRecyclerView(requireContext(), binding.reviewsRv, reviewsAdapter)
                binding.noReviews.isVisible = data.Reviews.isEmpty()
                reviews.clear()
            }
            isLoading = data.Reviews.isEmpty()
            reviews.addAll(data.Reviews)
            reviewsAdapter.differ.submitList(reviews)

            with(binding) {
                reviewRatingBar.rating = data.avg_reviews.toFloat()
                reviewRatingCount.text = data.avg_reviews.toString()
                reviewRatingDetails.text = "${data.total_reviews} Reviews"
                all.text = "All"
                fiveStarSelection.text = "5 Stars (${data.rating_filter.FiveStars})"
                fourStarSelection.text = "4 Stars (${data.rating_filter.FourStars})"
                threeStarSelection.text = "3 Stars (${data.rating_filter.ThreeStars})"
                twoStarSelection.text = "2 Stars (${data.rating_filter.TwoStars})"
                oneStarSelection.text = "1 Stars (${data.rating_filter.OneStar})"
                mediaSelection.text = "Media (${data.rating_filter.Media})"
            }
    }

    private fun apiError(message: String?) {
        appLoader.dismiss()
        showToast(message.toString())
        if (message == netWorkFailure) {
            noInternetDialog.show()
        }
    }

    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val pastVisibleItems = layoutManager.findFirstCompletelyVisibleItemPosition()
            val visibleItemCount = layoutManager.childCount
            val totalItemCount = layoutManager.itemCount

            if (!isLoading && (visibleItemCount + pastVisibleItems) >= totalItemCount) {
                pageNo++
                mainViewModel.getStoreReviews(sellerId, ratings, pageNo)
                isLoading = true
            }
        }
    }

    private fun setOnClickListener() {
        with(binding) {
            fiveStarSelection.setOnClickListener(this@StoreRatingFragment)
            fourStarSelection.setOnClickListener(this@StoreRatingFragment)
            threeStarSelection.setOnClickListener(this@StoreRatingFragment)
            twoStarSelection.setOnClickListener(this@StoreRatingFragment)
            oneStarSelection.setOnClickListener(this@StoreRatingFragment)
            all.setOnClickListener(this@StoreRatingFragment)
            mediaSelection.setOnClickListener(this@StoreRatingFragment)
        }
    }

    private fun setSelection(selectedView: TextView) {
        val grey =
            ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.grey_lite))
        val red = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.red))
        val white = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.white))
        val black = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.black))

        with(binding) {
            fiveStarSelection.backgroundTintList = grey
            fourStarSelection.backgroundTintList = grey
            threeStarSelection.backgroundTintList = grey
            twoStarSelection.backgroundTintList = grey
            oneStarSelection.backgroundTintList = grey
            all.backgroundTintList = grey
            mediaSelection.backgroundTintList = grey

            fiveStarSelection.setTextColor(black)
            fourStarSelection.setTextColor(black)
            threeStarSelection.setTextColor(black)
            twoStarSelection.setTextColor(black)
            oneStarSelection.setTextColor(black)
            all.setTextColor(black)
            mediaSelection.setTextColor(black)
        }
        selectedView.backgroundTintList = red
        selectedView.setTextColor(white)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.fiveStarSelection.id -> {
                setSelection(binding.fiveStarSelection)
                ratings = "5"
                pageNo = 0
                mainViewModel.getStoreReviews(sellerId = sellerId, ratings, pageNo)
            }

            binding.fourStarSelection.id -> {
                setSelection(binding.fourStarSelection)

                ratings = "4"
                pageNo = 0
                mainViewModel.getStoreReviews(sellerId = sellerId, ratings, pageNo)
            }

            binding.threeStarSelection.id -> {
                setSelection(binding.threeStarSelection)

                ratings = "3"
                pageNo = 0
                mainViewModel.getStoreReviews(sellerId = sellerId, ratings, pageNo)
            }

            binding.twoStarSelection.id -> {
                setSelection(binding.twoStarSelection)

                ratings = "2"
                pageNo = 0
                mainViewModel.getStoreReviews(sellerId = sellerId, ratings, pageNo)
            }

            binding.oneStarSelection.id -> {
                setSelection(binding.oneStarSelection)
                ratings = "1"
                pageNo = 0
                mainViewModel.getStoreReviews(sellerId = sellerId, ratings, pageNo)
            }

            binding.all.id -> {
                setSelection(binding.all)

                ratings = ""
                pageNo = 0
                mainViewModel.getStoreReviews(sellerId = sellerId, ratings, pageNo)
            }

            binding.mediaSelection.id -> {
                setSelection(binding.mediaSelection)
                ratings = ""
                pageNo = 0
                mainViewModel.getStoreReviews(sellerId = sellerId, ratings, pageNo,"1")

            }

        }
    }


}