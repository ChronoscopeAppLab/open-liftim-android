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
package com.chronoscoper.android.classschedule2.task

import android.content.Context
import android.util.Log
import com.chronoscoper.android.classschedule2.LiftimApplication
import com.chronoscoper.android.classschedule2.sync.LiftimContext
import com.chronoscoper.android.classschedule2.util.div
import com.google.gson.annotations.Expose
import java.io.IOException

data class RegisterTemporary(@Expose val target: Int, @Expose val data: String) {
    companion object {
        private const val TAG = "RegisterTmp"
        const val TARGET_INFO = 1
        const val TARGET_WEEKLY = 2
        const val TARGET_DELETE_INFO = 3

        fun save(context: Context, target: Int, data: String) {
            val content = LiftimContext.getGson().toJson(RegisterTemporary(target, data))
            try {
                (context.cacheDir / LiftimApplication.REGISTER_TMP_NAME).writeText(content)
            } catch (e: IOException) {
                Log.e(TAG, "Failed to save to cache directory", e)
            }
        }
    }
}
