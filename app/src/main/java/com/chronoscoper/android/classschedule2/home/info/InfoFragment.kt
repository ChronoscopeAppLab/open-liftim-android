/*
 * Copyright 2017-2018 Chronoscope
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

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ViewSwitcher
import com.chronoscoper.android.classschedule2.R
import com.chronoscoper.android.classschedule2.sync.LiftimContext
import jp.wasabeef.recyclerview.adapters.SlideInBottomAnimationAdapter
import kotterknife.bindView

class InfoFragment : Fragment() {
    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_info, container, false)
    }

    private val switcher by bindView<ViewSwitcher>(R.id.switcher)
    private val placeholder by bindView<View>(R.id.placeholder_container)
    private val list by bindView<RecyclerView>(R.id.list)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val count = LiftimContext.getOrmaDatabase()
                .selectFromInfo()
                .liftimCodeEq(LiftimContext.getLiftimCode())
                .deletedEq(false)
                .count()

        if (count > 0) {
            switcher.showNext()
            list.apply {
                adapter = SlideInBottomAnimationAdapter(InfoRecyclerViewAdapter(activity!!))
                addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
            }
        } else {
            placeholder.visibility = View.VISIBLE
        }
    }
}
