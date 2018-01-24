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
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.chronoscoper.android.classschedule2.LiftimApplication
import com.chronoscoper.android.classschedule2.R
import com.chronoscoper.android.classschedule2.home.HomeActivity
import com.chronoscoper.android.classschedule2.sync.LiftimCodeInfo
import com.chronoscoper.android.classschedule2.sync.LiftimContext
import com.chronoscoper.android.classschedule2.util.openInNewTask
import com.chronoscoper.android.classschedule2.view.RecyclerViewHolder
import kotterknife.bindView

class LiftimCodeChooserFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater?,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? =
            inflater?.inflate(R.layout.fragment_liftim_code_chooser, container, false)

    private val list by bindView<RecyclerView>(R.id.list)
    private val joinButton by bindView<Button>(R.id.join)
    private val createButton by bindView<Button>(R.id.create)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        list.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
    }

    override fun onResume() {
        super.onResume()
        list.adapter = LiftimCodeAdapter(activity)
        joinButton.setOnClickListener {
            startActivity(Intent(context, JoinLiftimCodeActivity::class.java))
        }
        createButton.setOnClickListener {
            startActivity(Intent(context, CreateLiftimCodeActivity::class.java))
        }
    }

    private class LiftimCodeAdapter(val activity: Activity) :
            RecyclerView.Adapter<RecyclerViewHolder>() {
        private val data = mutableListOf<LiftimCodeInfo>()

        init {
            data.addAll(LiftimContext.getOrmaDatabase()
                    .selectFromLiftimCodeInfo().toList())
        }

        override fun onBindViewHolder(holder: RecyclerViewHolder?, position: Int) {
            val view = holder?.itemView ?: return
            val item = data[position]
            val image = view.findViewById<ImageView>(R.id.image)
            val name = view.findViewById<TextView>(R.id.liftim_code_name)
            Glide.with(activity)
                    .load(LiftimContext
                            .getApiUrl("liftim_code_image.png?" +
                                    "liftim_code=${item.liftimCode}&" +
                                    "token=${LiftimContext.getToken()}"))
                    .apply(RequestOptions.circleCropTransform())
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
        }

        val inflater by lazy { LayoutInflater.from(activity) }

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerViewHolder =
                RecyclerViewHolder(inflater.inflate(
                        R.layout.liftim_code_chooser_item, parent, false))

        override fun getItemCount(): Int = data.size
    }
}
