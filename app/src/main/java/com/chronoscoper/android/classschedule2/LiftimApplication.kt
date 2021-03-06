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
package com.chronoscoper.android.classschedule2

import android.os.Build
import android.preference.PreferenceManager
import android.support.multidex.MultiDexApplication
import com.chronoscoper.android.classschedule2.job.LiftimJobCreator
import com.chronoscoper.android.classschedule2.sync.LiftimContext
import com.chronoscoper.android.classschedule2.util.NotificationChannel
import com.chronoscoper.android.classschedule2.util.div
import com.evernote.android.job.JobManager
import com.squareup.leakcanary.LeakCanary

class LiftimApplication : MultiDexApplication() {
    companion object {
        private const val TAG = "Application"
        const val REGISTER_TMP_NAME = "register_tmp.json"
    }

    override fun onCreate() {
        super.onCreate()

        JobManager.create(this).addJobCreator(LiftimJobCreator())
        initEnvironment()
        migrateIfNeeded()
        registerNotificationChannelIfNeeded()
    }

    override fun onTerminate() {
        super.onTerminate()
        (cacheDir / REGISTER_TMP_NAME).delete()
    }

    private val sharedPrefs by lazy { PreferenceManager.getDefaultSharedPreferences(this) }

    fun initEnvironment() {
        LiftimContext.init(
                this, sharedPrefs.getString(getString(R.string.p_sync_url),
                "http://example.com/"),
                sharedPrefs.getLong(getString(R.string.p_default_liftim_code), 0),
                sharedPrefs.getString(getString(R.string.p_account_token), ""))
        if (!LeakCanary.isInAnalyzerProcess(this)) {
            LeakCanary.install(this)
        }
    }

    private fun migrateIfNeeded() {
        val lastUsedVersion = sharedPrefs.getInt(getString(R.string.p_last_used_version), 0)
        if (lastUsedVersion != BuildConfig.VERSION_CODE) {
            sharedPrefs.edit()
                    .putInt(getString(R.string.p_last_used_version), BuildConfig.VERSION_CODE)
                    .apply()
        }
    }

    private fun registerNotificationChannelIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel.register(this)
        }
    }
}
