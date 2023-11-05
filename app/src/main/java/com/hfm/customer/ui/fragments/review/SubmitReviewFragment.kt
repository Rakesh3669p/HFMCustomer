package com.hfm.customer.ui.fragments.review

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.hfm.customer.R
import com.hfm.customer.databinding.FragmentSubmitReviewBinding
import com.hfm.customer.ui.fragments.review.adapter.ReviewsMediaAdapter
import com.hfm.customer.utils.Loader
import com.hfm.customer.utils.NoInternetDialog
import com.hfm.customer.utils.Resource
import com.hfm.customer.utils.SessionManager
import com.hfm.customer.utils.createFileFromContentUri
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
        productId = arguments?.getString("productId").toString()
        initRecyclerView(requireContext(), binding.mediaRv, reviewsMediaAdapter, true)
    }

    private fun setObserver() {
        mainViewModel.submitReview.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    showToast(response.data?.message.toString())
                    if (response.data?.httpcode == 200) {
                    }
                }

                is Resource.Loading -> appLoader.show()
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


    private fun validateAndSubmit() {
        val title = binding.titleEdt.text.toString()
        val review = binding.writeReviewEdt.text.toString()
        val rating = binding.reviewRatingBar.rating

        if (rating < 0) {
            showToast("please add a rating to submit")
            return
        }

        if (title.isEmpty()) {
            showToast("please add a title")
            return
        }

        if (review.isEmpty()) {
            showToast("please add a review")
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
            uploadImageVideosLbl.setOnClickListener(this@SubmitReviewFragment)
        }
        reviewsMediaAdapter.setOnItemClickListener {
            imageUris.removeAt(it)
            imageFiles.removeAt(it)
            reviewsMediaAdapter.differ.submitList(imageUris)
            reviewsMediaAdapter.notifyItemRemoved(it)
        }
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            binding.back.id -> findNavController().popBackStack()
            binding.submitReview.id -> validateAndSubmit()
            binding.uploadImage.id -> pickMedia.launch(
                PickVisualMediaRequest(
                    ActivityResultContracts.PickVisualMedia.ImageOnly
                )
            )

            binding.uploadImageVideosLbl.id -> pickMedia.launch(
                PickVisualMediaRequest(
                    ActivityResultContracts.PickVisualMedia.ImageOnly
                )
            )
        }


    }

    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(4)) { uri ->
            if (uri != null) {
                uri.forEach {
                    imgFile = createFileFromContentUri(requireActivity(),it)
                    imageUris.add(it)
                    imageFiles.add(imgFile)
                }
                initRecyclerView(requireContext(), binding.mediaRv, reviewsMediaAdapter, true)
                reviewsMediaAdapter.differ.submitList(imageUris)
                reviewsMediaAdapter.notifyDataSetChanged()
            }
        }
}