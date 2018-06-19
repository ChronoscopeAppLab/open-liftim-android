package com.chronoscoper.android.classschedule2.setup

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import com.chronoscoper.android.classschedule2.BaseActivity
import com.chronoscoper.android.classschedule2.R
import com.chronoscoper.android.classschedule2.sync.LiftimContext
import com.chronoscoper.android.classschedule2.task.FullSyncTask
import com.chronoscoper.android.classschedule2.task.JoinLiftimCodeTask
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotterknife.bindView
import java.util.concurrent.TimeUnit

@SuppressLint("Registered") // because it'll be registered by uncommenting manifest section if needed
class InvitationUrlActivity : BaseActivity() {
    companion object {
        private const val TAG = "Join"
    }

    private val confirmContainer by bindView<View>(R.id.join_confirm_container)
    private val confirm by bindView<TextView>(R.id.join_confirm)
    private val join by bindView<View>(R.id.join)
    private val cancel by bindView<View>(R.id.cancel)

    private val progressBar by bindView<View>(R.id.progress)

    private val resultContainer by bindView<View>(R.id.result_container)
    private val result by bindView<TextView>(R.id.result)
    private val close by bindView<View>(R.id.close)

    private val compositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_invitation_url)
        overridePendingTransition(R.anim.fade_in, 0)
        close.setOnClickListener {
            finish()
        }
        val uri = intent.data
        if (uri == null) {
            confirmContainer.visibility = View.GONE
            resultContainer.visibility = View.VISIBLE
            result.text = getString(R.string.incorrect_invitation_number)
            return
        }
        Log.d(TAG, uri.encodedQuery)
        val query = uri.encodedQuery
        val invitationNumber = query.split("&")
                .filter { it.startsWith("invitation=") }
                .firstOrNull()?.substring(11)
        if (invitationNumber == null) {
            confirmContainer.visibility = View.GONE
            resultContainer.visibility = View.VISIBLE
            result.text = getString(R.string.incorrect_invitation_number)
            return
        }
        Log.d(TAG, invitationNumber)

        confirm.text = getString(R.string.join_confirm, invitationNumber)

        join.setOnClickListener {
            it.isEnabled = false
            confirmContainer.animate()
                    .translationY(-confirmContainer.height.toFloat())
                    .withEndAction {
                        confirmContainer.visibility = View.GONE
                        progressBar.visibility = View.VISIBLE
                        progressBar.alpha = 0f
                        progressBar.animate()
                                .alpha(1f)
                                .withEndAction {
                                    compositeDisposable.add(
                                            Flowable.defer {
                                                Flowable.just(
                                                        JoinLiftimCodeTask(LiftimContext.getToken())
                                                        .joinAndObtainLiftimCode(
                                                                invitationNumber.toInt()),
                                                        FullSyncTask(this).run())
                                            }
                                                    .delay(500L, TimeUnit.MILLISECONDS)
                                                    .subscribeOn(Schedulers.io())
                                                    .observeOn(AndroidSchedulers.mainThread())
                                                    .subscribe({ t ->
                                                        if (t == null) {
                                                            result.text = getString(R.string.incorrect_invitation_number)
                                                        } else {
                                                            result.text = getString(R.string.joined)
                                                        }
                                                        progressBar.animate()
                                                                .alpha(0f)
                                                                .withEndAction {
                                                                    progressBar.visibility = View.GONE
                                                                    resultContainer.visibility = View.VISIBLE
                                                                    resultContainer.translationY =
                                                                            resultContainer.height.toFloat()
                                                                    resultContainer.animate()
                                                                            .translationY(0f)
                                                                            .start()
                                                                }
                                                                .start()
                                                    }, {
                                                        result.text = getString(R.string.incorrect_invitation_number)
                                                        progressBar.animate()
                                                                .alpha(0f)
                                                                .withEndAction {
                                                                    progressBar.visibility = View.GONE
                                                                    resultContainer.visibility = View.VISIBLE
                                                                    resultContainer.translationY =
                                                                            resultContainer.height.toFloat()
                                                                    resultContainer.animate()
                                                                            .translationY(0f)
                                                                            .start()
                                                                }
                                                                .start()
                                                    })
                                    )
                                }
                                .start()
                    }
                    .start()
        }
        cancel.setOnClickListener {
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(0, R.anim.fade_out)
    }

    override fun onBackPressed() {}
}
