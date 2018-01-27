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

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.chronoscoper.android.classschedule2.BaseActivity
import com.chronoscoper.android.classschedule2.R
import com.chronoscoper.android.classschedule2.sync.Info
import com.chronoscoper.android.classschedule2.sync.InfoRemoteModel
import com.chronoscoper.android.classschedule2.sync.LiftimContext
import com.chronoscoper.android.classschedule2.task.RegisterInfoService
import com.chronoscoper.android.classschedule2.util.progressiveFadeInTransition
import kotterknife.bindView
import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

class EditInfoActivity : BaseActivity() {
    companion object {
        private const val ID = "source_id"
        fun open(context: Context, sourceId: String) {
            context.startActivity(Intent(context, EditInfoActivity::class.java)
                    .putExtra(ID, sourceId))
        }
    }

    private val liftimCodeImage by bindView<ImageView>(R.id.liftim_code_image)
    private val liftimCodeLabel by bindView<TextView>(R.id.liftim_code)
    private val titleInput by bindView<EditText>(R.id.title)
    private val linkUrlInput by bindView<EditText>(R.id.link_url)
    private val detailInput by bindView<EditText>(R.id.detail)
    private val dateLabel by bindView<TextView>(R.id.date)
    private val timeLabel by bindView<TextView>(R.id.time)

    private var isManager = false

    private var date: DateTime? = null
    private var time: DateTime? = null

    private var sourceId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_info)

        val id = intent.getStringExtra(ID)
        if (id != null) {
            sourceId = id
            initWithSpecifiedId(id)
        }

        val liftimCode = LiftimContext.getLiftimCode()
        Glide.with(this)
                .load(LiftimContext.getApiUrl("liftim_code_image.png" +
                        "?liftim_code=$liftimCode" +
                        "&token=${LiftimContext.getToken()}"))
                .apply(RequestOptions.circleCropTransform())
                .transition(progressiveFadeInTransition())
                .into(liftimCodeImage)
        val liftimCodeInfo = LiftimContext.getOrmaDatabase()
                .selectFromLiftimCodeInfo().liftimCodeEq(liftimCode)
                .firstOrNull()
                ?: kotlin.run { finish(); return }
        liftimCodeLabel.text = liftimCodeInfo.name
        isManager = liftimCodeInfo.isManager
        dateLabel.setOnClickListener {
            val date = date ?: DateTime.now().plusDays(1)
            DatePickerDialog(this,
                    DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                        val selectedDate = DateTime(year, month + 1, dayOfMonth,
                                0, 0)
                        this.date = selectedDate
                        dateLabel.text = selectedDate.toString(DateTimeFormat.fullDate())
                    }, date.year, date.monthOfYear - 1, date.dayOfMonth).show()
        }
        timeLabel.setOnClickListener {
            val time = time ?: DateTime.now().plusHours(1)
            TimePickerDialog(this,
                    TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                        val selectedTime = DateTime(0, 1, 1, hourOfDay, minute)
                        this.time = selectedTime
                        timeLabel.text = selectedTime.toString(DateTimeFormat.shortTime())
                    }, time.hourOfDay, time.minuteOfHour, true).show()
        }
    }

    private fun initWithSpecifiedId(id: String) {
        val item = LiftimContext.getOrmaDatabase().selectFromInfo()
                .liftimCodeEq(LiftimContext.getLiftimCode())
                .idEq(id)
                .firstOrNull() ?: kotlin.run { finish(); return }
        titleInput.setText(item.title ?: "")
        detailInput.setText(item.detail ?: "")
        linkUrlInput.setText(item.link ?: "")
        val date = item.date
        if (date != null) {
            try {
                this.date = DateTime.parse(date, DateTimeFormat.forPattern("yyyy/MM/dd"))
                dateLabel.text = this.date!!.toString(DateTimeFormat.fullDate())
            } catch (ignore: Exception) {
            }
        }
        val time = item.time
        if (time != null) {
            try {
                this.time = DateTime.parse(time, DateTimeFormat.forPattern("HH:mm"))
                timeLabel.text = this.time!!.toString(DateTimeFormat.shortTime())
            } catch (ignore: Exception) {
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (isManager) {
            menuInflater.inflate(R.menu.options_edit_info_manager, menu)
        } else {
            menuInflater.inflate(R.menu.options_edit_info, menu)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        item ?: return super.onOptionsItemSelected(item)
        when (item.itemId) {
            R.id.options_register_local -> {
                if (titleInput.text.isNullOrEmpty()) {
                    AlertDialog.Builder(this)
                            .setMessage(R.string.form_error_info)
                            .setPositiveButton(R.string.ok, null).show()
                } else {
                    registerLocal()
                }
            }
            R.id.options_register_remote -> {
                if (titleInput.text.isNullOrEmpty()) {
                    AlertDialog.Builder(this)
                            .setMessage(R.string.form_error_info)
                            .setPositiveButton(R.string.ok, null).show()
                } else {
                    RemoteAdditionalFieldFragment().show(supportFragmentManager, null)
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun registerLocal() {
        val db = LiftimContext.getOrmaDatabase()
        val element = createElementFromCurrentState()
        val id = sourceId
        if (id != null) {
            db.deleteFromInfo().liftimCodeEq(LiftimContext.getLiftimCode()).idEq(id)
                    .execute()
            element.id = id
        } else {
            element.id = DateTime.now().toString(DateTimeFormat.fullDateTime())
        }
        db.insertIntoInfo(element)
        finish()
    }

    fun createElementFromCurrentState(): Info {
        var linkUrl = linkUrlInput.text.toString()
        if (linkUrl.isNotEmpty()
                && !linkUrl.startsWith("http://")
                && !linkUrl.startsWith("https://")) {
            linkUrl = "http://$linkUrl"
        }
        val result = Info()
        result.apply {
            liftimCode = LiftimContext.getLiftimCode()
            id = sourceId
            title = titleInput.text.toString()
            detail = detailInput.text.toString()
            weight = 0
            date = this@EditInfoActivity.date?.toString("yyyy/MM/dd")
            time = this@EditInfoActivity.time?.toString("HH:mm")
            link =
                    if (linkUrl.isBlank()) {
                        null
                    } else {
                        linkUrl
                    }
            removable = true
            type = Info.TYPE_LOCAL_MEMO
            this.addedBy = Info.LOCAL
        }
        return result
    }

    class RemoteAdditionalFieldFragment : DialogFragment() {
        init {
            isCancelable = false
        }

        override fun onCreateView(
                inflater: LayoutInflater,
                container: ViewGroup?,
                savedInstanceState: Bundle?): View? =
                inflater.inflate(R.layout.fragment_remote_additional_field,
                        container, false)

        private val removableSwitch by bindView<Switch>(R.id.user_removable)
        private val importanceSeekBar by bindView<DiscreteSeekBar>(R.id.importance)
        private val typeSpinner by bindView<Spinner>(R.id.type)
        private val okButton by bindView<Button>(R.id.ok)
        private val cancelButton by bindView<Button>(R.id.cancel)

        override fun onActivityCreated(savedInstanceState: Bundle?) {
            super.onActivityCreated(savedInstanceState)
            val element = (activity as? EditInfoActivity)?.createElementFromCurrentState()
                    ?: throw IllegalStateException("Must be added to EditInfoActivity")
            okButton.setOnClickListener {
                val remoteFormatElement = InfoRemoteModel.InfoBody()
                remoteFormatElement.apply {
                    id = element.id
                    title = element.title
                    detail = element.detail
                    weight = importanceSeekBar.progress
                    date = element.date
                    time = element.time
                    link = element.link
                    type = typeSpinner.selectedItemPosition
                    removable = removableSwitch.isChecked
                    RegisterInfoService.start(context!!, LiftimContext.getGson()
                            .toJson(remoteFormatElement))
                }
            }
            cancelButton.setOnClickListener {
                dismiss()
            }
        }
    }
}
