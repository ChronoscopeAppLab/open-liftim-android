package com.chronoscoper.android.classschedule2.setting.manager

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.View
import com.chronoscoper.android.classschedule2.BaseActivity
import com.chronoscoper.android.classschedule2.R
import com.chronoscoper.android.classschedule2.sync.LiftimContext
import com.chronoscoper.android.classschedule2.view.ImageClipRangeView
import com.chronoscoper.android.classschedule2.view.ProgressDialog
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import kotterknife.bindView
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.Request
import okhttp3.RequestBody
import java.io.ByteArrayOutputStream
import java.io.IOException

class ImageManipulationActivity : BaseActivity() {
    companion object {
        private const val TAG = "ImageManipulation"
    }

    private val disposables = CompositeDisposable()

    private val clipView by bindView<ImageClipRangeView>(R.id.clip)
    private val done by bindView<View>(R.id.done)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_manipulation)

        val uri = intent?.data
                ?: kotlin.run {
                    Log.e(TAG, "Uri is null. Aborting...")
                    finish()
                    return
                }
        contentResolver.openInputStream(uri).use {
            clipView.setImageBitmap(BitmapFactory.decodeStream(it))
        }

        done.setOnClickListener {
            val bitmap = clipView.getClippedImage()
            supportFragmentManager.beginTransaction()
                    .add(ProgressDialog(), null)
                    .commit()
            val subscriber = object : DisposableObserver<Unit>() {
                override fun onComplete() {
                    Log.d(TAG, "Request is successfully finished")
                    finish()
                }

                override fun onNext(t: Unit) {
                }

                override fun onError(e: Throwable) {
                    Log.e(TAG, "Failed to upload image", e)
                    finish()
                }
            }
            Observable.create<Unit> {
                val data = ByteArray(bitmap.byteCount)
                ByteArrayOutputStream().use {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 0/*ignored*/, it)
                    it.write(data)
                    bitmap.recycle()
                }
                val body = MultipartBody.Builder().setType(MultipartBody.FORM)
                        .addFormDataPart("image", "image",
                                RequestBody.create(MediaType.parse("image/png"), data))
                        .addFormDataPart("liftim_code",
                                LiftimContext.getLiftimCode().toString())
                        .addFormDataPart("token", LiftimContext.getToken())
                        .build()
                val request = Request.Builder()
                        .url(LiftimContext.getApiUrl("liftim_code_image.png"))
                        .post(body).build()
                val response = LiftimContext.getOkHttpClient().newCall(request).execute()
                if (!response.isSuccessful) {
                    it.onError(IOException())
                    return@create
                }
                it.onComplete()
            }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(subscriber)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.clear()
    }
}
