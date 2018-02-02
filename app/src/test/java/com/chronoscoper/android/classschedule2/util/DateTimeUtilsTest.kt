package com.chronoscoper.android.classschedule2.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class DateTimeUtilsTest {
    @Test
    fun getToday() {
        val calendar = Calendar.getInstance(TimeZone.getDefault())
        val expected = String.format("%04d/%02d/%02d",
                calendar[Calendar.YEAR],
                calendar[Calendar.MONTH] + 1,
                calendar[Calendar.DAY_OF_MONTH])
        assertEquals(expected, DateTimeUtils.getToday())
    }
}
