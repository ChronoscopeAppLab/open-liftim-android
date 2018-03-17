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

import android.graphics.PorterDuff
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.ViewSwitcher
import com.chronoscoper.android.classschedule2.R
import com.chronoscoper.android.classschedule2.sync.LiftimContext
import com.chronoscoper.android.classschedule2.sync.WeeklyItem
import com.chronoscoper.android.classschedule2.util.EventMessage
import com.chronoscoper.android.classschedule2.view.NavDrawerTargetFragment
import com.chronoscoper.android.classschedule2.view.ObservableScrollView
import com.google.gson.Gson
import kotterknife.bindView
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class WeeklyFragment : NavDrawerTargetFragment() {
    companion object {
        private const val TAG = "WeeklyFragment"
        const val EVENT_WEEKLY_TIMETABLE_UPDATED = "WEEKLY_TIMETABLE_UPDATED"
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_weekly, container, false)

    private val switcher by bindView<ViewSwitcher>(R.id.switcher)
    private val placeholderContainer by bindView<View>(R.id.placeholder_container)
    private val indexHoriz by bindView<View>(R.id.index_horiz)
    private val indexVert by bindView<ViewGroup>(R.id.index_vert)
    private val grid by bindView<RecyclerView>(R.id.grid)
    private val gridVertScroll by bindView<ObservableScrollView>(R.id.grid_horizontal_scroll)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        EventBus.getDefault().register(this)

        gridVertScroll.onScrollListener = {
            indexVert.scrollTo(0, it)
        }

        grid.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                indexHoriz.scrollTo(recyclerView!!.computeHorizontalScrollOffset(), 0)
            }
        })

        if (initializeTimetable()) {
            switcher.showNext()
        } else {
            placeholderContainer.visibility = View.VISIBLE
        }
    }

    @Suppress("UNUSED")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUpdated(event: EventMessage) {
        if (event.type == EVENT_WEEKLY_TIMETABLE_UPDATED) {
            Log.d(TAG, "Event: Updating weekly timetable...")
            initializeTimetable()
        } else {
            Log.i(TAG, "Not subscribing event $event. Ignoring...")
        }
    }

    override fun onDetach() {
        super.onDetach()
        Log.d(TAG, "Fragment is detached. Unsubscribing event bus...")
        EventBus.getDefault().unregister(this)
    }

    private fun initializeTimetable(): Boolean {
        val loadedData = LiftimContext.getOrmaDatabase().selectFromWeeklyItem()
                .liftimCodeEq(LiftimContext.getLiftimCode())
                .orderByDayOfWeekAsc()
                .toList()
        if (loadedData.isEmpty()) {
            return false
        }
        val data = ArrayList<WeeklyItem>(7)
        var minMinIndex = Integer.MAX_VALUE
        var rowCount = 0
        loadedData.forEach {
            if (it.subjects == null) {
                it.subjects = LiftimContext.getGson()
                        .fromJson(it.serializedSubjects, Array<String>::class.java)
            }
            if (it.minIndex < minMinIndex) minMinIndex = it.minIndex
            if (it.dayOfWeek in 1..7) {
                data.add(it.dayOfWeek - 1, it)
            }
        }
        loadedData.forEach {
            val count = it.minIndex + it.subjects.size - minMinIndex
            if (count > rowCount) rowCount = count
            Log.d(TAG, Gson().toJson(it))
        }
        if (data.isEmpty() || rowCount <= 0) {
            Log.e(TAG, "No subject registered. Skipping layout...")
            return false
        }
        grid.layoutManager = GridLayoutManager(context, rowCount,
                GridLayoutManager.HORIZONTAL, false)
        grid.adapter = WeeklyTimetableAdapter(context!!, data, minMinIndex, rowCount)
        setupIndexVert(indexVert, minMinIndex, rowCount)
        return true
    }

    private val inflater by lazy { LayoutInflater.from(context) }

    private fun setupIndexVert(container: ViewGroup, minMinIndex: Int, rowCount: Int) {
        val zebra = arrayOf(ContextCompat.getColor(context!!, R.color.zebra_dark),
                ContextCompat.getColor(context!!, R.color.zebra_light))
        container.removeAllViews()
        for (i in minMinIndex until minMinIndex + rowCount) {
            val view = inflater.inflate(R.layout.weekly_index_vert,
                    container, false) as TextView
            view.text = i.toString()
            view.background.setColorFilter(zebra[i % 2], PorterDuff.Mode.SRC)
            container.addView(view)
        }
    }
}
