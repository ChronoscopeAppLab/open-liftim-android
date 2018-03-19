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

import com.chronoscoper.android.classschedule2.sync.LiftimContext
import okhttp3.Request

fun enforceValid(token: String): Int {
    val response = LiftimContext.getOkHttpClient()
            .newCall(Request.Builder()
                    .url(LiftimContext.getApiUrl(
                            "token_availability_check?token=$token"))
                    .build())
            .execute()
    val statusCode = response.code()
    if (statusCode == 401) {
        throw InvalidTokenException()
    }
    return statusCode
}

class InvalidTokenException : Exception()
