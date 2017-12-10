/*
 * Copyright 2017 Chronoscope
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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.github.gfx.android.orma.annotation.Column;
import com.github.gfx.android.orma.annotation.Setter;
import com.github.gfx.android.orma.annotation.Table;

@Table("info")
public class Info {
    @Setter
    public Info(long liftimCode, @NonNull String id,
                @NonNull String title, @Nullable String detail,
                int weight, @Nullable String date,
                @Nullable String time, @Nullable String link,
                int type, @Nullable String timetable, boolean removable, int addedBy) {
        this.liftimCode = liftimCode;
        this.id = id;
        this.title = title;
        this.detail = detail;
        this.weight = weight;
        this.date = date;
        this.time = time;
        this.link = link;
        this.type = type;
        this.timetable = timetable;
        this.removable = removable;
        this.addedBy = addedBy;
    }

    public Info() {
    }

    @Column(value = "liftimCode", indexed = true)
    public long liftimCode;

    @Column("id")
    public String id;

    @Column("title")
    public String title;

    @Column("detail")
    @Nullable
    public String detail;

    @Column("weight")
    public int weight;

    @Column(value = "date", indexed = true)
    @Nullable
    public String date;

    @Column(value = "time", indexed = true)
    @Nullable
    public String time;

    @Column("link")
    @Nullable
    public String link;

    public static final int TYPE_LOCAL_MEMO = -1;
    public static final int TYPE_UNSPECIFIED = 0;
    public static final int TYPE_EVENT = 1;
    public static final int TYPE_INFORMATION = 2;
    public static final int TYPE_SUBMISSION = 3;
    public static final int TYPE_TIMETABLE = 4;
    @Column(value = "type", indexed = true)
    public int type;

    @Column("timetable")
    @Nullable
    public String timetable;

    @Column("removable")
    public boolean removable;

    public static final int REMOTE = 1;
    public static final int LOCAL = 2;
    @Column("addedBy")
    public int addedBy;
}
