/*
 * Copyright 2017-2018 Chronoscope
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
package com.chronoscoper.android.classschedule2

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import com.chronoscoper.android.classschedule2.home.HomeActivity
import com.chronoscoper.android.classschedule2.setting.ManageLiftimCodeActivity
import com.chronoscoper.android.classschedule2.setup.SetupActivity
import com.chronoscoper.android.classschedule2.sync.LiftimContext
import com.chronoscoper.android.classschedule2.task.TokenReloadTask
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotterknife.bindView

class LauncherActivity : BaseActivity() {
    private val iconForeground by bindView<View>(R.id.icon_foreground)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launcher)

        iconForeground.apply {
            visibility = View.VISIBLE
            alpha = 0f
            translationY = 300f
            scaleX = 0.8f
            scaleY = 0.8f
        }

        iconForeground.animate().alpha(1f).translationY(-50f)
                .scaleX(1f).scaleY(1f)
                .setDuration(200)
                .withEndAction {
                    iconForeground.animate().translationY(0f)
                            .setDuration(200)
                            .start()
                }
                .start()

        val setupCompleted = PreferenceManager.getDefaultSharedPreferences(this)
                .getString(getString(R.string.p_account_token),
                        null) != null
        if (setupCompleted) {
            launchMainWhenTokenUpdated()
        } else {
            //TODO: enable if you needn't show server address settings
            //startActivity(Intent(this, IntroductionActivity::class.java))
            startActivity(Intent(this, SetupActivity::class.java))
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

    private val compositeDisposable = CompositeDisposable()

    private fun launchMainWhenTokenUpdated() {
        compositeDisposable.add(
                Single.defer { Single.just(TokenReloadTask(this).run()) }
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ launchMain() }, { launchMain() })
        )
    }

    private fun launchMain() {
        val db = LiftimContext.getOrmaDatabase()
        if (db.selectFromLiftimCodeInfo().count() == 0) {
            startActivity(Intent(this, ManageLiftimCodeActivity::class.java))
            finish()
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        } else {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
    }
}
