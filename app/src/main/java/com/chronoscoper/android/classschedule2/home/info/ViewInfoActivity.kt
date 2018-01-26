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

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
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
import com.chronoscoper.android.classschedule2.sync.LiftimContext
import com.chronoscoper.android.classschedule2.util.DateTimeUtils
import com.chronoscoper.android.classschedule2.util.TransitionListenerAdapter
import com.chronoscoper.android.classschedule2.util.getColorForInfoType
import com.chronoscoper.android.classschedule2.util.getDarkerColor
import com.chronoscoper.android.classschedule2.util.openInCustomTab
import com.chronoscoper.android.classschedule2.view.SwipeDismissFrameLayout
import kotterknife.bindView

class ViewInfoActivity : BaseActivity() {
    companion object {
        private const val ID = "target_id"
        fun open(context: Context, id: String, options: Bundle?) {
            context.startActivity(Intent(context, ViewInfoActivity::class.java)
                    .putExtra(ID, id), options)
        }
    }

    private val dismissFrame by bindView<SwipeDismissFrameLayout>(R.id.dismiss_frame)
    private val toolbar by bindView<LinearLayout>(R.id.toolbar)
    private val title by bindView<TextView>(R.id.title)
    private val detail by bindView<TextView>(R.id.detail)
    private val date by bindView<TextView>(R.id.date)
    private val linkUrl by bindView<TextView>(R.id.link_url)
    private val type by bindView<TextView>(R.id.type)

    private var infoType = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_info)
        val item = obtainSpecifiedElement() ?: kotlin.run { finish(); return }
        infoType = item.type
        val backButton = LayoutInflater.from(this)
                .inflate(R.layout.back, toolbar, false)
        backButton.setOnClickListener {
            animateFinishCompat()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && savedInstanceState == null) {
            window.sharedElementEnterTransition.addListener(object : TransitionListenerAdapter() {
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
        title.text = item.title
        dismissFrame.swipeDismissCallback =
                object : SwipeDismissFrameLayout.SwipeDismissCallback() {
                    override fun onDismiss() {
                        animateFinishCompat()
                    }
                }
        detail.text = item.detail
        if (!item.date.isNullOrEmpty()) {
            date.visibility = View.VISIBLE
            date.text = DateTimeUtils.getParsedDateExpression(item.date)
        }
        if (!item.link.isNullOrEmpty()) {
            linkUrl.visibility = View.VISIBLE
            linkUrl.text = item.link
            linkUrl.setOnClickListener {
                openInCustomTab(this, item.link!!)
            }
        }
        when (item.type) {
            Info.TYPE_UNSPECIFIED -> {
                type.background.setColorFilter(-0x777778, PorterDuff.Mode.SRC_IN)
                type.text = getString(R.string.type_unspecified)
            }
            Info.TYPE_EVENT -> {
                type.background.setColorFilter(-0x6800, PorterDuff.Mode.SRC_IN)
                type.text = getString(R.string.type_event)
            }
            Info.TYPE_INFORMATION -> {
                type.background.setColorFilter(-0xde690d, PorterDuff.Mode.SRC_IN)
                type.text = getString(R.string.type_information)
            }
            Info.TYPE_SUBMISSION -> {
                type.background.setColorFilter(-0xb350b0, PorterDuff.Mode.SRC_IN)
                type.text = getString(R.string.type_submission)
            }
            Info.TYPE_LOCAL_MEMO -> {
                type.background.setColorFilter(-0x98c549, PorterDuff.Mode.SRC_IN)
                type.text = getString(R.string.type_memo)
            }
        }
    }

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

        val backButton = findViewById<ImageButton>(R.id.back)
        val titleAnimator = ValueAnimator.ofObject(ArgbEvaluator(),
                Color.BLACK, Color.WHITE)
        titleAnimator.duration = 250
        titleAnimator.addUpdateListener {
            val animated = it.animatedValue as Int
            title.setTextColor(animated)
            backButton.setColorFilter(animated)
        }
        if (reverse) {
            titleAnimator.reverse()
        } else {
            titleAnimator.start()
        }
    }

    override fun animateFinishCompat() {
        animateToolbar(infoType, true)
        toolbar.removeViewAt(0)
        super.animateFinishCompat()
    }

    private fun obtainSpecifiedElement(): Info? {
        val id = intent.getStringExtra(ID) ?: return null
        return LiftimContext.getOrmaDatabase()
                .selectFromInfo()
                .liftimCodeEq(LiftimContext.getLiftimCode())
                .idEq(id)
                .firstOrNull()
    }

    override fun onBackPressed() {
        animateFinishCompat()
    }
}
