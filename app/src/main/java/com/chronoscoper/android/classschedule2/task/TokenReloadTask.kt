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

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.preference.PreferenceManager
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import com.chronoscoper.android.classschedule2.R
import com.chronoscoper.android.classschedule2.ReLoginActivity
import com.chronoscoper.android.classschedule2.sync.LiftimContext
import com.chronoscoper.android.classschedule2.util.NotificationChannel

class TokenReloadTask(val context: Context) : Runnable {
    companion object {
        private const val NOTIFICATION_ID = 20
    }

    override fun run() {
        val response = LiftimContext.getLiftimService()
                .getToken(LiftimContext.getToken())
                .execute()
        if (!response.isSuccessful) {
            if (response.code() == 401) {
                showLoginError()
            }
            return
        }
        val body = response.body() ?: return
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putString(context.getString(R.string.p_account_token), body.token)
                .apply()
        LiftimContext.setToken(body.token)
    }

    private fun showLoginError() {
        val intent = PendingIntent.getActivity(context, 1,
                Intent(context, ReLoginActivity::class.java), PendingIntent.FLAG_CANCEL_CURRENT)
        val notification = NotificationCompat.Builder(context, NotificationChannel.ID_MISC)
                .setSmallIcon(R.drawable.ic_notification)
                .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setContentTitle(context.getString(R.string.login_failed))
                .setContentText(context.getString(R.string.login_error_description))
                .setContentIntent(intent)
                .setAutoCancel(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .build()
        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .notify(NOTIFICATION_ID, notification)
    }
}
