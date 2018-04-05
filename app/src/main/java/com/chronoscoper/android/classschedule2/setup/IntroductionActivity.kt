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

import android.animation.ArgbEvaluator
import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.support.v4.view.ViewPager
import android.view.View
import android.widget.Button
import com.chronoscoper.android.classschedule2.BaseActivity
import com.chronoscoper.android.classschedule2.R
import com.chronoscoper.android.classschedule2.sync.LiftimContext
import com.prolificinteractive.parallaxpager.ParallaxContainer
import com.prolificinteractive.parallaxpager.ParallaxContextWrapper
import kotterknife.bindView

class IntroductionActivity : BaseActivity() {
    companion object {
        private const val TAG = "Introduction"
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(ParallaxContextWrapper(newBase))
    }

    private val container by bindView<View>(R.id.container)
    private val parallaxContainer by bindView<ParallaxContainer>(R.id.pager)
    private val skip by bindView<Button>(R.id.skip)
    private val next by bindView<Button>(R.id.next)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_introduction)

        val pageIds = intArrayOf(R.layout.introduction_page_0, R.layout.introduction_page_1,
                R.layout.introduction_page_2, R.layout.introduction_page_3)

        parallaxContainer.setupChildren(layoutInflater, *pageIds)
        val colors = resources.getIntArray(R.array.intro_background)
        val backgroundDrawable = ColorDrawable(colors[0])
        container.background = backgroundDrawable
        parallaxContainer.viewPager.addOnPageChangeListener(
                object : ViewPager.OnPageChangeListener {
                    private val evaluator = ArgbEvaluator()
                    override fun onPageScrollStateChanged(state: Int) {}

                    override fun onPageScrolled(
                            position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                        backgroundDrawable.color = evaluator.evaluate(positionOffset,
                                colors[position], colors[position + 1]) as Int
                    }

                    override fun onPageSelected(position: Int) {
                        if (position == pageIds.size - 1) {
                            next.visibility = View.GONE
                        } else {
                            next.visibility = View.VISIBLE
                        }
                    }
                })
        skip.setOnClickListener {
            parallaxContainer.viewPager.setCurrentItem(pageIds.size - 1, true)
        }
        next.setOnClickListener {
            val nextPage = parallaxContainer.viewPager.currentItem + 1
            if (nextPage < pageIds.size)
                parallaxContainer.viewPager.setCurrentItem(nextPage, true)
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun onLoginButtonClicked(view: View) {
        val intent = Intent(Intent.ACTION_VIEW,
                Uri.parse(LiftimContext.getApiUrl("auth")))
        startActivity(intent)
        finish()
    }
}
