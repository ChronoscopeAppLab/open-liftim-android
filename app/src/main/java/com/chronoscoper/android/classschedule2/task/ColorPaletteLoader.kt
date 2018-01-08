package com.chronoscoper.android.classschedule2.task

import com.chronoscoper.android.classschedule2.sync.LiftimSyncEnvironment

class ColorPaletteLoader : Runnable {
    override fun run() {
        val response = LiftimSyncEnvironment.getLiftimService().colorPalette.execute()
        if (!response.isSuccessful){
            return
        }
        LiftimSyncEnvironment.getOrmaDatabase().deleteFromColorPalette().execute()
        val inserter = LiftimSyncEnvironment.getOrmaDatabase().prepareInsertIntoColorPalette()
        response.body()?.forEach {
            inserter.execute(it)
        }
    }
}
