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
    public LiftimCodeInfo(long liftimCode, String name, String addDate) {
        this.liftimCode = liftimCode;
        this.name = name;
        this.addDate = addDate;
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
}
