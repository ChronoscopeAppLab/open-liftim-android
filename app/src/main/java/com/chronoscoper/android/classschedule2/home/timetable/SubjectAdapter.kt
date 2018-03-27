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
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.chronoscoper.android.classschedule2.R
import com.chronoscoper.android.classschedule2.sync.LiftimContext
import com.chronoscoper.android.classschedule2.sync.Subject
import com.chronoscoper.android.classschedule2.util.obtainColorCorrespondsTo
import com.chronoscoper.android.classschedule2.view.RecyclerViewHolder
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers

class SubjectAdapter(context: Context) : RecyclerView.Adapter<RecyclerViewHolder>() {
    companion object {
        private const val TAG = "SubjectAdapter"
    }

    private val disposables = CompositeDisposable()
    private val data = mutableListOf<Subject>()

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        val subscriber = object : DisposableObserver<MutableList<Subject>>() {
            override fun onComplete() {
                notifyDataSetChanged()
            }

            override fun onNext(t: MutableList<Subject>) {
                Log.d(TAG, "${t.size} item(s) set")
                data.clear()
                data.addAll(t)
            }

            override fun onError(e: Throwable) {}
        }
        Observable.create<MutableList<Subject>> {
            val data = LiftimContext.getOrmaDatabase().selectFromSubject()
                    .liftimCodeEq(LiftimContext.getLiftimCode()).toList()
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

    private val inflater by lazy { LayoutInflater.from(context) }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        val view = inflater.inflate(R.layout.subject_picker_item, parent, false)
        val holder = RecyclerViewHolder(view)
        view.setOnClickListener {
            onSelectedListener?.invoke(data[holder.adapterPosition].subject)
        }
        return holder
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        val view = holder.itemView
        val subject = data[position].subject
        view.findViewById<View>(R.id.subject_color)
                .background.setColorFilter(
                obtainColorCorrespondsTo(subject), PorterDuff.Mode.SRC_IN)
        view.findViewById<TextView>(R.id.subject).text = subject
    }

    var onSelectedListener: ((subject: String) -> Unit)? = null

    var query: String? = null
        set(value) {
            field = value
            if (value == null) {
                return
            }
            val subscriber = object : DisposableObserver<MutableList<Subject>>() {
                override fun onComplete() {
                    notifyDataSetChanged()
                }

                override fun onNext(t: MutableList<Subject>) {
                    data.clear()
                    data.addAll(t)
                }

                override fun onError(e: Throwable) {}
            }
            Observable.create<MutableList<Subject>> {
                val escapedValue = value
                        .replace("~", "~~")
                        .replace("_", "~_")
                        .replace("%", "~%")
                val data = LiftimContext.getOrmaDatabase().selectFromSubject()
                        .liftimCodeEq(LiftimContext.getLiftimCode())
                        .where("subject LIKE ? ESCAPE '~'", "%$escapedValue%")
                        .toList()
                it.onNext(data)
                it.onComplete()
            }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(subscriber)
            disposables.add(subscriber)
        }

}