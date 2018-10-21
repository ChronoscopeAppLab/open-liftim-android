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
package com.chronoscoper.android.classschedule2.job

import com.chronoscoper.android.classschedule2.service.NotificationPublishJob
import com.chronoscoper.android.classschedule2.service.NotificationRegistererJob
import com.evernote.android.job.Job
import com.evernote.android.job.JobCreator

class LiftimJobCreator : JobCreator {
    override fun create(tag: String): Job? {
        if (tag != UpdateAccountInfoJob.TAG) return null
        return when (tag) {
            UpdateAccountInfoJob.TAG -> UpdateAccountInfoJob()
            NotificationPublishJob.TAG -> NotificationPublishJob()
            NotificationRegistererJob.TAG -> NotificationRegistererJob()
            else -> null
        }
    }
}
