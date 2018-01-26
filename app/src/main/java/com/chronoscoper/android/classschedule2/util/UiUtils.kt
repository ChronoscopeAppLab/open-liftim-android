package com.chronoscoper.android.classschedule2.util

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.support.annotation.ColorInt
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.TransitionOptions
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.chronoscoper.android.classschedule2.R
import com.chronoscoper.android.classschedule2.sync.Info
import com.chronoscoper.android.classschedule2.view.ProgressiveFadeInFactory

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

fun progressiveFadeInTransition(): TransitionOptions<DrawableTransitionOptions, Drawable> {
    return DrawableTransitionOptions.with(ProgressiveFadeInFactory())
}

@ColorInt
fun getColorForInfoType(type: Int): Int {
    return when (type) {
        Info.TYPE_UNSPECIFIED -> {
            -0x777778
        }
        Info.TYPE_EVENT -> {
            -0x6800
        }
        Info.TYPE_INFORMATION -> {
            -0xde690d
        }
        Info.TYPE_SUBMISSION -> {
            -0xb350b0
        }
        Info.TYPE_LOCAL_MEMO -> {
            -0x98c549
        }
        else -> {
            Color.BLACK
        }
    }
}

@ColorInt
fun getDarkerColor(source: Int): Int {
    val hsv = FloatArray(3)
    Color.colorToHSV(source, hsv)
    hsv[2] *= 0.8f
    return Color.HSVToColor(hsv)
}
