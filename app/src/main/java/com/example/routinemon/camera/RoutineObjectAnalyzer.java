package com.example.routinemon.camera;

import android.media.Image;
import androidx.annotation.NonNull;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.objects.DetectedObject;
import com.google.mlkit.vision.objects.ObjectDetection;
import com.google.mlkit.vision.objects.ObjectDetector;
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions;

public class RoutineObjectAnalyzer implements ImageAnalysis.Analyzer {
    private final String targetKeyword;
    private final OnResultListener listener;
    private final ObjectDetector objectDetector;

    public interface OnResultListener {
        void onResult(boolean isSuccess);
    }

    public RoutineObjectAnalyzer(String targetKeyword, OnResultListener listener) {
        this.targetKeyword = targetKeyword;
        this.listener = listener;
        ObjectDetectorOptions options = new ObjectDetectorOptions.Builder()
                .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
                .enableMultipleObjects()
                .enableClassification()
                .build();
        this.objectDetector = ObjectDetection.getClient(options);
    }

    @androidx.annotation.OptIn(markerClass = ExperimentalGetImage.class)
    @Override
    public void analyze(@NonNull ImageProxy imageProxy) {
        Image mediaImage = imageProxy.getImage();
        if (mediaImage != null) {
            InputImage image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());
            objectDetector.process(image)
                    .addOnSuccessListener(detectedObjects -> {
                        boolean isSuccess = false;
                        for (DetectedObject obj : detectedObjects) {
                            for (DetectedObject.Label label : obj.getLabels()) {
                                if (label.getText().toLowerCase().contains(targetKeyword.toLowerCase())) {
                                    isSuccess = true;
                                    break;
                                }
                            }
                            if (isSuccess) break;
                        }
                        listener.onResult(isSuccess);
                    })
                    .addOnFailureListener(e -> listener.onResult(false))
                    .addOnCompleteListener(task -> imageProxy.close());
        } else {
            imageProxy.close();
        }
    }
}