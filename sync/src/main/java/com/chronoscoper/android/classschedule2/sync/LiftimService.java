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

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface LiftimService {
    @GET("api/v1/liftim_code_info.json")
    Call<LiftimCodeInfo> getLiftimCodeInfo(
            @Query("liftim_code") long liftimCode,
            @Query("token") String token);

    @FormUrlEncoded
    @POST("api/v1/liftim_code_info.json")
    Call<JoinedLiftimCode> joinLiftimCode(
            @Field("token") String token,
            @Field("invitation_number") int invitationNum);

    @FormUrlEncoded
    @DELETE("api/v1/liftim_code_info.json")
    Call<Void> deleteLiftimCode(
            @Field("liftim_code") long liftimCode,
            @Field("token") String token);

    @GET("api/v1/info.json")
    Call<InfoRemoteModel> getInfo(
            @Query("liftim_code") long liftimCode,
            @Query("token") String token,
            @Query("cursor") long cursor);

    @FormUrlEncoded
    @POST("api/v1/info.json")
    Call<Void> registerInfo(
            @Field("liftim_code") long liftimCode,
            @Field("token") String token,
            @Field(value = "content", encoded = true) String content);

    @FormUrlEncoded
    @HTTP(method = "DELETE", path = "api/v1/info.json", hasBody = true)
    Call<Void> deleteInfo(
            @Field("liftim_code") long liftimCode,
            @Field("token") String token,
            @Field(value = "id", encoded = true) String id);

    @GET("api/v1/weekly.json")
    Call<HashMap<String, WeeklyItem>> getWeekly(
            @Query("liftim_code") long liftimCode,
            @Query("token") String token);

    @FormUrlEncoded
    @POST("api/v1/weekly.json")
    Call<Void> registerWeekly(
            @Field("liftim_code") long liftimCode,
            @Field("token") String token,
            @Field("content") String content);

    @GET("api/v1/account_info.json")
    Call<AccountInfo> getAccountInfo(@Query("token") String token);

    @GET("api/v1/subject.json")
    Call<Subject[]> getSubjects(
            @Query("liftim_code") long liftimCode, @Query("token") String token);

    @GET("api/v1/color_palette.json")
    Call<ColorPalette[]> getColorPalette();

    @GET("api/v1/color_palette_android.json")
    Call<ColorPaletteV2[]> getColorPaletteV2();

    @GET("api/v1/token.json")
    Call<Token> getToken(@Query("token") String token);

    @GET("api/v1/function_restriction.json")
    Call<String> getFunctionRestriction();
}
