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

import android.content.Context
import android.preference.PreferenceManager
import com.chronoscoper.android.classschedule2.R
import com.chronoscoper.android.classschedule2.sync.LiftimSyncEnvironment

class TokenReloadTask(val context: Context) : Runnable {
    override fun run() {
        val response = LiftimSyncEnvironment.getLiftimService()
                .getToken(LiftimSyncEnvironment.getToken())
                .execute()
        if (!response.isSuccessful) {
            return
        }
        val body = response.body() ?: return
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putString(context.getString(R.string.p_account_token), body.token)
                .apply()
        LiftimSyncEnvironment.setToken(body.token)
    }
}
