package com.chronoscoper.android.classschedule2.util

import android.annotation.SuppressLint
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import com.chronoscoper.android.classschedule2.R

@SuppressLint("InflateParams")
fun showToast(context: Context, text: CharSequence, duration: Int) {
    val toast = Toast(context)
    val view = LayoutInflater.from(context).inflate(R.layout.toast, null, false)
    (view.findViewById<TextView>(R.id.text)).text = text
    toast.view = view
    toast.setGravity(Gravity.BOTTOM or Gravity.FILL_HORIZONTAL, 0, 0)
    toast.duration = duration
    toast.show()
}
