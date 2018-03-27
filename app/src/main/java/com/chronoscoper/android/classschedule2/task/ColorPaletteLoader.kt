package com.chronoscoper.android.classschedule2.task

import com.chronoscoper.android.classschedule2.sync.LiftimContext

class ColorPaletteLoader : Runnable {
    override fun run() {
        val db = LiftimContext.getOrmaDatabase()
        val v2SyncResponse = LiftimContext.getLiftimService().colorPaletteV2.execute()
        if (v2SyncResponse.code() == 404) {
            // Server-side has not support Android-specialized color palette yet.
            // Try to acquire v1 one.
            val response = LiftimContext.getLiftimService().colorPalette.execute()
            if (!response.isSuccessful) {
                return
            }
            db.deleteFromColorPalette().execute()
            val inserter = db.prepareInsertIntoColorPalette()
            response.body()?.forEach {
                inserter.execute(it)
            }
            return
        } else if (!v2SyncResponse.isSuccessful) {
            return
        }
        db.deleteFromColorPaletteV2().execute()
        val inserter = db.prepareInsertIntoColorPaletteV2()
        v2SyncResponse.body()?.forEach {
            inserter.execute(it)
        }
    }
}
