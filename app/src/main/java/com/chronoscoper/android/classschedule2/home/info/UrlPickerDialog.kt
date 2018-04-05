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

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Spinner
import com.chronoscoper.android.classschedule2.R
import com.chronoscoper.android.classschedule2.util.EventMessage
import kotterknife.bindView
import org.greenrobot.eventbus.EventBus

class UrlPickerDialog : DialogFragment() {
    companion object {
        private const val TAG = "UrlPickerDialog"
        const val EVENT_URL_PICKED = "URL_PICKED"
        private const val EXTRA_OLD_LINK = "OLD_LINK"
        fun newInstance(currentSelectedLink: String?): UrlPickerDialog {
            val result = UrlPickerDialog()
            result.arguments = Bundle().apply {
                putString(EXTRA_OLD_LINK, currentSelectedLink)
            }
            return result
        }
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.dialog_url_picker, container, false)
    }

    private val scheme by bindView<Spinner>(R.id.scheme)
    private val url by bindView<EditText>(R.id.url)
    private val done by bindView<View>(R.id.done)
    private val cancel by bindView<View>(R.id.cancel)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val oldUrl = arguments?.getString(EXTRA_OLD_LINK, null)
        if (oldUrl != null) {
            if (oldUrl.startsWith("https://")) {
                scheme.setSelection(0)
                url.setText(oldUrl.substring(8)) // since "https://" consists with 8 chars
            } else if (oldUrl.startsWith("http://")) {
                scheme.setSelection(1)
                url.setText(oldUrl.substring(7)) // since "http://" consists with 7 chars
            } else {
                Log.e(TAG, "Malformed url $oldUrl; " +
                        "Regard as nothing was selected previously.")
            }
        } else {
            Log.i(TAG, "No URL given as argument.")
        }

        done.setOnClickListener {
            val url = this.url.text.toString()
            if (url.isBlank()) {
                Log.d(TAG, "Nothing selected or unset")
                EventBus.getDefault().post(EventMessage(EVENT_URL_PICKED, null))
            } else {
                val scheme =
                        if (this.scheme.selectedItemPosition == 1) {
                            "http"
                        } else {
                            "https"
                        }
                EventBus.getDefault().post(EventMessage(EVENT_URL_PICKED, "$scheme://$url"))
            }
            dismiss()
        }

        cancel.setOnClickListener {
            dismiss()
        }
    }
}
