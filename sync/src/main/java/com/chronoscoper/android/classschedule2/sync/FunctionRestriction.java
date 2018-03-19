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

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class FunctionRestriction {
    @Expose
    @SerializedName("create_liftim_code")
    public boolean createLiftimCode;

    @Expose
    @SerializedName("multi_liftim_code")
    public boolean multiLiftimCode;

    @Expose
    @SerializedName("configure_liftim_code")
    public ConfigureLiftimCodeRestriction configureLiftimCode;

    @Expose
    @SerializedName("add_info")
    public boolean addInfo;

    @Expose
    @SerializedName("add_note")
    public boolean addNote;

    @Expose
    @SerializedName("edit_weekly")
    public boolean editWeekly;

    public class ConfigureLiftimCodeRestriction {
        @Expose
        @SerializedName("delete")
        public boolean delete;

        @Expose
        @SerializedName("exit")
        public boolean exit;

        @Expose
        @SerializedName("change_image")
        public boolean changeImage;

        @Expose
        @SerializedName("rename")
        public boolean rename;
    }
}
