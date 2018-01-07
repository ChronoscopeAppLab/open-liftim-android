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
package com.chronoscoper.android.classschedule2.setup

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.chronoscoper.android.classschedule2.R
import kotterknife.bindView

class InitialSyncErrorFragment : Fragment() {
    override fun onCreateView(
            inflater: LayoutInflater?,
            container: ViewGroup?,
            savedInstanceState: Bundle?): View? =
            inflater?.inflate(R.layout.fragment_initial_sync_error, container, false)

    val retryButton by bindView<Button>(R.id.retry)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        retryButton.setOnClickListener {
            it.isEnabled = false
            val activity = activity as? TokenCallbackActivity ?: return@setOnClickListener
            activity.supportFragmentManager.beginTransaction()
                    .replace(android.R.id.content, TokenCallbackFragment())
                    .commit()
            activity.executeInitialSync()
        }
    }
}