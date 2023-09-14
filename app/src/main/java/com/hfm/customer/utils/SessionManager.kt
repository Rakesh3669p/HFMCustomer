package com.hfm.customer.utils

import android.content.Context
import android.content.SharedPreferences


class SessionManager(mcxt: Context) {
    companion object {
        val PREF_GENERAL = "PREF_GENERAL"
        const val KEY_TOKEN = "token"
        const val KEY_DEVICE_TOKEN = "deviceToken"
        const val KEY_IS_LOGIN = "KEY_IS_LOGIN"
        const val PUSH_NOTIFICATION = "PUSH_NOTIFICATION"

        const val USER_ID = "USER_ID"
        const val USER_PHONE = "USER_PHONE"
        const val USER_EMAIL = "USER_EMAIL"
        const val USER_IMAGE = "USER_IMAGE"
        const val USER_NAME = "USER_NAME"
        const val USER_DISPLAY_NAME = "USER_DISPLAY_NAME"
    }


    var generalEditor: SharedPreferences.Editor
    var generalPref: SharedPreferences

    private var PRIVATE_MODE = 0

    init {
        generalPref = mcxt.getSharedPreferences(PREF_GENERAL, PRIVATE_MODE)
        generalEditor = generalPref.edit()
    }

    var isLogin: Boolean
        get() = generalPref.getBoolean(KEY_IS_LOGIN, false)
        set(status) {
            generalEditor.putBoolean(KEY_IS_LOGIN, status)
            generalEditor.commit()
        }


    var token: String
        get() = generalPref.getString(KEY_TOKEN, "").toString()
        set(token) {
            generalEditor.putString(KEY_TOKEN, token)
            generalEditor.commit()
        }



  var showNotification: Boolean
        get() = generalPref.getBoolean(PUSH_NOTIFICATION, false)
        set(status) {
            generalEditor.putBoolean(PUSH_NOTIFICATION, status)
            generalEditor.commit()
        }


    var userId: Int
        get() = generalPref.getInt(USER_ID, 0)
        set(status) {
            generalEditor.putInt(USER_ID, status)
            generalEditor.commit()
        }

    var userPhone: String
        get() = generalPref.getString(USER_PHONE, "").toString()
        set(status) {
            generalEditor.putString(USER_PHONE, status)
            generalEditor.commit()
        }

    var userEmail: String
        get() = generalPref.getString(USER_EMAIL, "").toString()
        set(status) {
            generalEditor.putString(USER_EMAIL, status)
            generalEditor.commit()
        }

    var userName: String
        get() = generalPref.getString(USER_NAME, "").toString()
        set(status) {
            generalEditor.putString(USER_NAME, status)
            generalEditor.commit()
        }

    var displayName: String
        get() = generalPref.getString(USER_DISPLAY_NAME, "").toString()
        set(status) {
            generalEditor.putString(USER_DISPLAY_NAME, status)
            generalEditor.commit()
        }

    var userImage: String
        get() = generalPref.getString(USER_IMAGE, "").toString()
        set(status) {
            generalEditor.putString(USER_IMAGE, status)
            generalEditor.commit()
        }

}