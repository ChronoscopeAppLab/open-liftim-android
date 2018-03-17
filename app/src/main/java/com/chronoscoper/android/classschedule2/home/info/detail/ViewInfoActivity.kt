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
package com.chronoscoper.android.classschedule2.home.info.detail

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.transition.Transition
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import com.chronoscoper.android.classschedule2.BaseActivity
import com.chronoscoper.android.classschedule2.R
import com.chronoscoper.android.classschedule2.sync.Info
import com.chronoscoper.android.classschedule2.util.DateTimeUtils
import com.chronoscoper.android.classschedule2.util.TransitionListenerAdapter
import com.chronoscoper.android.classschedule2.util.getColorForInfoType
import com.chronoscoper.android.classschedule2.util.getDarkerColor
import com.chronoscoper.android.classschedule2.view.SwipeDismissFrameLayout
import kotterknife.bindView
import org.parceler.Parcels

class ViewInfoActivity : BaseActivity() {
    companion object {
        private const val EXTRA_TARGET = "target"
        private const val WINDOW_ANIMATION_NEEDED = "window_anim_needed"

        fun open(context: Context, item: Info, options: Bundle?) {
            context.startActivity(createIntent(context, item, true), options)
        }

        fun createIntent(context: Context, item: Info, windowAnimNeeded: Boolean = false): Intent {
            return Intent(context, ViewInfoActivity::class.java)
                    .putExtra(EXTRA_TARGET, Parcels.wrap(item))
                    .putExtra(WINDOW_ANIMATION_NEEDED, windowAnimNeeded)
        }
    }

    private val dismissFrame by bindView<SwipeDismissFrameLayout>(R.id.dismiss_frame)
    private val toolbar by bindView<LinearLayout>(R.id.toolbar)
    private val title by bindView<TextView>(R.id.title)

    private var infoType = 0

    private var isWindowAnimationNeeded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_info)
        val item = Parcels.unwrap<Info>(intent.getParcelableExtra(EXTRA_TARGET))
                ?: kotlin.run { finish(); return }
        infoType = item.type
        val backButton = LayoutInflater.from(this)
                .inflate(R.layout.back, toolbar, false)
        backButton.setOnClickListener {
            animateFinish()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && savedInstanceState == null) {
            isWindowAnimationNeeded = intent.getBooleanExtra(WINDOW_ANIMATION_NEEDED, false)
            if (isWindowAnimationNeeded) {
                window.sharedElementEnterTransition.addListener(
                        object : TransitionListenerAdapter() {
                            @SuppressLint("NewApi")
                            override fun onTransitionEnd(transition: Transition?) {
                                toolbar.addView(backButton, 0)
                                animateToolbar(item.type, false)
                                transition?.removeListener(this)
                            }
                        })
            } else {
                toolbar.addView(backButton, 0)
                animateToolbar(item.type, false)
            }
        } else {
            toolbar.addView(backButton, 0)
            animateToolbar(item.type, false)
        }
        dismissFrame.swipeDismissCallback =
                object : SwipeDismissFrameLayout.SwipeDismissCallback() {
                    override fun onDismiss() {
                        animateFinish()
                    }
                }
        if (infoType == Info.TYPE_TIMETABLE) {
            bindTimetable(item, savedInstanceState == null)
        } else {
            bindInfo(item, savedInstanceState == null)
        }
    }

    private fun bindInfo(item: Info, initial: Boolean) {
        title.text = item.title
        if (initial) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.content, InfoFragment.obtain(item))
                    .commit()
        }
    }

    private fun bindTimetable(item: Info, initial: Boolean) {
        title.text = getString(R.string.info_title_timetable,
                DateTimeUtils.getParsedDateExpression(item.date))
        if (initial) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.content, TimetableFragment.obtain(item))
                    .commit()
        }
    }

    @SuppressLint("NewApi")
    private fun animateToolbar(infoType: Int, reverse: Boolean) {
        val toolbarBackgroundAnimator = ValueAnimator.ofObject(ArgbEvaluator(),
                Color.TRANSPARENT, getColorForInfoType(infoType))
        toolbarBackgroundAnimator.duration = 250
        toolbarBackgroundAnimator.addUpdateListener {
            toolbar.background = ColorDrawable(it.animatedValue as Int)
        }
        if (reverse) {
            toolbarBackgroundAnimator.reverse()
        } else {
            toolbarBackgroundAnimator.start()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val statusBarAnimator = ValueAnimator.ofObject(ArgbEvaluator(),
                    window.statusBarColor, getDarkerColor(getColorForInfoType(infoType)))
            statusBarAnimator.duration = 250
            statusBarAnimator.addUpdateListener {
                window.statusBarColor = it.animatedValue as Int
            }
            if (reverse) {
                statusBarAnimator.reverse()
            } else {
                statusBarAnimator.start()
            }
        }

        val backButton = findViewById<View>(R.id.back) as? ImageButton
        val titleAnimator = ValueAnimator.ofObject(ArgbEvaluator(),
                Color.BLACK, Color.WHITE)
        titleAnimator.duration = 250
        titleAnimator.addUpdateListener {
            val animated = it.animatedValue as Int
            title.setTextColor(animated)
            backButton?.setColorFilter(animated)
        }
        if (reverse) {
            titleAnimator.reverse()
        } else {
            titleAnimator.start()
        }
    }

    override fun animateFinish() {
        if (isWindowAnimationNeeded) {
            animateToolbar(infoType, true)
            toolbar.removeViewAt(0)
            super.animateFinish()
        } else {
            finish()
        }
    }

    override fun onBackPressed() {
        animateFinish()
    }
}
