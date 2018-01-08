package com.chronoscoper.android.classschedule2.task

import com.chronoscoper.android.classschedule2.sync.LiftimSyncEnvironment

class SubjectLoader(private val liftimCode: Long, private val token: String) : Runnable {
    override fun run() {
        val response = LiftimSyncEnvironment.getLiftimService().getSubjects(liftimCode, token)
                .execute()
        if (!response.isSuccessful) {
            return
        }
        val body = response.body() ?: return
        val db = LiftimSyncEnvironment.getOrmaDatabase()
        val inserter = db.prepareInsertIntoSubject()
        db.deleteFromSubject().liftimCodeEq(liftimCode).execute()
        body.forEach {
            inserter.execute(it.apply { liftimCode = this@SubjectLoader.liftimCode })
        }
    }
}
