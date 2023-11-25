package com.notnaveedkhan.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.notnaveedkhan.entity.Overlay;

import java.util.List;

@Dao
public interface OverlayDao {

    @Insert
    void insert(Overlay overlay);

    @Query("DELETE FROM overlay WHERE id = :id")
    void deleteById(int id);

    @Query("SELECT * FROM overlay")
    List<Overlay> getAll();

    @Query("SELECT * FROM overlay WHERE x = :x AND y = :y")
    boolean existsByXAndY(int x, int y);

}
