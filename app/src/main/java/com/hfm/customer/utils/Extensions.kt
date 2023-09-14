package com.hfm.customer.utils

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.IOException
import java.util.regex.Matcher
import java.util.regex.Pattern

const val netWorkFailure = "Network Failure"
const val business = "BUSINESS"
const val normal = "NORMAL"

fun checkResponseBody(body: Any?): Any? = body?.let { it }

fun checkThrowable(t: Throwable): String {
    return when (t) {
        is IOException -> netWorkFailure
        else -> "Conversion Error ${t.message}"
    }
}

fun View.makeInvisible() {
    this.visibility = View.INVISIBLE
}

fun View.makeVisible() {
    this.visibility = View.VISIBLE
}

fun View.makeGone() {
    this.visibility = View.GONE
}

fun getDeviceId(context: Context): String {
    return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
}

fun getDeviceName(): String {
    val manufacturer = Build.MANUFACTURER
    val model = Build.MODEL
    return if (model.startsWith(manufacturer)) {
        model
    } else {
        "$manufacturer $model"
    }
}

fun String.isValidEmail(): Boolean {
    val regExpn = ("^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]{1}|[\\w-]{2,}))@"
            + "((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
            + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
            + "([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
            + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])){1}|"
            + "([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$")
    val inputStr: CharSequence = this
    val pattern: Pattern = Pattern.compile(regExpn, Pattern.CASE_INSENSITIVE)
    val matcher: Matcher = pattern.matcher(inputStr)
    return matcher.matches()
}


fun initRecyclerView(
    context: Context,
    recyclerView: RecyclerView,
    adapter: RecyclerView.Adapter<*>,
    horizontal: Boolean = false
) {
    recyclerView.apply {
        setHasFixedSize(true)
        layoutManager = if (horizontal) LinearLayoutManager(
            context,
            LinearLayoutManager.HORIZONTAL,
            false
        ) else LinearLayoutManager(context)
        this.adapter = adapter
    }
}

fun initRecyclerViewGrid(
    context: Context,
    recyclerView: RecyclerView,
    adapter: RecyclerView.Adapter<*>,
    count: Int
) {
    recyclerView.apply {
        setHasFixedSize(true)
        layoutManager = GridLayoutManager(context, count)
        this.adapter = adapter
    }
}


fun Fragment.showToast(msg: String) {
    Toast.makeText(this.requireContext(), msg, Toast.LENGTH_SHORT).show()
}

fun Activity.showToast(msg: String) {
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}

fun isInternetAvailable(context: Context): Boolean {
    val connectivityManager: ConnectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    return connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        .isNetworkCapabilitiesValid()
}

private fun NetworkCapabilities?.isNetworkCapabilitiesValid(): Boolean = when {
    this == null -> false
    hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) &&
            (hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    hasTransport(NetworkCapabilities.TRANSPORT_VPN) ||
                    hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) -> true

    else -> false
}