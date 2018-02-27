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
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.annotation.RequiresApi
import com.chronoscoper.android.classschedule2.service.NotificationRegistererService
import com.chronoscoper.android.classschedule2.service.UserInfoSyncJobService
import com.chronoscoper.android.classschedule2.service.UserInfoSyncService
import com.chronoscoper.android.classschedule2.util.DateTimeUtils

class ServiceTriggerReceiver : BroadcastReceiver() {
    companion object {
        private const val REQUEST_NOTIFICATION_REGISTERER = 101
        private const val REQUEST_UPDATE_USER_INFO = 102
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return
        val action = intent.action
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP
                && action == Intent.ACTION_BOOT_COMPLETED
                || action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            schedule(context, alarmManager)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                && action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            scheduleV21(context)
        }
    }

    private fun schedule(context: Context, alarmManager: AlarmManager) {
        // Register service to publish notification on Info
        val notificationPublish = PendingIntent.getService(context, REQUEST_NOTIFICATION_REGISTERER,
                Intent(context, NotificationRegistererService::class.java),
                PendingIntent.FLAG_CANCEL_CURRENT)
        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME,
                DateTimeUtils.getTomorrowPosixTime(),
                AlarmManager.INTERVAL_DAY, notificationPublish)

        // Register service to update user info
        val userInfo = PendingIntent.getService(context, REQUEST_UPDATE_USER_INFO,
                Intent(context, UserInfoSyncService::class.java), PendingIntent.FLAG_CANCEL_CURRENT)
        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, 0,
                AlarmManager.INTERVAL_DAY, userInfo)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun scheduleV21(context: Context) {
        val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler

        if (!isScheduled(jobScheduler, REQUEST_NOTIFICATION_REGISTERER)) {
            // TODO: Schedule notification publishing
        }

        if (!isScheduled(jobScheduler, REQUEST_UPDATE_USER_INFO)){
            val userInfoSync = JobInfo.Builder(REQUEST_NOTIFICATION_REGISTERER,
                    ComponentName(context, UserInfoSyncJobService::class.java))
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
                    .setRequiresCharging(true)
                    .setPersisted(true)
                    .setPeriodic(1000 * 60 * 60 * 24)
                    .build()
            jobScheduler.schedule(userInfoSync)
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun isScheduled(jobScheduler: JobScheduler, jobId: Int): Boolean {
        jobScheduler.allPendingJobs.forEach {
            if (it.id == jobId) return true
        }
        return false
    }
}
