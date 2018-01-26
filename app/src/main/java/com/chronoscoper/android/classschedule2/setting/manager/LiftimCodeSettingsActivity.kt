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
package com.chronoscoper.android.classschedule2.setting.manager

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.chronoscoper.android.classschedule2.BaseActivity
import com.chronoscoper.android.classschedule2.R

class LiftimCodeSettingsActivity : BaseActivity() {
    companion object {
        private const val EXTRA_LIFTIM_CODE = "liftim_code"
        fun start(context: Context, liftimCode: Long) {
            context.startActivity(Intent(context, LiftimCodeSettingsActivity::class.java)
                    .putExtra(EXTRA_LIFTIM_CODE, liftimCode))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val liftimCode = intent.getLongExtra(EXTRA_LIFTIM_CODE, -1)
        if (liftimCode < 0) {
            finish()
            return
        }
        setContentView(R.layout.activity_liftim_code_settings)

        //TODO: Add more implementation here..
    }
}
