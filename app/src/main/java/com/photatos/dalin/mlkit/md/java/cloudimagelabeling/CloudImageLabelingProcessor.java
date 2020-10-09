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
package com.photatos.dalin.mlkit.md.java.cloudimagelabeling;

import android.graphics.Bitmap;
//sean
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.label.FirebaseVisionCloudImageLabelerOptions;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler;
import com.photatos.dalin.mlkit.md.common.FrameMetadata;
import com.photatos.dalin.mlkit.md.common.GraphicOverlayLabel;
import com.photatos.dalin.mlkit.md.java.VisionProcessorBase;

import java.util.ArrayList;
import java.util.List;

/**
 * Cloud Label Detector Demo.
 */
public class CloudImageLabelingProcessor
        extends VisionProcessorBase<List<FirebaseVisionImageLabel>> {
    private static final String TAG = "CloudImgLabelProc";

    private final FirebaseVisionImageLabeler detector;

    public CloudImageLabelingProcessor() {
        FirebaseVisionCloudImageLabelerOptions.Builder optionsBuilder =
                new FirebaseVisionCloudImageLabelerOptions.Builder();

        detector = FirebaseVision.getInstance().getCloudImageLabeler(optionsBuilder.build());
    }

    @Override
    protected Task<List<FirebaseVisionImageLabel>> detectInImage(FirebaseVisionImage image) {
        return detector.processImage(image);
    }

    @Override
    protected void onSuccess(
            @Nullable Bitmap originalCameraImage,
            @NonNull List<FirebaseVisionImageLabel> labels,
            @NonNull FrameMetadata frameMetadata,
            @NonNull GraphicOverlayLabel graphicOverlayLabel) {
        graphicOverlayLabel.clear();
        Log.d(TAG, "cloud label size: " + labels.size());
        List<String> labelsStr = new ArrayList<>();
        for (int i = 0; i < labels.size(); ++i) {
            FirebaseVisionImageLabel label = labels.get(i);
            Log.d(TAG, "cloud label: " + label);
            if (label.getText() != null) {
                labelsStr.add((label.getText()));
            }
        }
        /* sean
        CloudLabelGraphic cloudLabelGraphic = new CloudLabelGraphic(graphicOverlayLabel, labelsStr);
        graphicOverlayLabel.add(cloudLabelGraphic);
        graphicOverlayLabel.postInvalidate();
        */
    }

    @Override
    protected void onFailure(@NonNull Exception e) {
        Log.e(TAG, "Cloud Label detection failed " + e);
    }
}
