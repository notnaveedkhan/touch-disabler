package com.notnaveedkhan.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.notnaveedkhan.dao.OverlayDao;
import com.notnaveedkhan.entity.Overlay;

@Database(entities = {Overlay.class}, version = 1)
public abstract class ApplicationDatabase extends RoomDatabase {

    public abstract OverlayDao overlayDao();
}
