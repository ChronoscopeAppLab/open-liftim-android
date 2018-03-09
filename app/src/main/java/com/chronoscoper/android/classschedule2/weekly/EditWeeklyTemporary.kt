package com.chronoscoper.android.classschedule2.weekly

import com.chronoscoper.android.classschedule2.sync.LiftimContext
import com.chronoscoper.android.classschedule2.sync.WeeklyItem
import java.util.LinkedList

object EditWeeklyTemporary {
    var weeklyItems: MutableList<WeeklyItem>? = null
        set(value) {
            value?.forEach {
                if (it.subjects == null) {
                    it.subjects = LiftimContext.getGson()
                            .fromJson(it.serializedSubjects, Array<String>::class.java)
                            ?: arrayOf()
                }
            }
            val result = ArrayList<WeeklyItem>(7)
            for (i in 1..7) {
                val item = value?.firstOrNull { it.dayOfWeek == i }
                        ?: WeeklyItem().apply {
                            dayOfWeek = i
                            subjects = arrayOf()
                            shortSubjects = arrayOf()
                        }
                result.add(item)
            }
            field = result
        }

    fun getMerged(liftimCode: Long): List<WeeklyItem> {
        val result = LinkedList<WeeklyItem>()
        val src = LiftimContext.getOrmaDatabase()
                .selectFromWeeklyItem().liftimCodeEq(liftimCode)
                .orderByDayOfWeekAsc()
                .toList()
        val edited = weeklyItems ?: mutableListOf()
        for (i in 1..7) {
            var added = false
            for (j in edited.indices) {
                val weeklyItem = edited[j]
                if (weeklyItem.dayOfWeek == i) {
                    edited.removeAt(j)
                    result.add(weeklyItem)
                    added = true
                    break
                }
            }
            if (!added) {
                for (j in src.indices) {
                    val weeklyItem = src[j]
                    if (weeklyItem.dayOfWeek == i) {
                        src.removeAt(j)
                        result.add(weeklyItem)
                        added = true
                        break
                    }
                }
            }
            if (!added) {
                result.add(WeeklyItem().apply {
                    this.liftimCode = liftimCode
                    dayOfWeek = i
                    minIndex = 0
                    subjects = arrayOf()
                    serializedSubjects = "[]"
                })
            }
        }
        return result
    }
}
