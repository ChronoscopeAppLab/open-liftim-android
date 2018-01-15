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
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.chronoscoper.android.classschedule2.BaseActivity
import com.chronoscoper.android.classschedule2.R
import com.chronoscoper.android.classschedule2.sync.LiftimSyncEnvironment
import com.chronoscoper.android.classschedule2.task.InfoLoader
import com.chronoscoper.android.classschedule2.task.JoinLiftimCodeTask
import com.chronoscoper.android.classschedule2.task.LiftimCodeInfoLoader
import com.chronoscoper.android.classschedule2.task.SubjectLoader
import com.chronoscoper.android.classschedule2.task.WeeklyLoader
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import kotterknife.bindView
import java.io.IOException

class JoinLiftimCodeActivity : BaseActivity() {

    private val invitationNumberInput by bindView<EditText>(R.id.invitation_number)
    private val doneButton by bindView<Button>(R.id.done)
    private val status by bindView<TextView>(R.id.status)

    private val disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_join_liftim_code)

        doneButton.setOnClickListener {
            val invitationNumber = try {
                invitationNumberInput.text.toString().toInt()
            } catch (e: NumberFormatException) {
                invitationNumberInput.error = getString(R.string.incorrect_invitation_number)
                return@setOnClickListener
            }
            status.text = getString(R.string.join_progress)
            it.isEnabled = false
            val observer = object : DisposableObserver<Long>() {
                override fun onComplete() {
                    finish()
                }

                override fun onNext(t: Long) {
                    status.text = getString(R.string.loading_data)
                }

                override fun onError(e: Throwable) {
                    status.text = getString(R.string.incorrect_invitation_number)
                    it.isEnabled = true
                }
            }
            Observable.create<Long> {
                val liftimCode = JoinLiftimCodeTask(LiftimSyncEnvironment.getToken())
                        .joinAndObtainLiftimCode(invitationNumber)
                if (liftimCode == null) {
                    it.onError(InvalidInvitationNumException())
                    return@create
                } else {
                    it.onNext(liftimCode)
                }
                val token = LiftimSyncEnvironment.getToken()
                try {
                    LiftimCodeInfoLoader(liftimCode, token, false).run()
                    obtainAllDataFor(liftimCode, token)
                } catch (ignore: IOException) {
                    // Continue anyway
                }
                it.onComplete()
            }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(observer)
            disposables.add(observer)
        }
    }

    private fun obtainAllDataFor(liftimCode: Long, token: String) {
        InfoLoader(liftimCode, token).run()
        WeeklyLoader(liftimCode, token).run()
        SubjectLoader(liftimCode, token).run()
    }

    private class InvalidInvitationNumException : Exception()
}
