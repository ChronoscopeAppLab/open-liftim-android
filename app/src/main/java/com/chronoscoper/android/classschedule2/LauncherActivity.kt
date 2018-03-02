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
import com.chronoscoper.android.classschedule2.sync.LiftimContext
import com.chronoscoper.android.classschedule2.sync.Subject
import com.chronoscoper.android.classschedule2.sync.WeeklyItem
import com.chronoscoper.android.classschedule2.task.AccountInfoLoader
import com.chronoscoper.android.classschedule2.task.InvalidTokenException
import com.chronoscoper.android.classschedule2.task.SubjectLoader
import com.chronoscoper.android.classschedule2.task.TokenReloadTask
import com.chronoscoper.android.classschedule2.task.WeeklyLoader
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

    private val prefs by lazy { PreferenceManager.getDefaultSharedPreferences(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
            if (userInfoSyncNeeded) {
                val token = LiftimContext.getToken()
                val accountInfo = AccountInfoLoader(token)
                        .load()
                        ?: kotlin.run {
                            it.onError(Exception())
                            return@create
                        }
                val prefEditor = prefs.edit()
                prefEditor.apply {
                    putString(getString(R.string.p_account_name), accountInfo.userName)
                    putString(getString(R.string.p_account_image_file), accountInfo.imageFile)
                    putString(getString(R.string.p_account_add_date), accountInfo.addDate)
                    putBoolean(getString(R.string.p_account_is_available), accountInfo.isAvailable)
                }
                prefEditor.apply()
                backupAndDeleteData()
                try {
                    accountInfo.liftimCodes.forEach {
                        WeeklyLoader(it.liftimCode, token).run()
                        SubjectLoader(it.liftimCode, token).run()
                    }
                } catch (e: Exception) {
                    restoreData()
                }
                val selectedLiftimCode =
                        prefs.getLong(getString(R.string.p_default_liftim_code), -1)
                if (db.selectFromLiftimCodeInfo().liftimCodeEq(selectedLiftimCode).count() <= 0) {
                    val code = db.selectFromLiftimCodeInfo().firstOrNull()?.liftimCode ?: -1
                    prefs.edit()
                            .putLong(getString(R.string.p_default_liftim_code), code)
                            .apply()
                }
                prefs.edit().putString(getString(R.string.p_last_user_info_synced),
                        DateTime.now().toString()).apply()
            }
            it.onComplete()
        }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber)
        disposables.add(subscriber)

        optimizeInfo()
    }


    private var weeklyDataBackup: List<WeeklyItem>? = null
    private var subjectDataBackup: List<Subject>? = null

    private fun backupAndDeleteData() {
        val db = LiftimContext.getOrmaDatabase()
        weeklyDataBackup = db.selectFromWeeklyItem().toList()
        subjectDataBackup = db.selectFromSubject().toList()
        db.deleteFromWeeklyItem().execute()
        db.deleteFromSubject().execute()
    }

    private fun restoreData() {
        val db = LiftimContext.getOrmaDatabase()
        db.deleteFromWeeklyItem().execute()
        db.deleteFromSubject().execute()
        weeklyDataBackup?.forEach {
            db.insertIntoWeeklyItem(it)
        }
        subjectDataBackup?.forEach {
            db.insertIntoSubject(it)
        }
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
