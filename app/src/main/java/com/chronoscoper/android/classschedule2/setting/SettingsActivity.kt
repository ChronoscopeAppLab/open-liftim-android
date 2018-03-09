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
package com.chronoscoper.android.classschedule2.setting

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceFragment
import android.support.design.widget.BottomSheetDialogFragment
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.chronoscoper.android.classschedule2.BaseActivity
import com.chronoscoper.android.classschedule2.R
import com.chronoscoper.library.licenseviewer.LicenseViewer
import kotterknife.bindView

class SettingsActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.slide_in, R.anim.no_anim)
        fragmentManager.beginTransaction()
                .replace(android.R.id.content, SettingsFragment())
                .commit()
    }

    override fun finish() {
        super.finish()
        android.R.anim.slide_in_left
        overridePendingTransition(R.anim.no_anim, R.anim.slide_out)
    }

    class SettingsFragment : PreferenceFragment() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.preference)

            findPreference(getString(R.string.p_manage_account))
                    .setOnPreferenceClickListener {
                        startActivity(Intent(activity, ManageAccountActivity::class.java))
                        false
                    }
            findPreference(getString(R.string.p_manage_liftim_code))
                    .setOnPreferenceClickListener {
                        startActivity(Intent(activity, ManageLiftimCodeActivity::class.java))
                        false
                    }
            findPreference(getString(R.string.p_send_feedback))
                    .setOnPreferenceClickListener {
                        startActivity(Intent(activity, FeedbackActivity::class.java))
                        false
                    }
            findPreference(getString(R.string.p_oss_license))
                    .setOnPreferenceClickListener {
                        LicenseViewer.open(activity, getString(R.string.open_source_license))
                        false
                    }
            findPreference(getString(R.string.p_app_info))
                    .setOnPreferenceClickListener {
                        val a = activity as? AppCompatActivity
                                ?: return@setOnPreferenceClickListener false
                        AppInfoDialog().show(a.supportFragmentManager, null)
                        false
                    }
        }
    }

    class AppInfoDialog : BottomSheetDialogFragment() {
        override fun onCreateView(
                inflater: LayoutInflater,
                container: ViewGroup?,
                savedInstanceState: Bundle?): View? =
                inflater.inflate(R.layout.fragment_app_info, container, false)

        private val openProjectPageButton by bindView<Button>(R.id.open_project_page)

        override fun onActivityCreated(savedInstanceState: Bundle?) {
            super.onActivityCreated(savedInstanceState)
            openProjectPageButton.setOnClickListener {
                startActivity(Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://github.com/ChronoscopeAppLab/open-liftim-android")))
                dismiss()
            }
        }
    }
}
