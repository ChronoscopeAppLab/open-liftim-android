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

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper

@SuppressLint("Registered")
abstract class BaseActivity : AppCompatActivity() {
    override fun attachBaseContext(base: Context?) {
        //Wrap base context to use custom font whole this app with Calligraphy.
        //See https://github.com/chrisjenx/Calligraphy for more details on Calligraphy.
        super.attachBaseContext(CalligraphyContextWrapper.wrap(base))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == android.R.id.home) {
            animateFinishCompat()
        }
        return super.onOptionsItemSelected(item)
    }

    open fun animateFinishCompat() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            finishAfterTransition()
        } else {
            finish()
        }
    }
}
