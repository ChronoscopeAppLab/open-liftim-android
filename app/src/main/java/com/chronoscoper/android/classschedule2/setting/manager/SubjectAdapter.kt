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
package com.chronoscoper.android.classschedule2.setting.manager

import android.app.Activity
import android.graphics.PorterDuff
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.util.Pair
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.chronoscoper.android.classschedule2.R
import com.chronoscoper.android.classschedule2.sync.Subject
import com.chronoscoper.android.classschedule2.util.obtainColorCorrespondsTo
import kotterknife.bindView

class SubjectAdapter(private val activity: Activity, private val subjects: MutableList<Subject>)
    : RecyclerView.Adapter<SubjectAdapter.SubjectViewHolder>() {

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView?) {
        super.onAttachedToRecyclerView(recyclerView)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView?) {
        super.onDetachedFromRecyclerView(recyclerView)
        itemTouchHelper.attachToRecyclerView(null)
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): SubjectViewHolder {
        return SubjectViewHolder(
                activity.layoutInflater.inflate(R.layout.subject_item, parent, false))
    }

    override fun getItemCount(): Int = subjects.size

    override fun onBindViewHolder(holder: SubjectViewHolder?, position: Int) {
        holder ?: return
        val subject = subjects[position]
        holder.subjectColor.background.setColorFilter(
                obtainColorCorrespondsTo(subject.subject), PorterDuff.Mode.SRC_IN)
        holder.subjectName.text = subject.subject
        holder.itemView.setOnClickListener {
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity,
                    Pair(holder.subjectColor, activity.getString(R.string.t_subject_color)),
                    Pair(holder.subjectName, activity.getString(R.string.t_subject_name)),
                    /* Dummy entry to call 'vararg' overloaded method.
                     * Plz tell me *better* way to resolve it!! */
                    Pair(View(activity), ""))
                    .toBundle()
            EditSubjectActivity.open(activity, 1, subject, holder.adapterPosition, options)
        }
    }

    inner class SubjectViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dragHandle by bindView<View>(R.id.drag_handle)

        init {
            dragHandle.setOnTouchListener { _, motionEvent ->
                if (motionEvent.actionMasked == MotionEvent.ACTION_DOWN) {
                    itemTouchHelper.startDrag(this)
                }
                return@setOnTouchListener false
            }
        }

        val subjectColor by bindView<View>(R.id.subject_color)
        val subjectName by bindView<TextView>(R.id.subject)
    }

    private val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN, ItemTouchHelper.RIGHT) {
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
            viewHolder ?: return
            val position = viewHolder.adapterPosition
            subjects.removeAt(position)
            notifyItemRemoved(position)
        }
    })
}
