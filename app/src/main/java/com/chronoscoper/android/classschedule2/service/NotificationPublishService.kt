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
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import com.chronoscoper.android.classschedule2.R
import com.chronoscoper.android.classschedule2.home.info.detail.ViewInfoActivity
import com.chronoscoper.android.classschedule2.sync.LiftimContext
import com.chronoscoper.android.classschedule2.util.NotificationChannel

class NotificationPublishService : Service() {
    companion object {
        private const val LIFTIM_CODE = "info_liftim_code"
        private const val ID = "info_id"

        fun createPublisherIntent(context: Context, liftimCode: Long, infoId: String): Intent {
            val result = Intent(context, NotificationPublishService::class.java)
            result.apply {
                putExtra(LIFTIM_CODE, liftimCode)
                putExtra(ID, infoId)
            }
            return result
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent ?: return START_NOT_STICKY
        val liftimCode = intent.getLongExtra(LIFTIM_CODE, -1)
        if (liftimCode < 0) {
            return START_NOT_STICKY
        }
        val id = intent.getStringExtra(ID) ?: return START_NOT_STICKY
        val info = LiftimContext.getOrmaDatabase().selectFromInfo()
                .liftimCodeEq(liftimCode)
                .idEq(id)
                .firstOrNull() ?: return START_NOT_STICKY
        val notificationBuilder = NotificationCompat.Builder(
                this, NotificationChannel.getChannelIdByType(info.type))
                .setContentTitle(info.title)
        notificationBuilder.setSmallIcon(R.drawable.ic_notification)
        notificationBuilder.color = ContextCompat.getColor(this, R.color.colorPrimary)
        if (!info.detail.isNullOrEmpty()) {
            notificationBuilder.setContentText(info.detail)
        }
        val pendingIntent = PendingIntent.getActivity(this, startId,
                ViewInfoActivity.createIntent(this, info),
                PendingIntent.FLAG_CANCEL_CURRENT)
        notificationBuilder.setContentIntent(pendingIntent)
        notificationBuilder.setAutoCancel(true)
        notificationBuilder.setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
        val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(startId, notificationBuilder.build())
        return START_NOT_STICKY
    }
}
