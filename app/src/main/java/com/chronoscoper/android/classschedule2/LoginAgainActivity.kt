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
package com.chronoscoper.android.classschedule2

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import com.chronoscoper.android.classschedule2.setup.TokenCallbackActivity
import com.chronoscoper.android.classschedule2.sync.LiftimContext
import com.chronoscoper.android.classschedule2.util.setComponentEnabled
import kotterknife.bindView

class LoginAgainActivity : BaseActivity() {
    private val loginButton by bindView<Button>(R.id.login)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_again)
        loginButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW,
                    Uri.parse(LiftimContext.getApiUrl("auth")))
            startActivity(intent)
            finish()
        }
        setComponentEnabled(this, true, TokenCallbackActivity::class.java)
    }
}
