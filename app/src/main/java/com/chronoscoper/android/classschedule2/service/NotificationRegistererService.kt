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

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import com.chronoscoper.android.classschedule2.sync.Info
import com.chronoscoper.android.classschedule2.sync.LiftimContext
import com.chronoscoper.android.classschedule2.util.DateTimeUtils
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

class NotificationRegistererService : Service() {
    companion object {
        private const val TAG = "NotificationRegisterer"
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        // Notification is published 7 o'clock by default.
        val baseDate = DateTime.now().withTime(7, 0, 0, 0)

        LiftimContext.getOrmaDatabase().selectFromInfo()
                .typeNotEq(Info.TYPE_TIMETABLE)
                .dateEq(DateTimeUtils.getToday())
                .filterNotNull()
                .forEachIndexed { index, info ->
                    val registeredTime = info.time
                    var notificationDateTime = baseDate
                    if (!registeredTime.isNullOrEmpty()) {
                        try {
                            val registered = DateTime.parse(registeredTime, DateTimeFormat.forPattern("HH:mm"))
                            notificationDateTime = notificationDateTime.withTime(
                                    registered.hourOfDay, registered.minuteOfHour, 0, 0)
                        } catch (e: Exception) {
                            Log.e(TAG, "Error occurred while parsing date. Using default time(7:00).")
                        }
                    }
                    val pendingIntent = PendingIntent.getService(this, index,
                            NotificationPublishService.createPublisherIntent(this,
                                    info.liftimCode, info.id),
                            PendingIntent.FLAG_CANCEL_CURRENT)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                                notificationDateTime.millis, pendingIntent)
                    } else {
                        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                                notificationDateTime.millis, pendingIntent)
                    }
                }
        return START_NOT_STICKY
    }
}