/*
 * Copyright 2017-2018 Chronoscope
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.chronoscoper.android.classschedule2.util

import com.chronoscoper.android.classschedule2.BuildConfig
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import java.util.*

object DateTimeUtils {
    fun getParsedDateExpression(date: String?): String {
        if (date == null) {
            return ""
        }
        val dateTime: DateTime
        try {
            val tokenizer = StringTokenizer(date, "/")
            val separated = arrayListOf<String>()
            while (tokenizer.hasMoreTokens()) {
                separated.add(tokenizer.nextToken())
            }
            if (separated.size != 3) {
                return date
            }
            val format = StringBuilder()
            if (separated[0].length == 4) {
                format.append("yyyy")
            } else {
                return date
            }
            format.append("/")
            when {
                separated[1].length == 1 -> format.append("M")
                separated[1].length == 2 -> format.append("MM")
                else -> return date
            }
            format.append("/")
            when {
                separated[2].length == 1 -> format.append("d")
                separated[2].length == 2 -> format.append("dd")
                else -> return date
            }

            dateTime = DateTime.parse(date, DateTimeFormat.forPattern(format.toString()))

        } catch (e: Exception) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace()
            }
            return date
        }
        return dateTime.toString(DateTimeFormat.fullDate())
    }
}