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
import android.os.Build
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.support.v7.widget.helper.ItemTouchHelper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import com.chronoscoper.android.classschedule2.BaseActivity
import com.chronoscoper.android.classschedule2.R
import com.chronoscoper.android.classschedule2.sync.LiftimContext
import com.chronoscoper.android.classschedule2.sync.WeeklyItem
import com.chronoscoper.android.classschedule2.task.RegisterWeeklyService
import com.chronoscoper.android.classschedule2.transition.FabExpandTransition
import com.chronoscoper.android.classschedule2.util.EventMessage
import com.chronoscoper.android.classschedule2.util.obtainColorCorrespondsTo
import com.chronoscoper.android.classschedule2.util.removedAt
import com.chronoscoper.android.classschedule2.view.RecyclerViewHolder
import kotterknife.bindView
import org.greenrobot.eventbus.EventBus

class EditWeeklyActivity : BaseActivity() {

    private val toolbar by bindView<Toolbar>(R.id.toolbar)
    private val tabs by bindView<TabLayout>(R.id.tabs)
    private val pager by bindView<ViewPager>(R.id.pager)

    private val adapter by lazy { PagerAdapter(supportFragmentManager) }

    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_edit_weekly)
        setSupportActionBar(toolbar)
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.sharedElementEnterTransition = FabExpandTransition()
        }

        EditWeeklyTemporary.weeklyItems = LiftimContext.getOrmaDatabase()
                .selectFromWeeklyItem()
                .liftimCodeEq(LiftimContext.getLiftimCode())
                .orderByDayOfWeekAsc()
                .toList()

        pager.adapter = adapter
        tabs.tabMode = TabLayout.MODE_SCROLLABLE
        tabs.tabGravity = TabLayout.GRAVITY_FILL
        tabs.setupWithViewPager(pager)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.options_edit_weekly, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        item ?: return false
        if (item.itemId == R.id.options_done) {
            LiftimContext.executeBackground {
                EditWeeklyTemporary.weeklyItems?.let { weeklyItems ->
                    val liftimCode = LiftimContext.getLiftimCode()
                    LiftimContext.getOrmaDatabase().deleteFromWeeklyItem()
                            .liftimCodeEq(liftimCode)
                            .execute()
                    val element = hashMapOf<String, WeeklyItem>()
                    weeklyItems.forEachIndexed { index, elem ->
                        elem.subjects = adapter.fragments[index]?.adapter
                                ?.subjects?.toTypedArray() ?: arrayOf()
                        elem.serializedSubjects = LiftimContext.getGson().toJson(elem.subjects)
                        elem.liftimCode = liftimCode
                        val shortSubjects = mutableListOf<String>()
                        elem.subjects.forEach { subject ->
                            val shortName = LiftimContext.getOrmaDatabase().selectFromSubject()
                                    .liftimCodeEq(liftimCode)
                                    .subjectEq(subject)
                                    .firstOrNull()?.shortSubject
                            shortSubjects.add(shortName ?: subject)
                        }
                        elem.shortSubjects = shortSubjects.toTypedArray()
                        element[(index + 1).toString()] = elem
                    }
                    LiftimContext.getOrmaDatabase().prepareInsertIntoWeeklyItem()
                            .executeAll(weeklyItems.slice(0..6))
                    RegisterWeeklyService.start(this, LiftimContext.getGson()
                            .toJson(element).apply {
                                Log.d("AAAAAA", this)
                            })
                    EventBus.getDefault()
                            .post(EventMessage(WeeklyFragment.EVENT_WEEKLY_TIMETABLE_UPDATED))
                }
            }
            animateFinishCompat()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    fun updateData(dayOfWeek: Int, index: Int, subject: String) {
        if (dayOfWeek in 1..7) {
            if (index >= 0) {
                EditWeeklyTemporary.weeklyItems!![dayOfWeek - 1].subjects[index] = subject
                adapter.fragments[dayOfWeek - 1]?.adapter?.let {
                    it.subjects[index] = subject
                    it.notifyItemChanged(index)
                }
            } else {
                EditWeeklyTemporary.weeklyItems!![dayOfWeek - 1].subjects += subject
                adapter.fragments[dayOfWeek - 1]?.adapter?.let {
                    it.subjects += subject
                    it.notifyItemInserted(it.subjects.lastIndex)
                }
            }
        }
    }

    private inner class PagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {
        override fun getPageTitle(position: Int): CharSequence? {
            return when (position + 1) {
                1 -> getString(R.string.day_of_week_sunday)
                2 -> getString(R.string.day_of_week_monday)
                3 -> getString(R.string.day_of_week_tuesday)
                4 -> getString(R.string.day_of_week_wednesday)
                5 -> getString(R.string.day_of_week_thursday)
                6 -> getString(R.string.day_of_week_friday)
                7 -> getString(R.string.day_of_week_saturday)
                else -> null
            }
        }

        val fragments = arrayOfNulls<DayFragment>(7)

        override fun getItem(position: Int): Fragment {
            val fragment = DayFragment.obtain(position + 1)
            fragments[position] = fragment
            return fragment
        }

        override fun getCount(): Int = 7

    }

    class DayFragment : Fragment() {
        companion object {
            private const val DAY_OF_WEEK = "DAY_OF_WEEK"
            fun obtain(dayOfWeek: Int): DayFragment {
                val f = DayFragment()
                f.arguments = Bundle().apply {
                    putInt(DAY_OF_WEEK, dayOfWeek)
                }
                return f
            }
        }

        override fun onCreateView(
                inflater: LayoutInflater,
                container: ViewGroup?,
                savedInstanceState: Bundle?): View? =
                inflater.inflate(R.layout.fragment_weekly_editor_day, container, false)

        private val list by bindView<RecyclerView>(R.id.list)
        private val startIndex by bindView<View>(R.id.start_index)
        private val add by bindView<View>(R.id.add_subject)

        val adapter by lazy { DayAdapter(context!!, arguments!!.getInt(DAY_OF_WEEK)) }

        override fun onActivityCreated(savedInstanceState: Bundle?) {
            super.onActivityCreated(savedInstanceState)

            list.adapter = adapter
            list.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))

            startIndex.setOnClickListener {
                ChangeMinIndexDialog.newInstance(arguments!!.getInt(DAY_OF_WEEK))
                        .show(childFragmentManager, null)
            }
            add.setOnClickListener {
                ClassEditorDialog.newInstance(arguments!!.getInt(DAY_OF_WEEK))
                        .show(childFragmentManager, null)
            }
        }

        inner class DayAdapter(context: Context, private val dayOfWeek: Int)
            : RecyclerView.Adapter<RecyclerViewHolder>() {
            private val data by lazy {
                EditWeeklyTemporary.weeklyItems?.forEach {
                    if (it.dayOfWeek == dayOfWeek) {
                        subjects.addAll(it.subjects)
                        return@lazy it
                    }
                } ?: kotlin.run { EditWeeklyTemporary.weeklyItems = mutableListOf() }
                val item = WeeklyItem()
                EditWeeklyTemporary.weeklyItems!!.add(dayOfWeek - 1, item)
                return@lazy item
            }

            val subjects = mutableListOf<String>()

            var minIndex: Int
                set(value) {
                    data.minIndex = value
                    notifyDataSetChanged()
                }
                get() {
                    return data.minIndex
                }

            private val itemTouchHelper =
                    ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
                            ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {
                        override fun onMove(
                                recyclerView: RecyclerView?,
                                viewHolder: RecyclerView.ViewHolder?,
                                target: RecyclerView.ViewHolder?): Boolean {
                            val from = viewHolder?.adapterPosition ?: return true
                            val to = target?.adapterPosition ?: return true
                            val temp = subjects[from]
                            subjects.removeAt(from)
                            subjects.add(to, temp)
                            notifyItemChanged(from)
                            notifyItemChanged(to)
                            notifyItemMoved(from, to)
                            return true
                        }

                        override fun onSwiped(viewHolder: RecyclerView.ViewHolder?, direction: Int) {
                            // No handle
                        }
                    })

            override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
                super.onAttachedToRecyclerView(recyclerView)
                itemTouchHelper.attachToRecyclerView(recyclerView)
            }

            private val inflater by lazy { LayoutInflater.from(context) }

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
                    RecyclerViewHolder = DragViewHolder(
                    inflater.inflate(R.layout.weekly_edit_item, parent, false))


            override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
                val view = holder.itemView
                val subjectName = subjects[position]

                val subject = view.findViewById<TextView>(R.id.subject)
                subject.text = subjectName
                val index = view.findViewById<TextView>(R.id.index)
                index.text = (position + minIndex).toString()
                index.background.setColorFilter(
                        obtainColorCorrespondsTo(subjectName), PorterDuff.Mode.SRC_IN)
                view.setOnClickListener {
                    ClassEditorDialog.newInstance(data.dayOfWeek, position)
                            .show(childFragmentManager, null)
                }
                view.findViewById<View>(R.id.delete).setOnClickListener {
                    val pos = holder.adapterPosition
                    subjects.removeAt(pos)
                    data.subjects = data.subjects.removedAt(pos)
                    notifyDataSetChanged()
                }
            }

            override fun getItemCount(): Int = subjects.size

            private inner class DragViewHolder(itemView: View) : RecyclerViewHolder(itemView) {
                init {
                    itemView.findViewById<View>(R.id.drag_handle).setOnTouchListener { _, event ->
                        if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                            itemTouchHelper.startDrag(this)
                        }
                        return@setOnTouchListener true
                    }
                }
            }
        }

        class ChangeMinIndexDialog : DialogFragment() {
            companion object {
                private const val DAY_OF_WEEK = "DAY_OF_WEEK"
                fun newInstance(dayOfWeek: Int): ChangeMinIndexDialog {
                    val result = ChangeMinIndexDialog()
                    result.arguments = Bundle().apply {
                        putInt(DAY_OF_WEEK, dayOfWeek)
                    }
                    return result
                }
            }

            override fun onCreateView(
                    inflater: LayoutInflater,
                    container: ViewGroup?,
                    savedInstanceState: Bundle?): View? =
                    inflater.inflate(R.layout.fragment_change_min_index, container, false)

            private val startTime by bindView<EditText>(R.id.start_time)

            private val increase by bindView<ImageButton>(R.id.increase)
            private val decrease by bindView<ImageButton>(R.id.decrease)

            private val adapter by lazy {
                val dayOfWeek = arguments?.getInt(DAY_OF_WEEK, -1) ?: -1
                if (dayOfWeek !in 1..7) {
                    throw IllegalArgumentException("Day of week must be in [1..7]")
                }
                (activity as EditWeeklyActivity)
                        .adapter.fragments[dayOfWeek - 1]?.adapter!!
            }

            override fun onActivityCreated(savedInstanceState: Bundle?) {
                super.onActivityCreated(savedInstanceState)
                notifyStartTimeChanged()


                increase.setOnClickListener {
                    adapter.minIndex += 1
                    notifyStartTimeChanged()
                }

                decrease.setOnClickListener {
                    adapter.minIndex -= 1
                    notifyStartTimeChanged()
                }

                startTime.addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(s: Editable?) = Unit

                    override fun beforeTextChanged(s: CharSequence?,
                                                   start: Int, count: Int, after: Int) = Unit

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        if (s != null) {
                            try {
                                adapter.minIndex = Integer.parseInt(s.toString())
                            } catch (ignore: NumberFormatException) {
                            }
                        }
                    }
                })
            }

            private fun notifyStartTimeChanged() =
                    startTime.setText(adapter.minIndex.toString())
        }

        class ClassEditorDialog : DialogFragment() {
            companion object {
                private const val DAY_OF_WEEK = "DAY_OF_WEEK"
                private const val INDEX = "INDEX"
                fun newInstance(dayOfWeek: Int, index: Int = -1): ClassEditorDialog {
                    return ClassEditorDialog().apply {
                        isCancelable = false
                        arguments = Bundle().apply {
                            putInt(DAY_OF_WEEK, dayOfWeek)
                            putInt(INDEX, index)
                        }
                    }
                }
            }

            override fun onCreateView(
                    inflater: LayoutInflater,
                    container: ViewGroup?,
                    savedInstanceState: Bundle?): View? =
                    inflater.inflate(R.layout.fragment_weekly_class_editor, container, false)

            private val subjectInput by bindView<EditText>(R.id.subject)
            private val okButton by bindView<Button>(R.id.ok)
            private val cancelButton by bindView<Button>(R.id.cancel)

            override fun onActivityCreated(savedInstanceState: Bundle?) {
                super.onActivityCreated(savedInstanceState)

                val dayOfWeek = arguments?.getInt(DAY_OF_WEEK, -1) ?: -1
                val index = arguments?.getInt(INDEX, -1) ?: -1
                val subject =
                        if (index >= 0) {
                            EditWeeklyTemporary.weeklyItems!![dayOfWeek - 1].subjects[index] ?: ""
                        } else {
                            ""
                        }
                subjectInput.setText(subject)

                okButton.setOnClickListener {
                    updateData(dayOfWeek, index)
                    dismiss()
                }
                cancelButton.setOnClickListener {
                    dismiss()
                }
            }

            private fun updateData(dayOfWeek: Int, index: Int) {
                (activity as EditWeeklyActivity)
                        .updateData(dayOfWeek, index, subjectInput.text.toString())
            }
        }
    }
}
