package com.example.routinemon.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface RoutineDao {

    // 🎯 범인 검거! 테이블명을 'routines'에서 'routine_table'로 정확하게 맞춰주었습니다.
    @Query("SELECT * FROM routine_table")
    LiveData<List<Routine>> getAllRoutines();

    @Insert
    void insertRoutine(Routine routine);

    @Update
    void updateRoutine(Routine routine);
}