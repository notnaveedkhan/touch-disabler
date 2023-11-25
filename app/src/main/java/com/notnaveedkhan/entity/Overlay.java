package com.notnaveedkhan.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "overlay")
public class Overlay {

    @PrimaryKey(autoGenerate = true)
    public int id;
    @ColumnInfo(name = "x")
    public int x;
    @ColumnInfo(name = "y")
    public int y;
    @ColumnInfo(name = "height")
    public int height;
    @ColumnInfo(name = "width")
    public int width;
    @ColumnInfo(name = "color")
    public int color;
    @ColumnInfo(name = "opacity")
    public float opacity;
    @ColumnInfo(name = "movable")
    public boolean movable;

}
