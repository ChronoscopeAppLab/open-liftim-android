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

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.view.View
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import com.chronoscoper.android.classschedule2.BaseActivity
import com.chronoscoper.android.classschedule2.R
import com.chronoscoper.android.classschedule2.sync.LiftimCodeInfo
import com.chronoscoper.android.classschedule2.sync.LiftimContext
import kotterknife.bindView

class CreateLiftimCodeActivity : BaseActivity() {
    private val webView by bindView<WebView>(R.id.web)
    private val progressBar by bindView<View>(R.id.progress)

    @SuppressLint("SetJavaScriptEnabled", "AddJavascriptInterface")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_liftim_code)
        initCookie()
        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                progressBar.visibility = View.VISIBLE
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                progressBar.visibility = View.GONE
            }
        }
        webView.settings.apply {
            javaScriptEnabled = true
            cacheMode = WebSettings.LOAD_NO_CACHE
        }
        webView.addJavascriptInterface(JSIAndroidNative(), "androidNative")
        webView.loadUrl(LiftimContext.getApiUrl("pages/create_liftim_code"))
    }

    @Suppress("DEPRECATION")
    private fun initCookie() {
        val cookieManager = CookieManager.getInstance()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.removeAllCookies(null)
        } else {
            cookieManager.removeAllCookie()
        }
        cookieManager.setAcceptCookie(true)
        cookieManager.setCookie(LiftimContext.getApiUrl(""),
                "token=${LiftimContext.getToken()}")
    }

    private inner class JSIAndroidNative {
        @JavascriptInterface
        @Suppress("UNUSED")
        fun finish(liftimCode: Long, liftimCodeInfo: String?) {
            if (liftimCode > 0 && liftimCodeInfo != null) {
                try {
                    val info = LiftimContext.getGson()
                            .fromJson(liftimCodeInfo, LiftimCodeInfo::class.java)
                    info.liftimCode = liftimCode
                    LiftimContext.getOrmaDatabase().insertIntoLiftimCodeInfo(info)
                } catch (ignore: Exception) {
                }
            }
            this@CreateLiftimCodeActivity.finish()
        }
    }
}
