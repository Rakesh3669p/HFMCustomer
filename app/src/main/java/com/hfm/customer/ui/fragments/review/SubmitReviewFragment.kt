package com.hfm.customer.ui.fragments.review

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import com.hfm.customer.R
import com.hfm.customer.databinding.DialogueMediaPickupBinding
import com.hfm.customer.databinding.DialogueReviewSubmittedBinding
import com.hfm.customer.databinding.FragmentSubmitReviewBinding
import com.hfm.customer.databinding.InternationalTermsAndConditionsPopUpBinding
import com.hfm.customer.ui.fragments.review.adapter.ReviewsMediaAdapter
import com.hfm.customer.utils.Loader
import com.hfm.customer.utils.NoInternetDialog
import com.hfm.customer.utils.Resource
import com.hfm.customer.utils.SessionManager
import com.hfm.customer.utils.createFileFromContentUri
import com.hfm.customer.utils.createVideoFileFromContentUri
import com.hfm.customer.utils.getVideoThumbnail
import com.hfm.customer.utils.initRecyclerView
import com.hfm.customer.utils.netWorkFailure
import com.hfm.customer.utils.showToast
import com.hfm.customer.viewModel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class SubmitReviewFragment : Fragment(), View.OnClickListener {

    private var videoFile: File? = null
    private lateinit var binding: FragmentSubmitReviewBinding
    private var currentView: View? = null
    private val mainViewModel: MainViewModel by viewModels()
    private lateinit var appLoader: Loader
    private lateinit var noInternetDialog: NoInternetDialog
    private var imgFile: File? = null

    private var imageUris: MutableList<Uri> = ArrayList()
    private var imageFiles: MutableList<File?> = ArrayList()

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var reviewsMediaAdapter: ReviewsMediaAdapter

    private var productId = ""
    private var orderId = ""
    private var saleIdId = ""
    private var from = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_submit_review, container, false)
            binding = FragmentSubmitReviewBinding.bind(currentView!!)
            init()
            setObserver()
            setOnClickListener()
        }
        return currentView!!
    }

    private fun init() {
        appLoader = Loader(requireContext())
        noInternetDialog = NoInternetDialog(requireContext())
        noInternetDialog.setOnDismissListener { noInternetDialog.dismiss() }
        from = arguments?.getString("from").toString()
        productId = arguments?.getString(   "productId").toString()
        orderId = arguments?.getString("orderId").toString()
        saleIdId = arguments?.getString("saleId").toString()
        initRecyclerView(requireContext(), binding.mediaRv, reviewsMediaAdapter, true)
    }

    private fun setObserver() {
        mainViewModel.submitReview.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    imageFiles.forEach { it?.delete() }
                    if (response.data?.httpcode == 200) {
                        showSuccessDialog()
//                        showToast("Your review has been submitted and is pending for approval")
                    }else{
                        showToast(response.data?.message.toString())
                    }
                }

                is Resource.Loading -> appLoader.show()
                is Resource.Error -> apiError(response.message)
            }
        }
    }

    private fun showSuccessDialog() {
        val appCompatDialog = AppCompatDialog(requireContext())
        val bindingDialog = DialogueReviewSubmittedBinding.inflate(layoutInflater)
        appCompatDialog.setContentView(bindingDialog.root)
        appCompatDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        appCompatDialog.window!!.attributes.width = 500
        appCompatDialog.setCancelable(false)
        appCompatDialog.show()
        bindingDialog.ok.setOnClickListener {
            val bundle = Bundle()
            when(from){
                "ratingDetails"->{
                    bundle.putString("productId",productId)
                    bundle.putInt("purchased",1)
                    bundle.putInt("reviewed",1)
                    findNavController().navigate(R.id.action_submitReview_to_review,bundle)
                }
                "productDetails"->{
                    bundle.putString("productId",productId)
                    bundle.putInt("purchased",1)
                    bundle.putInt("reviewed",1)
                    findNavController().navigate(R.id.action_submitReview_to_productDetails,bundle)
                }
                "orderDetails"->{
                    bundle.putString("orderId",orderId)
                    bundle.putString("saleId",saleIdId)
                    findNavController().navigate(R.id.action_submitReview_to_orders,bundle)
                }
                else->{
                    findNavController().popBackStack()
                }
            }
            appCompatDialog.dismiss()

        }
    }

    private fun apiError(message: String?) {
        appLoader.dismiss()
        showToast(message.toString())
        if (message == netWorkFailure) {
            noInternetDialog.show()
        }
    }

    private fun showImagePickupDialog() {
        val appCompatDialog = Dialog(requireContext())
        val bindingDialog = DialogueMediaPickupBinding.inflate(layoutInflater)
        appCompatDialog.setContentView(bindingDialog.root)
        appCompatDialog.setCancelable(true)
        appCompatDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        with(bindingDialog) {
            camera.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_gallery, 0, 0, 0);
            gallery.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_videocam, 0, 0, 0);
            camera.text = "Image"
            gallery.text = "Video"
        }

        appCompatDialog.show()
        bindingDialog.camera.setOnClickListener {
            if (imageFiles.size >= 3) {
                showToast("Only three images can be added for review")
            } else {
                pickMedia.launch(
                    PickVisualMediaRequest(
                        ActivityResultContracts.PickVisualMedia.ImageOnly
                    )
                )
            }
            appCompatDialog.dismiss()
        }
        bindingDialog.gallery.setOnClickListener {
            if (videoFile != null) {
                showToast("Only one video can be added for review")
            } else {
                pickVideo.launch(
                    PickVisualMediaRequest(
                        ActivityResultContracts.PickVisualMedia.VideoOnly
                    )
                )
            }
            appCompatDialog.dismiss()
        }

    }


    private fun validateAndSubmit() {
        val title = binding.titleEdt.text.toString().trim()
        val review = binding.writeReviewEdt.text.toString().trim()
        val rating = binding.reviewRatingBar.rating

        if (rating <= 0) {
            showToast("Please add a rating to submit")
            return
        }

        if (title.isEmpty()) {
            showToast("Please add a title")
            return
        }

        if (review.isEmpty()) {
            showToast("Please add a review")
            return
        }


        val requestBodyMap = mutableMapOf<String, RequestBody?>()

        if (imageFiles.isNotEmpty()) {
            for ((index, imgFile) in imageFiles.withIndex()) {
                if (imgFile != null) {
                    val key = "image[$index]\"; filename=\"review_$productId$index.jpg"
                    requestBodyMap[key] = imgFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
                }
            }
        }

        if (videoFile != null) {
            val key = "video_link\"; filename=\"review_$productId _video"
            requestBodyMap[key] = videoFile?.asRequestBody("video/mp4".toMediaTypeOrNull())
        }

        requestBodyMap["access_token"] = sessionManager.token.toRequestBody(MultipartBody.FORM)
        requestBodyMap["product_id"] = productId.toString().toRequestBody(MultipartBody.FORM)
        requestBodyMap["comment"] = review.toRequestBody(MultipartBody.FORM)
        requestBodyMap["rating"] = rating.toString().toRequestBody(MultipartBody.FORM)
        requestBodyMap["title"] = title.toRequestBody(MultipartBody.FORM)
        requestBodyMap["sale_id"] = sessionManager.deviceId.toRequestBody(MultipartBody.FORM)
        mainViewModel.submitReview(requestBodyMap)
    }

    private fun setOnClickListener() {
        with(binding) {
            back.setOnClickListener(this@SubmitReviewFragment)
            submitReview.setOnClickListener(this@SubmitReviewFragment)
            uploadImage.setOnClickListener(this@SubmitReviewFragment)
            removeMedia.setOnClickListener(this@SubmitReviewFragment)
            uploadImageVideosLbl.setOnClickListener(this@SubmitReviewFragment)
            uploadImageBg.setOnClickListener(this@SubmitReviewFragment)
        }
        reviewsMediaAdapter.setOnItemClickListener {
            imageFiles[it]?.delete()
            imageFiles.removeAt(it)
            reviewsMediaAdapter.differ.submitList(imageFiles)
            reviewsMediaAdapter.notifyItemRemoved(it)
        }
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            binding.back.id -> findNavController().popBackStack()

            binding.submitReview.id ->validateAndSubmit()


            binding.uploadImage.id -> showImagePickupDialog()
            binding.uploadImageVideosLbl.id -> showImagePickupDialog()
            binding.uploadImageBg.id -> showImagePickupDialog()
            binding.removeMedia.id -> {
                videoFile = null
                binding.videoImageLayout.isVisible = false
            }
        }
    }

    private val pickMedia: ActivityResultLauncher<PickVisualMediaRequest> =
        registerForActivityResult(
            ActivityResultContracts.PickMultipleVisualMedia(3)
        ) { uri ->
            if (uri != null) {
                uri.forEach {
                    imgFile = createFileFromContentUri(requireActivity(), it)
                    imageUris.add(it)
                    imageFiles.add(imgFile)
                }
                initRecyclerView(requireContext(), binding.mediaRv, reviewsMediaAdapter, true)
                reviewsMediaAdapter.differ.submitList(imageFiles)
                reviewsMediaAdapter.notifyDataSetChanged()
            }
        }

    private val pickVideo: ActivityResultLauncher<PickVisualMediaRequest> =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                try {
                    if (videoFile != null) {
                    } else {
                        videoFile = createVideoFileFromContentUri(requireActivity(), uri)
                        binding.videoImageLayout.isVisible = true
                    }

                } catch (e: Exception) {
                    showToast(e.message.toString())
                }
            }
        }
}