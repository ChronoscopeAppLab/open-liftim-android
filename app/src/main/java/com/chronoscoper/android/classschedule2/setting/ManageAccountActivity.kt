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

import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AlertDialog
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import com.chronoscoper.android.classschedule2.BaseActivity
import com.chronoscoper.android.classschedule2.LauncherActivity
import com.chronoscoper.android.classschedule2.R
import com.chronoscoper.android.classschedule2.setup.SetupActivity
import com.chronoscoper.android.classschedule2.sync.LiftimContext
import com.chronoscoper.android.classschedule2.task.FullSyncTask
import com.chronoscoper.android.classschedule2.util.openInNewTask
import com.chronoscoper.android.classschedule2.util.setComponentEnabled
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subscribers.DisposableSubscriber
import kotterknife.bindView

class ManageAccountActivity : BaseActivity() {

    private val userNameLabel by bindView<TextView>(R.id.user_name)
    private val logoutButton by bindView<Button>(R.id.logout)
    private val updateButton by bindView<View>(R.id.update_account_info)
    private val progress by bindView<ProgressBar>(R.id.progress)

    private val sharedPrefs by lazy { PreferenceManager.getDefaultSharedPreferences(this) }

    private val disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_account)

        userNameLabel.text = sharedPrefs.getString(getString(R.string.p_account_name), "")
        updateButton.setOnClickListener {
            progress.visibility = View.VISIBLE
            val subscriber = object : DisposableSubscriber<Unit>() {
                override fun onComplete() {
                    progress.visibility = View.GONE
                }

                override fun onNext(t: Unit?) {
                }

                override fun onError(t: Throwable?) {
                }
            }
            Flowable.defer { Flowable.just(FullSyncTask(this).run()) }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(subscriber)
            disposables.add(subscriber)
        }
        logoutButton.setOnClickListener {
            AlertDialog.Builder(this)
                    .setMessage(R.string.logout_warning)
                    .setPositiveButton(R.string.logout, { _, _ ->
                        removeAllData()
                        openInNewTask(this, LauncherActivity::class.java)
                        setComponentEnabled(this, true, SetupActivity::class.java)
                        setComponentEnabled(this, false, SettingsActivity::class.java)
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show()

        }
    }

    private fun removeAllData() {
        LiftimContext.getOrmaDatabase().deleteAll()
        sharedPrefs.edit().clear().apply()
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.clear()
    }
}
