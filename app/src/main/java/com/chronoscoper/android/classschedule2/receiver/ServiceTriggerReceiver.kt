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

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.chronoscoper.android.classschedule2.job.UpdateAccountInfoJob
import com.evernote.android.job.JobRequest
import java.util.concurrent.TimeUnit

class ServiceTriggerReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return
        val action = intent.action
        if (action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            JobRequest.Builder(UpdateAccountInfoJob.TAG)
                    .setPeriodic(TimeUnit.DAYS.toMillis(1), TimeUnit.MINUTES.toMillis(5))
                    .build()
                    .scheduleAsync()
        }
    }
}
