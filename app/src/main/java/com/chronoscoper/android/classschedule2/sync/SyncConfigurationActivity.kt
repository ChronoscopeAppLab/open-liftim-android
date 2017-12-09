/*
 * Copyright 2017 Chronoscope
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
package com.chronoscoper.android.classschedule2.sync

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.widget.Button
import android.widget.EditText
import com.chronoscoper.android.classschedule2.R
import kotterknife.bindView

class SyncConfigurationActivity : AppCompatActivity() {

    private val urlEditText by bindView<EditText>(R.id.sync_url)
    private val okButton by bindView<Button>(R.id.ok)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sync_configuration)

        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this)

        okButton.setOnClickListener {
            val url = urlEditText.text.toString()
            val regex = "^https?://[a-z0-9.-/]+/$"
            if (!url.matches(Regex(regex))) {
                return@setOnClickListener
            }
            sharedPrefs.edit()
                    .putString(getString(R.string.p_sync_url), url)
                    .putBoolean(getString(R.string.p_setup_completed), true)
                    .apply()
            finish()
        }
    }
}
