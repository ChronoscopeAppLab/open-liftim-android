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

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.chronoscoper.android.classschedule2.BaseActivity
import com.chronoscoper.android.classschedule2.R
import com.chronoscoper.android.classschedule2.functionrestriction.getFunctionRestriction
import com.chronoscoper.android.classschedule2.sync.LiftimContext
import com.chronoscoper.android.classschedule2.util.progressiveFadeInTransition
import com.chronoscoper.android.classschedule2.util.showToast
import kotterknife.bindView

class LiftimCodeSettingsActivity : BaseActivity() {
    companion object {
        private const val EXTRA_LIFTIM_CODE = "liftim_code"
        fun start(context: Context, liftimCode: Long) {
            context.startActivity(Intent(context, LiftimCodeSettingsActivity::class.java)
                    .putExtra(EXTRA_LIFTIM_CODE, liftimCode))
        }

        private const val RC_DELETE_CODE = 100
        private const val RC_PERMISSION_EXTERNAL_STORAGE = 101
        private const val RC_CHOOSE_IMAGE = 102
    }

    private val liftimCodeImage by bindView<ImageView>(R.id.liftim_code_image)
    private val liftimCodeName by bindView<TextView>(R.id.liftim_code_name)
    private val editSubjectListButton by bindView<View>(R.id.edit_subject_list)
    private val deleteLiftimCodeButton by bindView<View>(R.id.delete)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val liftimCode = intent.getLongExtra(EXTRA_LIFTIM_CODE, -1)
        if (liftimCode < 0) {
            finish()
            return
        }
        setContentView(R.layout.activity_liftim_code_settings)

        val liftimCodeInfo = LiftimContext.getOrmaDatabase()
                .selectFromLiftimCodeInfo()
                .liftimCodeEq(liftimCode)
                .firstOrNull() ?: kotlin.run { finish(); return }

        Glide.with(this)
                .load(LiftimContext
                        .getApiUrl("liftim_code_image.png?" +
                                "liftim_code=$liftimCode&" +
                                "token=${LiftimContext.getToken()}"))
                .apply(RequestOptions.circleCropTransform())
                .transition(progressiveFadeInTransition())
                .into(liftimCodeImage)
        val configureLiftimCodeRestriction =
                getFunctionRestriction(this).configureLiftimCode
        if (!configureLiftimCodeRestriction.changeImage) {
            liftimCodeImage.apply {
                isClickable = false
                isFocusable = false
                isFocusableInTouchMode = false
            }
        } else {
            liftimCodeImage.setOnClickListener {
                val permission = ContextCompat
                        .checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                if (permission != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,
                            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                            RC_PERMISSION_EXTERNAL_STORAGE)
                } else {
                    requestImagePicker()
                }
            }
        }
        if (!configureLiftimCodeRestriction.rename) {
            liftimCodeName.apply {
                isClickable = false
                isFocusable = false
                isFocusableInTouchMode = false
            }
        }
        liftimCodeName.text = liftimCodeInfo.name
        if (configureLiftimCodeRestriction.editSubjectList) {
            editSubjectListButton.setOnClickListener {
                EditSubjectListActivity.open(this, liftimCode)
            }
        } else {
            editSubjectListButton.visibility = View.GONE
        }
        if (configureLiftimCodeRestriction.delete) {
            deleteLiftimCodeButton.setOnClickListener {
                DeleteLiftimCodeActivity.start(this, liftimCode, RC_DELETE_CODE)
            }
        } else {
            deleteLiftimCodeButton.visibility = View.GONE
        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == RC_PERMISSION_EXTERNAL_STORAGE) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                requestImagePicker()
            }
        }
    }

    private fun requestImagePicker() {
        try {
            startActivityForResult(Intent(Intent.ACTION_GET_CONTENT)
                    .apply {
                        type = "image/*"
                        addCategory(Intent.CATEGORY_OPENABLE)
                    }, RC_CHOOSE_IMAGE)
        } catch (e: ActivityNotFoundException) {
            showToast(this, getString(R.string.activity_not_found),
                    Toast.LENGTH_LONG)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) return
        when (requestCode) {
            RC_DELETE_CODE -> finish()
            RC_CHOOSE_IMAGE -> {
                val intent = Intent(this, ImageManipulationActivity::class.java)
                intent.data = data?.data
                startActivity(intent)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.options_liftim_code_settings, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        item ?: return false
        if (item.itemId == R.id.options_done) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }
}
