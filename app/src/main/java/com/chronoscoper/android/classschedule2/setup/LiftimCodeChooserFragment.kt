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
package com.chronoscoper.android.classschedule2.setup

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.PopupMenu
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.chronoscoper.android.classschedule2.LiftimApplication
import com.chronoscoper.android.classschedule2.R
import com.chronoscoper.android.classschedule2.functionrestriction.getFunctionRestriction
import com.chronoscoper.android.classschedule2.home.HomeActivity
import com.chronoscoper.android.classschedule2.setting.manager.InvitationActivity
import com.chronoscoper.android.classschedule2.setting.manager.LiftimCodeSettingsActivity
import com.chronoscoper.android.classschedule2.sync.LiftimCodeInfo
import com.chronoscoper.android.classschedule2.sync.LiftimContext
import com.chronoscoper.android.classschedule2.util.openInNewTask
import com.chronoscoper.android.classschedule2.util.progressiveFadeInTransition
import com.chronoscoper.android.classschedule2.util.showToast
import com.chronoscoper.android.classschedule2.view.ProgressDialog
import com.chronoscoper.android.classschedule2.view.RecyclerViewHolder
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import kotterknife.bindView
import java.io.IOException

class LiftimCodeChooserFragment : Fragment() {
    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_liftim_code_chooser, container, false)

    private val list by bindView<RecyclerView>(R.id.list)
    private val joinButton by bindView<Button>(R.id.join)
    private val createButton by bindView<Button>(R.id.create)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        list.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        if (LiftimContext.getOrmaDatabase().selectFromLiftimCodeInfo().count() >= 1
                && !getFunctionRestriction(context!!).multiLiftimCode) {
            joinButton.visibility = View.GONE
        }
        if (!getFunctionRestriction(context!!).createLiftimCode) {
            createButton.visibility = View.GONE
        }

        list.adapter = LiftimCodeAdapter(activity!!)
    }

    private val disposables = CompositeDisposable()

    override fun onDestroyView() {
        super.onDestroyView()
        disposables.clear()
    }

    override fun onResume() {
        super.onResume()
        (list.adapter as? LiftimCodeAdapter)?.let {
            it.reloadEntry()
            it.notifyDataSetChanged()
        }
        joinButton.setOnClickListener {
            startActivity(Intent(context, JoinLiftimCodeActivity::class.java))
        }
        createButton.setOnClickListener {
            startActivity(Intent(context, CreateLiftimCodeActivity::class.java))
        }
    }

    private inner class LiftimCodeAdapter(val activity: Activity) :
            RecyclerView.Adapter<RecyclerViewHolder>() {
        private val data = mutableListOf<LiftimCodeInfo>()

        init {
            reloadEntry()
        }

        fun reloadEntry() {
            data.clear()
            data.addAll(LiftimContext.getOrmaDatabase()
                    .selectFromLiftimCodeInfo().toList())
        }

        override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
            val view = holder.itemView
            val item = data[position]
            val image = view.findViewById<ImageView>(R.id.image)
            val name = view.findViewById<TextView>(R.id.liftim_code_name)
            val more = view.findViewById<View>(R.id.more)
            Glide.with(activity)
                    .load(LiftimContext
                            .getApiUrl("liftim_code_image.png?" +
                                    "liftim_code=${item.liftimCode}&" +
                                    "token=${LiftimContext.getToken()}"))
                    .apply(RequestOptions.circleCropTransform())
                    .transition(progressiveFadeInTransition())
                    .into(image)
            name.text = item.name
            view.setOnClickListener {
                PreferenceManager.getDefaultSharedPreferences(activity)
                        .edit()
                        .putLong(activity.getString(R.string.p_default_liftim_code), data[position].liftimCode)
                        .apply()
                (activity.application as? LiftimApplication ?: return@setOnClickListener)
                        .initEnvironment()
                openInNewTask(activity, HomeActivity::class.java)
            }
            val configureLiftimCodeRestriction = getFunctionRestriction(activity)
                    .configureLiftimCode
            if ((LiftimContext.isManager()
                            && !configureLiftimCodeRestriction.changeImage
                            && !configureLiftimCodeRestriction.rename
                            && !configureLiftimCodeRestriction.delete
                            && !configureLiftimCodeRestriction.invite)
                    || (!LiftimContext.isManager()
                            && !configureLiftimCodeRestriction.exit)) {
                more.visibility = View.GONE
            } else {
                more.visibility = View.VISIBLE
                val popup = PopupMenu(activity, more, Gravity.NO_GRAVITY, 0,
                        android.R.style.Widget_Material_PopupMenu_Overflow)
                popup.inflate(R.menu.liftim_code_chooser_action)
                if (LiftimContext.isManager()) {
                    popup.menu.findItem(R.id.delete).isVisible = false
                } else {
                    arrayOf(R.id.settings, R.id.invite)
                            .forEach { popup.menu.findItem(it).isVisible = false }
                }
                popup.setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.delete -> {
                            AlertDialog.Builder(activity)
                                    .setMessage(
                                            activity.getString(R.string.delete_liftim_code_warning))
                                    .setPositiveButton(activity.getString(R.string.continue_anyway)
                                    ) { _, _ ->
                                        deleteLiftimCode(item.liftimCode)
                                    }
                                    .setNegativeButton(activity.getString(R.string.cancel), null)
                                    .show()
                        }
                        R.id.invite -> {
                            InvitationActivity.start(context!!, item.liftimCode)
                        }
                        R.id.settings -> {
                            LiftimCodeSettingsActivity.start(activity, item.liftimCode)
                        }
                    }
                    true
                }
                more.setOnTouchListener(popup.dragToOpenListener)
                more.setOnClickListener {
                    popup.show()
                }
            }
        }

        private fun deleteLiftimCode(liftimCode: Long) {
            childFragmentManager.beginTransaction()
                    .add(ProgressDialog(), "progress_dialog")
                    .commit()
            val subscriber = object : DisposableObserver<Unit>() {
                override fun onComplete() {
                    (list.adapter as? LiftimCodeAdapter)?.let {
                        it.reloadEntry()
                        it.notifyDataSetChanged()
                    }
                }

                override fun onNext(t: Unit) {
                }

                override fun onError(e: Throwable) {
                    showToast(this@LiftimCodeChooserFragment.context!!,
                            getString(R.string.liftim_code_exit_failed), Toast.LENGTH_SHORT)
                }
            }
            Observable.create<Unit> {
                val response = LiftimContext.getLiftimService()
                        .deleteLiftimCode(liftimCode, LiftimContext.getToken())
                        .execute()
                if (!response.isSuccessful) {
                    it.onError(IOException())
                    return@create
                }
                val db = LiftimContext.getOrmaDatabase()
                db.deleteFromLiftimCodeInfo()
                        .liftimCodeEq(liftimCode).execute()
                db.deleteFromWeeklyItem().liftimCodeEq(liftimCode).execute()
                db.deleteFromInfo().liftimCodeEq(liftimCode).execute()
                db.deleteFromSubject().liftimCodeEq(liftimCode).execute()
                it.onComplete()
            }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(subscriber)
            disposables.add(subscriber)
        }

        val inflater: LayoutInflater by lazy { LayoutInflater.from(activity) }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder =
                RecyclerViewHolder(inflater.inflate(
                        R.layout.liftim_code_chooser_item, parent, false))

        override fun getItemCount(): Int = data.size
    }
}
