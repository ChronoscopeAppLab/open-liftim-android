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

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import com.chronoscoper.android.classschedule2.BaseActivity
import com.chronoscoper.android.classschedule2.R

class SetupActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (fragments.isEmpty()) {
            complete()
        }

        if (savedInstanceState == null) {
            replaceFragment(fragments.first())
        }
    }

    private fun complete() {
        sharedPrefs.edit()
                .putBoolean(getString(R.string.p_setup_completed), true)
                .apply()
        val url = sharedPrefs.getString(getString(R.string.p_sync_url), "")
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("${url}api/v1/auth"))
        startActivity(intent)
        finish()
    }

    private val fragments by lazy {
        mutableListOf(ServerSettingsFragment())
    }

    private val sharedPrefs by lazy {
        PreferenceManager.getDefaultSharedPreferences(this)
    }

    private fun replaceFragment(target: Fragment) {
        supportFragmentManager.beginTransaction()
                .replace(android.R.id.content, target)
                .commit()
    }

    fun next() {
        fragments.removeAt(0)
        if (fragments.isEmpty()) {
            complete()
            return
        }
        replaceFragment(fragments.first())
    }
}
