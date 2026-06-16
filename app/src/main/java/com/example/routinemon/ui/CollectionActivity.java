package com.example.routinemon.ui;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.routinemon.R;

import java.util.ArrayList;
import java.util.List;

public class CollectionActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CollectionAdapter adapter;
    private List<Monster> monsterList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        recyclerView = findViewById(R.id.rvCollection);

        android.view.View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        String currentStage = getIntent().getStringExtra("currentStage");
        if (currentStage == null) {
            currentStage = "베이비";
        }

        monsterList = new ArrayList<>();

        if (currentStage.equals("베이비")) {
            monsterList.add(new Monster("베이비 뽀뽀", R.drawable.baby, true));
            monsterList.add(new Monster("어린이 뽀뽀", R.drawable.child, false));
            monsterList.add(new Monster("청소년 뽀뽀", R.drawable.teen, false));
            monsterList.add(new Monster("각성 지룡 뽀뽀", R.drawable.legend, false));
            monsterList.add(new Monster("타락 비룡 뽀뽀", R.drawable.legend, false));
            monsterList.add(new Monster("전설의 신룡 뽀뽀", R.drawable.legend, false));
        }
        else if (currentStage.equals("어린이")) {
            monsterList.add(new Monster("베이비 뽀뽀", R.drawable.baby, true));
            monsterList.add(new Monster("어린이 뽀뽀", R.drawable.child, true));
            monsterList.add(new Monster("청소년 뽀뽀", R.drawable.teen, false));
            monsterList.add(new Monster("각성 지룡 뽀뽀", R.drawable.legend, false));
            monsterList.add(new Monster("타락 비룡 뽀뽀", R.drawable.legend, false));
            monsterList.add(new Monster("전설의 신룡 뽀뽀", R.drawable.legend, false));
        }
        else if (currentStage.equals("청소년")) {
            monsterList.add(new Monster("베이비 뽀뽀", R.drawable.baby, true));
            monsterList.add(new Monster("어린이 뽀뽀", R.drawable.child, true));
            monsterList.add(new Monster("청소년 뽀뽀", R.drawable.teen, true));
            monsterList.add(new Monster("각성 지룡 뽀뽀", R.drawable.legend, false));
            monsterList.add(new Monster("타락 비룡 뽀뽀", R.drawable.legend, false));
            monsterList.add(new Monster("전설의 신룡 뽀뽀", R.drawable.legend, false));
        }

        if (recyclerView != null) {
            recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
            adapter = new CollectionAdapter(monsterList);
            recyclerView.setAdapter(adapter);
        }
    }
}