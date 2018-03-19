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
package com.chronoscoper.android.classschedule2.functionrestriction

import android.content.Context
import com.chronoscoper.android.classschedule2.sync.FunctionRestriction
import com.chronoscoper.android.classschedule2.sync.LiftimContext
import java.io.FileNotFoundException
import java.io.InputStream

private var functionRestriction: FunctionRestriction? = null

fun getFunctionRestriction(context: Context): FunctionRestriction {
    if (functionRestriction != null) return functionRestriction!!
    lateinit var input: InputStream
    try {
        input = context.openFileInput("function_restriction.json")
    } catch (e: FileNotFoundException) {
        input = context.assets.open("default_function_restriction.json")
    }
    input.use {
        it.bufferedReader().use {
            functionRestriction = LiftimContext.getGson()
                    .fromJson(it.readText(), FunctionRestriction::class.java)
        }
    }
    return functionRestriction!!
}
