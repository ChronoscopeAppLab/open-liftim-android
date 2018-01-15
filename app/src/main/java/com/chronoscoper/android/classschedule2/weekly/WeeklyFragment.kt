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
import android.graphics.PorterDuff
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.chronoscoper.android.classschedule2.R
import com.chronoscoper.android.classschedule2.sync.LiftimSyncEnvironment
import com.chronoscoper.android.classschedule2.sync.WeeklyItem
import com.chronoscoper.android.classschedule2.util.obtainColorCorrespondsTo
import com.chronoscoper.android.classschedule2.view.BottomMarginItemDecoration
import com.chronoscoper.android.classschedule2.view.RecyclerViewHolder
import kotterknife.bindView

class WeeklyFragment : Fragment() {
    override fun onCreateView(
            inflater: LayoutInflater?,
            container: ViewGroup?,
            savedInstanceState: Bundle?): View? =
            inflater?.inflate(R.layout.fragment_weekly, container, false)

    private val tabs by bindView<TabLayout>(R.id.tab)
    private val pager by bindView<ViewPager>(R.id.pager)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        pager.adapter = Adapter(context, childFragmentManager)
        tabs.setupWithViewPager(pager)
    }

    class Adapter(private val context: Context,
                  fm: FragmentManager) : FragmentStatePagerAdapter(fm) {
        private val data = mutableListOf<WeeklyItem>()

        init {
            data.addAll(LiftimSyncEnvironment.getOrmaDatabase()
                    .selectFromWeeklyItem()
                    .liftimCodeEq(LiftimSyncEnvironment.getLiftimCode())
                    .orderByDayOfWeekAsc())
        }

        override fun getPageTitle(position: Int): CharSequence? {
            val dayOfWeek = data[position].dayOfWeek
            return when (dayOfWeek) {
                1 -> context.getString(R.string.day_of_week_sunday)
                2 -> context.getString(R.string.day_of_week_monday)
                3 -> context.getString(R.string.day_of_week_tuesday)
                4 -> context.getString(R.string.day_of_week_wednesday)
                5 -> context.getString(R.string.day_of_week_thursday)
                6 -> context.getString(R.string.day_of_week_friday)
                7 -> context.getString(R.string.day_of_week_saturday)
                else -> null
            }
        }

        override fun getItem(position: Int): Fragment = DayFragment.obtain(data[position].dayOfWeek)

        override fun getCount(): Int = data.size

        class DayFragment : Fragment() {
            companion object {
                private const val DAY_OF_WEEK = "day_of_week"
                fun obtain(dayOfWeek: Int): DayFragment {
                    val f = DayFragment()
                    f.arguments = Bundle().apply {
                        putInt(DAY_OF_WEEK, dayOfWeek)
                    }
                    return f
                }
            }

            override fun onCreateView(
                    inflater: LayoutInflater?,
                    container: ViewGroup?,
                    savedInstanceState: Bundle?): View? =
                    inflater?.inflate(R.layout.fragment_day, container, false)

            private val list by bindView<RecyclerView>(R.id.list)

            override fun onActivityCreated(savedInstanceState: Bundle?) {
                super.onActivityCreated(savedInstanceState)

                list.adapter = DayAdapter(context, arguments.getInt(DAY_OF_WEEK))
                list.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
                list.addItemDecoration(BottomMarginItemDecoration())
            }

            class DayAdapter(context: Context, dayOfWeek: Int)
                : RecyclerView.Adapter<RecyclerViewHolder>() {
                private val data = LiftimSyncEnvironment.getOrmaDatabase()
                        .selectFromWeeklyItem()
                        .liftimCodeEq(LiftimSyncEnvironment.getLiftimCode())
                        .dayOfWeekEq(dayOfWeek)
                        .firstOrNull()
                        ?.apply {
                            subjects = LiftimSyncEnvironment.getGson()
                                    .fromJson(serializedSubjects, Array<String>::class.java)
                        } ?: WeeklyItem().apply { subjects = arrayOf() }

                private val inflater by lazy { LayoutInflater.from(context) }

                override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int):
                        RecyclerViewHolder = RecyclerViewHolder(
                        inflater.inflate(R.layout.weekly_day_item, parent, false))


                override fun onBindViewHolder(holder: RecyclerViewHolder?, position: Int) {
                    val view = holder?.itemView ?: return
                    val subjectName = data.subjects[position]

                    val subject = view.findViewById<TextView>(R.id.subject)
                    subject.text = subjectName
                    val index = view.findViewById<TextView>(R.id.index)
                    index.text = (position + data.minIndex).toString()
                    index.background.setColorFilter(
                            obtainColorCorrespondsTo(subjectName), PorterDuff.Mode.SRC_IN)
                }

                override fun getItemCount(): Int = data.subjects.size
            }
        }
    }
}
