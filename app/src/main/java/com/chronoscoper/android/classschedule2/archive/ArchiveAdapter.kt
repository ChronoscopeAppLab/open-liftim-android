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
package com.chronoscoper.android.classschedule2.archive

import android.app.Activity
import android.graphics.PorterDuff
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.chronoscoper.android.classschedule2.R
import com.chronoscoper.android.classschedule2.sync.Info
import com.chronoscoper.android.classschedule2.sync.InfoRemoteModel
import com.chronoscoper.android.classschedule2.sync.LiftimContext
import com.chronoscoper.android.classschedule2.util.DateTimeUtils
import com.chronoscoper.android.classschedule2.util.getColorForInfoType
import com.chronoscoper.android.classschedule2.view.RecyclerViewHolder
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import kotterknife.bindView

class ArchiveAdapter(private val activity: Activity) : RecyclerView.Adapter<ArchiveAdapter.ArchiveViewHolder>() {
    companion object {
        private const val TAG = "ArchiveAdapter"
    }

    private val data = arrayListOf<Info>()

    private val disposables = CompositeDisposable()

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)

        val subscriber = object : DisposableObserver<List<Info>>() {
            override fun onComplete() {
                notifyDataSetChanged()
            }

            override fun onNext(t: List<Info>) {
                data.clear()
                data.addAll(t)
                Log.d(TAG,"${t.size} data existing")
            }

            override fun onError(e: Throwable) {
                Log.e(TAG, "Error while loading archive items", e)
            }
        }

        Observable.create<List<Info>> {
            val data = LiftimContext.getOrmaDatabase()
                    .selectFromInfo()
                    .liftimCodeEq(LiftimContext.getLiftimCode())
                    .deletedEq(true)
                    .remoteDeletedEq(true)
                    .orderByDateAsc()
                    .orderByTimeAsc()
                    .orderByTypeAsc()
                    .toList()
            it.onNext(data)
            it.onComplete()
        }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber)
        disposables.add(subscriber)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        disposables.clear()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArchiveViewHolder {
        val view = activity.layoutInflater
                .inflate(R.layout.archive_item, parent, false)
        val holder = ArchiveViewHolder(view)
        holder.delete.setOnClickListener {
            val position = holder.adapterPosition
            val item = data[position]
            data.removeAt(position)
            notifyItemRemoved(position)
            LiftimContext.executeBackground {
                LiftimContext.getOrmaDatabase()
                        .deleteFromInfo()
                        .liftimCodeEq(LiftimContext.getLiftimCode())
                        .idEq(item.id)
                        .execute()
            }
        }
        holder.restore.setOnClickListener {
            val position = holder.adapterPosition
            val item = data[position]
            data.removeAt(position)
            notifyItemRemoved(position)
            LiftimContext.executeBackground {
                LiftimContext.getOrmaDatabase()
                        .updateInfo()
                        .liftimCodeEq(LiftimContext.getLiftimCode())
                        .idEq(item.id)
                        .deleted(false)
                        .execute()
            }
        }
        return holder
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: ArchiveViewHolder, position: Int) {
        val item = data[position]
        if (item.type == Info.TYPE_TIMETABLE) {
            holder.title.text = activity.getString(R.string.info_title_timetable,
                    DateTimeUtils.getParsedDateExpression(item.date))
            val detailBuilder = StringBuilder()
            val timetable = LiftimContext.getGson()
                    .fromJson(item.timetable, InfoRemoteModel.Timetable::class.java)
            val lastIndex = timetable.subjects.lastIndex
            timetable.subjects.forEachIndexed { index, element ->
                detailBuilder.append(element.subject)
                if (index != lastIndex) {
                    detailBuilder.append(", ")
                }
            }
            holder.detail.text = detailBuilder.toString()
            bindText(null, holder.date)
            bindText(null, holder.linkUrl)
            holder.type.apply {
                background.setColorFilter(getColorForInfoType(item.type),
                        PorterDuff.Mode.SRC_IN)
                text = activity.getString(R.string.class_schedule)
            }

        } else {
            holder.apply {
                title.text = item.title
                bindText(item.detail, detail)
                bindText(item.date, date)
                bindText(item.link, linkUrl)
                type.background.setColorFilter(
                        getColorForInfoType(item.type), PorterDuff.Mode.SRC_IN)
                type.text = when (item.type) {
                    Info.TYPE_UNSPECIFIED -> {
                        activity.getString(R.string.type_unspecified)
                    }
                    Info.TYPE_EVENT -> {
                        activity.getString(R.string.type_event)
                    }
                    Info.TYPE_INFORMATION -> {
                        activity.getString(R.string.type_information)
                    }
                    Info.TYPE_SUBMISSION -> {
                        activity.getString(R.string.type_submission)
                    }
                    Info.TYPE_LOCAL_MEMO -> {
                        activity.getString(R.string.type_memo)
                    }
                    else -> {
                        Log.e(TAG, "Unknown type info. Using empty string(\"\")")
                        ""
                    }
                }
            }
        }
    }

    private fun bindText(text: String?, view: TextView) {
        if (text.isNullOrBlank()) {
            view.visibility = View.GONE
        } else {
            view.text = text
            view.visibility = View.VISIBLE
        }
    }

    class ArchiveViewHolder(itemView: View) : RecyclerViewHolder(itemView) {
        val title by bindView<TextView>(R.id.title)
        val detail by bindView<TextView>(R.id.detail)
        val date by bindView<TextView>(R.id.date)
        val linkUrl by bindView<TextView>(R.id.link_url)
        val type by bindView<TextView>(R.id.type)
        val restore by bindView<View>(R.id.restore)
        val delete by bindView<View>(R.id.delete)
    }
}
