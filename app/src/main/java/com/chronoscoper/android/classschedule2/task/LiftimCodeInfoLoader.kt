/*
 * Copyright 2017 Chronoscope
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

import com.chronoscoper.android.classschedule2.sync.LiftimCodeInfo
import com.chronoscoper.android.classschedule2.sync.LiftimSyncEnvironment

import java.io.IOException

import retrofit2.Response

class LiftimCodeInfoLoader(private val liftimCode: Long) : Runnable {

    override fun run() {
        val response: Response<LiftimCodeInfo>
        try {
            response = LiftimSyncEnvironment.getLiftimService()
                    .getLiftimCodeInfo(liftimCode)
                    .execute()
        } catch (e: IOException) {
            return
        }
        if (!response.isSuccessful) {
            return
        }
        val liftimCodeInfo = response.body() ?: return
        liftimCodeInfo.liftimCode = liftimCode

        LiftimSyncEnvironment.getOrmaDatabase().insertIntoLiftimCodeInfo(liftimCodeInfo)
    }
}
