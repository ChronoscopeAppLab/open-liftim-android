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
import com.chronoscoper.android.classschedule2.home.HomeActivity
import com.chronoscoper.android.classschedule2.setting.ManageLiftimCodeActivity
import com.chronoscoper.android.classschedule2.setup.SetupActivity
import com.chronoscoper.android.classschedule2.setup.TokenCallbackActivity
import com.chronoscoper.android.classschedule2.sync.Info
import com.chronoscoper.android.classschedule2.sync.LiftimContext
import com.chronoscoper.android.classschedule2.task.InvalidTokenException
import com.chronoscoper.android.classschedule2.task.TokenReloadTask
import com.chronoscoper.android.classschedule2.task.enforceValidToken
import com.chronoscoper.android.classschedule2.util.optimizeInfo
import com.chronoscoper.android.classschedule2.util.setComponentEnabled
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import org.joda.time.DateTime

class LauncherActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this)
        val setupCompleted = sharedPrefs.getBoolean(getString(R.string.p_setup_completed), false)
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

    private val disposables = CompositeDisposable()

    private fun secondLaunchTime() {
        val db = LiftimContext.getOrmaDatabase()
        if (db.selectFromLiftimCodeInfo().count() == 0) {
            noLiftimCode()
            return
        }
        val subscriber = object : DisposableObserver<Unit>() {
            override fun onComplete() {
                startActivity(Intent(this@LauncherActivity, HomeActivity::class.java))
                finish()
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }

            override fun onError(e: Throwable) {
                e.printStackTrace()
                if (e is InvalidTokenException) {
                    startActivity(Intent(this@LauncherActivity, ReLoginActivity::class.java))
                    finish()
                    return
                }
                startActivity(Intent(this@LauncherActivity, HomeActivity::class.java))
                finish()
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }

            override fun onNext(t: Unit) {
            }
        }

        Observable.create<Unit> {
            enforceValidToken(LiftimContext.getToken())
            TokenReloadTask(this).run()
            it.onComplete()
        }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber)
        disposables.add(subscriber)

        optimizeInfo()
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
