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
package com.chronoscoper.android.classschedule2.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.drawable.Drawable
import android.support.v4.view.animation.FastOutSlowInInterpolator
import com.bumptech.glide.request.transition.Transition

class ProgressiveFadeInTransition : Transition<Drawable> {
    private val interpolator = FastOutSlowInInterpolator()

    override fun transition(current: Drawable?, adapter: Transition.ViewAdapter?): Boolean {
        current ?: return false
        adapter ?: return false
        val cm = ColorMatrix()
        val animator = ValueAnimator.ofFloat(0f, 1f)
        animator.duration = 2000
        animator.addUpdateListener {
            val updated = it.animatedValue as Float
            cm.setSaturation(updated)
            cm.setScale(1f, 1f, 1f,
                    interpolator.getInterpolation(updated))
            current.colorFilter = ColorMatrixColorFilter(cm)
        }
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                // Make sure to be displayed image without any filters
                current.clearColorFilter()
            }
        })
        animator.start()
        adapter.setDrawable(current)
        return true
    }
}
