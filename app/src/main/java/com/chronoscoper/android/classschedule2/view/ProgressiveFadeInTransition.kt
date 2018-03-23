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
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.drawable.Drawable
import android.support.v4.view.animation.FastOutSlowInInterpolator
import com.bumptech.glide.request.transition.Transition

class ProgressiveFadeInTransition : Transition<Drawable> {
    override fun transition(current: Drawable?, adapter: Transition.ViewAdapter?): Boolean {
        current ?: return false
        adapter ?: return false
        val cm = ColorMatrix()
        val saturation = ValueAnimator.ofFloat(0f, 1f)
        saturation.duration = 2000
        saturation.interpolator = FastOutSlowInInterpolator()
        saturation.addUpdateListener {
            val updated = it.animatedValue as Float
            cm.setSaturation(updated)
            current.colorFilter = ColorMatrixColorFilter(cm)
        }
        saturation.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                // Make sure to be displayed image without any filters
                current.clearColorFilter()
            }
        })
        val alpha = ObjectAnimator.ofInt(current, "alpha", 0, 255)
        alpha.duration = 1000
        val animators = AnimatorSet()
        animators.playTogether(saturation, alpha)
        animators.start()
        adapter.setDrawable(current)
        return true
    }
}
