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

import android.util.Log
import com.chronoscoper.android.classschedule2.sync.LiftimContext
import com.chronoscoper.android.classschedule2.task.FullSyncTask
import com.chronoscoper.android.classschedule2.task.TokenReloadTask
import com.evernote.android.job.Job
import java.io.IOException

class UpdateAccountInfoJob : Job() {
    companion object {
        const val TAG = "UpdateAccountInfoJob"
    }

    override fun onRunJob(params: Params): Result {
        val db = LiftimContext.getOrmaDatabase()
        if (db.selectFromLiftimCodeInfo().count() > 0) {
            try {
                TokenReloadTask(context).run()
                FullSyncTask(context).run()
            } catch (e: IOException) {
                Log.e(TAG, "Error in account info updater", e)
            }
        }
        return Result.SUCCESS
    }
}
