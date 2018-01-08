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
package com.chronoscoper.android.classschedule2.sync;

import com.github.gfx.android.orma.annotation.Column;
import com.github.gfx.android.orma.annotation.Table;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Table("subject")
public class Subject {
    @Column(value = "liftim_code", indexed = true)
    public long liftimCode;

    @Expose
    @SerializedName("subject")
    @Column(value = "subject", indexed = true)
    public String subject;

    @Expose
    @SerializedName("shortSubject")
    @Column("short_subject")
    public String shortSubject;

    @Expose
    @SerializedName("color")
    @Column("color")
    public String color;
}
