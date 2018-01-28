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
package com.chronoscoper.android.classschedule2.home

import android.content.Context
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.chronoscoper.android.classschedule2.R
import com.chronoscoper.android.classschedule2.home.info.InfoFragment
import com.chronoscoper.android.classschedule2.home.timetable.TimetableFragment
import kotterknife.bindView

class HomeFragment : Fragment(), ViewPager.OnPageChangeListener {
    companion object {
        private const val EXTRA_PAGE = "page"

        fun obtain(page: Int): HomeFragment {
            val result = HomeFragment()
            result.arguments = Bundle().apply { putInt(EXTRA_PAGE, page) }
            return result
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    private val tabLayout by bindView<TabLayout>(R.id.tab_layout)
    private val pager by bindView<ViewPager>(R.id.pager)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val page = arguments?.getInt(EXTRA_PAGE) ?: 0

        pager.pageMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5f,
                resources.displayMetrics).toInt()
        pager.setPageMarginDrawable(R.drawable.pager_margin)
        pager.adapter = HomePagerAdapter(context!!, childFragmentManager)
        pager.addOnPageChangeListener(this)
        pager.currentItem = page

        tabLayout.setupWithViewPager(pager)
    }

    var page
        get() = pager.currentItem
        set(value) = pager.setCurrentItem(value, true)

    override fun onPageScrollStateChanged(state: Int) {}

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

    override fun onPageSelected(position: Int) {
        (activity as? HomeActivity)?.onHomePageChanged(position)
    }

    class HomePagerAdapter(val context: Context, fragmentManager: FragmentManager)
        : FragmentPagerAdapter(fragmentManager) {
        override fun getPageTitle(position: Int): CharSequence {
            return when (position) {
                0 -> context.getString(R.string.timetable)
                1 -> context.getString(R.string.info)
                else -> ""
            }
        }

        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> TimetableFragment()
                1 -> InfoFragment()
                else -> TimetableFragment()
            }
        }

        override fun getCount(): Int = 2
    }
}