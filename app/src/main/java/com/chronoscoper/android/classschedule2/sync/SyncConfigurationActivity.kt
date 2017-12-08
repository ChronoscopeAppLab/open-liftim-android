package com.chronoscoper.android.classschedule2.sync

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.widget.Button
import android.widget.EditText
import com.chronoscoper.android.classschedule2.R
import kotterknife.bindView

class SyncConfigurationActivity : AppCompatActivity() {

    private val urlEditText by bindView<EditText>(R.id.sync_url)
    private val okButton by bindView<Button>(R.id.ok)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sync_configuration)

        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this)

        okButton.setOnClickListener {
            val url = urlEditText.text.toString()
            val regex = "^https?://[a-z0-9.-/]+/$"
            if (!url.matches(Regex(regex))) {
                return@setOnClickListener
            }
            sharedPrefs.edit()
                    .putString(getString(R.string.p_sync_url), url)
                    .putBoolean(getString(R.string.p_setup_completed), true)
                    .apply()
            finish()
        }
    }
}
