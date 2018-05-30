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

class ResultRegisterTask {
    companion object {
        private const val TAG = "ResultRegister"
    }

    fun execute(context: Context): Boolean {
        try {
            val tmpFile = context.cacheDir / LiftimApplication.REGISTER_TMP_NAME
            val config = LiftimContext.getGson()
                    .fromJson(tmpFile.readText(),
                            RegisterTemporary::class.java)
            tmpFile.delete()
            val res = when {
                config.target == RegisterTemporary.TARGET_INFO ->
                    LiftimContext.getLiftimService()
                            .registerInfo(LiftimContext.getLiftimCode(),
                                    LiftimContext.getToken(), config.data)
                            .execute()
                config.target == RegisterTemporary.TARGET_WEEKLY ->
                    LiftimContext.getLiftimService()
                            .registerWeekly(LiftimContext.getLiftimCode(),
                                    LiftimContext.getToken(), config.data)
                            .execute()
                else -> return false
            }
            if (!res.isSuccessful) {
                Log.e(TAG, "Failed in task(${res.code()}). " +
                        "Server says \"${res.raw().body()?.string()}\"")
                return false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Unable to register info", e)
            return false
        }
        return true
    }
}
