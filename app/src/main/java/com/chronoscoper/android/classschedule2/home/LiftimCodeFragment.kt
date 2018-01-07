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
package com.chronoscoper.android.classschedule2.home

import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.chronoscoper.android.classschedule2.R
import com.chronoscoper.android.classschedule2.sync.LiftimCodeInfo
import com.chronoscoper.android.classschedule2.sync.LiftimSyncEnvironment
import kotterknife.bindView

class LiftimCodeFragment : Fragment() {
    companion object {
        private const val EXTRA_LIFTIM_CODE = "liftim_code"
        private const val EXTRA_NAME = "liftim_code_name"

        fun obtain(liftimCodeInfo: LiftimCodeInfo): LiftimCodeFragment {
            return LiftimCodeFragment()
                    .apply {
                        arguments = Bundle()
                                .apply {
                                    putLong(EXTRA_LIFTIM_CODE, liftimCodeInfo.liftimCode)
                                    putString(EXTRA_NAME, liftimCodeInfo.name)
                                }
                    }
        }
    }

    override fun onCreateView(inflater: LayoutInflater?,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? =
            inflater?.inflate(R.layout.liftim_code_pager, container, false)

    private val nameLabel by bindView<TextView>(R.id.name)
    private val image by bindView<ImageView>(R.id.image)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val args = arguments ?: return
        val liftimCode = args.getLong(EXTRA_LIFTIM_CODE).apply {
            if (this == 0L) {
                return
            }
        }
        val name = args.getString(EXTRA_NAME) ?: return

        Glide.with(context).load(LiftimSyncEnvironment.getApiUrl("liftim_code_image.png?" +
                "liftim_code=$liftimCode"))
                .apply(RequestOptions.circleCropTransform())
                .into(image)
        nameLabel.text = name
    }
}