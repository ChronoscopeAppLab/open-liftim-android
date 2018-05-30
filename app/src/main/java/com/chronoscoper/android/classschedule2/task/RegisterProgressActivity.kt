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
package com.chronoscoper.android.classschedule2.task

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.chronoscoper.android.classschedule2.BaseActivity
import com.chronoscoper.android.classschedule2.R
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotterknife.bindView

class RegisterProgressActivity : BaseActivity() {
    companion object {
        private const val TAG = "RegisterProgress"
    }

    private val progress by bindView<View>(R.id.progress)
    private val done by bindView<View>(R.id.done)

    private val compositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.fade_in, 0)
        setContentView(R.layout.activity_register_progress)
        compositeDisposable.add(
                Single.defer { Single.just(ResultRegisterTask().execute(this)) }
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ success ->
                            if (success) {
                                progress.visibility = View.GONE
                                done.visibility = View.VISIBLE
                                setResult(Activity.RESULT_OK)
                                finish()
                            } else {
                                setResult(Activity.RESULT_CANCELED)
                                finish()
                            }
                        }, { e ->
                            Log.e(TAG, "Exception thrown in observable", e)
                            setResult(Activity.RESULT_CANCELED)
                            finish()
                        })

        )
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(0, android.R.anim.fade_out)
    }

    override fun onBackPressed() {
        // we don't want to have this activity finished by back button
    }
}
