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
package com.chronoscoper.android.classschedule2.home.info

import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.chronoscoper.android.classschedule2.BaseActivity
import com.chronoscoper.android.classschedule2.R
import com.chronoscoper.android.classschedule2.sync.Info
import com.chronoscoper.android.classschedule2.sync.LiftimSyncEnvironment
import com.chronoscoper.android.classschedule2.util.DateTimeUtils
import com.chronoscoper.android.classschedule2.util.openInCustomTab
import kotterknife.bindView
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

class ViewInfoActivity : BaseActivity() {
    companion object {
        private const val ID = "target_id"
        fun open(context: Context, id: String) {
            context.startActivity(Intent(context, ViewInfoActivity::class.java)
                    .putExtra(ID, id))
        }
    }

    private val title by bindView<TextView>(R.id.title)
    private val detail by bindView<TextView>(R.id.detail)
    private val date by bindView<TextView>(R.id.date)
    private val linkUrl by bindView<TextView>(R.id.link_url)
    private val type by bindView<TextView>(R.id.type)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_info)

        val item = obtainSpecifiedElement() ?: kotlin.run { finish(); return }
        title.text = item.title
        detail.text = item.detail
        if (!item.date.isNullOrEmpty()) {
            date.visibility = View.VISIBLE
            date.text = DateTimeUtils.getParsedDateExpression(item.date)
        }
        if (!item.link.isNullOrEmpty()) {
            linkUrl.visibility = View.VISIBLE
            linkUrl.text = item.link
            linkUrl.setOnClickListener {
                openInCustomTab(this, item.link!!)
            }
        }
        when (item.type) {
            Info.TYPE_UNSPECIFIED -> {
                type.background.setColorFilter(-0x777778, PorterDuff.Mode.SRC_IN)
                type.text = getString(R.string.type_unspecified)
            }
            Info.TYPE_EVENT -> {
                type.background.setColorFilter(-0x6800, PorterDuff.Mode.SRC_IN)
                type.text = getString(R.string.type_event)
            }
            Info.TYPE_INFORMATION -> {
                type.background.setColorFilter(-0xde690d, PorterDuff.Mode.SRC_IN)
                type.text = getString(R.string.type_information)
            }
            Info.TYPE_SUBMISSION -> {
                type.background.setColorFilter(-0xb350b0, PorterDuff.Mode.SRC_IN)
                type.text = getString(R.string.type_submission)
            }
            Info.TYPE_LOCAL_MEMO -> {
                type.background.setColorFilter(-0x98c549, PorterDuff.Mode.SRC_IN)
                type.text = getString(R.string.type_memo)
            }
        }
    }

    private fun obtainSpecifiedElement(): Info? {
        val id = intent.getStringExtra(ID) ?: return null
        return LiftimSyncEnvironment.getOrmaDatabase()
                .selectFromInfo()
                .liftimCodeEq(LiftimSyncEnvironment.getLiftimCode())
                .idEq(id)
                .firstOrNull()
    }
}
