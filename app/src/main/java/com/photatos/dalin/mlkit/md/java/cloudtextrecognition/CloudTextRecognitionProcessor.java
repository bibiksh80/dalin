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
package com.photatos.dalin.mlkit.md.java.cloudtextrecognition;

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
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.photatos.dalin.mlkit.md.common.FrameMetadata;
import com.photatos.dalin.mlkit.md.common.GraphicOverlayLabel;
import com.photatos.dalin.mlkit.md.java.VisionProcessorBase;

import java.util.List;

/**
 * Processor for the cloud text detector demo.
 */
public class CloudTextRecognitionProcessor extends VisionProcessorBase<FirebaseVisionText> {

    private static final String TAG = "CloudTextRecProc";

    private final FirebaseVisionTextRecognizer detector;

    public CloudTextRecognitionProcessor() {
        super();
        detector = FirebaseVision.getInstance().getCloudTextRecognizer();
    }

    @Override
    protected Task<FirebaseVisionText> detectInImage(FirebaseVisionImage image) {
        return detector.processImage(image);
    }

    @Override
    protected void onSuccess(
            @Nullable Bitmap originalCameraImage,
            @NonNull FirebaseVisionText text,
            @NonNull FrameMetadata frameMetadata,
            @NonNull GraphicOverlayLabel graphicOverlayLabel) {
        graphicOverlayLabel.clear();
        if (text == null) {
            return; // TODO: investigate why this is needed
        }
        List<FirebaseVisionText.TextBlock> blocks = text.getTextBlocks();
        for (int i = 0; i < blocks.size(); i++) {
            List<FirebaseVisionText.Line> lines = blocks.get(i).getLines();
            for (int j = 0; j < lines.size(); j++) {
                List<FirebaseVisionText.Element> elements = lines.get(j).getElements();
                for (int l = 0; l < elements.size(); l++) {
                    CloudTextGraphic cloudTextGraphic = new CloudTextGraphic(graphicOverlayLabel,
                            elements.get(l));
                    graphicOverlayLabel.add(cloudTextGraphic);
                }
            }
        }
        graphicOverlayLabel.postInvalidate();
    }

    @Override
    protected void onFailure(@NonNull Exception e) {
        Log.w(TAG, "Cloud Text detection failed." + e);
    }
}
