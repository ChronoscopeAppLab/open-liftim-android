package com.chronoscoper.android.classschedule2.sync;

import com.github.gfx.android.orma.annotation.Column;
import com.github.gfx.android.orma.annotation.Table;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Table("color_palette")
public class ColorPalette {
    @Expose
    @SerializedName("name")
    @Column(value = "name", indexed = true)
    public String name;

    @Expose
    @SerializedName("color")
    @Column("color")
    public String color;
}
