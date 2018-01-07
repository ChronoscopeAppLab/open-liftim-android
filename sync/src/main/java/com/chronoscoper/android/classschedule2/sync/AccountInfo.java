package com.chronoscoper.android.classschedule2.sync;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AccountInfo {
    @Expose
    @SerializedName("userName")
    public String userName;

    @Expose
    @SerializedName("imageFile")
    public String imageFile;

    @Expose
    @SerializedName("addDate")
    public String addDate;

    @Expose
    @SerializedName("isAvailable")
    public boolean isAvailable;

    @Expose
    @SerializedName("liftimCodes")
    public LiftimCode[] liftimCodes;

    public class LiftimCode {
        @Expose
        @SerializedName("liftim_code")
        public long liftimCode;

        @Expose
        @SerializedName("isManager")
        public boolean isManager;
    }
}
