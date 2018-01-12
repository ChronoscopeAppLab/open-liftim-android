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
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import com.chronoscoper.android.classschedule2.R
import com.chronoscoper.android.classschedule2.sync.LiftimSyncEnvironment

class RegisterInfoService : Service() {
    companion object {
        private const val CONTENT = "content"
        fun start(context: Context, content: String) {
            context.startService(Intent(context, RegisterInfoService::class.java)
                    .putExtra(CONTENT, content))
        }
    }

    override fun onBind(p0: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val content = intent?.getStringExtra(CONTENT)
                ?: kotlin.run { stopSelf(); return START_STICKY }
        object : Thread() {
            override fun run() {
                val notification = NotificationCompat.Builder(
                        this@RegisterInfoService, "").apply {
                    color = ContextCompat.getColor(this@RegisterInfoService,
                            R.color.colorPrimaryDarkText)
                    setContentTitle(getString(R.string.register_progress))
                }.build()
                startForeground(1, notification)
                try {
                    LiftimSyncEnvironment.getLiftimService()
                            .registerInfo(LiftimSyncEnvironment.getLiftimCode(),
                                    LiftimSyncEnvironment.getToken(), content)
                            .execute()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                stopForeground(true)
                stopSelf()
            }
        }.start()

        return START_STICKY
    }
}
