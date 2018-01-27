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
package com.chronoscoper.android.classschedule2.setting.manager

import android.app.Activity
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.TextView
import com.chronoscoper.android.classschedule2.BaseActivity
import com.chronoscoper.android.classschedule2.R
import com.chronoscoper.android.classschedule2.sync.Subject
import com.chronoscoper.android.classschedule2.util.obtainColorCorrespondsTo
import kotterknife.bindView
import org.parceler.Parcels

class EditSubjectActivity : BaseActivity() {
    companion object {
        const val EXTRA_ITEM = "item"
        const val EXTRA_POSITION = "position"
        fun open(activity: Activity, requestCode: Int,
                 item: Subject?, position: Int, options: Bundle?) {
            val intent = Intent(activity, EditSubjectActivity::class.java)
                    .putExtra(EXTRA_POSITION, position)
            if (item != null) {
                intent.putExtra(EXTRA_ITEM, Parcels.wrap(item))
            }
            activity.startActivityForResult(intent, requestCode, options)
        }
    }

    private val subjectColor by bindView<View>(R.id.subject_color)
    private val subjectName by bindView<TextView>(R.id.subject_name)
    private val subjectShortName by bindView<TextView>(R.id.subject_short_name)

    private var position = -1
    private var colorName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val item = Parcels.unwrap<Subject>(intent.getParcelableExtra(EXTRA_ITEM))
        position = intent.getIntExtra(EXTRA_POSITION, -1)
        setContentView(R.layout.activity_edit_subject)

        val color = item?.let { obtainColorCorrespondsTo(item.subject) }
                ?: ContextCompat.getColor(this, R.color.colorAccent)
        subjectColor.background.setColorFilter(color, PorterDuff.Mode.SRC_IN)
        item?.run {
            colorName = this.color
            subjectName.text = subject
            subjectShortName.text = shortSubject
        }
    }

    override fun animateFinishCompat() {
        val result = Subject().apply {
            subject = subjectName.text.toString()
            shortSubject = subjectShortName.text.toString()
            color = colorName
        }
        setResult(Activity.RESULT_OK, Intent()
                .putExtra(EXTRA_ITEM, Parcels.wrap(result))
                .putExtra(EXTRA_POSITION, position))
        if (position < 0) {
            finish()
        } else {
            super.animateFinishCompat()
        }
    }
}
