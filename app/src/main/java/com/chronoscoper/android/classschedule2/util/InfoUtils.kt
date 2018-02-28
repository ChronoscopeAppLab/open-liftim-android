/*
 * Copyright 2018 Chronoscope
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

import com.chronoscoper.android.classschedule2.sync.Info
import com.chronoscoper.android.classschedule2.sync.LiftimContext
import org.joda.time.DateTime

fun optimizeInfo() {
    val db = LiftimContext.getOrmaDatabase()
    if (DateTime.now().withTime(17, 0, 0, 0).isBeforeNow) {
        db.updateInfo().typeEq(Info.TYPE_TIMETABLE)
                .dateIsNotNull()
                .dateLe(DateTime.now().toString("yyyy/MM/dd"))
                .deleted(true)
                .execute()
    }
    db.updateInfo()
            .dateIsNotNull()
            .dateLt(DateTime.now().toString("yyyy/MM/dd"))
            .deleted(true)
            .execute()
}
