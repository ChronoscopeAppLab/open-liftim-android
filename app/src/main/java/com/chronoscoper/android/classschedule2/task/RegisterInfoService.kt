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
package com.chronoscoper.android.classschedule2.task

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import com.chronoscoper.android.classschedule2.sync.LiftimContext

class RegisterInfoService : Service() {
    companion object {
        private const val CONTENT = "content"
        fun start(context: Context, content: String) {
            context.startService(Intent(context, RegisterInfoService::class.java)
                    .putExtra(CONTENT, content))
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val content = intent?.getStringExtra(CONTENT) ?: return START_NOT_STICKY
        LiftimContext.executeBackground {
            try {
                LiftimContext.getLiftimService()
                        .registerInfo(LiftimContext.getLiftimCode(),
                                LiftimContext.getToken(), content)
                        .execute()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return START_NOT_STICKY
    }
}
