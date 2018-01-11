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

import android.app.DatePickerDialog
import android.content.Context
import android.graphics.PorterDuff
import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.chronoscoper.android.classschedule2.BaseActivity
import com.chronoscoper.android.classschedule2.R
import com.chronoscoper.android.classschedule2.sync.Info
import com.chronoscoper.android.classschedule2.sync.InfoRemoteModel
import com.chronoscoper.android.classschedule2.sync.LiftimSyncEnvironment
import com.chronoscoper.android.classschedule2.util.obtainColorCorrespondsTo
import com.chronoscoper.android.classschedule2.view.RecyclerViewHolder
import kotterknife.bindView
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat

class EditTimetableActivity : BaseActivity() {
    private val liftimCodeImage by bindView<ImageView>(R.id.liftim_code_image)
    private val liftimCodeLabel by bindView<TextView>(R.id.liftim_code)
    private val dateLabel by bindView<TextView>(R.id.date)
    private val info by bindView<EditText>(R.id.info)
    private val classList by bindView<RecyclerView>(R.id.list)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_timetable)

        initialize()
    }

    private var date: DateTime? = null

    private fun initialize() {
        val liftimCode = LiftimSyncEnvironment.getLiftimCode()
        Glide.with(this)
                .load(LiftimSyncEnvironment.getApiUrl("liftim_code_image.png" +
                        "?liftim_code=$liftimCode" +
                        "&token=${LiftimSyncEnvironment.getToken()}"))
                .apply(RequestOptions.circleCropTransform())
                .into(liftimCodeImage)
        val liftimCodeInfo = LiftimSyncEnvironment.getOrmaDatabase()
                .selectFromLiftimCodeInfo().liftimCodeEq(liftimCode)
                .firstOrNull()
                ?: kotlin.run { finish(); return }
        liftimCodeLabel.text = liftimCodeInfo.name
        val latest = LiftimSyncEnvironment.getOrmaDatabase().selectFromInfo()
                .liftimCodeEq(liftimCode)
                .typeEq(Info.TYPE_TIMETABLE)
                .orderByDateAsc()
                .firstOrNull()
        if (latest == null) {
            date = DateTime.now(DateTimeZone.getDefault()).plusDays(1)
        } else {
            date = DateTime.parse(latest.date, DateTimeFormat.forPattern("yyyy/MM/dd"))
            if (latest.detail != null) {
                info.setText(latest.detail)
            }
        }
        dateLabel.text = date!!.toString(DateTimeFormat.fullDate())
        dateLabel.setOnClickListener {
            DatePickerDialog(this,
                    DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                        date = DateTime(year, month + 1, dayOfMonth, 0, 0)
                        dateLabel.text = date!!.toString(DateTimeFormat.fullDate())
                    }, date!!.year, date!!.monthOfYear - 1, date!!.dayOfMonth).show()
        }
        if (latest?.timetable != null) {
            classList.adapter = ClassAdapter(this, LiftimSyncEnvironment.getGson()
                    .fromJson(latest.timetable, InfoRemoteModel.Timetable::class.java))
        } else {
            classList.adapter = ClassAdapter(this, null)
        }
        classList.addItemDecoration(
                DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
    }

    class ClassAdapter(private val context: Context, initialItem: InfoRemoteModel.Timetable?)
        : RecyclerView.Adapter<ClassAdapter.DragViewHolder>() {
        private val data = mutableListOf<InfoRemoteModel.SubjectElement>()
        private var minIndex = 1

        init {
            if (initialItem != null) {
                data.addAll(initialItem.subjects)
                minIndex = initialItem.subjectMinIndex
            }
        }

        private val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {
            override fun onMove(
                    recyclerView: RecyclerView?,
                    viewHolder: RecyclerView.ViewHolder?,
                    target: RecyclerView.ViewHolder?): Boolean {
                val from = viewHolder?.adapterPosition ?: return true
                val to = target?.adapterPosition ?: return true
                val temp = data[from]
                data.removeAt(from)
                data.add(to, temp)
                notifyItemChanged(from)
                notifyItemChanged(to)
                notifyItemMoved(from, to)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder?, direction: Int) {
                // No handle
            }
        })

        override fun onAttachedToRecyclerView(recyclerView: RecyclerView?) {
            super.onAttachedToRecyclerView(recyclerView)
            itemTouchHelper.attachToRecyclerView(recyclerView)
        }

        private val inflater by lazy { LayoutInflater.from(context) }

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): DragViewHolder =
                DragViewHolder(
                        inflater.inflate(R.layout.timetable_editor_item, parent, false))

        override fun getItemCount(): Int = data.size

        override fun onBindViewHolder(holder: DragViewHolder?, position: Int) {
            val view = holder?.itemView ?: return
            val item = data[position]

            val index = view.findViewById<TextView>(R.id.index)
            val subject = view.findViewById<TextView>(R.id.subject)
            val detail = view.findViewById<TextView>(R.id.detail)
            val delete = view.findViewById<View>(R.id.delete)

            index.text = (position + minIndex).toString()
            subject.text = item.subject
            index.background.setColorFilter(
                    obtainColorCorrespondsTo(item.subject), PorterDuff.Mode.SRC_IN)
            detail.text = item.detail
            delete.setOnClickListener {
                data.removeAt(holder.adapterPosition)
                notifyItemRemoved(holder.adapterPosition)
            }
        }

        inner class DragViewHolder(itemView: View) : RecyclerViewHolder(itemView) {
            init {
                itemView.findViewById<View>(R.id.drag_handle)
                        .setOnTouchListener { view, motionEvent ->
                            if (motionEvent.actionMasked == MotionEvent.ACTION_DOWN) {
                                itemTouchHelper.startDrag(this)
                            }
                            return@setOnTouchListener false
                        }
            }
        }

        fun generateCurrentStateJson(): String {
            TODO()
        }
    }
}
