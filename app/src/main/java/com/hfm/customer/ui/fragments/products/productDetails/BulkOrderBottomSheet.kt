package com.hfm.customer.ui.fragments.products.productDetails

import android.app.ActionBar
import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Point
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import coil.load
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hfm.customer.R
import com.hfm.customer.databinding.BottomSheetBulkOrderBinding
import com.hfm.customer.ui.dashBoard.profile.model.Profile
import com.hfm.customer.ui.fragments.products.productDetails.model.Product
import com.hfm.customer.utils.showToast
import com.hfm.customer.viewModel.MainViewModel
import kotlinx.coroutines.flow.callbackFlow
import java.time.LocalDate

class BulkOrderBottomSheet(
    private val profileData: Profile,
    private val productData: Product,
    val callback: (
        product_id: String,
        name: String,
        email: String,
        qty: String,
        phone: String,
        dateNeeded: String,
        deliveryAddress: String,
        remarks: String

    ) -> Unit
) : BottomSheetDialogFragment() {
    private lateinit var binding: BottomSheetBulkOrderBinding
    var unitOfMeasures = ""
    var dateNeeded = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialog)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val currentView = inflater.inflate(R.layout.bottom_sheet_bulk_order, container, false)
        binding = BottomSheetBulkOrderBinding.bind(currentView)
        return currentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init() {
        val windowManager =
            requireActivity().getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        val screenHeight: Int = size.y
        val desiredHeight = (screenHeight * 0.85).toInt() // Adjust the fraction as needed

        with(binding) {
            val layoutParams = scrollView.layoutParams
            layoutParams.width = ActionBar.LayoutParams.MATCH_PARENT
            layoutParams.height = desiredHeight
            scrollView.layoutParams = layoutParams

            productName.text = productData.product_name
            val imageOriginal = productData.image?.get(0)?.image
            val imageReplaced =
                imageOriginal?.replace("https://uat.hfm.synuos.com", "http://4.194.191.242")
            productImage.load(imageReplaced) {
                placeholder(R.drawable.logo)

            }
            name.text = "${profileData.first_name} ${profileData.last_name}"
            email.text = profileData.email
            address.setText(profileData.address1)


            // Define an array of items
            val items = arrayOf(
                "Units of Measurement",
                "Pieces",
                "Packs",
                "Sets",
                "Numbers",
                "Kilograms",
                "Boxes",
                "Outers",
                "Carton"
            )


            val adapter: ArrayAdapter<String> =
                ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item, items)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            unitsSpinner.adapter = adapter
            unitsSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parentView: AdapterView<*>,
                    selectedItemView: View,
                    position: Int,
                    id: Long
                ) {
                    unitOfMeasures = parentView.getItemAtPosition(position) as String

                }

                override fun onNothingSelected(parentView: AdapterView<*>?) {
                    // Do nothing here
                }
            }
            val currentDate = LocalDate.now()


            date.setOnClickListener {
                val datePickerDialog = DatePickerDialog(
                    requireContext(),
                    dateSetListener,
                    currentDate.year,
                    currentDate.monthValue - 1,
                    currentDate.dayOfMonth
                )
                datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
                datePickerDialog.show()
            }

            send.setOnClickListener {

                val name = name.text.toString()
                val email = email.text.toString()
                val qty = qty.text.toString()
                val phone = contact.text.toString()
                val date = date.text.toString()
                val deliveryAddress = address.text.toString()
                val remarks = remarks.text.toString()

                if (name.isEmpty()) {
                    showToast("Please Enter a valid Name")
                    return@setOnClickListener
                }

                if (email.isEmpty()) {
                    showToast("Please Enter a valid Email")
                    return@setOnClickListener
                }
                if (qty.isEmpty() || qty.toInt() < 1) {
                    showToast("Purchase Quantity is required")
                    return@setOnClickListener
                }

                if (unitOfMeasures.isEmpty() || unitOfMeasures == "Units of Measurement") {
                    showToast("Unit of Measures is required!")
                    return@setOnClickListener
                }

                if (phone.isEmpty()) {
                    showToast("Contact Number is required")
                    return@setOnClickListener
                }
                if (date.isEmpty() || dateNeeded.isEmpty()) {
                    showToast("Date is required!")
                    return@setOnClickListener
                }

                if (deliveryAddress.isEmpty()) {
                    showToast("Delivery Address is required!")
                    return@setOnClickListener
                }

                callback( productData.product_id.toString(),
                    name,
                    email,
                    qty,
                    phone,
                    dateNeeded,
                    deliveryAddress,
                    remarks)
            }
            cancel.setOnClickListener {
                findNavController().popBackStack()
            }
        }
    }

    private var dateSetListener =
        DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            binding.date.text = "$dayOfMonth-${month + 1}-$year"
            dateNeeded = "$year-${month + 1}-$dayOfMonth"
        }
}