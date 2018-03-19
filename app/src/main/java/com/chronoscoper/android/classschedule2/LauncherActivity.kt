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
import com.chronoscoper.android.classschedule2.service.TokenLoadService
import com.chronoscoper.android.classschedule2.setting.ManageLiftimCodeActivity
import com.chronoscoper.android.classschedule2.setup.SetupActivity
import com.chronoscoper.android.classschedule2.setup.TokenCallbackActivity
import com.chronoscoper.android.classschedule2.sync.LiftimContext
import com.chronoscoper.android.classschedule2.task.FullSyncTask
import com.chronoscoper.android.classschedule2.task.InvalidTokenException
import com.chronoscoper.android.classschedule2.task.enforceValid
import com.chronoscoper.android.classschedule2.util.optimizeInfo
import com.chronoscoper.android.classschedule2.util.setComponentEnabled
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import kotterknife.bindView
import org.joda.time.DateTime

class LauncherActivity : BaseActivity() {

    private val prefs by lazy { PreferenceManager.getDefaultSharedPreferences(this) }
    private val iconForeground by bindView<View>(R.id.icon_foreground)

    private var animationFinished = false
    private var syncFinished = false

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
                    animationFinished = true
                    startMainIfNeeded()
                    iconForeground.animate().translationY(0f)
                            .setDuration(200)
                            .start()
                }
                .start()

        val setupCompleted = prefs.getBoolean(getString(R.string.p_setup_completed), false)
        if (setupCompleted) {
            secondLaunchTime()
        } else {
            setComponentEnabled(this, true,
                    SetupActivity::class.java, TokenCallbackActivity::class.java)
            startActivity(Intent(this, SetupActivity::class.java))
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

    private fun startMainIfNeeded() {
        if (animationFinished && syncFinished) {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }
    }

    private val disposables = CompositeDisposable()

    private fun secondLaunchTime() {
        val db = LiftimContext.getOrmaDatabase()
        if (db.selectFromLiftimCodeInfo().count() == 0) {
            noLiftimCode()
            return
        }
        val subscriber = object : DisposableObserver<Unit>() {
            override fun onComplete() {
                syncFinished = true
                startMainIfNeeded()
            }

            override fun onError(e: Throwable) {
                if (e is InvalidTokenException) {
                    startActivity(Intent(this@LauncherActivity, ReLoginActivity::class.java))
                    finish()
                    return
                }
                syncFinished = true
                startMainIfNeeded()
            }

            override fun onNext(t: Unit) {
            }
        }

        Observable.create<Unit> {
            try {
                val connectivityStatus = enforceValid(LiftimContext.getToken())
                prefs.edit().putInt(getString(R.string.p_last_sync_status), connectivityStatus)
                        .apply()
            } catch (e: Exception) {
                it.onError(e)
                return@create
            }
            startService(Intent(this, TokenLoadService::class.java))
            if (userInfoSyncNeeded) {
                FullSyncTask(this).run()
            }
            it.onComplete()
        }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber)
        disposables.add(subscriber)

        optimizeInfo()
    }

    private val userInfoSyncNeeded: Boolean
        get() {
            val lastSynced = prefs.getString(
                    getString(R.string.p_last_user_info_synced), null)
                    ?: return true
            val dateTime = try {
                DateTime.parse(lastSynced)
            } catch (e: Exception) {
                return true
            }
            return dateTime.plusDays(3).isBeforeNow
        }

    private fun noLiftimCode() {
        startActivity(Intent(this, ManageLiftimCodeActivity::class.java))
        finish()
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    override fun onRestart() {
        super.onRestart()
        secondLaunchTime()
    }

    override fun onStop() {
        super.onStop()
        disposables.clear()
    }
}
