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

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.chronoscoper.android.classschedule2.R
import com.chronoscoper.android.classschedule2.sync.Info
import com.chronoscoper.android.classschedule2.sync.LiftimContext
import com.chronoscoper.android.classschedule2.util.DateTimeUtils
import com.chronoscoper.android.classschedule2.view.BottomMarginItemDecoration
import kotterknife.bindView

class TimetableFragment : Fragment() {
    companion object {
        private const val ID = "target_id"
        fun obtain(id: String): TimetableFragment {
            val result = TimetableFragment()
            result.arguments = Bundle().apply {
                putString(ID, id)
            }
            return result
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_timetable, container, false)

    private val timetableList by bindView<RecyclerView>(R.id.timetable_list)
    private val dateLabel by bindView<TextView>(R.id.date)
    private val doneButton by bindView<View>(R.id.done)
    private val infoLabel by bindView<TextView>(R.id.info)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        initTimetable()

        doneButton.setOnClickListener {
            LiftimContext.getOrmaDatabase()
                    .updateInfo().deleted(true).idEq(currentItemId).execute()
            initTimetable()
        }
    }

    private var currentItemId = ""

    private fun initTimetable() {
        val timetable = obtainTargetElement()
        timetableList.adapter = TimetableAdapter(context!!, timetable)
        timetableList.addItemDecoration(
                DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        timetableList.addItemDecoration(BottomMarginItemDecoration())
        if (timetable != null) {
            dateLabel.text = DateTimeUtils.getParsedDateExpression(timetable.date)
            currentItemId = timetable.id
            doneButton.visibility = View.VISIBLE
            dateLabel.visibility = View.VISIBLE
            if (!timetable.detail.isNullOrEmpty()) {
                infoLabel.visibility = View.VISIBLE
                infoLabel.text = timetable.detail
            } else {
                infoLabel.visibility = View.GONE
            }
        } else {
            doneButton.visibility = View.GONE
            dateLabel.visibility = View.GONE
            infoLabel.visibility = View.GONE
        }
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
