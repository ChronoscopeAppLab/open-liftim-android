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
import android.support.v4.app.Fragment
import com.chronoscoper.android.classschedule2.BaseActivity

class SetupActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (fragments.isEmpty()) {
            finish()
            return
        }

        if (savedInstanceState == null) {
            replaceFragment(fragments.first())
        }
    }

    private val fragments by lazy {
        mutableListOf(
                ServerSettingsFragment(),
                LoginFragment())
    }

    private fun replaceFragment(target: Fragment) {
        supportFragmentManager.beginTransaction()
                .replace(android.R.id.content, target)
                .commit()
    }

    fun next() {
        fragments.removeAt(0)
        if (fragments.isEmpty()) {
            return
        }
        replaceFragment(fragments.first())
    }
}
