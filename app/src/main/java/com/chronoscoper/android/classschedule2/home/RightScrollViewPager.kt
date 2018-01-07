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
package com.chronoscoper.android.classschedule2.home

import android.annotation.SuppressLint
import android.content.Context
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.MotionEvent

class RightScrollViewPager(context: Context, attrs: AttributeSet?) : ViewPager(context, attrs) {
    constructor(context: Context) : this(context, null)

    private var previousX = 0F

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        ev ?: return super.onTouchEvent(ev)
        if (ev.action == MotionEvent.ACTION_MOVE) {
            if (previousX != 0F && previousX > ev.x) {
                return false
            }
            previousX = ev.x
        } else if (ev.action == MotionEvent.ACTION_UP) {
            previousX = 0F
        }
        return super.onTouchEvent(ev)
    }

    override fun dispatchKeyEvent(event: KeyEvent?): Boolean = false
}
