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
package com.chronoscoper.android.classschedule2.task

import com.chronoscoper.android.classschedule2.sync.LiftimSyncEnvironment
import java.io.IOException

class WeeklyLoader(private val liftimCode: Long, private val token: String) : Runnable {
    override fun run() {
        val data = try {
            val response = LiftimSyncEnvironment.getLiftimService()
                    .getWeekly(liftimCode, token).execute()
            if (!response.isSuccessful) return
            response.body() ?: return
        } catch (e: IOException) {
            return
        }
        LiftimSyncEnvironment.getOrmaDatabase().deleteFromWeeklyItem()
                .liftimCodeEq(liftimCode)
                .execute()
        val inserter = LiftimSyncEnvironment.getOrmaDatabase().prepareInsertIntoWeeklyItem()
        data.forEach { item ->
            item.value.apply {
                liftimCode = this@WeeklyLoader.liftimCode
                dayOfWeek = item.key.toIntOrNull() ?: 0
                serializedSubjects = LiftimSyncEnvironment.getGson().toJson(subjects)
            }
            if (item.value.dayOfWeek in 1..7) {
                inserter.execute(item.value)
            }
        }
    }
}
