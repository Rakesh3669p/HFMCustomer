package com.hfm.customer.utils

import android.os.Handler
import android.widget.ImageView

import android.widget.TextView
import java.util.Locale


class CountDownRunnable(
    handler: Handler,
    holder: TextView,
    millisUntilFinished: Long

) :
    Runnable {
    private var millisUntilFinished: Long = 40000
    var holder: TextView
    private var handler: Handler

    init {
        this.handler = handler
        this.holder = holder
        this.millisUntilFinished = millisUntilFinished
    }

    override fun run() {


        val days = millisUntilFinished / (1000 * 60 * 60 * 24)
        val hours = (millisUntilFinished % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60)
        val minutes = (millisUntilFinished % (1000 * 60 * 60)) / (1000 * 60)
        val seconds = (millisUntilFinished % (1000 * 60)) / 1000

        val newCountdownText = "Flash Deals Ends In: ${
            String.format(
                Locale.getDefault(),
                "%02d:%02d:%02d:%02d",
                days,
                hours,
                minutes,
                seconds
            )
        }"

        holder.text = newCountdownText
        millisUntilFinished -= 1000
        handler.postDelayed(this, 1000)
    }
}