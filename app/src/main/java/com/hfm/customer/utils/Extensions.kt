package com.hfm.customer.utils

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.text.DecimalFormat
import java.util.regex.Matcher
import java.util.regex.Pattern

const val netWorkFailure = "Network Failure"
const val business = "BUSINESS"
const val normal = "NORMAL"
val cartCount = MutableLiveData<Int>()

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


fun Fragment.showToastLong(msg: String) {
    Toast.makeText(this.requireContext(), msg, Toast.LENGTH_LONG).show()
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

fun replaceBaseUrl(originalUrl: String): String {
    return originalUrl.replace(
        "https://uat.hfm.synuos.com",
        "http://4.194.191.242"
    )
}

fun formatToTwoDecimalPlaces(number: Double): String {
    val df = DecimalFormat("0.00")
    return df.format(number)
}

fun extractYouTubeVideoId(youtubeUrl: String): String? {
    // Regex pattern to match YouTube video IDs
    val pattern = Regex("""(?:youtube\.com/.*[?&]v=|youtu\.be/|youtube\.com/embed/)([^&?/]+)""")

    val matchResult = pattern.find(youtubeUrl)

    return matchResult?.groupValues?.get(1)
}


fun containsSensitiveWords(message: String): Boolean {
    val sensitiveWords = listOf(
        "islam", "muslim", "buddhist", "buddha", "christian", "jesus", "hindu",
        "taoism", "judaism", "folk religion", "tuhan", "god", "allah",
        "allahu akbar", "church", "pray", "satan", "temple", "mosque", "masjid",
        "sembayang", "shaitan", "syaitan", "setan", "malay", "chinese", "indian",
        "kadazan", "iban", "negro", "pendatang", "gay", "lgbt", "lesbian", "bisexual",
        "transgender", "homosexual", "intersex", "maknyah", "mak", "nyah", "pondan",
        "bapuk", "darai", "ahqua", "terrorist", "violent", "extremist", "elections",
        "riot", "war", "bomb", "rally", "government", "kerajaan", "communist",
        "komunisme", "communism", "politik", "gun", "weapon", "riffle", "bullet",
        "assassinate", "taliban", "isis", "isil", "feminist", "sexist", "porn",
        "horny", "kiss", "nude", "ableism", "suicide", "death", "mati", "slut",
        "anjing", "babi", "bitch", "whore", "jalang", "pukima", "bangsat", "babi setan",
        "pundek", "cunt", "bastard", "idiot", "fuck", "coward", "stupid", "dumb",
        "bloody", "rubbish", "useless", "bodoh", "brainless", "tahi", "shit", "turd",
        "phone", "email", "contact", "bank", "account", "account number", "phone number"
    )

    val wordsInMessage = message.split("\\s+".toRegex())
    for (word in wordsInMessage) {
        if (sensitiveWords.contains(word.lowercase())) {
            return true // Message contains a sensitive word
        }
    }
    return false // Message does not contain any sensitive word
}


fun deductFromWallet(walletAmount: Double, cartAmount: Double): Pair<Double, Double> {
    var newWalletAmount = walletAmount
    var newCartAmount = cartAmount

    if (newWalletAmount >= newCartAmount) {
        newWalletAmount -= newCartAmount
        newCartAmount = 0.00
    } else {
        newCartAmount -= newWalletAmount
        newWalletAmount = 0.00
    }

    return Pair(newWalletAmount, newCartAmount)
}


fun bitmapFromVector(context: Context, vectorResId: Int): BitmapDescriptor {
    val vectorDrawable = ContextCompat.getDrawable(context, vectorResId)
    vectorDrawable!!.setBounds(0, 0, vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight)
    val bitmap = Bitmap.createBitmap(
        vectorDrawable.intrinsicWidth,
        vectorDrawable.intrinsicHeight,
        Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    vectorDrawable.draw(canvas)
    return BitmapDescriptorFactory.fromBitmap(bitmap)
}

fun createFileFromContentUri(activity: Activity, fileUri: Uri): File {
    val fileName = getFileNameFromUri(activity, fileUri)
    val outputFile = File(activity.cacheDir, fileName)

    val iStream: InputStream = activity.contentResolver.openInputStream(fileUri) ?: throw NullPointerException("Failed to open input stream")

    copyStreamToFile(iStream, outputFile)
    iStream.close()

    return outputFile
}

fun getFileNameFromUri(activity: Activity, fileUri: Uri): String {
    activity.contentResolver.query(fileUri, null, null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (cursor.moveToFirst()) {
            return cursor.getString(nameIndex)
        }
    }

    return "file"
}

fun copyStreamToFile(inputStream: InputStream, outputFile: File) {
    FileOutputStream(outputFile).use { output ->
        val buffer = ByteArray(4 * 1024) // buffer size
        var byteCount: Int
        while (inputStream.read(buffer).also { byteCount = it } != -1) {
            output.write(buffer, 0, byteCount)
        }
        output.flush()
    }
}