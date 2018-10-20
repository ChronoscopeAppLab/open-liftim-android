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
package com.chronoscoper.android.classschedule2.service

import android.util.Log
import com.chronoscoper.android.classschedule2.sync.Info
import com.chronoscoper.android.classschedule2.sync.LiftimContext
import com.chronoscoper.android.classschedule2.util.DateTimeUtils
import com.evernote.android.job.Job
import com.evernote.android.job.JobRequest
import com.evernote.android.job.util.support.PersistableBundleCompat
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

class NotificationRegistererJob : Job() {
    companion object {
        const val TAG = "NotificationRegisterer"
    }

    override fun onRunJob(params: Params): Result {
        // Notification is published 7 o'clock by default.
        val baseDate = DateTime.now().withTime(7, 0, 0, 0)

        LiftimContext.getOrmaDatabase().selectFromInfo()
                .dateEq(DateTimeUtils.getToday())
                .filterNotNull()
                .forEachIndexed { index, info ->
                    val registeredTime = info.time
                    var notificationDateTime = baseDate
                    var timeSpecified = false
                    if (!registeredTime.isNullOrEmpty()) {
                        try {
                            val registered = DateTime.parse(registeredTime, DateTimeFormat.forPattern("HH:mm"))
                            notificationDateTime = notificationDateTime.withTime(
                                    registered.hourOfDay, registered.minuteOfHour, 0, 0)
                            notificationDateTime.minusMinutes(15)
                            timeSpecified = true
                        } catch (e: Exception) {
                            Log.e(TAG, "Error occurred while parsing date. Using default time(7:00).")
                        }
                    }
                    val misc = PersistableBundleCompat()
                            .apply {
                                putString(NotificationPublishJob.ID, info.id)
                                putLong(NotificationPublishJob.LIFTIM_CODE, info.liftimCode)
                                putBoolean(NotificationPublishJob.TIME_SPECIFIED, timeSpecified)
                            }
                    JobRequest.Builder(NotificationPublishJob.TAG)
                            .setExact(notificationDateTime.millis - DateTime.now().millis)
                            .setExtras(misc)
                            .build()
                }
        return Result.SUCCESS
    }
}