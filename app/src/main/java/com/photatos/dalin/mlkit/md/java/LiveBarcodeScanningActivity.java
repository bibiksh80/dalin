/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.photatos.dalin.mlkit.md.java;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;
import com.google.android.material.chip.Chip;
import com.google.common.base.Objects;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;
import com.photatos.dalin.mlkit.R;
import com.photatos.dalin.mlkit.md.java.camera.GraphicOverlay;
import com.photatos.dalin.mlkit.md.java.camera.WorkflowModel;
import com.photatos.dalin.mlkit.md.java.camera.WorkflowModel.WorkflowState;
import com.photatos.dalin.mlkit.md.java.barcodedetection.BarcodeField;
import com.photatos.dalin.mlkit.md.java.barcodedetection.BarcodeProcessor;
import com.photatos.dalin.mlkit.md.java.barcodedetection.BarcodeResultFragment;
import com.photatos.dalin.mlkit.md.java.camera.CameraSource;
import com.photatos.dalin.mlkit.md.java.camera.CameraSourcePreview;
import com.photatos.dalin.mlkit.md.java.settings.SettingsActivity;

import java.io.IOException;
import java.util.ArrayList;
import android.net.Uri;

/** Demonstrates the barcode scanning workflow using camera preview. */
public class LiveBarcodeScanningActivity extends AppCompatActivity implements OnClickListener {

  private static final String TAG = "LiveBarcodeActivity";

  private CameraSource cameraSource;
  private CameraSourcePreview preview;
  private GraphicOverlay graphicOverlay;
  private View settingsButton;
  private View flashButton;
  private Chip promptChip;
  private AnimatorSet promptChipAnimator;
  private WorkflowModel workflowModel;
  private WorkflowState currentWorkflowState;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_live_barcode);
    preview = findViewById(R.id.camera_preview);
    graphicOverlay = findViewById(R.id.camera_preview_graphic_overlay);
    graphicOverlay.setOnClickListener(this);
    cameraSource = new CameraSource(graphicOverlay);

    promptChip = findViewById(R.id.bottom_prompt_chip);
    promptChipAnimator =
        (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.bottom_prompt_chip_enter);
    promptChipAnimator.setTarget(promptChip);

    findViewById(R.id.close_button).setOnClickListener(this);
    flashButton = findViewById(R.id.flash_button);
    flashButton.setOnClickListener(this);
    settingsButton = findViewById(R.id.settings_button);
    settingsButton.setOnClickListener(this);

    setUpWorkflowModel();
  }

  @Override
  protected void onResume() {
    super.onResume();

    workflowModel.markCameraFrozen();
    settingsButton.setEnabled(true);
    currentWorkflowState = WorkflowState.NOT_STARTED;
    cameraSource.setFrameProcessor(new BarcodeProcessor(graphicOverlay, workflowModel));
    workflowModel.setWorkflowState(WorkflowState.DETECTING);
  }

  @Override
  protected void onPostResume() {
    super.onPostResume();
    BarcodeResultFragment.dismiss(getSupportFragmentManager());
  }

  @Override
  protected void onPause() {
    super.onPause();
    currentWorkflowState = WorkflowState.NOT_STARTED;
    stopCameraPreview();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (cameraSource != null) {
      cameraSource.release();
      cameraSource = null;
    }
  }

  @Override
  public void onClick(View view) {
    int id = view.getId();
    if (id == R.id.close_button) {
      onBackPressed();

    } else if (id == R.id.flash_button) {
      if (flashButton.isSelected()) {
        flashButton.setSelected(false);
        cameraSource.updateFlashMode(Camera.Parameters.FLASH_MODE_OFF);
      } else {
        flashButton.setSelected(true);
        cameraSource.updateFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
      }

    } else if (id == R.id.settings_button) {
      // Sets as disabled to prevent the user from clicking on it too fast.
      settingsButton.setEnabled(false);
      startActivity(new Intent(this, SettingsActivity.class));
    }
  }

  private void startCameraPreview() {
    if (!workflowModel.isCameraLive() && cameraSource != null) {
      try {
        workflowModel.markCameraLive();
        preview.start(cameraSource);
      } catch (IOException e) {
        Log.e(TAG, "Failed to start camera preview!", e);
        cameraSource.release();
        cameraSource = null;
      }
    }
  }

  private void stopCameraPreview() {
    if (workflowModel.isCameraLive()) {
      workflowModel.markCameraFrozen();
      flashButton.setSelected(false);
      preview.stop();
    }
  }
  private boolean checkblogpost(String value) {

    String substring="";
    String company=value.substring(3,9);
    String product=value.substring(9,12);
    Log.d(TAG, "company : " + company);
    Log.d(TAG, "product : " + product);
    // GS water bottle
    if (company.equals("948250") &&(product.equals("048"))) {
      Intent view = new Intent(Intent.ACTION_VIEW);
      view.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
      view.setData(Uri.parse("http://34.67.19.15/peteubyeong/"));
      startActivity(view);
      return true;
    } else if (company.equals("105604") &&(product.equals("988"))){
      Intent view = new Intent(Intent.ACTION_VIEW);
      view.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
      view.setData(Uri.parse("http://34.67.19.15/peteubyeong/"));
      startActivity(view);
      return true;
    }
    else if (company.equals("104302") &&(product.equals("278"))){
      Intent view = new Intent(Intent.ACTION_VIEW);
      view.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
      view.setData(Uri.parse("http://34.67.19.15/peteubyeong/"));
      startActivity(view);
      return true;
    }

    return false;
  }


  private void setUpWorkflowModel() {
    workflowModel = ViewModelProviders.of(this).get(WorkflowModel.class);

    // Observes the workflow state changes, if happens, update the overlay view indicators and
    // camera preview state.
    workflowModel.workflowState.observe(
        this,
        workflowState -> {
          if (workflowState == null || Objects.equal(currentWorkflowState, workflowState)) {
            return;
          }

          currentWorkflowState = workflowState;
          Log.d(TAG, "Current workflow state: " + currentWorkflowState.name());

          boolean wasPromptChipGone = (promptChip.getVisibility() == View.GONE);

          switch (workflowState) {
            case DETECTING:
              promptChip.setVisibility(View.VISIBLE);
              promptChip.setText(R.string.prompt_point_at_a_barcode);
              startCameraPreview();
              break;
            case CONFIRMING:
              promptChip.setVisibility(View.VISIBLE);
              promptChip.setText(R.string.prompt_move_camera_closer);
              startCameraPreview();
              break;
            case SEARCHING:
              promptChip.setVisibility(View.VISIBLE);
              promptChip.setText(R.string.prompt_searching);
              stopCameraPreview();
              break;
            case DETECTED:
            case SEARCHED:
              promptChip.setVisibility(View.GONE);
              stopCameraPreview();
              break;
            default:
              promptChip.setVisibility(View.GONE);
              break;
          }

          boolean shouldPlayPromptChipEnteringAnimation =
              wasPromptChipGone && (promptChip.getVisibility() == View.VISIBLE);
          if (shouldPlayPromptChipEnteringAnimation && !promptChipAnimator.isRunning()) {
            promptChipAnimator.start();
          }
        });

    workflowModel.detectedBarcode.observe(
        this,
        barcode -> {
          if (barcode != null) {
            ArrayList<BarcodeField> barcodeFieldList = new ArrayList<>();

            Log.d(TAG, "Barcode : " + barcode.getRawValue());

            boolean check=checkblogpost(barcode.getRawValue());
            if (check==true){
              Log.d(TAG, "check==true : " + barcode.getRawValue());
            }else
            {
              int type = barcode.getValueType();
              switch (type) {
                case FirebaseVisionBarcode.TYPE_URL:
                  Intent view = new Intent(Intent.ACTION_VIEW);
                  view.setData(Uri.parse(barcode.getDisplayValue()));
                  startActivity(view);
                  break;
                default:
                  barcodeFieldList.add(new BarcodeField("바코드숫자", barcode.getRawValue()));
                  BarcodeResultFragment.show(getSupportFragmentManager(), barcodeFieldList);
                  break;

              }
            }


            /*
            barcodeFieldList.add(new BarcodeField("Raw Value", barcode.getRawValue()));
            BarcodeResultFragment.show(getSupportFragmentManager(), barcodeFieldList);
            */
          }
        });
  }
}
