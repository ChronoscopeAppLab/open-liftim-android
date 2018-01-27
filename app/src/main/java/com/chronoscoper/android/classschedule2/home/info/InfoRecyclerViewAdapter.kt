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
package com.chronoscoper.android.classschedule2.home.info

import android.app.Activity
import android.app.ActivityOptions
import android.graphics.PorterDuff
import android.os.Build
import android.support.v7.widget.RecyclerView
import android.util.Pair
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import com.chronoscoper.android.classschedule2.R
import com.chronoscoper.android.classschedule2.home.info.detail.ViewInfoActivity
import com.chronoscoper.android.classschedule2.home.timetable.EditTimetableActivity
import com.chronoscoper.android.classschedule2.sync.Info
import com.chronoscoper.android.classschedule2.sync.LiftimContext
import com.chronoscoper.android.classschedule2.task.InfoLoader
import com.chronoscoper.android.classschedule2.util.DateTimeUtils
import com.chronoscoper.android.classschedule2.util.getColorForInfoType
import com.chronoscoper.android.classschedule2.util.openInCustomTab
import com.chronoscoper.android.classschedule2.view.RecyclerViewHolder
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subscribers.DisposableSubscriber
import kotterknife.bindView

open class InfoRecyclerViewAdapter(val activity: Activity) : RecyclerView.Adapter<RecyclerViewHolder>() {
    companion object {
        private const val VIEW_TYPE_INFO = 1
        private const val VIEW_TYPE_TIMETABLE = 2
    }

    private val disposables = CompositeDisposable()

    private val subscriber = object : DisposableSubscriber<Unit>() {
        override fun onError(t: Throwable?) {
            initView()
        }

        override fun onNext(t: Unit?) {
        }

        override fun onComplete() {
            initView()
        }
    }

    private val data = mutableListOf<Info>()

    private fun initView() {
        data.clear()
        loadData().forEach { data.add(it) }
        notifyDataSetChanged()
    }

    open fun loadData(): Iterable<Info> =
            LiftimContext.getOrmaDatabase().selectFromInfo()
                    .liftimCodeEq(LiftimContext.getLiftimCode())
                    .deletedEq(false)


    override fun onAttachedToRecyclerView(recyclerView: RecyclerView?) {
        super.onAttachedToRecyclerView(recyclerView)
        val liftimCode = LiftimContext.getLiftimCode()
        InfoLoader.resetCursor()
        Flowable.defer {
            Flowable.just(
                    InfoLoader(liftimCode, LiftimContext.getToken()).run())
        }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber)
        disposables.addAll(subscriber)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView?) {
        super.onDetachedFromRecyclerView(recyclerView)
        disposables.clear()
    }

    val inflater: LayoutInflater by lazy { LayoutInflater.from(activity) }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerViewHolder =
            if (viewType == VIEW_TYPE_INFO) {
                InfoHolder(inflater
                        .inflate(R.layout.info_item, parent, false))
            } else {
                TimetableHolder(inflater
                        .inflate(R.layout.info_timetable_item, parent, false))
            }

    override fun getItemCount(): Int = data.size

    override fun getItemViewType(position: Int): Int =
            if (data[position].type in -1..3) {
                VIEW_TYPE_INFO
            } else {
                VIEW_TYPE_TIMETABLE
            }

    override fun onBindViewHolder(holder: RecyclerViewHolder?, position: Int) {
        holder ?: return
        when (holder.itemViewType) {
            VIEW_TYPE_INFO -> {
                (holder as InfoHolder).bindContent(data[position])
            }
            VIEW_TYPE_TIMETABLE -> {
                (holder as TimetableHolder).bindContent(data[position])
            }
        }
    }

    inner class InfoHolder(itemView: View) : RecyclerViewHolder(itemView) {
        private val title by bindView<TextView>(R.id.title)
        private val detail by bindView<TextView>(R.id.detail)
        private val date by bindView<TextView>(R.id.date)
        private val linkUrl by bindView<TextView>(R.id.link_url)
        private val type by bindView<TextView>(R.id.type)
        private val delete by bindView<View>(R.id.delete)
        private val more by bindView<View>(R.id.more)

        fun bindContent(infoData: Info) {
            itemView.setOnClickListener {
                val options = if (Build.VERSION.SDK_INT
                        >= Build.VERSION_CODES.LOLLIPOP) {
                    ActivityOptions.makeSceneTransitionAnimation(activity,
                            Pair(itemView, activity.getString(R.string.t_background))
                    ).toBundle()
                } else {
                    null
                }
                ViewInfoActivity.open(activity, infoData, options)
            }
            title.text = infoData.title
            if (!infoData.detail.isNullOrEmpty()) {
                detail.visibility = View.VISIBLE
                detail.text = infoData.detail
            } else {
                detail.visibility = View.GONE
            }
            if (!infoData.date.isNullOrEmpty()) {
                date.visibility = View.VISIBLE
                date.text = infoData.date
            } else {
                date.visibility = View.GONE
            }
            if (!infoData.link.isNullOrEmpty()) {
                linkUrl.visibility = View.VISIBLE
                linkUrl.text = infoData.link
                linkUrl.setOnClickListener {
                    openInCustomTab(activity, infoData.link!!)
                }
            } else {
                linkUrl.visibility = View.GONE
                linkUrl.setOnClickListener(null)
            }
            type.background.setColorFilter(
                    getColorForInfoType(infoData.type), PorterDuff.Mode.SRC_IN)
            type.text = when (infoData.type) {
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
                    ""
                }
            }
            if (infoData.removable) {
                delete.apply {
                    visibility = View.VISIBLE
                    setOnClickListener {
                        LiftimContext.getOrmaDatabase().updateInfo()
                                .deleted(true)
                                .liftimCodeEq(LiftimContext.getLiftimCode())
                                .idEq(infoData.id)
                                .execute()
                        data.remove(infoData)
                        notifyItemRemoved(adapterPosition)
                    }
                }
            } else {
                delete.apply {
                    visibility = View.INVISIBLE
                    setOnClickListener(null)
                }
            }
            more.setOnClickListener {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    PopupMenu(activity, it, Gravity.TOP or Gravity.END)
                } else {
                    PopupMenu(activity, it)
                }.apply {
                    inflate(
                            if (!infoData.link.isNullOrEmpty()) {
                                R.menu.info_item_action
                            } else {
                                R.menu.info_item_action_no_link
                            })
                    setOnMenuItemClickListener {
                        when (it.itemId) {
                            R.id.item_open_link -> {
                                openInCustomTab(activity, infoData.link!!)
                            }
                            R.id.item_edit -> {
                                EditInfoActivity.open(activity, infoData.id)
                            }
                        }
                        true
                    }
                    show()
                }
            }
        }
    }

    inner class TimetableHolder(itemView: View) : RecyclerViewHolder(itemView) {
        private val title by bindView<TextView>(R.id.title)
        private val info by bindView<TextView>(R.id.detail)
        private val type by bindView<TextView>(R.id.type)
        private val delete by bindView<View>(R.id.delete)
        private val more by bindView<View>(R.id.more)

        fun bindContent(infoData: Info) {
            itemView.setOnClickListener {
                val options = if (Build.VERSION.SDK_INT
                        >= Build.VERSION_CODES.LOLLIPOP) {
                    ActivityOptions.makeSceneTransitionAnimation(activity,
                            Pair(itemView, activity.getString(R.string.t_background))
                    ).toBundle()
                } else {
                    null
                }
                ViewInfoActivity.open(activity, infoData, options)
            }
            title.text = activity.getString(R.string.info_title_timetable,
                    DateTimeUtils.getParsedDateExpression(infoData.date))
            if (!infoData.detail.isNullOrEmpty()) {
                info.visibility = View.VISIBLE
                info.text = infoData.detail
            } else {
                info.visibility = View.GONE
            }

            type.background.setColorFilter(getColorForInfoType(infoData.type),
                    PorterDuff.Mode.SRC_IN)
            type.text = activity.getString(R.string.class_schedule)
            delete.setOnClickListener {
                LiftimContext.getOrmaDatabase().updateInfo()
                        .deleted(true)
                        .liftimCodeEq(LiftimContext.getLiftimCode())
                        .idEq(infoData.id)
                        .execute()
                data.remove(infoData)
                notifyItemRemoved(adapterPosition)
            }
            more.setOnClickListener {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    PopupMenu(activity, it, Gravity.TOP or Gravity.END)
                } else {
                    PopupMenu(activity, it)
                }.apply {
                    inflate(R.menu.info_item_action_no_link)
                    setOnMenuItemClickListener {
                        when (it.itemId) {
                            R.id.item_edit -> {
                                EditTimetableActivity.open(activity, infoData.id)
                            }
                        }
                        true
                    }
                    show()
                }
            }
        }
    }
}
