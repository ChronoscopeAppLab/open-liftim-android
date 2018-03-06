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
import android.graphics.Color
import android.graphics.PorterDuff
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import com.chronoscoper.android.classschedule2.R
import com.chronoscoper.android.classschedule2.sync.WeeklyItem
import com.chronoscoper.android.classschedule2.util.obtainColorCorrespondsTo
import com.chronoscoper.android.classschedule2.view.RecyclerViewHolder

class WeeklyTimetableAdapter(context: Context,
                             private val data: List<WeeklyItem>,
                             private val minMinIndex: Int,
                             private val maxRowCount: Int)
    : RecyclerView.Adapter<RecyclerViewHolder>() {

    private val inflater by lazy { LayoutInflater.from(context) }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerViewHolder? {
        return RecyclerViewHolder(inflater.inflate(R.layout.weekly_cell, parent, false))
    }

    override fun getItemCount(): Int = maxRowCount * 7

    override fun onBindViewHolder(holder: RecyclerViewHolder?, position: Int) {
        holder ?: return
        val view = holder.itemView as TextView
        val name = getSubjectNameForPosition(position)
        view.text = name ?: ""
        val color = if (name == null) Color.WHITE else obtainColorCorrespondsTo(name)
        view.background.setColorFilter(color, PorterDuff.Mode.LIGHTEN)
    }

    private fun getSubjectNameForPosition(position: Int): String? {
        if (maxRowCount == 0) return null
        val dayOfWeek = position / maxRowCount
        val item = data[dayOfWeek]
        // ----------------------    ;;
        // | \ # S | M | T | W | ... ;;  Following data is expected to be rendered as left.
        // |=====================    ;;  Please note that "shortSubjects" in data is omitted.
        // | 0 # a |   |   | m |     ;;  ```
        // |---#---|---|---|---|-    ;;  [1:{"minIndex":0,"subjects":["a","b","c","d","e"]},
        // | 1 # b | f | j | n |     ;;   2:{"minIndex":1,"subjects":["f","g","h","i"]},
        // |---#---|---|---|---|-    ;;   3:{"minIndex":1,"subjects":["j","k","l"]},
        // | 2 # c | g | k | o | ... ;;   4:{"minIndex":0,"subjects":["m","n","o","p","q"]}
        // |---#---|---|---|---|-    ;;   ...
        // | 3 # d | h | l | p |     ;;  ]
        // |---#---|---|---|---|-    ;;  ```
        // | 4 # e | i |   | q |     ;;  In this situation, `maxRowCount` = 5, `minMinIndex` = 0.
        // ----------------------    ;;  Index of array for each day's subject name can be
        // calculated in following way.
        // (index_in_the_column) - (amount_of_top_offset)
        // = {(position_in_recycler_view) % (recycler_view_span_count)} - (amount_of_top_offset)
        // For example,
        //   index of cell "a": 0 % 5 - 0 = 0
        //   index of cell "f": 6 % 5 - 1 = 0
        //   index of cell "l": 13 % 5 - 1 = 2
        val index = position % maxRowCount - (item.minIndex - minMinIndex)
        if (index !in item.subjects.indices) return null
        return item.subjects[index]
    }
}
