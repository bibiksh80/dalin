// Copyright 2018 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.photatos.dalin.mlkit.md.java;

import android.graphics.Bitmap;
//sean
import androidx.annotation.GuardedBy;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

//import android.support.annotation.GuardedBy;
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.photatos.dalin.mlkit.md.common.BitmapUtils;
import com.photatos.dalin.mlkit.md.common.FrameMetadata;
import com.photatos.dalin.mlkit.md.common.GraphicOverlayLabel;
import com.photatos.dalin.mlkit.md.common.VisionImageProcessor;

import java.nio.ByteBuffer;

/**
 * Abstract base class for ML Kit frame processors. Subclasses need to implement {@link
 * # to define what they want to with
 * the detection results and {@link #detectInImage(FirebaseVisionImage)} to specify the detector
 * object.
 *
 * @param <T> The type of the detected feature.
 */
public abstract class VisionProcessorBase<T> implements VisionImageProcessor {

    // To keep the latest images and its metadata.
    @GuardedBy("this")
    private ByteBuffer latestImage;

    @GuardedBy("this")
    private FrameMetadata latestImageMetaData;

    // To keep the images and metadata in process.
    @GuardedBy("this")
    private ByteBuffer processingImage;

    @GuardedBy("this")

    private FrameMetadata processingMetaData;

    public VisionProcessorBase() {
    }

    @Override
    public synchronized void process(
            ByteBuffer data, final FrameMetadata frameMetadata, final GraphicOverlayLabel
            graphicOverlayLabel) {
        latestImage = data;
        latestImageMetaData = frameMetadata;
        if (processingImage == null && processingMetaData == null) {
            processLatestImage(graphicOverlayLabel);
        }
    }

    // Bitmap version
    @Override
    public void process(Bitmap bitmap, final GraphicOverlayLabel
            graphicOverlayLabel) {
        detectInVisionImage(null /* bitmap */, FirebaseVisionImage.fromBitmap(bitmap), null,
                graphicOverlayLabel);
    }

    private synchronized void processLatestImage(final GraphicOverlayLabel graphicOverlayLabel) {
        processingImage = latestImage;
        processingMetaData = latestImageMetaData;
        latestImage = null;
        latestImageMetaData = null;
        if (processingImage != null && processingMetaData != null) {
            processImage(processingImage, processingMetaData, graphicOverlayLabel);
        }
    }

    private void processImage(
            ByteBuffer data, final FrameMetadata frameMetadata,
            final GraphicOverlayLabel graphicOverlayLabel) {
        FirebaseVisionImageMetadata metadata =
                new FirebaseVisionImageMetadata.Builder()
                        .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_NV21)
                        .setWidth(frameMetadata.getWidth())
                        .setHeight(frameMetadata.getHeight())
                        .setRotation(frameMetadata.getRotation())
                        .build();

        Bitmap bitmap = BitmapUtils.getBitmap(data, frameMetadata);
        detectInVisionImage(
                bitmap, FirebaseVisionImage.fromByteBuffer(data, metadata), frameMetadata,
                graphicOverlayLabel);
    }

    private void detectInVisionImage(
            final Bitmap originalCameraImage,
            FirebaseVisionImage image,
            final FrameMetadata metadata,
            final GraphicOverlayLabel graphicOverlayLabel) {
        detectInImage(image)
                .addOnSuccessListener(
                        new OnSuccessListener<T>() {
                            @Override
                            public void onSuccess(T results) {
                                VisionProcessorBase.this.onSuccess(originalCameraImage, results,
                                        metadata,
                                        graphicOverlayLabel);
                                processLatestImage(graphicOverlayLabel);
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                VisionProcessorBase.this.onFailure(e);
                            }
                        });
    }

    @Override
    public void stop() {
    }

    protected abstract Task<T> detectInImage(FirebaseVisionImage image);

    /**
     * Callback that executes with a successful detection result.
     *
     * @param originalCameraImage hold the original image from camera, used to draw the background
     *                            image.
     */
    protected abstract void onSuccess(
            @Nullable Bitmap originalCameraImage,
            @NonNull T results,
            @NonNull FrameMetadata frameMetadata,
            @NonNull GraphicOverlayLabel graphicOverlayLabel);

    protected abstract void onFailure(@NonNull Exception e);
}
