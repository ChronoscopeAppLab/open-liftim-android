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

import com.chronoscoper.android.classschedule2.sync.LiftimSyncEnvironment
import okhttp3.Request
import java.io.IOException

fun enforceValidToken(token: String) {
    try {
        val response = LiftimSyncEnvironment.getOkHttpClient()
                .newCall(Request.Builder()
                        .url(LiftimSyncEnvironment.getApiUrl(
                                "token_availability_check?token=$token"))
                        .build())
                .execute()
        if (!response.isSuccessful) {
            throw InvalidTokenException()
        }
    } catch (e: IOException) {
        throw InvalidTokenException()
    }
}

class InvalidTokenException : Exception()
