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
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.chronoscoper.android.classschedule2.R
import com.chronoscoper.android.classschedule2.util.EventMessage
import kotterknife.bindView
import org.greenrobot.eventbus.EventBus
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

class DateTimePickerDialog : DialogFragment() {
    companion object {
        private const val TAG = "DateTimePickerDialog"
        const val EVENT_DATE_TIME_PICKED = "DATE_TIME_PICKED"
        private const val EXTRA_DATE = "DATE"
        private const val EXTRA_TIME = "TIME"

        /**
         * @param currentSelectDate Should be "yyyy/MM/dd" format
         * @param currentSelectTime Should be "HH:mm" format
         */
        fun newInstance(currentSelectDate: String?, currentSelectTime: String?)
                : DateTimePickerDialog {
            val result = DateTimePickerDialog()
            result.arguments = Bundle().apply {
                putString(EXTRA_DATE, currentSelectDate)
                putString(EXTRA_TIME, currentSelectTime)
            }
            return result
        }
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_date_time_picker, container, false)
    }

    private val dateSpinner by bindView<Spinner>(R.id.date)
    private val dateAdapter by lazy {
        ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, dateAdapterEntry)
    }

    /**
     * 0 -> Currently selected
     * 1 -> Tomorrow
     * 2 -> Pick
     * 3 -> Unset
     */
    private val dateAdapterEntry = arrayOfNulls<String>(4)
    private val timeSpinner by bindView<Spinner>(R.id.time)
    private val timeAdapter by lazy {
        ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, timeAdapterEntry)
    }

    private val done by bindView<View>(R.id.done)
    private val cancel by bindView<View>(R.id.cancel)

    private var date: String? = null
    private var time: String? = null

    /**
     * 0 -> Currently selected
     * 1 -> 7:00
     * 2 -> Pick
     * 3 -> Unset
     */
    private val timeAdapterEntry = arrayOfNulls<String>(4)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        date = arguments?.getString(EXTRA_DATE, null)
        time = arguments?.getString(EXTRA_TIME, null)

        initializeDateSpinner(date)
        initializeTimeSpinner(time)

        done.setOnClickListener {
            val picked = PickedDateTime(date,
                    if (date != null) {
                        time
                    } else {
                        null
                    })
            EventBus.getDefault().post(EventMessage(EVENT_DATE_TIME_PICKED, picked))
            dismiss()
        }
        cancel.setOnClickListener {
            dismiss()
        }
    }

    private fun initializeDateSpinner(date: String?) {
        if (date == null) {
            dateAdapterEntry[0] = getString(R.string.not_specified)
        } else {
            try {
                dateAdapterEntry[0] =
                        DateTime.parse(date, DateTimeFormat.forPattern("yyyy/MM/dd"))
                                .toString(DateTimeFormat.mediumDate())
            } catch (e: Exception) {
                Log.e(TAG, "Invalid date format", e)
                this.date = null
                dateAdapterEntry[0] = getString(R.string.not_specified)
            }
        }
        dateAdapterEntry[1] = getString(R.string.tomorrow)
        dateAdapterEntry[2] = getString(R.string.specify_date)
        dateAdapterEntry[3] = getString(R.string.unset)
        dateSpinner.adapter = dateAdapter
        dateSpinner.setSelection(0)
        dateSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?,
                                        position: Int, id: Long) {
                when (position) {
                    1 -> {
                        val picked = DateTime.now().plusDays(1)
                        this@DateTimePickerDialog.date = picked.toString("yyyy/MM/dd")
                        dateAdapterEntry[0] = picked.toString(DateTimeFormat.mediumDate())
                        dateAdapter.notifyDataSetChanged()
                    }
                    2 -> {
                        val oldDate =
                                if (this@DateTimePickerDialog.date == null) {
                                    DateTime.now().plusDays(1)
                                } else {
                                    DateTime.parse(this@DateTimePickerDialog.date,
                                            DateTimeFormat.forPattern("yyyy/MM/dd"))
                                }
                        DatePickerDialog(context, { _, year, month, dayOfMonth ->
                            val picked = DateTime(year, month + 1, dayOfMonth, 0, 0)
                            this@DateTimePickerDialog.date = picked.toString("yyyy/MM/dd")
                            dateAdapterEntry[0] = picked.toString(DateTimeFormat.mediumDate())
                            dateAdapter.notifyDataSetChanged()
                            timeSpinner.visibility = View.VISIBLE
                        }, oldDate.year, oldDate.monthOfYear - 1, oldDate.dayOfMonth)
                                .show()
                    }
                    3 -> {
                        this@DateTimePickerDialog.date = null
                        dateAdapterEntry[0] = getString(R.string.not_specified)
                        dateAdapter.notifyDataSetChanged()
                    }
                }
                if (this@DateTimePickerDialog.date == null) {
                    timeSpinner.visibility = View.GONE
                } else {
                    timeSpinner.visibility = View.VISIBLE
                }
                dateSpinner.setSelection(0)
            }
        }
    }

    private fun initializeTimeSpinner(time: String?) {
        if (time == null) {
            timeAdapterEntry[0] = getString(R.string.not_specified)
        } else {
            try {
                timeAdapterEntry[0] = DateTime.parse(time,
                        DateTimeFormat.forPattern("HH:mm"))
                        .toString(DateTimeFormat.shortTime())
            } catch (e: Exception) {
                Log.e(TAG, "Invalid time format")
                this.time = null
                timeAdapterEntry[0] = getString(R.string.not_specified)
            }
        }
        timeAdapterEntry[1] = DateTime.now()
                .withTime(7, 0, 0, 0)
                .toString(DateTimeFormat.shortTime())
        timeAdapterEntry[2] = getString(R.string.specify_time)
        timeAdapterEntry[3] = getString(R.string.unset)
        timeSpinner.adapter = timeAdapter
        timeSpinner.setSelection(0)
        timeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?,
                                        view: View?, position: Int, id: Long) {
                when (position) {
                    1 -> {
                        val picked = DateTime().withTime(7, 0, 0, 0)
                        this@DateTimePickerDialog.time = picked.toString("HH:mm")
                        timeAdapterEntry[0] = picked.toString(DateTimeFormat.shortTime())
                        timeAdapter.notifyDataSetChanged()
                    }
                    2 -> {
                        val oldTime =
                                if (this@DateTimePickerDialog.time == null) {
                                    DateTime().withTime(7, 0, 0, 0)
                                } else {
                                    DateTime.parse(this@DateTimePickerDialog.time,
                                            DateTimeFormat.forPattern("HH:mm"))
                                }
                        TimePickerDialog(context, { _, hourOfDay, minute ->
                            val picked = DateTime().withTime(hourOfDay, minute, 0, 0)
                            this@DateTimePickerDialog.time = picked.toString("HH:mm")
                            timeAdapterEntry[0] = picked.toString(DateTimeFormat.shortTime())
                            timeAdapter.notifyDataSetChanged()
                        }, oldTime.hourOfDay, oldTime.minuteOfHour, true)
                                .show()
                    }
                    3 -> {
                        this@DateTimePickerDialog.time = null
                        timeAdapterEntry[0] = getString(R.string.not_specified)
                        timeAdapter.notifyDataSetChanged()
                    }
                }
                timeSpinner.setSelection(0)
            }
        }
    }

    data class PickedDateTime(val date: String?, val time: String?)
}