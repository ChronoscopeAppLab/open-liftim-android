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

import com.chronoscoper.android.classschedule2.sync.Info
import com.chronoscoper.android.classschedule2.sync.LiftimContext
import com.chronoscoper.android.classschedule2.task.InfoLoader.Companion.nextCursor

class IncrementalInfoLoader(private val liftimCode: Long, private val token: String) {
    fun execute(): List<Info>? {
        val response = LiftimContext.getLiftimService()
                .getInfo(liftimCode, token, nextCursor)
                .execute()
        if (!response.isSuccessful) {
            return null
        }
        val info = response.body() ?: return null
        nextCursor = info.nextCursor
        val data = arrayListOf<Info>()
        info.info.forEach {
            data.add(Info(liftimCode, it.id, it.title, it.detail, it.weight, it.date,
                    it.time, it.link, it.type, it.timetable?.toString(), it.removable, Info.REMOTE))
        }
        return data
    }
}
