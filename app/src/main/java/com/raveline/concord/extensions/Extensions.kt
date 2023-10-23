package com.raveline.concord.extensions

import android.content.Context
import android.util.Log
import android.widget.Toast

fun Context.showMessage(message: String, longTime: Boolean = false) {
    Toast.makeText(
        this,
        message,
        if (longTime) {
            Toast.LENGTH_LONG
        } else {
            Toast.LENGTH_SHORT
        }
    ).show()
}

fun Context.showLog(tag:String,message: String) {
    Log.i(tag, message)
}