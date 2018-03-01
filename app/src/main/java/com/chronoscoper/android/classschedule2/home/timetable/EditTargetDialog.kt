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
package com.chronoscoper.android.classschedule2.home.timetable

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.content.ContextCompat
import android.view.View
import com.chronoscoper.android.classschedule2.BaseActivity
import com.chronoscoper.android.classschedule2.R
import com.chronoscoper.android.classschedule2.transition.FabExpandTransition
import com.chronoscoper.android.classschedule2.transition.FabTransformTransition
import kotterknife.bindView

class EditTargetDialog : BaseActivity() {

    private val background by bindView<View>(R.id.background)

    private val addNew by bindView<View>(R.id.add_new_timetable)
    private val editExisting by bindView<View>(R.id.edit_existing_timetable)
    private val cancel by bindView<View>(R.id.cancel)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_target_dialog)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.sharedElementEnterTransition = FabTransformTransition()
        }

        background.setOnClickListener {
            animateFinishCompat()
        }

        addNew.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                it.transitionName = getString(R.string.t_fab)
                FabExpandTransition.configure(
                        ContextCompat.getColor(this, R.color.colorAccent))
            }
            val activityOptions = ActivityOptionsCompat
                    .makeSceneTransitionAnimation(this, it, getString(R.string.t_fab))
            startActivity(Intent(this, EditTimetableActivity::class.java),
                    activityOptions.toBundle())
            finish()
        }

        editExisting.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                it.transitionName = getString(R.string.t_fab)
                FabExpandTransition.configure(
                        ContextCompat.getColor(this, R.color.colorAccent))
            }
            val activityOptions = ActivityOptionsCompat
                    .makeSceneTransitionAnimation(this, it, getString(R.string.t_fab))
            startActivity(Intent(this, EditTimetableActivity::class.java),
                    activityOptions.toBundle())
            finish()
        }

        cancel.setOnClickListener {
            animateFinishCompat()
        }
    }
}
