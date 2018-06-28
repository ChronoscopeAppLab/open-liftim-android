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

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import com.chronoscoper.android.classschedule2.job.UpdateAccountInfoService

class ServiceTriggerReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return
        val action = intent.action
        if (action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            val jobBuilder = JobInfo.Builder(1,
                    ComponentName(context, UpdateAccountInfoService::class.java))
                    // Although the javadoc says that's defaulted to false, it seems that
                    // it's better to set `false' explicitly, probably because of Android system's
                    // cache(in previous version, we set it to `true'.)
                    .setRequiresCharging(false)
                    .setPersisted(true)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                jobBuilder.setPeriodic(1000 * 60 * 60 * 24, 1000 * 60 * 60 * 5)
            } else {
                jobBuilder.setPeriodic(1000 * 60 * 60 * 24)
            }
            val scheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
            scheduler.schedule(jobBuilder.build())
        }
    }
}
