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
package com.chronoscoper.android.classschedule2.home.info

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ViewSwitcher
import com.chronoscoper.android.classschedule2.R
import com.chronoscoper.android.classschedule2.sync.LiftimContext
import com.chronoscoper.android.classschedule2.task.InfoLoader
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subscribers.DisposableSubscriber
import jp.wasabeef.recyclerview.adapters.SlideInBottomAnimationAdapter
import kotterknife.bindView

class InfoFragment : Fragment() {
    companion object {
        private const val TAG = "InfoFragment"
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_info, container, false)
    }

    private val switcher by bindView<ViewSwitcher>(R.id.switcher)
    private val placeholder by bindView<View>(R.id.placeholder_container)
    private val list by bindView<RecyclerView>(R.id.list)

    private val disposables = CompositeDisposable()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if (validInfoCount > 0) {
            Log.d(TAG, "Info found initializing list...")
            initView()
        } else {
            Log.d(TAG, "No info found. Showing placeholder...")
            placeholder.visibility = View.VISIBLE

            val initialSync = object : DisposableSubscriber<Unit>() {
                override fun onError(t: Throwable?) {
                    Log.e(TAG, "Sync error occurred", t)
                }

                override fun onNext(t: Unit?) {
                }

                override fun onComplete() {
                    if (validInfoCount > 0) {
                        Log.d(TAG, "Info synced. Switch content to list")
                        initView(false)
                    }
                }
            }
            InfoLoader.resetCursor()
            Flowable.defer {
                Flowable.just(
                        InfoLoader(LiftimContext.getLiftimCode(), LiftimContext.getToken()).run())
            }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(initialSync)
            disposables.addAll(initialSync)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        disposables.clear()
    }

    private fun initView(initialSyncNeeded: Boolean = true) {
        switcher.showNext()
        list.adapter = SlideInBottomAnimationAdapter(
                InfoRecyclerViewAdapter(activity!!, initialSyncNeeded))
        list.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
    }

    private val validInfoCount: Int
        get() {
            val count = LiftimContext.getOrmaDatabase()
                    .selectFromInfo()
                    .liftimCodeEq(LiftimContext.getLiftimCode())
                    .deletedEq(false)
                    .count()
            Log.d(TAG, "Current amount of info to show is $count")
            return count
        }
}
