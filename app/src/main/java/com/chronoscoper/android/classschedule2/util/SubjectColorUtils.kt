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

import android.graphics.Color
import android.util.Log
import com.chronoscoper.android.classschedule2.sync.LiftimContext

private const val TAG = "SubjectColor"

fun obtainColorCorrespondsTo(subjectName: String): Int {
    val db = LiftimContext.getOrmaDatabase()
    if (db.selectFromColorPaletteV2().count() <= 0) {
        Log.d(TAG, "V2 has not synced yet. Using v1 color palette...")
        return loadColorV1(subjectName)
    } else {
        Log.d(TAG, "V2 detected. Loading color from v2 color palette")
        return loadColorV2(subjectName)
    }
}

private fun loadColorV1(name: String): Int {
    val db = LiftimContext.getOrmaDatabase()
    val colorName = db.selectFromSubject()
            .liftimCodeEq(LiftimContext.getLiftimCode())
            .subjectEq(name).firstOrNull()?.color ?: return 0xff999999.toInt()
    val colorRRGGBB = db.selectFromColorPalette()
            .nameEq(colorName)
            .firstOrNull()?.color ?: return 0xff999999.toInt()
    return try {
        Color.parseColor(colorRRGGBB)
    } catch (e: IllegalArgumentException) {
        Log.e(TAG, "Malformed color string. Is it #RRGGBB or #AARRGGBB?", e)
        0xff999999.toInt()
    }
}

private fun loadColorV2(name: String): Int {
    val db = LiftimContext.getOrmaDatabase()
    val colorName = db.selectFromSubject()
            .liftimCodeEq(LiftimContext.getLiftimCode())
            .subjectEq(name).firstOrNull()?.color ?: return 0xff999999.toInt()
    return db.selectFromColorPaletteV2().nameEq(colorName).firstOrNull()?.color
            ?: 0xff999999.toInt()
}
