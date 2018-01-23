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

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.Toast
import com.chronoscoper.android.classschedule2.BaseActivity
import com.chronoscoper.android.classschedule2.R
import com.google.android.cameraview.CameraView
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import kotterknife.bindView
import okhttp3.HttpUrl

class JoinWithBarcodeActivity : BaseActivity() {
    companion object {
        private const val RC_CAMERA_PERMISSION = 50
    }

    private val cameraView by bindView<CameraView>(R.id.camera)
    private val captureButton by bindView<View>(R.id.capture)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_join_with_barcode)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && ContextCompat.checkSelfPermission(
                        this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.CAMERA), RC_CAMERA_PERMISSION)
        } else {
            initContents()
        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RC_CAMERA_PERMISSION) {
            if (grantResults.isNotEmpty()
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initContents()
            } else {
                finish()
            }
        }
    }

    private fun initContents() {
        cameraView.addCallback(object : CameraView.Callback() {
            override fun onPictureTaken(cameraView: CameraView?, data: ByteArray?) {
                cameraView ?: return
                data ?: return
                val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
                val pixels = IntArray(bitmap.width * bitmap.height)
                bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
                val src = RGBLuminanceSource(bitmap.width, bitmap.height, pixels)
                val bb: BinaryBitmap
                try {
                    bb = BinaryBitmap(HybridBinarizer(src))
                    val result = MultiFormatReader().decodeWithState(bb)
                    setResultAndFinish(result.text)
                } catch (e: Exception) {
                    //TODO: Another approach better than Toast
                    Toast.makeText(
                            this@JoinWithBarcodeActivity,
                            getString(R.string.barcode_not_found), Toast.LENGTH_LONG)
                            .show()
                    captureButton.isEnabled = true
                }
                bitmap.recycle()
            }
        })
        captureButton.setOnClickListener {
            cameraView.takePicture()
            it.isEnabled = false
        }
        cameraView.start()
    }

    private fun setResultAndFinish(scanned: String) {
        val url = HttpUrl.parse(scanned)
        if (url == null) {
            setResult(Activity.RESULT_CANCELED)
            finish()
            return
        }
        val invitationNumber = url.queryParameter("invitation")?.toInt()
        if (invitationNumber == null) {
            setResult(Activity.RESULT_CANCELED)
            finish()
            return
        }
        setResult(Activity.RESULT_OK,
                Intent().putExtra(
                        JoinLiftimCodeActivity.EXTRA_INVITATION_NUM, invitationNumber))
        finish()
    }
}
