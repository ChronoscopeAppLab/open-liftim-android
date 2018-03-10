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

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.ActivityOptionsCompat
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.chronoscoper.android.classschedule2.BaseActivity
import com.chronoscoper.android.classschedule2.R
import com.chronoscoper.android.classschedule2.sync.LiftimContext
import com.chronoscoper.android.classschedule2.task.InfoLoader
import com.chronoscoper.android.classschedule2.task.JoinLiftimCodeTask
import com.chronoscoper.android.classschedule2.task.LiftimCodeInfoLoader
import com.chronoscoper.android.classschedule2.task.SubjectLoader
import com.chronoscoper.android.classschedule2.task.WeeklyLoader
import com.chronoscoper.android.classschedule2.util.showToast
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import kotterknife.bindView
import java.io.IOException

class JoinLiftimCodeActivity : BaseActivity() {
    companion object {
        private const val RC_BARCODE = 100
        internal const val EXTRA_INVITATION_NUM = "invitation_num"
    }

    private val invitationNumberInput by bindView<EditText>(R.id.invitation_number)
    private val joinWithBarcodeButton by bindView<Button>(R.id.join_with_barcode)
    private val doneButton by bindView<View>(R.id.done)
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
            tryToJoin(invitationNumber)
        }

        joinWithBarcodeButton.setOnClickListener {
            val options = ActivityOptionsCompat
                    .makeSceneTransitionAnimation(this, doneButton, getString(R.string.t_fab))
                    .toBundle()
            (this as Activity).startActivityForResult(
                    Intent(this, JoinWithBarcodeActivity::class.java),
                    RC_BARCODE, options)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        data ?: return
        if (requestCode == RC_BARCODE && resultCode == Activity.RESULT_OK) {
            val invitationNumber = data.getIntExtra(EXTRA_INVITATION_NUM, -1)
            if (invitationNumber <= 0) {
                showToast(this, getString(R.string.barcode_not_found), Toast.LENGTH_LONG)
                return
            }
            invitationNumberInput.setText(invitationNumber.toString())
            tryToJoin(invitationNumber)
        }
    }

    private fun tryToJoin(invitationNumber: Int) {
        val observer = object : DisposableObserver<Long>() {
            override fun onComplete() {
                finish()
            }

            override fun onNext(t: Long) {
                status.text = getString(R.string.loading_data)
            }

            override fun onError(e: Throwable) {
                status.text = getString(R.string.incorrect_invitation_number)
                doneButton.isEnabled = true
            }
        }
        Observable.create<Long> {
            val liftimCode = JoinLiftimCodeTask(LiftimContext.getToken())
                    .joinAndObtainLiftimCode(invitationNumber)
            if (liftimCode == null) {
                it.onError(InvalidInvitationNumException())
                return@create
            } else {
                it.onNext(liftimCode)
            }
            val token = LiftimContext.getToken()
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

    private fun obtainAllDataFor(liftimCode: Long, token: String) {
        InfoLoader(liftimCode, token).run()
        WeeklyLoader(liftimCode, token).run()
        SubjectLoader(liftimCode, token).run()
    }

    private class InvalidInvitationNumException : Exception()
}
