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

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.util.Log
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
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.chronoscoper.android.classschedule2.BaseActivity
import com.chronoscoper.android.classschedule2.R
import com.chronoscoper.android.classschedule2.functionrestriction.getFunctionRestriction
import com.chronoscoper.android.classschedule2.sync.Info
import com.chronoscoper.android.classschedule2.sync.InfoRemoteModel
import com.chronoscoper.android.classschedule2.sync.LiftimContext
import com.chronoscoper.android.classschedule2.task.RegisterProgressActivity
import com.chronoscoper.android.classschedule2.task.RegisterTemporary
import com.chronoscoper.android.classschedule2.util.EventMessage
import com.chronoscoper.android.classschedule2.util.isNetworkConnected
import com.chronoscoper.android.classschedule2.util.progressiveFadeInTransition
import com.chronoscoper.android.classschedule2.util.showToast
import kotterknife.bindView
import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

class EditInfoActivity : BaseActivity() {
    companion object {
        private const val TAG = "EditInfoActivity"
        private const val ID = "source_id"
        fun open(context: Context, sourceId: String) {
            context.startActivity(Intent(context, EditInfoActivity::class.java)
                    .putExtra(ID, sourceId))
        }

        internal const val RC_REGISTER = 101
    }

    private val liftimCodeImage by bindView<ImageView>(R.id.liftim_code_image)
    private val liftimCodeLabel by bindView<TextView>(R.id.liftim_code)
    private val titleInput by bindView<EditText>(R.id.title)
    private val detailInput by bindView<EditText>(R.id.detail)
    private val optionDateTime by bindView<View>(R.id.date_time)
    private val optionLink by bindView<View>(R.id.link_url)

    private var isManager = false

    private var date: DateTime? = null
    private var time: DateTime? = null
    private var linkUrl: String? = null

    private var sourceId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_info)
        overridePendingTransition(R.anim.slide_in, R.anim.no_anim)

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

        optionDateTime.setOnClickListener {
            DateTimePickerDialog.newInstance(
                    date?.toString("yyyy/MM/dd"),
                    time?.toString("HH:mm"))
                    .show(supportFragmentManager, null)
        }

        optionLink.setOnClickListener {
            UrlPickerDialog.newInstance(linkUrl).show(supportFragmentManager, null)
        }

        EventBus.getDefault().register(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_REGISTER) {
            if (resultCode == Activity.RESULT_OK) {
                registerLocal()
                EventBus.getDefault().post(
                        EventMessage.of(InfoRecyclerViewAdapter.EVENT_ENTRY_UPDATED))
            } else {
                showToast(this, getString(R.string.register_failed), Toast.LENGTH_SHORT)
            }
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.no_anim, R.anim.slide_out)
    }

    @Suppress("UNUSED")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onFieldPicked(event: EventMessage) {
        if (event.type == DateTimePickerDialog.EVENT_DATE_TIME_PICKED) {
            Log.d(TAG, "Datetime set ${event.data}")
            val picked = event.data as? DateTimePickerDialog.PickedDateTime
            val pickedDate = picked?.date
            val pickedTime = picked?.time
            date = pickedDate?.let {
                DateTime.parse(it, DateTimeFormat.forPattern("yyyy/MM/dd"))
            }
            time = pickedTime?.let {
                DateTime.parse(it, DateTimeFormat.forPattern("HH:mm"))
            }
        } else if (event.type == UrlPickerDialog.EVENT_URL_PICKED) {
            Log.d(TAG, "URL \"${event.data}\" selected.")
            linkUrl = event.data as? String
        } else {
            Log.i(TAG, "Not subscribing event. Ignoring...")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    private var item: Info? = null

    private fun initWithSpecifiedId(id: String) {
        val item = LiftimContext.getOrmaDatabase().selectFromInfo()
                .liftimCodeEq(LiftimContext.getLiftimCode())
                .idEq(id)
                .firstOrNull() ?: kotlin.run { finish(); return }
        this.item = item
        titleInput.setText(item.title ?: "")
        detailInput.setText(item.detail ?: "")
        linkUrl = item.link
        val date = item.date
        if (date != null) {
            try {
                this.date = DateTime.parse(date, DateTimeFormat.forPattern("yyyy/MM/dd"))
            } catch (ignore: Exception) {
            }
        }
        val time = item.time
        if (time != null) {
            try {
                this.time = DateTime.parse(time, DateTimeFormat.forPattern("HH:mm"))
            } catch (ignore: Exception) {
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (isManager && getFunctionRestriction(this).addInfo) {
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
                EventBus.getDefault()
                        .post(EventMessage(InfoRecyclerViewAdapter.EVENT_ENTRY_UPDATED))
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
        animateFinish()
    }

    fun createElementFromCurrentState(): Info {
        var linkUrl = this.linkUrl
        if (!linkUrl.isNullOrBlank()
                && linkUrl?.matches(Regex("^https?://.+")) == false) {
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
                    if (linkUrl.isNullOrBlank()) {
                        null
                    } else {
                        linkUrl
                    }
            removable = true
            addedBy = item?.addedBy ?: Info.LOCAL
            type = item?.type ?: Info.TYPE_LOCAL_MEMO
            edited = true
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
                if (!isNetworkConnected(context!!)) {
                    AlertDialog.Builder(context!!)
                            .setTitle(R.string.network_disconnected)
                            .setMessage(R.string.network_disconnected_message)
                            .setPositiveButton(R.string.retry,
                                    { _, _ -> okButton.performClick() })
                            .show()
                    return@setOnClickListener
                }
                it.isEnabled = false
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

                    RegisterTemporary.save(context!!, RegisterTemporary.TARGET_INFO,
                            LiftimContext.getGson().toJson(remoteFormatElement))
                    activity!!.startActivityForResult(
                            Intent(context!!, RegisterProgressActivity::class.java), RC_REGISTER)
                }
            }
            cancelButton.setOnClickListener {
                dismiss()
            }
        }
    }
}
