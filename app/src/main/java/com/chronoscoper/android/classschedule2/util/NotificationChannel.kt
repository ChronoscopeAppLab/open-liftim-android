package com.chronoscoper.android.classschedule2.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.support.annotation.RequiresApi
import com.chronoscoper.android.classschedule2.R
import com.chronoscoper.android.classschedule2.sync.Info

object NotificationChannel {
    const val ID_INFO_LOCAL_MEMO = "info_local_memo"
    const val ID_INFO_UNSPECIFIED = "info_unspecified"
    const val ID_INFO_EVENT = "info_event"
    const val ID_INFO_INFORMATION = "info_information"
    const val ID_INFO_ASSIGNMENT = "info_assignment"
    const val ID_INFO_TIMETABLE = "info_timetable"
    const val ID_MISC = "misc"

    private val IDS = arrayOf(ID_INFO_LOCAL_MEMO, ID_INFO_UNSPECIFIED,
            ID_INFO_EVENT, ID_INFO_INFORMATION,
            ID_INFO_ASSIGNMENT, ID_INFO_TIMETABLE, ID_MISC)
    private val NAME_RES_IDS = arrayOf(R.string.type_memo, R.string.type_unspecified,
            R.string.type_event, R.string.type_information, R.string.type_submission,
            R.string.timetable, R.string.misc)

    @RequiresApi(Build.VERSION_CODES.O)
    fun register(context: Context) {
        val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        IDS.forEachIndexed { index, channelId ->
            val channel = NotificationChannel(channelId, context.getString(NAME_RES_IDS[index]),
                    NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun getChannelIdByType(type: Int): String {
        return when (type) {
            Info.TYPE_LOCAL_MEMO -> ID_INFO_LOCAL_MEMO
            Info.TYPE_EVENT -> ID_INFO_EVENT
            Info.TYPE_INFORMATION -> ID_INFO_INFORMATION
            Info.TYPE_SUBMISSION -> ID_INFO_ASSIGNMENT
            Info.TYPE_TIMETABLE -> ID_INFO_TIMETABLE
        // Info.TYPE_UNSPECIFIED (= 0) will be caught by 'else'
            else -> ID_INFO_UNSPECIFIED
        }
    }
}