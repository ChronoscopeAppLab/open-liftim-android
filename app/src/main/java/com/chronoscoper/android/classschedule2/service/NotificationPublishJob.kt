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

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import com.chronoscoper.android.classschedule2.R
import com.chronoscoper.android.classschedule2.home.info.detail.ViewInfoActivity
import com.chronoscoper.android.classschedule2.sync.LiftimContext
import com.chronoscoper.android.classschedule2.util.NotificationChannel
import com.evernote.android.job.Job

class NotificationPublishJob : Job() {
    companion object {
        const val TAG = "NotificationPublish"
        const val LIFTIM_CODE = "info_liftim_code"
        const val ID = "info_id"
        const val TIME_SPECIFIED = "time_specified"
        const val GROUP_KEY = "liftim_event"

        private var notificationId = 1
    }

    override fun onRunJob(params: Params): Result {
        val liftimCode = params.extras.getLong(LIFTIM_CODE, -1)
        if (liftimCode < 0) {
            return Result.FAILURE
        }
        val id = params.extras.getString(ID, null) ?: return Result.FAILURE
        val info = LiftimContext.getOrmaDatabase().selectFromInfo()
                .liftimCodeEq(liftimCode)
                .idEq(id)
                .firstOrNull() ?: return Result.FAILURE
        val notification = NotificationCompat.Builder(
                context, NotificationChannel.getChannelIdByType(info.type))
                .apply {
                    if (params.extras.getBoolean(TIME_SPECIFIED, false)) {
                        setContentTitle(context.getString(R.string.notification_time_specified_title))
                        setContentText(info.title)
                    } else {
                        setContentTitle(context.getString(R.string.notification_title))
                        setContentText(info.title)
                    }
                    setSmallIcon(R.drawable.ic_notification)
                    color = ContextCompat.getColor(context, R.color.colorPrimary)
                    val pendingIntent = PendingIntent.getActivity(context, notificationId,
                            ViewInfoActivity.createIntent(context, info),
                            PendingIntent.FLAG_CANCEL_CURRENT)
                    setContentIntent(pendingIntent)
                    setAutoCancel(true)
                    setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                    if (!info.detail.isNullOrEmpty()) {
                        setStyle(NotificationCompat.BigTextStyle().bigText(info.detail))
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        setGroup(GROUP_KEY)
                    }
                }
                .build()
        val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notification)
        notificationId++
        return Result.SUCCESS
    }
}
