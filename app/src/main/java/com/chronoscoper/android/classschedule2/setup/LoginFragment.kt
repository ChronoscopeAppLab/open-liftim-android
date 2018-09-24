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
package com.chronoscoper.android.classschedule2.setup

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.chronoscoper.android.classschedule2.R
import com.chronoscoper.android.classschedule2.sync.LiftimContext
import kotterknife.bindView

class LoginFragment : BaseSetupFragment() {
    companion object {
        private const val TAG = "Login"
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_login, container, false)

    private val loginButton by bindView<Button>(R.id.login)
    private val termsButton by bindView<Button>(R.id.terms)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        loginButton.setOnClickListener {
            try {
                val intent = Intent(Intent.ACTION_VIEW,
                        Uri.parse(LiftimContext.getApiUrl("auth")))
                        .setClassName("com.android.chrome",
                                "com.google.android.apps.chrome.Main")
                startActivity(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Chrome isn't installed. Using system default...")
                startActivity(Intent(Intent.ACTION_VIEW,
                        Uri.parse(LiftimContext.getApiUrl("auth"))))
            }
            activity!!.finish()
        }
        termsButton.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW,
                    Uri.parse(getString(R.string.terms_url))))
        }
    }
}
