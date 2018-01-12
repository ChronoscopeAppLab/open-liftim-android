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

import android.support.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class InfoRemoteModel {
    @Expose
    @SerializedName("nextCursor")
    public long nextCursor;

    @Expose
    @SerializedName("nextDate")
    public String nextDate;

    @Expose
    @SerializedName("info")
    public InfoBody[] info;

    public static class InfoBody {
        @Expose
        @SerializedName("id")
        public String id;

        @Expose
        @SerializedName("title")
        public String title;

        @Expose
        @SerializedName("detail")
        @Nullable
        public String detail;

        @Expose
        @SerializedName("weight")
        public int weight;

        @Expose
        @SerializedName("date")
        @Nullable
        public String date;

        @Expose
        @SerializedName("time")
        @Nullable
        public String time;

        @Expose
        @SerializedName("link")
        @Nullable
        public String link;

        @Expose
        @SerializedName("type")
        public int type;

        @Expose
        @SerializedName("timetable")
        @Nullable
        public Timetable timetable;

        @Expose
        @SerializedName("removable")
        public boolean removable;
    }

    public static class Timetable {
        @Expose
        @SerializedName("subjectMinIndex")
        public int subjectMinIndex;

        @Expose
        @SerializedName("subjects")
        public SubjectElement[] subjects;

        @Override
        public String toString() {
            return LiftimSyncEnvironment.getGson().toJson(this);
        }
    }


    public static class SubjectElement {
        @Expose
        @SerializedName("subject")
        public String subject;

        @Expose
        @SerializedName("detail")
        public String detail;
    }
}
