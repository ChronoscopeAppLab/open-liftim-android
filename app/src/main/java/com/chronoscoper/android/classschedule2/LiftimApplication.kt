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

import android.app.Application
import android.preference.PreferenceManager
import com.chronoscoper.android.classschedule2.sync.LiftimSyncEnvironment

class LiftimApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this)
        LiftimSyncEnvironment.init(
                this, sharedPrefs.getString(getString(R.string.p_sync_url),
                "http://example.com/"),
                sharedPrefs.getLong(getString(R.string.p_default_liftim_code), 0),
                sharedPrefs.getString(getString(R.string.p_account_token), ""))
    }
}
