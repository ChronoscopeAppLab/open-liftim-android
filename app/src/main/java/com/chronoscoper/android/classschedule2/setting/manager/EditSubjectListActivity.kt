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

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.ActivityOptionsCompat
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.RecyclerView
import android.view.View
import com.chronoscoper.android.classschedule2.BaseActivity
import com.chronoscoper.android.classschedule2.R
import com.chronoscoper.android.classschedule2.sync.LiftimContext
import kotterknife.bindView

class EditSubjectListActivity : BaseActivity() {
    companion object {
        private const val EXTRA_LIFTIM_CODE = "liftim_code"
        fun open(context: Context, liftimCode: Long) {
            context.startActivity(Intent(context, EditSubjectListActivity::class.java)
                    .putExtra(EXTRA_LIFTIM_CODE, liftimCode))
        }
    }

    private val addButton by bindView<View>(R.id.add)
    private val list by bindView<RecyclerView>(R.id.list)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val liftimCode = intent.getLongExtra(EXTRA_LIFTIM_CODE, -1)
        if (liftimCode < 0) {
            finish()
            return
        }
        setContentView(R.layout.activity_edit_subject_list)

        val subjects = LiftimContext.getOrmaDatabase()
                .selectFromSubject().liftimCodeEq(liftimCode)
                .toList()

        list.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        list.adapter = SubjectAdapter(this, subjects)

        addButton.setOnClickListener {
            EditSubjectActivity.open(this, 1, null, -1,
                    ActivityOptionsCompat.makeSceneTransitionAnimation(
                            this, it, getString(R.string.t_subject_color)).toBundle())
        }
    }
}
