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

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Matrix
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout

class SwipeDownDiscardView(context: Context, attrs: AttributeSet?, defStyleAttr: Int)
    : FrameLayout(context, attrs, defStyleAttr) {
    constructor(context: Context) : this(context, null, 0)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    companion object {
        private const val TAG = "SwipeDownDiscardView"
    }

    private val canvasMatrix = Matrix()

    private var lastY = 0f
    private var totalScroll = 0f
    private var swipeProportion = 0f
    private var maxSwipe = 0
    private var swipeDownward = false

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        maxSwipe = context.resources.displayMetrics.heightPixels / 2
    }

    override fun onDraw(canvas: Canvas?) {
        canvas ?: return
        canvas.matrix = canvasMatrix
        super.onDraw(canvas)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event ?: return false
        val action = event.action
        if (action == MotionEvent.ACTION_DOWN) {
            lastY = event.y
        } else if (action == MotionEvent.ACTION_MOVE) {
            totalScroll += event.y - lastY
            translationY = totalScroll
            swipeProportion = Math.abs(totalScroll) / maxSwipe
            swipeDownward = totalScroll > 0
            onSwipeListener?.onSwipe(swipeProportion)
            if (totalScroll > 0) {
                canvasMatrix.postScale(1 - swipeProportion * 2 / 3, 1 - swipeProportion / 4,
                        width / 2f, height.toFloat())
                invalidate()
            }
        } else {
            // Regard as action is done.
            if (swipeDownward && swipeProportion > 0.9f) {
                // Regard as discarded
                onSwipeListener?.onDiscard()
            } else {
                animateReturn()
            }
        }
        return true
    }

    var onSwipeListener: OnSwipeListener? = null

    private fun animateReturn() {
        canvasMatrix.reset()
        invalidate()
        animate().translationY(0f).start()
        swipeProportion = 0f
        totalScroll = 0f
    }

    interface OnSwipeListener {
        fun onSwipe(proportion: Float)

        fun onDiscard()
    }
}
