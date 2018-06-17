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

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.widget.ViewSwitcher
import com.chronoscoper.android.classschedule2.BaseActivity
import com.chronoscoper.android.classschedule2.R
import com.chronoscoper.android.classschedule2.sync.Invitation
import com.chronoscoper.android.classschedule2.sync.LiftimCodeInfo
import com.chronoscoper.android.classschedule2.sync.LiftimContext
import com.chronoscoper.android.classschedule2.util.showToast
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotterknife.bindView
import java.util.EnumMap

class InvitationActivity : BaseActivity() {
    companion object {
        private const val EXTRA_LIFTIM_CODE = "liftim_code"
        fun start(context: Context, liftimCode: Long) {
            context.startActivity(Intent(context, InvitationActivity::class.java)
                    .putExtra(EXTRA_LIFTIM_CODE, liftimCode))
        }
    }

    private val compositeDisposable = CompositeDisposable()

    private val switcher by bindView<ViewSwitcher>(R.id.switcher)
    private val liftimCodeName by bindView<TextView>(R.id.liftim_code_name)
    private val invitationNumber by bindView<TextView>(R.id.invitation_number)
    private val barcode by bindView<ImageView>(R.id.barcode)
    private val deadline by bindView<TextView>(R.id.deadline)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_invitation)
        val liftimCode = intent.getLongExtra(EXTRA_LIFTIM_CODE, -1)
        if (liftimCode < 0) {
            finish()
            return
        }
        compositeDisposable.add(
                Single.defer {
                    Single.just(LiftimContext.getLiftimService()
                            .getInvitationNumber(liftimCode, LiftimContext.getToken(), false)
                            .execute())
                }
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ res ->
                            val body = res.body()
                            if (res.isSuccessful && body != null) {
                                inflate(body)
                            } else {
                                errorExit()
                            }
                        }, {
                            errorExit()
                        })
        )

        compositeDisposable.add(
                LiftimContext.getOrmaDatabase().selectFromLiftimCodeInfo()
                        .liftimCodeEq(liftimCode)
                        .executeAsObservable()
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io())
                        .firstOrError()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe { info: LiftimCodeInfo? ->
                            info ?: return@subscribe
                            liftimCodeName.text = info.name
                        }
        )
    }

    private fun inflate(invitation: Invitation) {
        invitationNumber.text = invitation.invitationNumber.toString()
        deadline.text = getString(R.string.invitaton_number_deadline, invitation.expires)
        compositeDisposable.add(Single.defer {
            Single.just(createBarcode(invitation.url))
        }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { bitmap ->
                    barcode.setImageBitmap(bitmap)
                    switcher.showNext()
                }
        )
    }

    private fun createBarcode(url: String): Bitmap {
        val hints = EnumMap<EncodeHintType, Any>(EncodeHintType::class.java)
        hints[EncodeHintType.ERROR_CORRECTION] = ErrorCorrectionLevel.M
        hints[EncodeHintType.QR_VERSION] = 5
        val writer = QRCodeWriter()
        val matrix = writer.encode(url,
                BarcodeFormat.QR_CODE, 256, 256, hints)
        val pixels = IntArray(256 * 256)
        val darkBlockColor = ContextCompat.getColor(this, R.color.barcode_fg)
        for (y in 0 until 256)
            for (x in 0 until 256)
                pixels[y * 256 + x] = if (matrix.get(x, y)) {
                    darkBlockColor
                } else {
                    Color.TRANSPARENT
                }
        return Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_4444)
                .apply { setPixels(pixels, 0, 256, 0, 0, 256, 256) }
    }

    private fun errorExit() {
        showToast(this, getString(R.string.invitation_load_fail), Toast.LENGTH_SHORT)
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
    }
}
