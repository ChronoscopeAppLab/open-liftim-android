package com.chronoscoper.android.classschedule2.util

import java.io.File

operator fun File.div(segment: String): File {
    return File(this, segment)
}