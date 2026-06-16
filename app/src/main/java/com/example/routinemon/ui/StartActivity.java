package com.example.routinemon.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.airbnb.lottie.LottieAnimationView;
import com.example.routinemon.R;

public class StartActivity extends AppCompatActivity {

    private SharedPreferences prefs;
    private String currentRank;

    // 부화 시스템 제어 변수
    private int eggTapCount = 0;
    private final int MAX_EGG_TAPS = 3;
    private boolean isEggMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        prefs = getSharedPreferences("RoutineMonPrefs", MODE_PRIVATE);
        currentRank = prefs.getString("currentRank", "베이비");

        TextView tvStartTitle = findViewById(R.id.tvStartTitle);
        TextView tvStartSubtitle = findViewById(R.id.tvStartSubtitle);
        LottieAnimationView lottieStartPet = findViewById(R.id.lottieStartPet);
        ImageView imgStartEgg = findViewById(R.id.imgStartEgg); // 알 이미지 뷰 매칭
        Button btnStartContinue = findViewById(R.id.btnStartContinue);
        Button btnStartReset = findViewById(R.id.btnStartReset);

        // 1. 기존 데이터 기반 메인 펫 초기화
        if (lottieStartPet != null) {
            if (currentRank.equals("어린이")) {
                lottieStartPet.setAnimation(R.raw.pet_child);
            } else if (currentRank.equals("청소년")) {
                lottieStartPet.setAnimation(R.raw.pet_teen);
            } else {
                lottieStartPet.setAnimation(R.raw.pet_animation);
            }
            lottieStartPet.playAnimation();
        }

        // 2. 모험 계속하기
        btnStartContinue.setOnClickListener(v -> {
            Intent intent = new Intent(StartActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        // 3. 새로 시작 ➡️ 알 부화 모드 돌입
        btnStartReset.setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("🧹 새로운 부화")
                    .setMessage("기존 데이터를 모두 지우고, 새로운 공룡 알을 품으시겠습니까?")
                    .setPositiveButton("알 받기", (dialog, which) -> {

                        // 세이브 데이터 포맷
                        prefs.edit().remove("currentRank")
                                .remove("currentExp")
                                .remove("maxExp")
                                .remove("savedRoutines").apply();

                        isEggMode = true;
                        eggTapCount = 0;

                        if (tvStartTitle != null) tvStartTitle.setText("신비로운 공룡 알");
                        if (tvStartSubtitle != null) tvStartSubtitle.setText("알을 터치해서 깨워보세요! (0/3)");

                        // 하단 버튼 숨기기
                        btnStartContinue.setVisibility(View.GONE);
                        btnStartReset.setVisibility(View.GONE);

                        // 🦖 뽀뽀 숨기고 🥚 알 이미지 띄우기
                        if (lottieStartPet != null) lottieStartPet.setVisibility(View.GONE);
                        if (imgStartEgg != null) {
                            imgStartEgg.setVisibility(View.VISIBLE);
                            imgStartEgg.setScaleX(1.0f);
                            imgStartEgg.setScaleY(1.0f);
                            imgStartEgg.setAlpha(1.0f);
                        }

                        Toast.makeText(StartActivity.this, "화면 중앙의 알을 터치하세요!", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("취소", null)
                    .show();
        });

        // 4. 🥚 알 이미지 터치 리스너
        if (imgStartEgg != null) {
            imgStartEgg.setOnClickListener(v -> {
                if (!isEggMode) return;

                eggTapCount++;

                if (eggTapCount < MAX_EGG_TAPS) {
                    if (tvStartSubtitle != null) tvStartSubtitle.setText("알을 터치해서 깨워보세요! (" + eggTapCount + "/3)");

                    // [흔들림 연출] 알 이미지가 양옆으로 팅팅 튕기는 애니메이션
                    v.animate().rotation(15f).setDuration(50).withEndAction(() ->
                            v.animate().rotation(-15f).setDuration(50).withEndAction(() ->
                                    v.animate().rotation(0f).setDuration(50).start()
                            ).start()
                    ).start();

                } else {
                    // 🎉 부화 완료 시퀀스
                    isEggMode = false;
                    if (tvStartTitle != null) tvStartTitle.setText("ROUTINEMON");
                    if (tvStartSubtitle != null) tvStartSubtitle.setText("뽀뽀가 세상에 태어났습니다!");

                    // 알이 회전하면서 싹 사라지고(Fade-out), 그 자리에 뽀뽀(Lottie)가 뿅 나타나는 교체 연출
                    v.animate().scaleX(0.2f).scaleY(0.2f).alpha(0.0f).rotation(180f).setDuration(400).withEndAction(() -> {
                        v.setVisibility(View.GONE); // 알 퇴장

                        if (lottieStartPet != null) {
                            lottieStartPet.setVisibility(View.VISIBLE); // 뽀뽀 입장
                            lottieStartPet.setAnimation(R.raw.pet_animation); // 베이비 뽀뽀 강제 세팅
                            lottieStartPet.playAnimation();

                            // 뽀뽀 등장 임팩트 애니메이션 (크기 커짐)
                            lottieStartPet.setScaleX(0.5f); lottieStartPet.setScaleY(0.5f);
                            lottieStartPet.animate().scaleX(1.0f).scaleY(1.0f).setDuration(400).withEndAction(() -> {
                                Toast.makeText(StartActivity.this, "반가워 뽀뽀야! 모험을 시작합니다.", Toast.LENGTH_LONG).show();

                                // 메인 게임 화면 전환
                                Intent intent = new Intent(StartActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            }).start();
                        }
                    }).start();
                }
            });
        }
    }
}