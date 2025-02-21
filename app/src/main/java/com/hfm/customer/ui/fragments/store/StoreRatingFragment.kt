package com.hfm.customer.ui.fragments.store

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hfm.customer.R
import com.hfm.customer.commonModel.Review
import com.hfm.customer.databinding.FragmentStoreRatingBinding
import com.hfm.customer.databinding.ReviewMediaImagesBinding
import com.hfm.customer.databinding.ReviewMediaVideoBinding
import com.hfm.customer.ui.fragments.products.productDetails.adapter.ReviewsAdapter
import com.hfm.customer.ui.fragments.store.model.StoreData
import com.hfm.customer.ui.fragments.store.model.StoreReviewsData
import com.hfm.customer.utils.Loader
import com.hfm.customer.utils.NoInternetDialog
import com.hfm.customer.utils.Resource
import com.hfm.customer.utils.initRecyclerView
import com.hfm.customer.utils.loadImage
import com.hfm.customer.utils.makeGone
import com.hfm.customer.utils.makeVisible
import com.hfm.customer.utils.netWorkFailure
import com.hfm.customer.utils.replaceBaseUrl
import com.hfm.customer.utils.showToast
import com.hfm.customer.viewModel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.io.IOException
import javax.inject.Inject

@SuppressLint("UnsafeOptInUsageError")
@AndroidEntryPoint
class StoreRatingFragment(private val storeData: StoreData) : Fragment(), View.OnClickListener {

    private lateinit var binding: FragmentStoreRatingBinding
    private var currentView: View? = null
    @Inject lateinit var reviewsAdapter: ReviewsAdapter
    private val reviews: MutableList<Review> = ArrayList()
    private lateinit var appLoader: Loader
    private lateinit var noInternetDialog: NoInternetDialog
    private lateinit var dataSourceFactory: DefaultDataSource.Factory
    private val mainViewModel: MainViewModel by viewModels()
    private var sellerId: Int = 0
    private var ratings = ""
    private var media = ""
    private var isLoading = false
    private var pageNo = 0

    private val reviewPlayer: ExoPlayer by lazy {
        ExoPlayer.Builder(requireContext()).build().apply {
            setAudioAttributes(audioAttributes, true)
            pauseAtEndOfMediaItems = true
            setHandleAudioBecomingNoisy(true)
        }
    }
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
        dataSourceFactory = DefaultDataSource.Factory(requireContext())
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

        reviewsAdapter.setOnImageClickListener { images, index ->
            showImageDialog(images, index)

        }
        reviewsAdapter.setOnVideoClickListener { video ->
            showVideoDialog(video)

        }
    }

    private fun showImageDialog(images: List<String>, index: Int) {
        var currentIndex = index
        val appCompatDialog = Dialog(requireContext())
        val bindingDialog = ReviewMediaImagesBinding.inflate(layoutInflater)
        appCompatDialog.setContentView(bindingDialog.root)
        appCompatDialog.setCancelable(true)

        bindingDialog.productImage.loadImage(replaceBaseUrl(images[currentIndex]))
        bindingDialog.right.setOnClickListener {

            if (currentIndex < images.size - 1) {
                currentIndex++
                bindingDialog.productImage.loadImage(replaceBaseUrl(images[currentIndex]))
            }
        }

        bindingDialog.left.setOnClickListener {
            if (currentIndex > 0) {
                currentIndex--
                bindingDialog.productImage.loadImage(replaceBaseUrl(images[currentIndex]))
            }
        }

        bindingDialog.close.setOnClickListener { appCompatDialog.dismiss() }
        appCompatDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        appCompatDialog.show()
    }

    private fun showVideoDialog(video: String) {
        val appCompatDialog = Dialog(requireContext())
        val bindingDialog = ReviewMediaVideoBinding.inflate(layoutInflater)
        appCompatDialog.setContentView(bindingDialog.root)
        appCompatDialog.setCancelable(true)
        bindingDialog.reviewVideo.clipToOutline = true
        bindingDialog.reviewVideo.player = reviewPlayer
        val exoPlay: ImageView =
            bindingDialog.reviewVideo.findViewById(androidx.media3.ui.R.id.exo_play)
        val exoPause: ImageView =
            bindingDialog.reviewVideo.findViewById(androidx.media3.ui.R.id.exo_pause)
        val exoBuffer: ProgressBar =
            bindingDialog.reviewVideo.findViewById(androidx.media3.ui.R.id.exo_buffering)
        exoPlay.setOnClickListener {
            playReviewVideo(video)
        }

        exoPause.setOnClickListener {
            reviewPlayer.pause()
        }

        playReviewVideo(video)

        reviewPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                super.onIsPlayingChanged(isPlaying)
                if (isPlaying) {
                    exoBuffer.isVisible = false
                    exoPlay.isVisible = false
                    exoPause.isVisible = true
                } else {
                    exoBuffer.isVisible = true
                    exoPlay.isVisible = true
                    exoPause.isVisible = false
                }
            }
        })

        bindingDialog.close.setOnClickListener { appCompatDialog.dismiss() }
        appCompatDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        appCompatDialog.show()
    }

    private fun playReviewVideo(video: String) {
        val mediaItem: MediaItem = MediaItem.fromUri(video)

        val mediaSource =
            ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem)
        val currentMediaItem: MediaItem? = reviewPlayer.currentMediaItem
        if (currentMediaItem == null || currentMediaItem != mediaSource.mediaItem) {
            try {
                reviewPlayer.stop()
                reviewPlayer.setMediaSource(mediaSource)
                reviewPlayer.prepare()
//                reviewPlayer.addListener(playerListener)
                reviewPlayer.playWhenReady = true
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } else {
            toggleReviewPlayPause()
        }
    }

    private fun toggleReviewPlayPause() {
        if (reviewPlayer.isPlaying) {
            reviewPlayer.pause()
        } else {
            if (reviewPlayer.currentPosition >= reviewPlayer.duration) {
                reviewPlayer.seekTo(0)
            }
            reviewPlayer.play()
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

    override fun onPause() {
        super.onPause()
        reviewPlayer.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        reviewPlayer.release()
    }

}