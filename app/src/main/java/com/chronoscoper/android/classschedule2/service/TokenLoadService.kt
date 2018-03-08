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
package com.chronoscoper.android.classschedule2.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.chronoscoper.android.classschedule2.sync.LiftimContext
import com.chronoscoper.android.classschedule2.task.TokenReloadTask
import java.io.IOException

class TokenLoadService : Service() {
    companion object {
        private const val TAG = "TokenLoadService"
    }

    override fun onBind(intent: Intent?): IBinder? = null
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        LiftimContext.executeBackground {
            try {
                TokenReloadTask(this).run()
            } catch (e: IOException) {
                Log.e(TAG, "Error while loading token.", e)
            }
        }
        return START_NOT_STICKY
    }
}