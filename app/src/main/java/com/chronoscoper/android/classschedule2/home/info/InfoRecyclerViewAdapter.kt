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

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PorterDuff
import android.os.Build
import android.support.constraint.ConstraintLayout
import android.support.constraint.ConstraintSet
import android.support.transition.AutoTransition
import android.support.transition.TransitionManager
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import com.chronoscoper.android.classschedule2.R
import com.chronoscoper.android.classschedule2.sync.Info
import com.chronoscoper.android.classschedule2.sync.LiftimSyncEnvironment
import com.chronoscoper.android.classschedule2.task.InfoLoader
import com.chronoscoper.android.classschedule2.util.DateTimeUtils
import com.chronoscoper.android.classschedule2.view.RecyclerViewHolder
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subscribers.DisposableSubscriber

open class InfoRecyclerViewAdapter(val context: Context) : RecyclerView.Adapter<RecyclerViewHolder>() {
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
            LiftimSyncEnvironment.getOrmaDatabase().selectFromInfo()
                    .liftimCodeEq(LiftimSyncEnvironment.getLiftimCode())
                    .deletedEq(false)


    override fun onAttachedToRecyclerView(recyclerView: RecyclerView?) {
        super.onAttachedToRecyclerView(recyclerView)
        val liftimCode = LiftimSyncEnvironment.getLiftimCode()
        InfoLoader.resetCursor()
        Flowable.defer {
            Flowable.just(
                    InfoLoader(liftimCode, LiftimSyncEnvironment.getToken()).run())
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

    val inflater: LayoutInflater by lazy { LayoutInflater.from(context) }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerViewHolder =
            if (viewType == VIEW_TYPE_INFO) {
                InfoHolder(inflater
                        .inflate(R.layout.info_item_collapsed, parent, false))
            } else {
                TimetableHolder(inflater
                        .inflate(R.layout.info_timetable_item_collapsed, parent, false))
            }

    override fun getItemCount(): Int = data.size

    override fun getItemViewType(position: Int): Int =
            if (data[position].type in 0..3) {
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
        private var expanded = false
        private val expandedConstraints = ConstraintSet()
        private val collapsedConstraints = ConstraintSet()

        init {
            expandedConstraints.clone(context, R.layout.info_item_expanded)
            collapsedConstraints.clone(context, R.layout.info_item_collapsed)
        }

        fun bindContent(infoData: Info) {
            expanded = false
            val parent = itemView as ConstraintLayout? ?: return
            collapsedConstraints.applyTo(parent)
            bindContent(parent, infoData)
        }

        @SuppressLint("ClickableViewAccessibility")
        private fun bindContent(parent: ConstraintLayout, infoData: Info) {
            parent.setOnClickListener {
                val parentRecyclerView = parent.parent as RecyclerView
                TransitionManager.beginDelayedTransition(parentRecyclerView, AutoTransition())
                if (expanded) {
                    collapsedConstraints.applyTo(parent)
                } else {
                    expandedConstraints.applyTo(parent)
                }
                expanded = !expanded
                bindContent(parent, infoData)
            }
            (parent.findViewById<TextView>(R.id.title)).text = infoData.title
            val detailView = parent.findViewById<TextView>(R.id.detail)
            if (!infoData.detail.isNullOrEmpty()) {
                detailView.visibility = View.VISIBLE
                detailView.text = infoData.detail
            } else {
                detailView.visibility = View.GONE
            }
            if (expanded) {
                detailView.maxLines = 50
            } else {
                detailView.maxLines = 1
            }
            val dateView = parent.findViewById<TextView>(R.id.date)
            if (!infoData.date.isNullOrEmpty()) {
                dateView.visibility = View.VISIBLE
                dateView.text = infoData.date
            } else {
                dateView.visibility = View.GONE
            }
            val linkView = parent.findViewById<TextView>(R.id.link_url)
            if (!infoData.link.isNullOrEmpty()) {
                linkView.visibility = View.VISIBLE
                linkView.text = infoData.link
            } else {
                linkView.visibility = View.GONE
            }
            val typeView = parent.findViewById<TextView>(R.id.type)
            when (infoData.type) {
                Info.TYPE_UNSPECIFIED -> {
                    typeView.background.setColorFilter(-0x777778, PorterDuff.Mode.SRC_IN)
                    typeView.text = context.getString(R.string.type_unspecified)
                }
                Info.TYPE_EVENT -> {
                    typeView.background.setColorFilter(-0x6800, PorterDuff.Mode.SRC_IN)
                    typeView.text = context.getString(R.string.type_event)
                }
                Info.TYPE_INFORMATION -> {
                    typeView.background.setColorFilter(-0xde690d, PorterDuff.Mode.SRC_IN)
                    typeView.text = context.getString(R.string.type_information)
                }
                Info.TYPE_SUBMISSION -> {
                    typeView.background.setColorFilter(-0xb350b0, PorterDuff.Mode.SRC_IN)
                    typeView.text = context.getString(R.string.type_submission)
                }
                Info.TYPE_LOCAL_MEMO -> {
                    typeView.background.setColorFilter(-0x98c549, PorterDuff.Mode.SRC_IN)
                    typeView.text = context.getString(R.string.type_memo)
                }
            }
            val removeButton = parent.findViewById<View>(R.id.delete)
            if (infoData.removable) {
                removeButton.apply {
                    visibility = View.VISIBLE
                    setOnClickListener {
                        LiftimSyncEnvironment.getOrmaDatabase().updateInfo()
                                .deleted(true)
                                .liftimCodeEq(LiftimSyncEnvironment.getLiftimCode())
                                .idEq(infoData.id)
                                .execute()
                        data.remove(infoData)
                        notifyItemRemoved(adapterPosition)
                    }
                }
            } else {
                removeButton.apply {
                    visibility = View.INVISIBLE
                    setOnClickListener(null)
                }
            }
            removeButton.apply {
                isClickable = infoData.removable
                isFocusable = infoData.removable
            }
            val moreButton = parent.findViewById<View>(R.id.more)
            moreButton.setOnClickListener {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    PopupMenu(context, it, Gravity.TOP or Gravity.END)
                } else {
                    PopupMenu(context, it)
                }.apply {
                    inflate(R.menu.info_item_action)
                    setOnMenuItemClickListener {
                        when (it.itemId) {
                            R.id.item_open -> {
                                //TODO
                            }
                            R.id.item_edit -> {
                                //TODO
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
        private var expanded = false
        private val expandedConstraints = ConstraintSet()
        private val collapsedConstraints = ConstraintSet()

        init {
            expandedConstraints.clone(context, R.layout.info_timetable_item_expanded)
            collapsedConstraints.clone(context, R.layout.info_timetable_item_collapsed)
        }

        fun bindContent(infoData: Info) {
            expanded = false
            val parent = itemView as ConstraintLayout? ?: return
            collapsedConstraints.applyTo(parent)
            bindContent(parent, infoData)
        }

        private fun bindContent(parent: ConstraintLayout, infoData: Info) {
            parent.setOnClickListener {
                TransitionManager.beginDelayedTransition(parent.parent as RecyclerView)
                if (expanded) {
                    collapsedConstraints.applyTo(parent)
                } else {
                    expandedConstraints.applyTo(parent)
                }
                bindContent(parent, infoData)
                expanded = !expanded
            }
            val titleView = parent.findViewById<TextView>(R.id.title)
            titleView.text = context.getString(R.string.info_title_timetable, DateTimeUtils.getParsedDateExpression(infoData.date))
            val infoView = parent.findViewById<TextView>(R.id.detail)
            if (!infoData.detail.isNullOrEmpty()) {
                infoView.visibility = View.VISIBLE
                infoView.text = infoData.detail
            } else {
                infoView.visibility = View.GONE
            }

            val typeView = parent.findViewById<TextView>(R.id.type)
            typeView.background.setColorFilter(-0xff6978, PorterDuff.Mode.SRC_IN)
            typeView.text = context.getString(R.string.class_schedule)
        }

        private fun buildClassScheduleText(json: String) {

        }
    }
}