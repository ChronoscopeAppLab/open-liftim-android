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
package com.chronoscoper.android.classschedule2.task

import com.chronoscoper.android.classschedule2.sync.Info
import com.chronoscoper.android.classschedule2.sync.InfoRemoteModel
import com.chronoscoper.android.classschedule2.sync.LiftimSyncEnvironment
import retrofit2.Response
import java.io.IOException

class InfoLoader(private val liftimCode: Long, private val token: String) : Runnable {
    companion object {
        private var nextCursor = 0L

        fun resetCursor() {
            nextCursor = 0L
        }
    }

    override fun run() {
        val response: Response<InfoRemoteModel>
        response = LiftimSyncEnvironment.getLiftimService()
                .getInfo(liftimCode, token, nextCursor)
                .execute()
        if (!response.isSuccessful) {
            return
        }
        val info = response.body() ?: return
        LiftimSyncEnvironment.getOrmaDatabase().deleteFromInfo().execute()
        nextCursor = info.nextCursor
        val inserter = LiftimSyncEnvironment.getOrmaDatabase().prepareInsertIntoInfo()
        info.info?.forEach {
            inserter.execute(Info(liftimCode, it.id, it.title, it.detail, it.weight, it.date,
                    it.time, it.link, it.type, it.timetable?.toString(), it.removable, Info.REMOTE))
        }
    }
}
