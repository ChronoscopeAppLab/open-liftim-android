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
package com.chronoscoper.android.classschedule2.home

import android.content.Context
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import com.chronoscoper.android.classschedule2.R
import com.chronoscoper.android.classschedule2.sync.LiftimCodeInfo
import com.chronoscoper.android.classschedule2.sync.LiftimContext

class LiftimCodePagerAdapter(fm: FragmentManager, private val context: Context) :
        FragmentPagerAdapter(fm) {
    val data = mutableListOf<LiftimCodeInfo>()

    init {
        data.addAll(LiftimContext.getOrmaDatabase().selectFromLiftimCodeInfo())
    }

    override fun getItem(position: Int): Fragment {
        val page = position % data.size
        return LiftimCodeFragment.obtain(data[page])
    }

    fun getLiftimCodeInfo(page: Int) = data[page % data.size]

    override fun getCount(): Int =
            if (data.size <= 1) {
                1
            } else {
                Int.MAX_VALUE
            }

    val initialPosition: Int
        get() {
            if (data.size <= 1) {
                return 1
            }
            val liftimCode = PreferenceManager.getDefaultSharedPreferences(context)
                    .getLong(context.getString(R.string.p_default_liftim_code), 0)
            var pos = 0
            data.forEachIndexed { index, liftimCodeInfo ->
                if (liftimCodeInfo.liftimCode == liftimCode) {
                    pos = index
                }
            }
            return count - data.size + pos - 1
        }
}
