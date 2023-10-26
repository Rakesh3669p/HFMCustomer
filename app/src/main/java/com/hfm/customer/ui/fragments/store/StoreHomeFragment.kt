package com.hfm.customer.ui.fragments.store

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat.getSystemService
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
import com.hfm.customer.ui.fragments.cart.adapter.PlatformVoucherAdapter
import com.hfm.customer.ui.fragments.cart.model.Coupon
import com.hfm.customer.ui.fragments.products.productDetails.adapter.VouchersAdapter
import com.hfm.customer.ui.fragments.products.productDetails.model.SellerVoucherModel
import com.hfm.customer.ui.fragments.store.adapter.StoreBannerAdapter
import com.hfm.customer.ui.fragments.store.model.StoreData
import com.hfm.customer.utils.Loader
import com.hfm.customer.utils.NoInternetDialog
import com.hfm.customer.utils.Resource
import com.hfm.customer.utils.extractYouTubeVideoId
import com.hfm.customer.utils.initRecyclerView
import com.hfm.customer.utils.netWorkFailure
import com.hfm.customer.utils.showToast
import com.hfm.customer.viewModel.MainViewModel
import com.maxrave.kotlinyoutubeextractor.State
import com.maxrave.kotlinyoutubeextractor.YTExtractor
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.Timer
import java.util.TimerTask
import javax.inject.Inject


@SuppressLint("UnsafeOptInUsageError")
@AndroidEntryPoint
class StoreHomeFragment(val storeData: StoreData) : Fragment(), View.OnClickListener {


    @Inject
    lateinit var vouchersAdapter: VouchersAdapter

    private var sellerVouchers: List<Coupon> = ArrayList()

    private lateinit var binding: FragmentStoreHomeBinding
    private var currentView: View? = null

    @Inject
    lateinit var bannerAdapter: StoreBannerAdapter
    private val mainViewModel: MainViewModel by viewModels()

    private lateinit var appLoader: Loader
    private lateinit var noInternetDialog: NoInternetDialog
    var handler: Handler = Handler(Looper.getMainLooper())
    var runnable: Runnable? = null
    var delay = 5000

    private lateinit var exoPause: ImageView
    private lateinit var exoPlay: ImageView

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
        mainViewModel.getSellerVouchers(storeData.shop_detail[0].seller_id.toString())
        dataSourceFactory = DefaultDataSource.Factory(requireContext())
        binding.storeVideoLayout.isVisible = !storeData.shop_detail[0].video.isNullOrEmpty()
        binding.storeVideo.clipToOutline = true
        binding.storeVideo.player = videoPlayer
        exoPlay = binding.storeVideo.findViewById(androidx.media3.ui.R.id.exo_play)
        exoPause = binding.storeVideo.findViewById(androidx.media3.ui.R.id.exo_pause)

    }

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

    }


    private fun setObserver() {
        mainViewModel.sellerVouchers.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    if(response.data?.httpcode==200){
                        binding.storeVouchers.isVisible = response.data.data.coupon_list.isNotEmpty()

                        sellerVouchers = response.data.data.coupon_list
                        initRecyclerView(
                            requireContext(),
                            binding.vouchersRv,
                            vouchersAdapter, true
                        )
                        vouchersAdapter.differ.submitList(response.data.data.coupon_list)
                        vouchersAdapter.setOnItemClickListener { position ->
                            val couponCode = sellerVouchers[position].couponCode
                            val clipboardManager =
                                requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clipData = ClipData.newPlainText("text", couponCode)
                            clipboardManager.setPrimaryClip(clipData)
                            showToast("Coupon code copied")
                        }

                    }else{
                        binding.storeVouchers.isVisible = true
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
        val yt = YTExtractor(con = requireActivity(), CACHING = false, LOGGING = true, retryCount = 3)
        lifecycleScope.launch {
            yt.extract(extractYouTubeVideoId(storeData.shop_detail[0].video).toString())
            if (yt.state == State.SUCCESS) {
                yt.getYTFiles().let {
                    val videoMedia = it?.get(135)?.url.toString()
                    val audioMedia = it?.get(251)?.url.toString()
                    startVideo(videoMedia,audioMedia)
                }
            }
        }
    }
    private fun startVideo(videoMedia:String,audioMedia:String){
        val videoMediaItem: MediaItem = MediaItem.fromUri(videoMedia)
        val audioMediaItem: MediaItem = MediaItem.fromUri(audioMedia)
        val videoMediaSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(videoMediaItem)
        val audioMediaSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(audioMediaItem)
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


    private val playerListener= object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            super.onIsPlayingChanged(isPlaying)
            if(isPlaying){
                binding.playBtn.isVisible = false
                exoPlay.isVisible = false
                exoPause.isVisible = true
            }else{
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
            playBtn.setOnClickListener (this@StoreHomeFragment)


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
}