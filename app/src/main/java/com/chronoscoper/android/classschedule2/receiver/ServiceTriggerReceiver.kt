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
package com.chronoscoper.android.classschedule2.receiver

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.chronoscoper.android.classschedule2.service.NotificationRegistererService
import com.chronoscoper.android.classschedule2.util.DateTimeUtils

class ServiceTriggerReceiver : BroadcastReceiver() {
    companion object {
        private const val REQUEST_NOTIFICATION_REGISTERER = 101
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        context ?: return
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        scheduleNotificationRegisterer(context, alarmManager)
    }

    private fun scheduleNotificationRegisterer(context: Context, alarmManager: AlarmManager) {
        val pendingIntent = PendingIntent.getService(context, REQUEST_NOTIFICATION_REGISTERER,
                Intent(context, NotificationRegistererService::class.java),
                PendingIntent.FLAG_CANCEL_CURRENT)
        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME,
                DateTimeUtils.getTomorrowPosixTime(),
                AlarmManager.INTERVAL_DAY, pendingIntent)
    }
}
