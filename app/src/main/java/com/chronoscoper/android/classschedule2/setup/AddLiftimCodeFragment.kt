/*
 * Copyright 2017 Chronoscope
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
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import com.chronoscoper.android.classschedule2.R
import com.chronoscoper.android.classschedule2.task.LiftimCodeInfoLoader
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subscribers.DisposableSubscriber
import kotterknife.bindView

class AddLiftimCodeFragment : BaseSetupFragment() {
    override fun onCreateView(
            inflater: LayoutInflater?,
            container: ViewGroup?,
            savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_add_liftim_code, container, false)
    }

    private val liftimCode by bindView<EditText>(R.id.liftim_code)
    private val okButton by bindView<Button>(R.id.ok)

    private val disposables = CompositeDisposable()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        okButton.setOnClickListener {
            it.isEnabled = false
            val liftimCode = liftimCode.text.toString().toLong()
            val subscriber = object : DisposableSubscriber<Unit>() {
                override fun onError(t: Throwable?) {
                }

                override fun onNext(t: Unit?) {
                }

                override fun onComplete() {
                    PreferenceManager.getDefaultSharedPreferences(context)
                            .edit()
                            .putLong(getString(R.string.p_default_liftim_code), liftimCode)
                            .apply()
                    nextStep()
                }
            }
            Flowable.defer { Flowable.just(LiftimCodeInfoLoader(liftimCode).run()) }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(subscriber)
            disposables.add(subscriber)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        disposables.clear()
    }
}
