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
package com.chronoscoper.android.classschedule2.home.timetable

import android.content.Context
import android.graphics.PorterDuff
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.chronoscoper.android.classschedule2.R
import com.chronoscoper.android.classschedule2.sync.Info
import com.chronoscoper.android.classschedule2.sync.InfoRemoteModel
import com.chronoscoper.android.classschedule2.sync.LiftimContext
import com.chronoscoper.android.classschedule2.util.obtainColorCorrespondsTo
import com.chronoscoper.android.classschedule2.view.RecyclerViewHolder

class TimetableAdapter(context: Context, timetableInfoElement: Info?) :
        RecyclerView.Adapter<RecyclerViewHolder>() {
    private val timetable by lazy {
        val timetableJson = timetableInfoElement?.timetable ?: return@lazy null
        LiftimContext.getGson()
                .fromJson(timetableJson, InfoRemoteModel.Timetable::class.java)
    }

    override fun getItemCount(): Int = timetable?.subjects?.size ?: 1

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        if (holder.itemViewType == ViewType.NORMAL.type) {
            val item = timetable?.subjects?.get(position) ?: return
            val view = holder.itemView
            val index = view.findViewById<TextView>(R.id.index)
            val subject = view.findViewById<TextView>(R.id.subject)
            val detail = view.findViewById<TextView>(R.id.detail)
            index.text = (timetable!!.subjectMinIndex + position).toString()
            index.background.setColorFilter(
                    obtainColorCorrespondsTo(item.subject), PorterDuff.Mode.SRC_IN)
            subject.text = item.subject
            if (item.detail.isNullOrEmpty()) {
                detail.visibility = View.GONE
                detail.setOnClickListener(null)
            } else {
                detail.visibility = View.VISIBLE
                detail.text = item.detail
                view.setOnClickListener {
                    detail.maxLines = if (detail.maxLines == 1) {
                        50
                    } else {
                        1
                    }
                }
            }
        }
    }

    private val inflater by lazy { LayoutInflater.from(context) }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder =
            if (viewType == ViewType.NORMAL.type) {
                RecyclerViewHolder(inflater.inflate(
                        R.layout.timetable_item, parent, false))
            } else {
                RecyclerViewHolder(inflater.inflate(
                        R.layout.timetable_placeholder, parent, false))
            }

    override fun getItemViewType(position: Int): Int =
            if (timetable != null) {
                ViewType.NORMAL.type
            } else {
                ViewType.PLACEHOLDER.type
            }

    private enum class ViewType(val type: Int) {
        NORMAL(1),
        PLACEHOLDER(2)
    }
}