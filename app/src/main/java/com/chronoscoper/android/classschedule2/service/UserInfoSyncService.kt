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

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.preference.PreferenceManager
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import com.chronoscoper.android.classschedule2.R
import com.chronoscoper.android.classschedule2.ReLoginActivity
import com.chronoscoper.android.classschedule2.sync.LiftimContext
import com.chronoscoper.android.classschedule2.sync.Subject
import com.chronoscoper.android.classschedule2.sync.WeeklyItem
import com.chronoscoper.android.classschedule2.task.AccountInfoLoader
import com.chronoscoper.android.classschedule2.task.InvalidTokenException
import com.chronoscoper.android.classschedule2.task.SubjectLoader
import com.chronoscoper.android.classschedule2.task.WeeklyLoader
import com.chronoscoper.android.classschedule2.task.enforceValidToken
import com.chronoscoper.android.classschedule2.util.NotificationChannel
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers

class UserInfoSyncService : Service() {
    companion object {
        private const val STATE_NOT_SET_UP = 1
        private const val STATE_NOT_LOGGED_IN = 2
        private const val STATE_LIFTIM_CODE_DELETED = 3
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Observable.create<Int> {
            val prefs = PreferenceManager.getDefaultSharedPreferences(this)
            val token = prefs.getString(getString(R.string.p_account_token), null)
            if (!prefs.getBoolean(getString(R.string.p_setup_completed), false)) {
                it.onNext(STATE_NOT_SET_UP)
                it.onComplete()
                return@create
            }
            if (token == null) {
                it.onNext(STATE_NOT_LOGGED_IN)
                it.onComplete()
                return@create
            }
            try {
                enforceValidToken(token)
            } catch (e: InvalidTokenException) {
                it.onNext(STATE_NOT_LOGGED_IN)
                it.onComplete()
                return@create
            }
            val accountInfo = AccountInfoLoader(token)
                    .load()
                    ?: kotlin.run {
                        it.onError(Exception())
                        return@create
                    }
            val prefEditor = prefs.edit()
            prefEditor.apply {
                putString(getString(R.string.p_account_name), accountInfo.userName)
                putString(getString(R.string.p_account_image_file), accountInfo.imageFile)
                putString(getString(R.string.p_account_add_date), accountInfo.addDate)
                putBoolean(getString(R.string.p_account_is_available), accountInfo.isAvailable)
            }
            prefEditor.apply()
            backupAndDeleteData()
            try {
                accountInfo.liftimCodes.forEach {
                    WeeklyLoader(it.liftimCode, token).run()
                    SubjectLoader(it.liftimCode, token).run()
                }
            } catch (e: Exception) {
                restoreData()
            }
            val selectedLiftimCode = prefs.getLong(
                    getString(R.string.p_default_liftim_code), -1)
            val db = LiftimContext.getOrmaDatabase()
            if (db.selectFromLiftimCodeInfo().liftimCodeEq(selectedLiftimCode).count() <= 0) {
                val code = db.selectFromLiftimCodeInfo().firstOrNull()?.liftimCode ?: -1
                prefs.edit()
                        .putLong(getString(R.string.p_default_liftim_code), code)
                        .apply()
                it.onNext(STATE_LIFTIM_CODE_DELETED)
            }

            it.onComplete()
        }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : DisposableObserver<Int>() {
                    override fun onComplete() {
                    }

                    override fun onNext(t: Int) {
                        when (t) {
                            STATE_NOT_LOGGED_IN -> {
                                showLoginNotification()
                            }
                            STATE_LIFTIM_CODE_DELETED -> {
                                showLiftimCodeDeletedNotification()
                            }
                        }
                    }

                    override fun onError(e: Throwable) {
                    }
                })
        return START_STICKY
    }

    private var weeklyDataBackup: List<WeeklyItem>? = null
    private var subjectDataBackup: List<Subject>? = null

    private fun backupAndDeleteData() {
        val db = LiftimContext.getOrmaDatabase()
        weeklyDataBackup = db.selectFromWeeklyItem().toList()
        subjectDataBackup = db.selectFromSubject().toList()
        db.deleteFromWeeklyItem().execute()
        db.deleteFromSubject().execute()
    }

    private fun restoreData() {
        val db = LiftimContext.getOrmaDatabase()
        db.deleteFromWeeklyItem().execute()
        db.deleteFromSubject().execute()
        weeklyDataBackup?.forEach {
            db.insertIntoWeeklyItem(it)
        }
        subjectDataBackup?.forEach {
            db.insertIntoSubject(it)
        }
    }

    private fun showLoginNotification() {
        val notificationBuilder =
                NotificationCompat.Builder(this, NotificationChannel.ID_MISC)
        notificationBuilder.setSmallIcon(R.drawable.ic_notification)
        notificationBuilder.color = ContextCompat.getColor(this, R.color.colorPrimary)
        notificationBuilder.setContentTitle(getString(R.string.login_failed))
        notificationBuilder.setContentText(getString(R.string.notification_login_content))
        val pendingIntent = PendingIntent.getActivity(
                this, 1,
                Intent(this, ReLoginActivity::class.java),
                PendingIntent.FLAG_CANCEL_CURRENT)
        notificationBuilder.setContentIntent(pendingIntent)
        notificationBuilder.setAutoCancel(true)
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .notify(1, notificationBuilder.build())
    }

    private fun showLiftimCodeDeletedNotification() {
        val notificationBuilder =
                NotificationCompat.Builder(this, NotificationChannel.ID_MISC)
        notificationBuilder.setSmallIcon(R.drawable.ic_notification)
        notificationBuilder.color = ContextCompat.getColor(this, R.color.colorPrimary)
        notificationBuilder.setContentTitle(getString(R.string.liftim_code_deleted_title))
        notificationBuilder.setContentText(getString(R.string.liftim_code_deleted_content))
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .notify(1, notificationBuilder.build())
    }
}
