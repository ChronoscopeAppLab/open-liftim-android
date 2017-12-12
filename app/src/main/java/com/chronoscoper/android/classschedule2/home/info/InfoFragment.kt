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
package com.chronoscoper.android.classschedule2.home.info

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.chronoscoper.android.classschedule2.R
import com.chronoscoper.android.classschedule2.sync.LiftimSyncEnvironment
import com.chronoscoper.android.classschedule2.task.InfoLoader
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subscribers.DisposableSubscriber

class InfoFragment : Fragment() {
    override fun onCreateView(
            inflater: LayoutInflater?,
            container: ViewGroup?,
            savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_info, container, false)
    }

    private val disposables = CompositeDisposable()
    private val subscriber = object : DisposableSubscriber<Unit>() {
        override fun onError(t: Throwable?) {
            initView()
        }

        override fun onNext(t: Unit?) {
        }

        override fun onComplete() {
            initView()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val liftimCode = LiftimSyncEnvironment.getLiftimCode()
        InfoLoader.resetCursor()
        Flowable.defer { Flowable.just(InfoLoader(liftimCode).run()) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber)
        disposables.addAll(subscriber)
    }

    fun initView() {

    }
}
