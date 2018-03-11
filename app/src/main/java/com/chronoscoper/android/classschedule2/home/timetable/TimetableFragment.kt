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
package com.chronoscoper.android.classschedule2.home.timetable

import android.animation.ArgbEvaluator
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.chronoscoper.android.classschedule2.R
import com.chronoscoper.android.classschedule2.sync.Info
import com.chronoscoper.android.classschedule2.sync.LiftimContext
import com.chronoscoper.android.classschedule2.util.DateTimeUtils
import com.chronoscoper.android.classschedule2.util.EventMessage
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import kotterknife.bindView
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.joda.time.DateTime
import java.util.concurrent.TimeUnit

class TimetableFragment : Fragment() {
    companion object {
        private const val TAG = "TimetableFragment"
        private const val ID = "target_id"
        fun obtain(id: String): TimetableFragment {
            val result = TimetableFragment()
            result.arguments = Bundle().apply {
                putString(ID, id)
            }
            return result
        }

        private val BACKGROUND_COLORS = arrayOf(-0xead7b9/*00:00*/, -0xd8b67e/*03:00*/,
                -0xc79351/*06:00*/, -0x9b3311/*09:00*/, -0x640014/*12:00*/, -0x3e0b01/*15:00*/,
                -0x2776a1/*18:00*/, -0xc5e693/*21:00*/, -0xead7b9/*00:00 once again!!*/)

        const val EVENT_TIMETABLE_UPDATED = "TIMETABLE_UPDATED"
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_timetable, container, false)

    private val timetableList by bindView<RecyclerView>(R.id.timetable_list)
    private val dateLabel by bindView<TextView>(R.id.date)
    private val infoLabel by bindView<TextView>(R.id.info)
    private val header by bindView<View>(R.id.header)
    private val background by bindView<View>(R.id.background)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        EventBus.getDefault().register(this)
        initTimetable()
        setUpBackgroundUpdater()
    }

    override fun onDetach() {
        super.onDetach()
        EventBus.getDefault().unregister(this)
    }

    @Suppress("UNUSED")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onTimetableUpdated(event: EventMessage) {
        if (event.type == EVENT_TIMETABLE_UPDATED) {
            Log.d(TAG, "Event: Updating timetable UI...")
            initTimetable()
        } else {
            Log.i(TAG, "Event: Not subscribing event $event. Ignoreing...")
        }
    }

    private var currentItemId = ""

    private fun initTimetable() {
        val timetable = obtainTargetElement()
        timetableList.adapter = TimetableAdapter(context!!, timetable)
        timetableList.addItemDecoration(
                DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        timetableList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                val scrolledY = recyclerView?.computeVerticalScrollOffset() ?: return
                background.alpha =
                        if (scrolledY > 400) {
                            100f / 400f
                        } else {
                            (500 - scrolledY).toFloat() / 500f
                        }
            }
        })
        if (timetable != null) {
            dateLabel.text = DateTimeUtils.getParsedDateExpression(timetable.date)
            currentItemId = timetable.id
            dateLabel.visibility = View.VISIBLE
            if (!timetable.detail.isNullOrEmpty()) {
                infoLabel.visibility = View.VISIBLE
                infoLabel.text = timetable.detail
            } else {
                infoLabel.visibility = View.GONE
            }
        } else {
            dateLabel.visibility = View.GONE
            infoLabel.visibility = View.GONE
        }
        header.addOnLayoutChangeListener { v, _, _, _, _, _, _, _, _ ->
            timetableList.setPadding(0, header.height, 0, timetableList.paddingBottom)
        }
    }

    private val disposables = CompositeDisposable()
    private val argbEvaluator = ArgbEvaluator()

    private fun setUpBackgroundUpdater() {
        background.background = ColorDrawable(calculateCurrentColor())
        val subscriber = object : DisposableObserver<Int>() {
            override fun onComplete() {
            }

            override fun onNext(t: Int) {
                (background.background as ColorDrawable).color = t
            }

            override fun onError(e: Throwable) {
                Log.d(TAG, "Error calculating background color", e)
            }
        }

        Observable.defer {
            Observable.just(calculateCurrentColor())
        }
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .delay(5L, TimeUnit.MINUTES)
                .repeat()
                .subscribe(subscriber)
        disposables.add(subscriber)
    }

    private fun calculateCurrentColor(): Int {
        Log.d(TAG, "Calculating background color...")
        val dateTime = DateTime.now()
        val index = dateTime.hourOfDay
        val offset = dateTime.minuteOfHour.toFloat() / 60f
        return argbEvaluator.evaluate(offset,
                BACKGROUND_COLORS[index / 3], BACKGROUND_COLORS[index / 3 + 1]) as Int
    }

    override fun onDestroyView() {
        super.onDestroyView()
        disposables.clear()
    }

    private fun obtainTargetElement(): Info? {
        val specified = arguments?.getString(ID)
        return if (specified == null) {
            LiftimContext.getOrmaDatabase()
                    .selectFromInfo().typeEq(Info.TYPE_TIMETABLE)
                    .liftimCodeEq(LiftimContext.getLiftimCode())
                    .deletedEq(false)
                    .orderByDateAsc()
                    .firstOrNull()
        } else {
            LiftimContext.getOrmaDatabase()
                    .selectFromInfo().typeEq(Info.TYPE_TIMETABLE)
                    .liftimCodeEq(LiftimContext.getLiftimCode())
                    .idEq(specified)
                    .firstOrNull()
        }
    }
}
