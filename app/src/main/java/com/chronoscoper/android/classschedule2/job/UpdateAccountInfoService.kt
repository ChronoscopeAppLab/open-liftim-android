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

import android.app.job.JobParameters
import android.app.job.JobService
import android.util.Log
import com.chronoscoper.android.classschedule2.sync.LiftimContext
import com.chronoscoper.android.classschedule2.task.FullSyncTask
import com.chronoscoper.android.classschedule2.task.TokenReloadTask
import java.io.IOException

class UpdateAccountInfoService : JobService() {
    companion object {
        private const val TAG = "AccountInfoUpdater"
    }

    override fun onStopJob(params: JobParameters?): Boolean = false

    override fun onStartJob(params: JobParameters?): Boolean {
        val db = LiftimContext.getOrmaDatabase()
        if (db.selectFromLiftimCodeInfo().count() > 0) {
            LiftimContext.executeBackground {
                try {
                    TokenReloadTask(this).run()
                    FullSyncTask(this).run()
                } catch (e: IOException) {
                    Log.e(TAG, "Error in account info updater", e)
                }
            }
        }
        return true
    }
}
