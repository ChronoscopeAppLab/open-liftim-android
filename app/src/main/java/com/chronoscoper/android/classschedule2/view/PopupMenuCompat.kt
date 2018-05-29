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
import android.os.Build
import android.support.annotation.MenuRes
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu

class PopupMenuCompat(context: Context, private val anchor: View) {
    private val popup by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            PopupMenu(context, anchor, Gravity.NO_GRAVITY,
                    0, android.R.style.Widget_Material_PopupMenu_Overflow)
        } else {
            PopupMenu(context, anchor)
        }
    }

    fun inflate(@MenuRes menu: Int) {
        popup.inflate(menu)
    }

    fun setOnMenuItemClickListener(l: (item: MenuItem) -> Boolean) {
        popup.setOnMenuItemClickListener(l)
    }

    fun show() {
        popup.show()
    }

    val dragToOpenListener: View.OnTouchListener?
        get() = popup.dragToOpenListener

}
