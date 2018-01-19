package com.chronoscoper.android.classschedule2.task

import com.chronoscoper.android.classschedule2.sync.LiftimContext

class ColorPaletteLoader : Runnable {
    override fun run() {
        val response = LiftimContext.getLiftimService().colorPalette.execute()
        if (!response.isSuccessful) {
            return
        }
        LiftimContext.getOrmaDatabase().deleteFromColorPalette().execute()
        val inserter = LiftimContext.getOrmaDatabase().prepareInsertIntoColorPalette()
        response.body()?.forEach {
            inserter.execute(it)
        }
    }
}
