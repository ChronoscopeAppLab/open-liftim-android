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
package com.chronoscoper.android.classschedule2.weekly

import android.content.Context
import android.support.v4.app.Fragment
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.TextView
import com.chronoscoper.android.classschedule2.R
import com.chronoscoper.android.classschedule2.sync.LiftimSyncEnvironment
import com.chronoscoper.android.classschedule2.sync.WeeklyItem
import com.chronoscoper.android.classschedule2.task.WeeklyLoader
import com.chronoscoper.android.classschedule2.view.RecyclerViewHolder
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subscribers.DisposableSubscriber
import kotterknife.bindView

class WeeklyFragment : Fragment() {
    override fun onCreateView(
            inflater: LayoutInflater?,
            container: ViewGroup?,
            savedInstanceState: Bundle?): View? =
            inflater?.inflate(R.layout.fragment_weekly, container, false)

    private val list by bindView<RecyclerView>(R.id.list)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        list.adapter = Adapter(context)
    }

    private class Adapter(val context: Context) : RecyclerView.Adapter<RecyclerViewHolder>() {
        private val data = mutableListOf<WeeklyItem>()
        private var rangeStart = Int.MAX_VALUE
        private var rangeEnd = Int.MIN_VALUE
        private val disposables = CompositeDisposable()

        override fun onAttachedToRecyclerView(recyclerView: RecyclerView?) {
            super.onAttachedToRecyclerView(recyclerView)

            val subscriber = object : DisposableSubscriber<Unit>() {
                override fun onNext(t: Unit?) {
                }

                override fun onComplete() {
                    initView(recyclerView ?: return)
                }

                override fun onError(t: Throwable?) {
                    initView(recyclerView ?: return)
                }
            }

            Flowable.defer {
                Flowable.just(
                        WeeklyLoader(LiftimSyncEnvironment.getLiftimCode(),
                                LiftimSyncEnvironment.getToken()).run())
            }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(subscriber)

            disposables.add(subscriber)
        }

        private fun initView(recyclerView: RecyclerView) {
            data.clear()
            LiftimSyncEnvironment.getOrmaDatabase().selectFromWeeklyItem()
                    .liftimCodeEq(LiftimSyncEnvironment.getLiftimCode())
                    .orderByDayOfWeekAsc().forEach {
                it.subjects = LiftimSyncEnvironment.getGson()
                        .fromJson(it.serializedSubjects, Array<String>::class.java)
                if (it.minIndex < rangeStart) {
                    rangeStart = it.minIndex
                }
                if (it.minIndex + it.subjects.size - 1 > rangeEnd) {
                    rangeEnd = it.minIndex + it.subjects.size - 1
                }
                data.add(it)
            }
            weeklyItemPositionsInitializer.run()
            (recyclerView.layoutManager as GridLayout)
            notifyDataSetChanged()
        }

        private val inflater by lazy { LayoutInflater.from(context) }

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerViewHolder? =
                when (viewType) {
                    ViewType.SQUARE.type -> {
                        RecyclerViewHolder(inflater.inflate(
                                R.layout.weekly_square, parent, false))
                    }
                    ViewType.LEFT_INDEX.type -> {
                        RecyclerViewHolder(inflater.inflate(
                                R.layout.weekly_left_index, parent, false))
                    }
                    ViewType.TOP_INDEX.type -> {
                        RecyclerViewHolder(inflater.inflate(
                                R.layout.weekly_top_index, parent, false))
                    }
                    ViewType.ITEM.type -> {
                        RecyclerViewHolder(inflater.inflate(
                                R.layout.weekly_cell, parent, false))
                    }
                    else -> {
                        null
                    }
                }


        override fun onBindViewHolder(holder: RecyclerViewHolder?, position: Int) {
            holder ?: return
            when (holder.itemViewType) {
                ViewType.LEFT_INDEX.type -> {
                    (holder.itemView as TextView).text = (position + rangeStart).toString()
                }
                ViewType.TOP_INDEX.type -> {
                    val dayOfWeek = weeklyItemPositions
                            .filter { it.value.start - 1 == position }.keys
                            .firstOrNull() ?: 0
                    val label = when (dayOfWeek) {
                        1 -> context.getString(R.string.day_of_week_sunday)
                        2 -> context.getString(R.string.day_of_week_monday)
                        3 -> context.getString(R.string.day_of_week_tuesday)
                        4 -> context.getString(R.string.day_of_week_wednesday)
                        5 -> context.getString(R.string.day_of_week_thursday)
                        6 -> context.getString(R.string.day_of_week_friday)
                        7 -> context.getString(R.string.day_of_week_saturday)
                        else -> return
                    }
                    (holder.itemView as TextView).text = label
                }
                ViewType.ITEM.type -> {
                    val dayOfWeek = weeklyItemPositions
                            .filter { position in it.value }.keys
                            .firstOrNull() ?: return
                    val index = position - (weeklyItemPositions[dayOfWeek] ?: return).start
                    val items = data.find { it.dayOfWeek == dayOfWeek } ?: return
                    (holder.itemView as TextView).text = items.subjects[index]
                }
            }
        }

        private val weeklyItemPositions = hashMapOf<Int, IntRange>()

        private val weeklyItemPositionsInitializer = Runnable {
            val result = hashMapOf<Int, IntRange>()
            var lastInsertedDayOfWeek = 0
            data.forEachIndexed { index, item ->
                if (index == 0) {
                    val start = rangeEnd - rangeStart + 3
                    result.put(item.dayOfWeek, start..start + item.subjects.size)
                    lastInsertedDayOfWeek = item.dayOfWeek
                } else {
                    val start = result[lastInsertedDayOfWeek]!!.endInclusive + 1
                    result.put(item.dayOfWeek, start..start + item.subjects.size)
                    lastInsertedDayOfWeek = item.dayOfWeek
                }
            }
            weeklyItemPositions.clear()
            weeklyItemPositions.putAll(result)
        }

        override fun getItemViewType(position: Int): Int {
            if (position == 0) {
                return ViewType.SQUARE.type
            }
            if (position in 1..(rangeEnd - rangeStart + 1)) {
                return ViewType.LEFT_INDEX.type
            }
            val topIndexes = mutableListOf<Int>()
            topIndexes.add(rangeEnd - rangeStart + 2)
            data.filterIndexed { index, _ -> index < data.size - 1 }.forEach {
                topIndexes.add(topIndexes.last() + it.subjects.size + 1)
            }
            if (position in topIndexes) {
                return ViewType.TOP_INDEX.type
            }
            return ViewType.ITEM.type
        }

        override fun getItemCount(): Int {
            var subjectCount = 0
            data.forEach {
                subjectCount += it.subjects.size
            }
            return if (data.isEmpty()) {
                0
            } else {
                1 /*Square*/ + rangeEnd - rangeStart + 1/*Left index*/ +
                        subjectCount/*Inner item*/ +
                        data.size/*Top index*/
            }
        }

    }

    enum class ViewType(val type: Int) {
        SQUARE(1),
        LEFT_INDEX(2),
        TOP_INDEX(3),
        ITEM(4)
    }
}