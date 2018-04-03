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
package com.chronoscoper.android.classschedule2.util

import android.content.Context
import android.content.Intent
import com.chronoscoper.android.classschedule2.BaseActivity
import org.greenrobot.eventbus.EventBus

/**
 * Opens Activity as a new task and clear old task
 */
fun openInNewTask(context: Context, clazz: Class<*>) {
    EventBus.getDefault().post(EventMessage
            .of(BaseActivity.BroadcastActivityFinisher.EVENT_FINISH_ALL_ACTIVITIES))
    context.startActivity(Intent(context, clazz))
}
