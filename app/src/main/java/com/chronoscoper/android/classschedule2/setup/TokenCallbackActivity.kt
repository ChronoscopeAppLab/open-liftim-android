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
package com.chronoscoper.android.classschedule2.setup

import android.os.Bundle
import android.os.SystemClock
import android.preference.PreferenceManager
import com.chronoscoper.android.classschedule2.BaseActivity
import com.chronoscoper.android.classschedule2.R
import com.chronoscoper.android.classschedule2.sync.LiftimSyncEnvironment
import com.chronoscoper.android.classschedule2.task.AccountInfoLoader
import com.chronoscoper.android.classschedule2.task.InfoLoader
import com.chronoscoper.android.classschedule2.task.LiftimCodeInfoLoader
import com.chronoscoper.android.classschedule2.task.WeeklyLoader
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import io.reactivex.subscribers.DisposableSubscriber
import okhttp3.Request
import java.io.IOException

class TokenCallbackActivity : BaseActivity() {

    private val disposables = CompositeDisposable()

    private val sharedPrefs by lazy { PreferenceManager.getDefaultSharedPreferences(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(android.R.id.content, TokenCallbackFragment())
                    .commit()
        }
        val token = obtainTokenFromIntent()
        if (token == null) {
            supportFragmentManager.beginTransaction()
                    .replace(android.R.id.content, TokenLoadFailedFragment())
                    .commit()
            return
        }
        val subscriber = object : DisposableSubscriber<Boolean>() {
            override fun onError(t: Throwable?) {
                supportFragmentManager.beginTransaction()
                        .replace(android.R.id.content, TokenLoadFailedFragment())
                        .commit()
                return
            }

            override fun onNext(t: Boolean?) {}

            override fun onComplete() {
                sharedPrefs.edit()
                        .putString(getString(R.string.p_account_token), token)
                        .apply()
                LiftimSyncEnvironment.setToken(token)
                executeInitialSync()
            }
        }
        Flowable.defer { Flowable.just(isValidToken(token)) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber)
        disposables.add(subscriber)
    }

    private fun executeInitialSync() {
        val observer = object : DisposableObserver<Unit>() {
            override fun onComplete() {
                supportFragmentManager.beginTransaction()
                        .replace(android.R.id.content, LiftimCodeChooserFragment())
                        .commit()
            }

            override fun onNext(t: Unit) {
            }

            override fun onError(e: Throwable) {
                supportFragmentManager.beginTransaction()
                        .replace(android.R.id.content, InitialSyncErrorFragment())
                        .commit()
            }
        }

        Observable.create<Unit> {
            val token = LiftimSyncEnvironment.getToken()
            val accountInfo = AccountInfoLoader(token)
                    .load()
                    ?: kotlin.run {
                it.onError(Exception())
                return@create
            }
            val prefEditor = sharedPrefs.edit()
            prefEditor.apply {
                putString(getString(R.string.p_account_name), accountInfo.userName)
                putString(getString(R.string.p_account_image_file), accountInfo.imageFile)
                putString(getString(R.string.p_account_add_date), accountInfo.addDate)
                putBoolean(getString(R.string.p_account_is_available), accountInfo.isAvailable)
            }
            prefEditor.apply()
            accountInfo.liftimCodes.forEach {
                try {
                    LiftimCodeInfoLoader(it.liftimCode, token)
                    obtainAllDataFor(it.liftimCode, token)
                } catch (ignore: IOException) {
                    // Continue anyway
                }
            }
            it.onComplete()
        }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer)
        disposables.add(observer)
    }

    private fun obtainAllDataFor(liftimCode: Long, token: String) {
        InfoLoader(liftimCode, token).run()
        WeeklyLoader(liftimCode, token).run()
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.clear()
    }

    private fun obtainTokenFromIntent(): String? {
        val urlQuery = intent?.data?.query ?: return null
        return urlQuery.split("&")
                .find { it.startsWith("token=") }
                ?.removePrefix("token=")
    }

    private fun isValidToken(token: String): Boolean {
        return try {
            val url = PreferenceManager.getDefaultSharedPreferences(this)
                    .getString(getString(R.string.p_sync_url), "")
            val response = LiftimSyncEnvironment.getOkHttpClient()
                    .newCall(Request.Builder()
                            .url("${url}api/v1/token_availability_check?token=$token")
                            .build())
                    .execute()
            response.isSuccessful
        } catch (e: IOException) {
            false
        }
    }
}
