package com.chronoscoper.android.classschedule2

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import com.chronoscoper.android.classschedule2.home.HomeActivity
import com.chronoscoper.android.classschedule2.sync.SyncConfigurationActivity

class LauncherActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this)
        val setupCompleted = sharedPrefs.getBoolean(getString(R.string.p_setup_completed), false)
        if (setupCompleted) {
            startActivity(Intent(this, HomeActivity::class.java))
        } else {
            startActivity(Intent(this, SyncConfigurationActivity::class.java))
        }
        finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}
