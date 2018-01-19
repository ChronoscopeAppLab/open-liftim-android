/*
 * Copyright 2017-2018 Chronoscope
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
package com.chronoscoper.android.classschedule2.sync;

import android.content.Context;
import android.support.annotation.NonNull;

import com.github.gfx.android.orma.AccessThreadConstraint;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class LiftimContext {
    private static OkHttpClient sOkHttpClient;
    private static LiftimService sLiftimService;
    private static Gson sGson;
    private static OrmaDatabase sOrmaDatabase;
    private static String sBaseUrl;

    public static void init(
            @NonNull final Context context,
            @NonNull final String baseUrl,
            final long liftimCode,
            final String token) {
        sGson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder();
        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            okHttpClientBuilder.addInterceptor(loggingInterceptor);
        }
        sOkHttpClient = okHttpClientBuilder.build();
        sLiftimService = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create(sGson))
                .client(sOkHttpClient)
                .build()
                .create(LiftimService.class);
        sOrmaDatabase = OrmaDatabase.builder(context)
                .name("liftim_user_data")
                .readOnMainThread(AccessThreadConstraint.WARNING)
                .writeOnMainThread(AccessThreadConstraint.WARNING)
                .build();
        sLiftimCode = liftimCode;
        sToken = token;
        sBaseUrl = baseUrl;
    }

    @NonNull
    public static OkHttpClient getOkHttpClient() {
        if (sOkHttpClient == null) {
            throw new IllegalStateException("Envorionment is not initialized");
        }
        return sOkHttpClient;
    }

    @NonNull
    public static LiftimService getLiftimService() {
        if (sLiftimService == null) {
            throw new IllegalStateException("Envorionment is not initialized");
        }
        return sLiftimService;
    }

    @NonNull
    public static Gson getGson() {
        if (sGson == null) {
            throw new IllegalStateException("Envorionment is not initialized");
        }
        return sGson;
    }

    @NonNull
    public static OrmaDatabase getOrmaDatabase() {
        if (sOrmaDatabase == null) {
            throw new IllegalStateException("Environment is not initialized");
        }
        return sOrmaDatabase;
    }

    private static long sLiftimCode;
    private static String sToken;

    public static long getLiftimCode() {
        return sLiftimCode;
    }

    @NonNull
    public static String getToken() {
        return sToken;
    }

    public static void setToken(@NonNull String token) {
        sToken = token;
    }

    @NonNull
    public static String getApiUrl(@NonNull String fileName) {
        return sBaseUrl + "api/v1/" + fileName;
    }
}