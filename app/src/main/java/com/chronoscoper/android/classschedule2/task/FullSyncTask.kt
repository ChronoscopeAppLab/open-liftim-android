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
import com.chronoscoper.android.classschedule2.sync.LiftimContext
import com.chronoscoper.android.classschedule2.sync.Subject
import com.chronoscoper.android.classschedule2.sync.WeeklyItem
import org.joda.time.DateTime
import java.io.IOException

class FullSyncTask(private val context: Context) : Runnable {
    override fun run() {
        val token = LiftimContext.getToken()
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        LiftimContext.getOrmaDatabase().transactionSync {
            val accountInfo = AccountInfoLoader(token)
                    .load()
                    ?: kotlin.run {
                        throw IOException("Error occurred while syncing account info")
                    }
            val db = LiftimContext.getOrmaDatabase()
            val prefEditor = prefs.edit()
            prefEditor.apply {
                putString(context.getString(R.string.p_account_name), accountInfo.userName)
                putString(context.getString(R.string.p_account_image_file), accountInfo.imageFile)
                putString(context.getString(R.string.p_account_add_date), accountInfo.addDate)
                putBoolean(context.getString(R.string.p_account_is_available), accountInfo.isAvailable)
            }
            prefEditor.apply()
            db.deleteFromWeeklyItem().execute()
            db.deleteFromSubject().execute()
            accountInfo.liftimCodes.forEach {
                WeeklyLoader(it.liftimCode, token).run()
                SubjectLoader(it.liftimCode, token).run()
            }
            val selectedLiftimCode =
                    prefs.getLong(context.getString(R.string.p_default_liftim_code), -1)
            if (db.selectFromLiftimCodeInfo().liftimCodeEq(selectedLiftimCode).count() <= 0) {
                val code = db.selectFromLiftimCodeInfo().firstOrNull()?.liftimCode ?: -1
                prefs.edit()
                        .putLong(context.getString(R.string.p_default_liftim_code), code)
                        .apply()
            }
            db.deleteFromColorPaletteV2()
            ColorPaletteLoader().run()
            db.selectFromSubject().forEach {
                println("${it.liftimCode} ${it.subject} ${it.color}")
            }
        }
        FunctionRestrictionLoader(context).run()
        prefs.edit().putString(context.getString(R.string.p_last_user_info_synced),
                DateTime.now().toString()).apply()
    }
}
