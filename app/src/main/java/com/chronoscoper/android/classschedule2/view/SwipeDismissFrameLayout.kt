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

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout


class SwipeDismissFrameLayout(context: Context, attrs: AttributeSet?, defStyleAttr: Int)
    : FrameLayout(context, attrs, defStyleAttr) {
    companion object {
        private const val SCROLL_SENSITIVITY = 0.15f
    }

    constructor(context: Context) : this(context, null, 0)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    override fun onStartNestedScroll(child: View, target: View, nestedScrollAxes: Int): Boolean {
        return nestedScrollAxes and View.SCROLL_AXIS_VERTICAL != 0
    }

    override fun onNestedScroll(target: View?, dxConsumed: Int, dyConsumed: Int,
                                dxUnconsumed: Int, dyUnconsumed: Int) {
        scroll(dyUnconsumed)
    }

    override fun onStopNestedScroll(child: View?) {
        super.onStopNestedScroll(child)
        //In case user has scrolled upward, totalScroll is negative so we have to
        //judge scroll distance by absolute number.
        if (Math.abs(totalScroll) < dismissDistance) {
            animate()
                    .translationY(0f)
                    .start()
        } else {
            swipeDismissCallback?.onDismiss()
        }
        totalScroll = 0f
    }

    private var dismissDistance = 0f

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        dismissDistance = h / 10f
    }

    private var totalScroll = 0f

    private fun scroll(dy: Int) {
        val shouldScroll = totalScroll - dy * SCROLL_SENSITIVITY
        totalScroll = shouldScroll
        translationY = shouldScroll
        swipeDismissCallback?.onSwipe(shouldScroll)
    }

    var swipeDismissCallback: SwipeDismissCallback? = null

    abstract class SwipeDismissCallback {
        open fun onSwipe(totalScroll: Float) {}
        open fun onDismiss() {}
    }
}
