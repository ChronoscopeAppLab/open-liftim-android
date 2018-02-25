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
package com.chronoscoper.android.classschedule2.setting.manager

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.chronoscoper.android.classschedule2.BaseActivity
import com.chronoscoper.android.classschedule2.R
import com.chronoscoper.android.classschedule2.sync.LiftimContext
import com.chronoscoper.android.classschedule2.view.ProgressDialog
import com.chronoscoper.android.classschedule2.view.SwipeDownDiscardView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import kotterknife.bindView

class DeleteLiftimCodeActivity : BaseActivity() {
    companion object {
        private const val EXTRA_LIFTIM_CODE = "LIFTIM_CODE"
        fun start(activity: Activity, liftimCode: Long, requestCode: Int) {
            activity.startActivityForResult(Intent(activity, DeleteLiftimCodeActivity::class.java)
                    .putExtra(EXTRA_LIFTIM_CODE, liftimCode), requestCode)
        }
    }

    private val container by bindView<View>(R.id.container)
    private val liftimCodeName by bindView<TextView>(R.id.liftim_code)
    private val swipeContainer by bindView<SwipeDownDiscardView>(R.id.swipe_container)

    private val disposables = CompositeDisposable()
    private val observer = object : DisposableObserver<Boolean>() {
        override fun onComplete() {
            setResult(Activity.RESULT_OK)
            finish()
        }

        override fun onNext(t: Boolean) {
        }

        override fun onError(e: Throwable) {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.fade_in, 0)
        val liftimCode = intent.getLongExtra(EXTRA_LIFTIM_CODE, -1)
        if (liftimCode < 0) {
            finish()
            return
        }
        setContentView(R.layout.activity_delete_liftim_code)
        val liftimCodeInfo = LiftimContext.getOrmaDatabase()
                .selectFromLiftimCodeInfo().liftimCodeEq(liftimCode)
                .firstOrNull() ?: kotlin.run { finish(); return }
        liftimCodeName.text = liftimCodeInfo.name

        container.setOnClickListener {
            finish()
        }

        swipeContainer.onSwipeListener = object : SwipeDownDiscardView.OnSwipeListener {
            override fun onSwipe(proportion: Float) {
            }

            override fun onDiscard() {
                swipeContainer.visibility = View.GONE
                supportFragmentManager.beginTransaction()
                        .replace(android.R.id.content, ProgressDialog())
                        .commit()
                Observable.create<Boolean> {
                    val response = LiftimContext.getLiftimService()
                            .deleteLiftimCode(liftimCode, LiftimContext.getToken())
                            .execute()
                    if (!response.isSuccessful) {
                        it.onError(Throwable())
                    }
                    it.onComplete()
                }
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(observer)
                disposables.add(observer)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.clear()
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(0, R.anim.fade_out)
    }
}
