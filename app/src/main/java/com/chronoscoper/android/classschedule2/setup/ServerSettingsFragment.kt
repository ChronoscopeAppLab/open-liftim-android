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
package com.chronoscoper.android.classschedule2.setup

import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import com.chronoscoper.android.classschedule2.R
import com.chronoscoper.android.classschedule2.sync.LiftimSyncEnvironment
import kotterknife.bindView

class ServerSettingsFragment : BaseSetupFragment() {
    override fun onCreateView(
            inflater: LayoutInflater?,
            container: ViewGroup?,
            savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_server_settings, container, false)
    }

    private val urlEditText by bindView<EditText>(R.id.sync_url)
    private val okButton by bindView<Button>(R.id.ok)
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)

        okButton.setOnClickListener {
            val url = urlEditText.text.toString()
            val regex = "^https?://[a-z0-9.-/]+/$"
            if (!url.matches(Regex(regex))) {
                return@setOnClickListener
            }
            sharedPrefs.edit()
                    .putString(getString(R.string.p_sync_url), url)
                    .apply()

            LiftimSyncEnvironment.init(context, url, 0)

            nextStep()
        }
    }
}