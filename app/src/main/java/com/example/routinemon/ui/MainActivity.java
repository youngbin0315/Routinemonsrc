package com.example.routinemon.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.example.routinemon.R;
import com.example.routinemon.data.Routine;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private TextView tvExp;
    private LottieAnimationView lottiePet;
    private LinearLayout mainLayout;
    private RoutineAdapter adapter;

    private LinearLayout layoutHiddenInput;
    private EditText etRoutineInput;

    private SharedPreferences prefs;

    private int currentExp = 0;
    private int maxExp = 1000;
    private String currentRank = "베이비";
    private List<Routine> routineList;

    private int clickedRoutinePosition = -1;

    // ✨ [카메라 콜백 연동 고정] 카메라 정밀 라벨 인식이 완료되었을 때 작동하는 안전한 처리 구역
    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    boolean isSuccess = result.getData().getBooleanExtra("isSuccess", false);
                    if (isSuccess) {
                        // 1. 유저가 선택했던 투두 아이템을 완료 상태로 변경하고 화면 갱신
                        if (clickedRoutinePosition != -1 && routineList != null) {
                            routineList.get(clickedRoutinePosition).setCompleted(true);
                            if (adapter != null) {
                                adapter.notifyDataSetChanged();
                            }
                            saveRoutineList(); // 로컬에 완료 상태 즉시 저장
                        }
                        // 2. 유저님의 기존 고유 시스템인 경험치 획득 및 펫 애니메이션 로직 가동!
                        handleExpGain(100);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        prefs = getSharedPreferences("RoutineMonPrefs", MODE_PRIVATE);

        mainLayout = findViewById(R.id.main);
        progressBar = findViewById(R.id.progressBar);
        tvExp = findViewById(R.id.tvExp);
        lottiePet = findViewById(R.id.lottiePet);

        layoutHiddenInput = findViewById(R.id.layoutHiddenInput);
        etRoutineInput = findViewById(R.id.etRoutineInput);
        Button btnToggleInput = findViewById(R.id.btnToggleInput);
        Button btnSubmitRoutine = findViewById(R.id.btnSubmitRoutine);

        currentRank = prefs.getString("currentRank", "베이비");
        currentExp = prefs.getInt("currentExp", 0);
        maxExp = prefs.getInt("maxExp", 1000);

        if (btnToggleInput != null && layoutHiddenInput != null) {
            btnToggleInput.setOnClickListener(v -> {
                if (layoutHiddenInput.getVisibility() == View.GONE) {
                    layoutHiddenInput.setVisibility(View.VISIBLE);
                    etRoutineInput.requestFocus();
                } else {
                    layoutHiddenInput.setVisibility(View.GONE);
                }
            });
        }

        // 🧙‍♂️ [업그레이드 콘솔] 유저님의 완벽한 치트키 및 백업 기능 공간 유지
        if (btnSubmitRoutine != null && etRoutineInput != null) {
            btnSubmitRoutine.setOnClickListener(v -> {
                String inputTask = etRoutineInput.getText().toString().trim();

                if (inputTask.isEmpty()) {
                    Toast.makeText(this, "루틴 내용을 입력해 주세요!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // ⚡ 1. 치트키 명령어 판독 (#치트키)
                if (inputTask.equals("#치트키")) {
                    backupCurrentDataBeforeCheat();

                    currentRank = "어린이";
                    currentExp = 1950;
                    maxExp = 2000;

                    prefs.edit().putString("currentRank", currentRank)
                            .putInt("currentExp", currentExp)
                            .putInt("maxExp", maxExp).apply();

                    updateUI();
                    setupCurrentPetAnimation();

                    etRoutineInput.setText("");
                    layoutHiddenInput.setVisibility(View.GONE);
                    Toast.makeText(this, "🧙‍♂️ [명령어] 데이터 백업 후 치트키 가동 완료!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 🧹 2. 초기화 명령어 판독 (#초기화)
                if (inputTask.equals("#초기화")) {
                    backupCurrentDataBeforeCheat();

                    prefs.edit().remove("currentRank")
                            .remove("currentExp")
                            .remove("maxExp")
                            .remove("savedRoutines").apply();

                    currentRank = "베이비";
                    currentExp = 0;
                    maxExp = 1000;

                    routineList = new ArrayList<>();
                    routineList.add(new Routine("물 한 잔 마시기", false, "cup"));
                    routineList.add(new Routine("책 읽기", false, "book"));
                    routineList.add(new Routine("스트레칭 하기", false, "exercise"));

                    adapter.setRoutines(routineList);
                    updateUI();
                    setupCurrentPetAnimation();

                    etRoutineInput.setText("");
                    layoutHiddenInput.setVisibility(View.GONE);
                    Toast.makeText(this, "🧹 [명령어] 데이터 백업 후 초기화 완료!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // ↩️ 3. 복구 명령어 판독 (#이전)
                if (inputTask.equals("#이전")) {
                    String backupRoutines = prefs.getString("backupRoutines", "");

                    if (backupRoutines.isEmpty()) {
                        Toast.makeText(this, "복구할 이전 백업 데이터가 없습니다!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    currentRank = prefs.getString("backupRank", "베이비");
                    currentExp = prefs.getInt("backupExp", 0);
                    maxExp = prefs.getInt("backupMaxExp", 1000);

                    prefs.edit().putString("currentRank", currentRank)
                            .putInt("currentExp", currentExp)
                            .putInt("maxExp", maxExp)
                            .putString("savedRoutines", backupRoutines).apply();

                    loadRoutineList();
                    updateUI();
                    setupCurrentPetAnimation();

                    etRoutineInput.setText("");
                    layoutHiddenInput.setVisibility(View.GONE);
                    Toast.makeText(this, "↩️ [복구 성공] 소중한 이전 데이터로 원상 복구되었습니다!", Toast.LENGTH_LONG).show();
                    return;
                }

                // 📦 4. 일반 텍스트일 때는 평소처럼 루틴 추가 작동
                Routine newRoutine = new Routine(inputTask, false, "star");
                routineList.add(newRoutine);

                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }

                saveRoutineList();

                etRoutineInput.setText("");
                layoutHiddenInput.setVisibility(View.GONE);
                Toast.makeText(this, "📦 새로운 루틴이 추가되었습니다!", Toast.LENGTH_SHORT).show();
            });
        }

        Button btnCollection = findViewById(R.id.btnCollection);
        if (btnCollection != null) {
            btnCollection.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, CollectionActivity.class);
                intent.putExtra("currentStage", currentRank);
                startActivity(intent);
            });
        }

        RecyclerView recyclerView = findViewById(R.id.recyclerViewRoutines);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new RoutineAdapter();
        recyclerView.setAdapter(adapter);

        loadRoutineList();
        checkMidnightReset();

        // 👆 단일 클릭 (정밀 카메라 인증 연동 구역)
        adapter.setOnItemClickListener(clickedRoutine -> {
            if (clickedRoutine.isCompleted()) {
                Toast.makeText(this, "이미 완료한 루틴입니다!", Toast.LENGTH_SHORT).show();
                return;
            }

            // 클릭한 위치를 기억해 두어야 돌아왔을 때 체크표시를 해줍니다.
            clickedRoutinePosition = routineList.indexOf(clickedRoutine);

            Intent intent = new Intent(MainActivity.this, CameraActivity.class);
            // 🎯 선택한 투두의 진짜 이름("물 한 잔 마시기", "책 읽기" 등)을 카메라 액티비티로 패스!
            intent.putExtra("targetRoutine", clickedRoutine.getTitle());
            cameraLauncher.launch(intent);
        });

        // 🖐️ 롱 클릭 (길게 누르면 삭제)
        adapter.setOnItemLongClickListener(clickedRoutine -> {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("🗑️ 루틴 삭제")
                    .setMessage("[" + clickedRoutine.getTitle() + "] 루틴을 정말 삭제하시겠습니까?")
                    .setPositiveButton("삭제", (dialog, which) -> {
                        routineList.remove(clickedRoutine);
                        if (adapter != null) {
                            adapter.notifyDataSetChanged();
                        }
                        saveRoutineList();
                        Toast.makeText(this, "루틴이 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("취소", null)
                    .show();
        });

        if (lottiePet != null) {
            lottiePet.setOnClickListener(v -> {
                lottiePet.playAnimation();
                String message;
                if (currentRank.equals("베이비")) message = "뽀뽀가 꺄르르 웃습니다!";
                else if (currentRank.equals("어린이")) message = "뽀뽀가 폴짝폴짝 뜁니다!";
                else message = "뽀뽀가 늠름하게 인사합니다!";
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            });
        }

        updateUI();
        setupCurrentPetAnimation();
    }

    private void backupCurrentDataBeforeCheat() {
        String currentRoutinesRaw = prefs.getString("savedRoutines", "");
        prefs.edit().putString("backupRank", currentRank)
                .putInt("backupExp", currentExp)
                .putInt("backupMaxExp", maxExp)
                .putString("backupRoutines", currentRoutinesRaw).apply();
    }

    private void checkMidnightReset() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        String today = sdf.format(new Date());
        String lastResetDate = prefs.getString("lastResetDate", "");

        if (!today.equals(lastResetDate)) {
            if (adapter != null) {
                adapter.resetAllRoutines();
                saveRoutineList();
            }
            prefs.edit().putString("lastResetDate", today).apply();
            Toast.makeText(this, "☀️ 새로운 하루가 시작되었습니다! 루틴이 초기화됩니다.", Toast.LENGTH_LONG).show();
        }
    }

    private void saveRoutineList() {
        StringBuilder sb = new StringBuilder();
        for (Routine r : routineList) {
            sb.append(r.getTitle()).append("|")
                    .append(r.isCompleted()).append("|")
                    .append(r.getIconType()).append("_#_");
        }
        prefs.edit().putString("savedRoutines", sb.toString()).apply();
    }

    private void loadRoutineList() {
        routineList = new ArrayList<>();
        String rawData = prefs.getString("savedRoutines", "");

        if (rawData.isEmpty()) {
            routineList.add(new Routine("물 한 잔 마시기", false, "cup"));
            routineList.add(new Routine("책 읽기", false, "book"));
            routineList.add(new Routine("스트레칭 하기", false, "exercise"));
            saveRoutineList();
        } else {
            String[] items = rawData.split("_#_");
            for (String item : items) {
                if (item.trim().isEmpty()) continue;
                String[] parts = item.split("\\|");
                if (parts.length >= 3) {
                    String title = parts[0];
                    boolean isCompleted = Boolean.parseBoolean(parts[1]);
                    String iconType = parts[2];
                    routineList.add(new Routine(title, isCompleted, iconType));
                }
            }
        }
        adapter.setRoutines(routineList);
    }

    private void setupCurrentPetAnimation() {
        if (lottiePet != null) {
            lottiePet.setSpeed(1.0f);
            lottiePet.setRepeatCount(com.airbnb.lottie.LottieDrawable.INFINITE);
            if (currentRank.equals("베이비")) {
                lottiePet.setAnimation(R.raw.pet_animation);
            } else if (currentRank.equals("어린이")) {
                lottiePet.setAnimation(R.raw.pet_child);
            } else if (currentRank.equals("청소년")) {
                lottiePet.setAnimation(R.raw.pet_teen);
            }
            lottiePet.playAnimation();
        }
    }

    private void handleExpGain(int amount) {
        currentExp += amount;

        if (currentExp >= maxExp && currentRank.equals("베이비")) {
            startEvolutionSequence();
        }
        else if (currentExp >= maxExp && currentRank.equals("어린이")) {
            startTeenEvolutionSequence();
        }
        else {
            if (currentExp > maxExp) {
                currentExp = maxExp;
            }
            prefs.edit().putInt("currentExp", currentExp).apply();
            updateUI();
            if (lottiePet != null) lottiePet.playAnimation();
            Toast.makeText(this, "루틴 인증 완료! 경험치 +" + amount, Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressWarnings("MissingPermission")
    private void startEvolutionSequence() {
        triggerVibration();
        currentRank = "어린이";
        currentExp = 0;
        maxExp = 2000;

        prefs.edit().putString("currentRank", currentRank)
                .putInt("currentExp", currentExp)
                .putInt("maxExp", maxExp).apply();
        updateUI();

        if (lottiePet != null) {
            try {
                lottiePet.setRepeatCount(0);
                lottiePet.setAnimation(R.raw.pet_child_evolution);
                lottiePet.setSpeed(0.5f);
                lottiePet.playAnimation();

                new Handler(Looper.getMainLooper()).postDelayed(() -> showFinalChildPet(), 3500);
            } catch (Exception e) {
                showFinalChildPet();
            }
        } else {
            showFinalChildPet();
        }
    }

    private void showFinalChildPet() {
        setupCurrentPetAnimation();
        Toast.makeText(this, "✨ 뽀뽀가 '어린이'로 각성했습니다! ✨", Toast.LENGTH_LONG).show();
    }

    @SuppressWarnings("MissingPermission")
    private void startTeenEvolutionSequence() {
        triggerVibration();
        currentRank = "청소년";
        currentExp = 0;
        maxExp = 3000;

        prefs.edit().putString("currentRank", currentRank)
                .putInt("currentExp", currentExp)
                .putInt("maxExp", maxExp).apply();
        updateUI();

        if (lottiePet != null) {
            try {
                lottiePet.setRepeatCount(0);
                lottiePet.setAnimation(R.raw.pet_child_evolution);
                lottiePet.setSpeed(0.5f);
                lottiePet.playAnimation();

                new Handler(Looper.getMainLooper()).postDelayed(() -> showFinalTeenPet(), 3500);
            } catch (Exception e) {
                showFinalTeenPet();
            }
        } else {
            showFinalTeenPet();
        }
    }

    private void showFinalTeenPet() {
        setupCurrentPetAnimation();
        Toast.makeText(this, "👑 대성장! 뽀뽀가 늠름한 '청소년'으로 최종 진화했습니다! 👑", Toast.LENGTH_LONG).show();
    }

    @SuppressWarnings("MissingPermission")
    private void triggerVibration() {
        Vibrator vibrator;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            VibratorManager vibratorManager = (VibratorManager) getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
            vibrator = vibratorManager != null ? vibratorManager.getDefaultVibrator() : null;
        } else {
            vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        }

        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(new long[]{0, 200, 100, 200, 100, 600}, -1));
            } else {
                vibrator.vibrate(1000);
            }
        }
    }

    private void updateUI() {
        if (progressBar != null) {
            progressBar.setMax(maxExp);
            progressBar.setProgress(currentExp);
        }
        if (tvExp != null) {
            tvExp.setText("현재 단계: " + currentRank + " (EXP " + currentExp + "/" + maxExp + ")");
        }
    }
}