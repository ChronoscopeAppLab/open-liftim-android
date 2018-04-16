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
import android.graphics.Outline
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.support.annotation.ColorInt
import android.support.annotation.RequiresApi
import android.transition.ArcMotion
import android.transition.Transition
import android.transition.TransitionValues
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import io.plaidapp.util.AnimUtils

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class FabTransformTransition : Transition() {
    companion object {
        fun configure(@ColorInt color: Int) {
            this.color = color
        }

        private var color = 0

        private const val BOUNDS = "bounds"
    }

    init {
        pathMotion = ArcMotion()
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
        val fabBounds = if (toFab) {
            end
        } else {
            start
        }
        val dialogBounds = if (toFab) {
            start
        } else {
            end
        }
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
        val translateX = (start.centerX() - end.centerX()).toFloat()
        val translateY = (start.centerY() - end.centerY()).toFloat()
        if (!toFab) {
            endView.translationX = translateX
            endView.translationY = translateY
        }
        val fabColor = ColorDrawable(color)
        fabColor.setBounds(sceneRoot.left, sceneRoot.top, sceneRoot.right, sceneRoot.bottom)
        fabColor.alpha = if (toFab) {
            0
        } else {
            255
        }
        endView.overlay.add(fabColor)
        val colorAnimator = ObjectAnimator.ofInt(fabColor, "alpha",
                if (toFab) {
                    255
                } else {
                    0
                })

        colorAnimator.duration = duration
        val translatePath = if (toFab) {
            pathMotion.getPath(0f, 0f, -translateX, -translateY)
        } else {
            pathMotion.getPath(translateX, translateY, 0f, 0f)
        }
        val translateAnimator = ObjectAnimator.ofFloat(
                endView, View.TRANSLATION_X, View.TRANSLATION_Y, translatePath)
        translateAnimator.duration = duration

        val centerX = endView.width / 2
        val centerY = endView.height / 2
        val startRadius = if (toFab) {
            Math.hypot(dialogBounds.width().toDouble() / 2, dialogBounds.height().toDouble() / 2).toFloat()
        } else {
            fabBounds.width().toFloat() / 2
        }
        val endRadius = if (toFab) {
            fabBounds.width().toFloat() / 2
        } else {
            Math.hypot(dialogBounds.width().toDouble() / 2, dialogBounds.height().toDouble() / 2).toFloat()
        }
        val circularReveal = ViewAnimationUtils.createCircularReveal(
                endView, centerX, centerY, startRadius, endRadius)
        circularReveal.duration = duration
        if (toFab) {
            circularReveal.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    endView.outlineProvider = object : ViewOutlineProvider() {
                        override fun getOutline(view: View?, outline: Outline?) {
                            if (view == null || outline == null) return
                            val left = (view.left - fabBounds.width()) / 2
                            val top = (view.top - fabBounds.height()) / 2
                            outline.setOval(left, top, left + fabBounds.width(), top + fabBounds.height())
                            view.clipToOutline = true
                        }
                    }
                }
            })
        }

        val animatorSet = AnimatorSet()
        animatorSet.playTogether(colorAnimator, translateAnimator, circularReveal)
        animatorSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                endView.overlay.clear()
            }
        })
        return AnimUtils.NoPauseAnimator(animatorSet)
    }

    private fun captureValues(transitionValues: TransitionValues) {
        val view = transitionValues.view ?: return
        transitionValues.values.put(BOUNDS, Rect(view.left, view.top, view.right, view.bottom))
    }
}
