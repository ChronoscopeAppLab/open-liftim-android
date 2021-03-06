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

import com.github.gfx.android.orma.annotation.Column;
import com.github.gfx.android.orma.annotation.PrimaryKey;
import com.github.gfx.android.orma.annotation.Setter;
import com.github.gfx.android.orma.annotation.Table;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Table("liftim_code_info")
public class LiftimCodeInfo {
    @Setter
    public LiftimCodeInfo(long liftimCode, String name, String addDate, boolean isManager) {
        this.liftimCode = liftimCode;
        this.name = name;
        this.addDate = addDate;
        this.isManager = isManager;
    }

    @PrimaryKey(auto = false)
    public long liftimCode;

    @Column("name")
    @Expose
    @SerializedName("name")
    public String name;

    @Column("add_date")
    @Expose
    @SerializedName("addDate")
    public String addDate;

    @Column("is_manager")
    public boolean isManager;
}
