package com.chronoscoper.android.classschedule2.util

@Suppress("UNCHECKED_CAST")
inline fun <reified T> Array<T>.removedAt(index: Int): Array<T> {
    val list = mutableListOf<T>()
    forEachIndexed { i, element ->
        if (i != index) {
            list.add(element)
        }
    }
    val result = arrayOfNulls<T>(list.size)
    list.forEachIndexed { i, t ->
        result[i] = t
    }
    return result as Array<T>
}
