package com.example.routinemon.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.routinemon.R;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CameraActivity extends AppCompatActivity {

    private PreviewView viewFinder;
    private TextView tvGuide;
    private TextView tvQuestTitle;
    private TextView btnBack;

    private static final int CAMERA_PERMISSION_CODE = 100;
    private ExecutorService cameraExecutor;
    private boolean isFinished = false;
    private String targetRoutine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        viewFinder = findViewById(R.id.viewFinder);
        tvGuide = findViewById(R.id.tvGuide);
        tvQuestTitle = findViewById(R.id.tvQuestTitle);
        btnBack = findViewById(R.id.btnBack);

        targetRoutine = getIntent().getStringExtra("targetRoutine");
        if (targetRoutine == null) targetRoutine = "";

        tvQuestTitle.setText("퀘스트: " + targetRoutine);
        tvGuide.setText("AI 분석 준비 중...");

        btnBack.setOnClickListener(v -> finish());

        cameraExecutor = Executors.newSingleThreadExecutor();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(viewFinder.getSurfaceProvider());

                // 확신도를 적절히 낮춰 스캔 반응 속도를 유지합니다.
                ImageLabelerOptions options = new ImageLabelerOptions.Builder()
                        .setConfidenceThreshold(0.35f)
                        .build();
                ImageLabeler labeler = ImageLabeling.getClient(options);

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                imageAnalysis.setAnalyzer(cameraExecutor, image -> {
                    if (!isFinished) {
                        processImageProxy(labeler, image);
                    } else {
                        image.close();
                    }
                });

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);

            } catch (ExecutionException | InterruptedException e) {
                Log.e("ML_KIT", "카메라 연결 실패", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    @SuppressLint("UnsafeOptInUsageError")
    private void processImageProxy(ImageLabeler detector, ImageProxy imageProxy) {
        if (imageProxy.getImage() == null) {
            imageProxy.close();
            return;
        }

        InputImage image = InputImage.fromMediaImage(
                imageProxy.getImage(),
                imageProxy.getImageInfo().getRotationDegrees()
        );

        detector.process(image)
                .addOnSuccessListener(labels -> {
                    if (labels.isEmpty()) {
                        updateGuideText("분석 중... 사물을 정면으로 맞춰주세요.");
                        return;
                    }

                    // 실시간 단어 중계 뷰 (AI가 읽는 영어 단어를 보며 디버깅하기 위함)
                    StringBuilder sb = new StringBuilder("AI 스캔 목록:\n");
                    for (int i = 0; i < Math.min(3, labels.size()); i++) {
                        ImageLabel l = labels.get(i);
                        sb.append("[").append(l.getText()).append(" : ").append((int)(l.getConfidence()*100)).append("%]  ");
                    }
                    updateGuideText(sb.toString());

                    for (ImageLabel label : labels) {
                        String detectedText = label.getText().toLowerCase().trim();
                        boolean isMatch = false;

                        // ==================== 🛠️ [미션별 연관어 매칭 콘솔] ====================

                        // 💧 1. 물 마시기 / 컵 관련 루틴
                        if (targetRoutine.contains("물") || targetRoutine.contains("마시기") || targetRoutine.contains("cup")) {
                            if (detectedText.contains("cup") || detectedText.contains("glass") ||
                                    detectedText.contains("drink") || detectedText.contains("bottle") ||
                                    detectedText.contains("mug") || detectedText.contains("tableware")) {
                                isMatch = true;
                            }
                        }

                        // 📚 2. 책 읽기 / 독서 관련 루틴
                        else if (targetRoutine.contains("책") || targetRoutine.contains("읽기") || targetRoutine.contains("book")) {
                            if (detectedText.contains("book") || detectedText.contains("paper") ||
                                    detectedText.contains("text") || detectedText.contains("publication")) {
                                isMatch = true;
                            }
                        }

                        // 🧘‍♂️ 3. 스트레칭 / 운동 관련 루틴
                        else if (targetRoutine.contains("스트레칭") || targetRoutine.contains("운동") || targetRoutine.contains("exercise")) {
                            if (detectedText.contains("mat") || detectedText.contains("physical") ||
                                    detectedText.contains("joint") || detectedText.contains("leg") ||
                                    detectedText.contains("shoe") || detectedText.contains("footwear")) {
                                isMatch = true;
                            }
                        }

                        // 🛏️ 4. 이불 개기 / 정리 관련 루틴
                        else if (targetRoutine.contains("이불") || targetRoutine.contains("개기") || targetRoutine.contains("bed")) {
                            if (detectedText.contains("bedroom") || detectedText.contains("bed") ||
                                    detectedText.contains("pillow") || detectedText.contains("linens") ||
                                    detectedText.contains("comfort") || detectedText.contains("quilt") ||
                                    detectedText.contains("sheet")) {
                                isMatch = true;
                            }
                        }

                        // 💡여기에 다른 미션(예: 영양제 먹기, 이닦기 등)을 if-else 문으로 계속 추가하시면 됩니다!
// 🏃‍♂️ 5. 산책 / 걷기 관련 루틴 (오타 교정 완료!)
                        else if (targetRoutine.contains("산책") || targetRoutine.contains("걷기") || targetRoutine.contains("road")) {
                            if (detectedText.contains("branch") || detectedText.contains("forest") ||
                                    detectedText.contains("soil") || detectedText.contains("rock")) { // 👈 괄호를 닫고 깔끔하게 중괄호 시작!
                                isMatch = true;
                            }
                        }

// 💊 6. 영양제 / 먹기 관련 루틴 (오타 교정 완료!)
                        else if (targetRoutine.contains("영양제") || targetRoutine.contains("먹기") || targetRoutine.contains("medicine")) {
                            if (detectedText.contains("pill") || detectedText.contains("food")) { // 👈 || 지우고 괄호 닫기 완료!
                                isMatch = true;
                            }
                        }
                        // ====================================================================

                        // 매칭 성공 시 완료 처리 루프 탈출
                        if (isMatch && !isFinished) {
                            isFinished = true;
                            handleSuccess(detectedText);
                            break;
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("ML_KIT_LOG", "인식 실패", e))
                .addOnCompleteListener(task -> imageProxy.close());
    }

    private void updateGuideText(String text) {
        runOnUiThread(() -> {
            if (!isFinished && tvGuide != null) {
                tvGuide.setText(text);
            }
        });
    }

    private void handleSuccess(String name) {
        runOnUiThread(() -> {
            tvGuide.setText("🎯 인증 통과! 인식된 타겟: " + name);
            tvGuide.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_light));
            Toast.makeText(this, "체크리스트가 업데이트됩니다!", Toast.LENGTH_SHORT).show();

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                Intent res = new Intent();
                res.putExtra("isSuccess", true);
                setResult(RESULT_OK, res);
                finish();
            }, 1200);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            Toast.makeText(this, "카메라 권한 거부됨", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}