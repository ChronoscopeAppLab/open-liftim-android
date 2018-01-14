package com.chronoscoper.android.classschedule2.home.timetable

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.chronoscoper.android.classschedule2.BaseActivity

class ViewTimetableActivity : BaseActivity() {
    companion object {
        private const val ID = "target_id"
        fun open(context: Context, id: String) {
            context.startActivity(Intent(context, ViewTimetableActivity::class.java)
                    .putExtra(ID, id))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            val id=intent.getStringExtra(ID)?:kotlin.run { finish(); return }
            supportFragmentManager.beginTransaction()
                    .replace(android.R.id.content,TimetableFragment.obtain(id))
                    .commit()
        }
    }
}