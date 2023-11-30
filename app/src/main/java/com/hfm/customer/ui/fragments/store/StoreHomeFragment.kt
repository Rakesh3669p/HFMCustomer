package com.hfm.customer.ui.fragments.store

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.MergingMediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import com.google.android.material.tabs.TabLayoutMediator
import com.hfm.customer.R
import com.hfm.customer.databinding.FragmentStoreHomeBinding
import com.hfm.customer.ui.fragments.cart.model.Coupon
import com.hfm.customer.ui.fragments.store.adapter.StoreBannerAdapter
import com.hfm.customer.ui.fragments.store.adapter.StoreVouchersAdapter
import com.hfm.customer.ui.fragments.store.model.StoreData
import com.hfm.customer.utils.Loader
import com.hfm.customer.utils.NoInternetDialog
import com.hfm.customer.utils.Resource
import com.hfm.customer.utils.SessionManager
import com.hfm.customer.utils.extractYouTubeVideoId
import com.hfm.customer.utils.initRecyclerView
import com.hfm.customer.utils.moveToLogin
import com.hfm.customer.utils.netWorkFailure
import com.hfm.customer.utils.showToast
import com.hfm.customer.viewModel.MainViewModel
import com.maxrave.kotlinyoutubeextractor.State
import com.maxrave.kotlinyoutubeextractor.YTExtractor
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject


@SuppressLint("UnsafeOptInUsageError")
@AndroidEntryPoint
class StoreHomeFragment(private val storeData: StoreData) : Fragment(), View.OnClickListener {


    @Inject
    lateinit var vouchersAdapter: StoreVouchersAdapter

    private var sellerVouchers: List<Coupon> = ArrayList()

    private lateinit var binding: FragmentStoreHomeBinding
    private var currentView: View? = null

    @Inject
    lateinit var bannerAdapter: StoreBannerAdapter
    private val mainViewModel: MainViewModel by viewModels()

    private lateinit var appLoader: Loader
    private lateinit var noInternetDialog: NoInternetDialog
    private var handler: Handler = Handler(Looper.getMainLooper())
    private var runnable: Runnable? = null
    var delay = 5000

    private lateinit var exoPause: ImageView
    private lateinit var exoPlay: ImageView
    private lateinit var exoBuffer: ProgressBar
    @Inject lateinit var sessionManager: SessionManager

    private val videoPlayer: ExoPlayer by lazy {
        ExoPlayer.Builder(requireContext()).build().apply {
            setAudioAttributes(audioAttributes, true)
            pauseAtEndOfMediaItems = true
            setHandleAudioBecomingNoisy(true)
        }
    }

    private lateinit var dataSourceFactory: DefaultDataSource.Factory

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_store_home, container, false)
            binding = FragmentStoreHomeBinding.bind(currentView!!)
            init()
            setBanner()
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
        noInternetDialog = NoInternetDialog(requireContext())
        noInternetDialog.setOnDismissListener { init() }
        mainViewModel.getSellerVouchers(storeData.shop_detail[0].seller_id.toString(), 0)
        dataSourceFactory = DefaultDataSource.Factory(requireContext())
        binding.storeVideoLayout.isVisible = !storeData.shop_detail[0].video.isNullOrEmpty()
        binding.storeVideo.clipToOutline = true
        binding.storeVideo.player = videoPlayer
        exoPlay = binding.storeVideo.findViewById(androidx.media3.ui.R.id.exo_play)
        exoPause = binding.storeVideo.findViewById(androidx.media3.ui.R.id.exo_pause)
        exoBuffer = binding.storeVideo.findViewById(androidx.media3.ui.R.id.exo_buffering)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setBanner() {
        bannerAdapter.differ.submitList(storeData.shop_detail[0].banners)
        binding.banner.adapter = bannerAdapter
        val tabLayoutMediator =
            TabLayoutMediator(binding.bannerDots, binding.banner, true) { _, _ -> }
        tabLayoutMediator.attach()

        handler.postDelayed(Runnable {
            handler.postDelayed(runnable!!, delay.toLong())
            if (binding.banner.currentItem == storeData.shop_detail[0].banners.size - 1) {
                binding.banner.currentItem = 0
            } else {
                val increaseItem = binding.banner.currentItem + 1
                binding.banner.setCurrentItem(increaseItem, true)
            }

        }.also { runnable = it }, delay.toLong())

        val htmlContent = storeData.shop_detail[0].store_details
        binding.storeMore.apply {
            loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
            settings.javaScriptEnabled = true
            val webSettings = settings
            webSettings.loadsImagesAutomatically = true
            webSettings.domStorageEnabled = true
        }
    }

    private fun setObserver() {

        mainViewModel.claimStoreVoucher.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    if (response.data?.httpcode == 200) {
                        mainViewModel.getSellerVouchers(
                            storeData.shop_detail[0].seller_id.toString(),
                            0
                        )
                    } else {
                        binding.storeVouchers.isVisible = true
                    }
                }

                is Resource.Loading -> appLoader.show()
                is Resource.Error ->
                    apiError(response.message)
            }
        }

        mainViewModel.sellerVouchers.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    if (response.data?.httpcode == 200) {
                        binding.noVouchesAvailable.isVisible = response.data.data.coupon_list.isEmpty()
                        sellerVouchers = response.data.data.coupon_list
                        binding.noVouchesAvailable.isVisible = response.data.data.coupon_list.isEmpty()
                        initRecyclerView(requireContext(), binding.vouchersRv, vouchersAdapter, true)
                        vouchersAdapter.differ.submitList(response.data.data.coupon_list)

                    }
                }

                is Resource.Loading -> appLoader.show()
                is Resource.Error ->
                    apiError(response.message)
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

    private fun playVideo() {
        exoBuffer.isVisible = true
        exoPlay.isVisible = false
        exoPause.isVisible = false
        val yt =
            YTExtractor(con = requireActivity(), CACHING = false, LOGGING = true, retryCount = 3)
        lifecycleScope.launch {
            yt.extract(extractYouTubeVideoId(storeData.shop_detail[0].video.toString()).toString())
            if (yt.state == State.SUCCESS) {
                yt.getYTFiles().let {
                    val videoMedia = it?.get(135)?.url.toString()
                    val audioMedia = it?.get(251)?.url.toString()
                    startVideo(videoMedia, audioMedia)
                }
            }
        }
    }

    private fun startVideo(videoMedia: String, audioMedia: String) {
        val videoMediaItem: MediaItem = MediaItem.fromUri(videoMedia)
        val audioMediaItem: MediaItem = MediaItem.fromUri(audioMedia)
        val videoMediaSource =
            ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(videoMediaItem)
        val audioMediaSource =
            ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(audioMediaItem)
        val mergedSource = MergingMediaSource(videoMediaSource, audioMediaSource)

        val currentMediaItem: MediaItem? = videoPlayer.currentMediaItem
        if (currentMediaItem == null || currentMediaItem != mergedSource.mediaItem) {
            try {
                videoPlayer.stop()
                videoPlayer.setMediaSource(mergedSource)
                videoPlayer.prepare()
                videoPlayer.playWhenReady = true
                videoPlayer.addListener(playerListener)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } else {
            togglePlayPause()
        }

    }


    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            super.onIsPlayingChanged(isPlaying)
            if (isPlaying) {
                binding.playBtn.isVisible = false
                exoBuffer.isVisible = false
                exoPlay.isVisible = false
                exoPause.isVisible = true
            } else {
                exoBuffer.isVisible = true
                exoPlay.isVisible = true
                exoPause.isVisible = false
            }
        }
    }

    private fun togglePlayPause() {
        if (videoPlayer.isPlaying) {
            videoPlayer.pause()
        } else {
            if (videoPlayer.currentPosition >= videoPlayer.duration) {
                videoPlayer.seekTo(0)
            }
            videoPlayer.play()
        }
    }

    private fun setOnClickListener() {
        with(binding) {
            playBtn.setOnClickListener(this@StoreHomeFragment)
        }

        vouchersAdapter.setOnItemClickListener { position ->
            if(!sessionManager.isLogin){
                showToast("Please login first")
                requireActivity().moveToLogin(sessionManager)
                return@setOnItemClickListener
            }
            val couponCode = sellerVouchers[position].couponCode
            if (storeData.shop_detail[0].seller_id != null) {
                mainViewModel.claimStoreVoucher(
                    couponCode,
                    sellerId = storeData.shop_detail[0].seller_id.toString()
                )
            }
        }

        exoPlay.setOnClickListener {
            videoPlayer.play()
        }

        exoPause.setOnClickListener {
            videoPlayer.pause()
        }
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            binding.playBtn.id -> {
                playVideo()
                binding.playBtn.setImageResource(R.drawable.ic_pause_circle)
                binding.playBtn.isVisible = false
            }
        }
    }

    override fun onPause() {
        super.onPause()
        videoPlayer.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        videoPlayer.pause()
        videoPlayer.release()

    }
}