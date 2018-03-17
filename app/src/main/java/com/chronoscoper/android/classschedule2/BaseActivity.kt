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
import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem
import com.chronoscoper.android.classschedule2.util.EventMessage
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

@SuppressLint("Registered")
abstract class BaseActivity : AppCompatActivity() {

    private var activityFinisher: BroadcastActivityFinisher? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        activityFinisher = BroadcastActivityFinisher(this)
        EventBus.getDefault().register(activityFinisher)
        super.onCreate(savedInstanceState)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(activityFinisher)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == android.R.id.home) {
            animateFinish()
        }
        return super.onOptionsItemSelected(item)
    }

    open fun animateFinish() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            finishAfterTransition()
        } else {
            finish()
        }
    }

    class BroadcastActivityFinisher(private val activityRef: Activity) {
        companion object {
            private const val TAG = "ActivityFinisher"
            const val EVENT_FINISH_ALL_ACTIVITIES = "FINISH_ALL_ACTIVITIES"
        }

        @Suppress("UNUSED")
        @Subscribe(threadMode = ThreadMode.MAIN)
        fun finish(event: EventMessage) {
            if (event.type == EVENT_FINISH_ALL_ACTIVITIES) {
                Log.d(TAG, "Finishing all activities...")
                activityRef.finish()
            }
        }
    }
}
