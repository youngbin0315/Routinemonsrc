package com.example.routinemon.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

// 🎯 Room DB에게 이 클래스가 데이터베이스 테이블임을 알려줍니다!
@Entity(tableName = "routine_table")
public class Routine {

    // 🎯 데이터베이스의 고유 키(ID)를 자동으로 생성하도록 설정합니다.
    @PrimaryKey(autoGenerate = true)
    private int id;

    private String title;
    private boolean isCompleted;
    private String iconType;

    // 생성자
    public Routine(String title, boolean isCompleted, String iconType) {
        this.title = title;
        this.isCompleted = isCompleted;
        this.iconType = iconType;
    }

    // DB용 ID의 게터/세터
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title != null ? title : "";
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public String getIconType() {
        return iconType != null ? iconType : "star";
    }

    public void setIconType(String iconType) {
        this.iconType = iconType;
    }
}