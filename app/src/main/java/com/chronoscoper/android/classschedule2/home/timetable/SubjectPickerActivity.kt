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

import android.app.Activity
import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import com.chronoscoper.android.classschedule2.BaseActivity
import com.chronoscoper.android.classschedule2.R
import jp.wasabeef.recyclerview.adapters.SlideInBottomAnimationAdapter
import kotterknife.bindView

class SubjectPickerActivity : BaseActivity() {
    companion object {
        private const val TAG = "SubjectPicker"
        const val EXTRA_SUBJECT = "SUBJECT"
        const val EXTRA_DETAIL = "DETAIL"
    }

    private val subject by bindView<EditText>(R.id.subject)
    private val detail by bindView<EditText>(R.id.detail)
    private val list by bindView<RecyclerView>(R.id.list)
    private val addInfo by bindView<View>(R.id.add_info)

    private val subjectAdapter by lazy { SubjectAdapter(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.slide_in, R.anim.no_anim)
        setContentView(R.layout.activity_subject_picker)
        subject.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s == null) return
                subjectAdapter.query = s.toString()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        addInfo.setOnClickListener {
            it.visibility = View.GONE
            detail.visibility = View.VISIBLE
            detail.requestFocus()
        }
        list.adapter = SlideInBottomAnimationAdapter(subjectAdapter)
        list.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        subjectAdapter.onSelectedListener = {
            Log.d(TAG, "Subject selected: $it")
            subject.setText(it)
            detail.requestFocus()
        }
        intent.getStringExtra(EXTRA_DETAIL)?.let {
            if (it.isEmpty()) return
            detail.setText(it)
            addInfo.visibility = View.GONE
            detail.visibility = View.VISIBLE
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.options_edit_subject, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.done -> {
                intent.apply {
                    putExtra(EXTRA_SUBJECT, subject.text.toString())
                    putExtra(EXTRA_DETAIL, detail.text.toString())
                }
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.no_anim, R.anim.slide_out)
    }
}
