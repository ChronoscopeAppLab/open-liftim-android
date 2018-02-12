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
package com.chronoscoper.android.classschedule2.transition

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.support.annotation.ColorInt
import android.support.annotation.RequiresApi
import android.transition.Transition
import android.transition.TransitionValues
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewGroup

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class FabExpandTransition : Transition() {
    companion object {
        fun configure(@ColorInt color: Int) {
            this.color = color
        }

        private var color = 0

        private const val BOUNDS = "bounds"
    }

    override fun captureStartValues(transitionValues: TransitionValues) =
            captureValues(transitionValues)

    override fun captureEndValues(transitionValues: TransitionValues) =
            captureValues(transitionValues)

    override fun createAnimator(
            sceneRoot: ViewGroup,
            startValues: TransitionValues?, endValues: TransitionValues?): Animator? {
        if (startValues == null || endValues == null) {
            return null
        }
        val start = startValues.values[BOUNDS] as Rect
        val end = endValues.values[BOUNDS] as Rect
        val endView = endValues.view
        val toFab = start.width() > end.width()
        val duration = 250L
        if (toFab) {
            endView.measure(
                    View.MeasureSpec.makeMeasureSpec(start.width(), View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(start.height(), View.MeasureSpec.EXACTLY))
            endView.layout(
                    start.left,
                    start.top,
                    start.right,
                    start.bottom)
        }
        val fabColor = ColorDrawable(color)
        if (toFab) {
            fabColor.alpha = 0
            fabColor.bounds = start
        } else {
            fabColor.alpha = 255
            fabColor.bounds = end
        }
        endView.overlay.add(fabColor)
        val colorAnimator = ObjectAnimator.ofInt(fabColor, "alpha",
                if (toFab) {
                    255
                } else {
                    0
                })

        colorAnimator.duration = duration

        val centerX = if (toFab) {
            end.centerX()
        } else {
            start.centerX()
        }
        val centerY = if (toFab) {
            end.centerY()
        } else {
            start.centerY()
        }
        val startRadius = if (toFab) {
            Math.hypot(start.width().toDouble(), start.height().toDouble()).toFloat()
        } else {
            start.width().toFloat()
        }
        val endRadius = if (toFab) {
            end.width().toFloat()
        } else {
            Math.hypot(end.width().toDouble(), end.height().toDouble()).toFloat()
        }
        val circularReveal = ViewAnimationUtils.createCircularReveal(
                endView, centerX, centerY, startRadius, endRadius)
        circularReveal.duration = duration

        val animatorSet = AnimatorSet()
        animatorSet.playTogether(colorAnimator, circularReveal)
        animatorSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                endView.overlay.clear()
                if (toFab) {
                    sceneRoot.visibility = View.GONE
                }
            }
        })
        return animatorSet
    }

    private fun captureValues(transitionValues: TransitionValues) {
        val view = transitionValues.view ?: return
        transitionValues.values.put(BOUNDS, Rect(view.left, view.top, view.right, view.bottom))
    }
}
