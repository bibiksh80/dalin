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
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.RectF;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.label.FirebaseVisionCloudImageLabelerOptions;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler;
import com.photatos.dalin.mlkit.R;
import com.photatos.dalin.mlkit.md.common.FrameMetadata;
import com.photatos.dalin.mlkit.md.common.GraphicOverlayLabel;
import com.photatos.dalin.mlkit.md.common.VisionImageProcessor;
import com.photatos.dalin.mlkit.md.java.camera.CameraSource;
import com.photatos.dalin.mlkit.md.java.camera.CameraSourcePreview;
import com.photatos.dalin.mlkit.md.java.camera.GraphicOverlay;
import com.photatos.dalin.mlkit.md.java.camera.WorkflowModel;
import com.photatos.dalin.mlkit.md.java.camera.WorkflowModel.WorkflowState;
import com.photatos.dalin.mlkit.md.java.cloudimagelabeling.CloudImageLabelingProcessor;
import com.photatos.dalin.mlkit.md.java.objectdetection.DetectedObject;
import com.photatos.dalin.mlkit.md.java.objectdetection.MultiObjectProcessor;
import com.photatos.dalin.mlkit.md.java.objectdetection.ProminentObjectProcessor;
import com.photatos.dalin.mlkit.md.java.productsearch.BottomSheetScrimView;
import com.photatos.dalin.mlkit.md.java.productsearch.Product;
import com.photatos.dalin.mlkit.md.java.productsearch.ProductAdapter;

import com.photatos.dalin.mlkit.md.java.productsearch.SearchedObject;
import com.photatos.dalin.mlkit.md.java.settings.PreferenceUtils;
import com.photatos.dalin.mlkit.md.java.settings.SettingsActivity;
import com.photatos.dalin.mlkit.md.java.PostingLinkActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
//sean
import com.photatos.dalin.mlkit.md.java.productsearch.SearchEngineCloud;




//sean import com.google.android.material.floatingactionbutton.FloatingActionButton;
// error use FloatingActionButton

/** Demonstrates the object detection and visual search workflow using camera preview. */
public class LiveObjectCloudDetectionActivity extends AppCompatActivity implements OnClickListener {

  private static final String TAG = "LiveCloudActivity";

  private CameraSource cameraSource;
  private CameraSourcePreview preview;
  private GraphicOverlay graphicOverlay;
  private View settingsButton;
  private View flashButton;
  private Chip promptChip;
  private AnimatorSet promptChipAnimator;
  private ExtendedFloatingActionButton searchButton;
  // sean private FloatingActionButton searchButton;

  private AnimatorSet searchButtonAnimator;
  private ProgressBar searchProgressBar;
  //private WorkflowModel workflowModel;
  private WorkflowModel workflowModel;
  private WorkflowState currentWorkflowState;

  //private SearchEngine searchEngine;
  //sean
  private GraphicOverlayLabel graphicImageOverlay;
  private SearchEngineCloud searchEngineCloud;
  private Bitmap searchbitmap;
  private VisionImageProcessor imageProcessor;

  private  FirebaseVisionImageLabeler detector;

  private BottomSheetBehavior<View> bottomSheetBehavior;
  private BottomSheetScrimView bottomSheetScrimView;
  private RecyclerView productRecyclerView;
  private TextView bottomSheetTitleView;
  private Bitmap objectThumbnailForBottomSheet;
  private boolean slidingSheetUpFromHiddenState;

  //database
  DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
  DatabaseReference RecycleTag = mRootRef.child("recycletag");

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    //searchEngine = new SearchEngine(getApplicationContext());
    searchEngineCloud = new SearchEngineCloud(getApplicationContext());

    setContentView(R.layout.activity_live_object_cloud);
    preview = findViewById(R.id.camera_preview);
    graphicOverlay = findViewById(R.id.camera_preview_graphic_overlay);
    graphicOverlay.setOnClickListener(this);
    cameraSource = new CameraSource(graphicOverlay);

    promptChip = findViewById(R.id.bottom_prompt_chip);
    promptChipAnimator =
        (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.bottom_prompt_chip_enter);
    promptChipAnimator.setTarget(promptChip);

    searchButton = findViewById(R.id.product_search_button);
    searchButton.setOnClickListener(this);
    searchButtonAnimator =
        (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.search_button_enter);
    searchButtonAnimator.setTarget(searchButton);

    searchProgressBar = findViewById(R.id.search_progress_bar);

    //sean
    graphicImageOverlay = (GraphicOverlayLabel) findViewById(R.id.previewOverlay);
    if (graphicImageOverlay == null) {
      Log.d(TAG, "graphicOverlay is null");
    }
    FirebaseVisionCloudImageLabelerOptions.Builder optionsBuilder =
            new FirebaseVisionCloudImageLabelerOptions.Builder();

    detector = FirebaseVision.getInstance().getCloudImageLabeler(optionsBuilder.build());



    setUpBottomSheet();

    findViewById(R.id.close_button).setOnClickListener(this);
    flashButton = findViewById(R.id.flash_button);
    flashButton.setOnClickListener(this);
    settingsButton = findViewById(R.id.settings_button);
    settingsButton.setOnClickListener(this);

    setUpWorkflowModel();
  }
  public void addListenerOnButton() {

    RecycleTag.addValueEventListener(new ValueEventListener() {
      @Override
      public void onDataChange(DataSnapshot dataSnapshot) {
//        ReservData Reservdata = dataSnapshot.getValue(ReservData.class);
        String text = dataSnapshot.getValue(String.class);
        Log.d(TAG, "dataSnapshot text: " + text);

        if (text==null)
        {
//          ReservationDate ="No Reservateion";
//          display="예약 없습니다.";
        } else
          {
//            if(yearint>=year2 && monthint>=dayofweek && dayint >= dayofmonth){
//            ReservationDate = text;
//            display=text+"예약 되었습니다";
//             }else{
//            ReservationDate = text;
//            display=text+"예약이  지났습니다.";
//             }
//
//            ReservationDate = text;
//            display=text+"예약 되었습니다";
        }

      }

      @Override
      public void onCancelled(DatabaseError databaseError) {

      }
    });




  }

  @Override
  protected void onResume() {
    super.onResume();

    workflowModel.markCameraFrozen();
    settingsButton.setEnabled(true);
    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    currentWorkflowState = WorkflowState.NOT_STARTED;
    cameraSource.setFrameProcessor(
        PreferenceUtils.isMultipleObjectsMode(this)
            ? new MultiObjectProcessor(graphicOverlay, workflowModel)
            : new ProminentObjectProcessor(graphicOverlay, workflowModel));
    workflowModel.setWorkflowState(WorkflowState.DETECTING);
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
    //searchEngine.shutdown();
    searchEngineCloud.shutdown();

  }

  @Override
  public void onBackPressed() {
    if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
      bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    } else {
      super.onBackPressed();
    }
  }

  @Override
  public void onClick(View view) {
    int id = view.getId();
    if (id == R.id.product_search_button) {
      searchButton.setEnabled(false);
      workflowModel.onSearchButtonClicked();

    } else if (id == R.id.bottom_sheet_scrim_view) {
      bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

    } else if (id == R.id.close_button) {
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
  private boolean checkblogpost(List<Product> pd){

    int flag=0;

    for(int i=0; i < pd.size(); i++) {
      //Toast.makeText(getApplicationContext(), productList.get(i).title, Toast.LENGTH_LONG).show();
      Log.d(TAG, "Product Search: " + pd.get(i).title);
      // Can
      if (pd.get(i).title.contains("Metal") || pd.get(i).title.contains("Can") )  {
        //Toast.makeText(getApplicationContext(), "Metal", Toast.LENGTH_LONG).show();
        Intent view = new Intent(Intent.ACTION_VIEW);
        view.setData(Uri.parse("http://34.67.19.15/kaen/"));
        startActivity(view);
        flag = 1;
        break;
      }
      else if (pd.get(i).title.contains("Juicebox") ||pd.get(i).title.contains("Packaging and labeling"))  {
        //Toast.makeText(getApplicationContext(), "Paper", Toast.LENGTH_LONG).show();
        Intent view = new Intent(Intent.ACTION_VIEW);
        view.setData(Uri.parse("http://34.67.19.15/jongipaegryu/"));
        startActivity(view);
        flag = 1;
        break;
      }  // water bottle
      else if (pd.get(i).title.contains("Bottled water") || pd.get(i).title.contains("Mineral water")
              ||pd.get(i).title.contains("Water")
              ||pd.get(i).title.contains("Aqua")
              ||pd.get(i).title.contains("Drinking water")
              ||pd.get(i).title.contains("Turquoise")
              ||pd.get(i).title.contains("Liquid")
      ) {
        //Toast.makeText(getApplicationContext(), "can", Toast.LENGTH_LONG).show();
        Log.d(TAG, "Bottled water & Plastic bottle: " + pd.get(i).title);
        Intent view = new Intent(Intent.ACTION_VIEW);
        view.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        view.setData(Uri.parse("http://34.67.19.15/peteubyeong/"));
        flag = 1;
        startActivity(view);
        break;
      }  // Fruit
      else if (pd.get(i).title.contains("Apple") || pd.get(i).title.contains("Fruit")) {
        //Toast.makeText(getApplicationContext(), "Paper", Toast.LENGTH_LONG).show();
        Intent view = new Intent(Intent.ACTION_VIEW);
        view.setData(Uri.parse("http://34.67.19.15/gwailryu/"));
        startActivity(view);
        flag = 1;
        break;
      }
      //box
      else if (pd.get(i).title.contains("Box")) {
        //Toast.makeText(getApplicationContext(), "Paper", Toast.LENGTH_LONG).show();
        Intent view = new Intent(Intent.ACTION_VIEW);
        view.setData(Uri.parse("http://34.67.19.15/bagseu/"));
        startActivity(view);
        flag = 1;
        break;
      }  // plastic
      else if (pd.get(i).title.contains("Plastic") ) {
        //Toast.makeText(getApplicationContext(), "Paper", Toast.LENGTH_LONG).show();
        Intent view = new Intent(Intent.ACTION_VIEW);
        view.setData(Uri.parse("http://34.67.19.15/plastic/"));
        flag = 1;
        startActivity(view);
      }  // glass
      else if (pd.get(i).title.contains("Glass") ) {
        //Toast.makeText(getApplicationContext(), "Glass", Toast.LENGTH_LONG).show();
        Log.d(TAG, "Glass & !Plastic bottle : " + pd.get(i).title);
        Intent view = new Intent(Intent.ACTION_VIEW);
        view.setData(Uri.parse("http://34.67.19.15/yuribyeong/"));
        startActivity(view);
        flag = 1;
        break;
      }
      else if (pd.get(i).title.contains("Book")) {
        //Toast.makeText(getApplicationContext(), "Paper", Toast.LENGTH_LONG).show();
        Intent view = new Intent(Intent.ACTION_VIEW);
        view.setData(Uri.parse("http://34.67.19.15/jongi/"));
        startActivity(view);
        flag = 1;
        break;
      }
      else if (pd.get(i).title.contains("Mouse")) {
        //Toast.makeText(getApplicationContext(), "Paper", Toast.LENGTH_LONG).show();
        Intent view = new Intent(Intent.ACTION_VIEW);
        view.setData(Uri.parse("http://34.67.19.15/mouse/"));
        startActivity(view);
        flag = 1;
        break;
      }  // fruit

    }


    if (flag==1){
      return true;
    }else
    {
      return false;
    }


  }

  private void setUpBottomSheet() {
    bottomSheetBehavior = BottomSheetBehavior.from(findViewById(R.id.bottom_sheet));
    bottomSheetBehavior.setBottomSheetCallback(
        new BottomSheetBehavior.BottomSheetCallback() {
          @Override
          public void onStateChanged(@NonNull View bottomSheet, int newState) {
            Log.d(TAG, "Bottom sheet new state: " + newState);
            bottomSheetScrimView.setVisibility(
                newState == BottomSheetBehavior.STATE_HIDDEN ? View.GONE : View.VISIBLE);
            graphicOverlay.clear();

            switch (newState) {
              case BottomSheetBehavior.STATE_HIDDEN:
                workflowModel.setWorkflowState(WorkflowState.DETECTING);
                break;
              case BottomSheetBehavior.STATE_COLLAPSED:
              case BottomSheetBehavior.STATE_EXPANDED:
              case BottomSheetBehavior.STATE_HALF_EXPANDED:
                slidingSheetUpFromHiddenState = false;
                break;
              case BottomSheetBehavior.STATE_DRAGGING:
              case BottomSheetBehavior.STATE_SETTLING:
              default:
                break;
            }
          }

          @Override
          public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            SearchedObject searchedObject = workflowModel.searchedObject.getValue();
            if (searchedObject == null || Float.isNaN(slideOffset)) {
              return;
            }

            int collapsedStateHeight =
                Math.min(bottomSheetBehavior.getPeekHeight(), bottomSheet.getHeight());
            if (slidingSheetUpFromHiddenState) {
              RectF thumbnailSrcRect =
                  graphicOverlay.translateRect(searchedObject.getBoundingBox());
              bottomSheetScrimView.updateWithThumbnailTranslateAndScale(
                  objectThumbnailForBottomSheet,
                  collapsedStateHeight,
                  slideOffset,
                  thumbnailSrcRect);

            } else {
              bottomSheetScrimView.updateWithThumbnailTranslate(
                  objectThumbnailForBottomSheet, collapsedStateHeight, slideOffset, bottomSheet);
            }
          }
        });

    bottomSheetScrimView = findViewById(R.id.bottom_sheet_scrim_view);
    bottomSheetScrimView.setOnClickListener(this);

    bottomSheetTitleView = findViewById(R.id.bottom_sheet_title);
    productRecyclerView = findViewById(R.id.product_recycler_view);
    productRecyclerView.setHasFixedSize(true);
    productRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    productRecyclerView.setAdapter(new ProductAdapter(ImmutableList.of()));
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

          if (PreferenceUtils.isAutoSearchEnabled(this)) {
            stateChangeInAutoSearchMode(workflowState);
          } else {
            stateChangeInManualSearchMode(workflowState);
          }
        });

    // Observes changes on the object to search, if happens, fire product search request.
    workflowModel.objectToSearch.observe(
            this, object -> searchEngineCloud.search(object, workflowModel));



    // Observes changes on the object that has search completed, if happens, show the bottom sheet
    // to present search result.
    workflowModel.searchedObject.observe(
            this,
            searchedObject -> {
              if (searchedObject != null) {
                List<Product> productList = searchedObject.getProductList();

                objectThumbnailForBottomSheet = searchedObject.getObjectThumbnail();

                //sean
                // check if there is text in blog posting
                boolean flag=false;
                flag=checkblogpost(productList);

                if (flag==true){

                  //
                  /*
                  bottomSheetTitleView.setText(
                          getResources()
                                  .getQuantityString(
                                          R.plurals.bottom_sheet_title, productList.size(), productList.size()));
                  productRecyclerView.setAdapter(new ProductAdapter(productList));
                  slidingSheetUpFromHiddenState = true;
                  bottomSheetBehavior.setPeekHeight(preview.getHeight() / 2);
                  bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                  */
                }else
                {
//                  Toast.makeText(getApplicationContext(), "정보가 없습니다. 당신이 이 물건의 맨 처음 등록자가 되어주세요.(1000 point 지급)", Toast.LENGTH_LONG).show();
//                  Activity activity = LiveObjectCloudDetectionActivity.this;
//                  activity.startActivity(new Intent(activity, com.seanlab.dalin.mlkit.ghost.view.LoginActivity.class));





                  Activity activity = LiveObjectCloudDetectionActivity.this;
                  activity.startActivity(new Intent(activity, com.photatos.dalin.mlkit.md.java.PostingLinkActivity.class));
                }

               /*
                bottomSheetTitleView.setText(
                        getResources()
                                .getQuantityString(
                                        R.plurals.bottom_sheet_title, productList.size(), productList.size()));
                productRecyclerView.setAdapter(new ProductAdapter(productList));

                Log.d(TAG, "searched : " + productList.size());




                slidingSheetUpFromHiddenState = true;
                bottomSheetBehavior.setPeekHeight(preview.getHeight() / 2);
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                */


              }
            });


  }

  public void getFromCloud() {
    DetectedObject object = workflowModel.getConfirmedObject();
    Log.e(TAG, "SEAN:FirebaseVisionImage===>start");
    //searchbitmap=searchedObject.getObjectThumbnail();
    if (object != null) {
      searchbitmap = object.getBitmap();

     FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(searchbitmap);
      detector.processImage(image)
      .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionImageLabel>>() {
        @Override
        public void onSuccess(List<FirebaseVisionImageLabel> firebaseVisionImageLabels) {
          //
          Log.e(TAG, "SEAN:FirebaseVisionImage===>onSuccess");
          Log.d(TAG, "cloud label size: " + firebaseVisionImageLabels.size());
          List<String> labelsStr = new ArrayList<>();
          List<Product> productList = new ArrayList<>();
          for (int i = 0; i < firebaseVisionImageLabels.size(); ++i) {
            FirebaseVisionImageLabel label = firebaseVisionImageLabels.get(i);
            Log.d(TAG, "cloud label: " + label);
            if (label.getText() != null) {
              labelsStr.add((label.getText()));
              String labels=label.getText();
              Float confidence=label.getConfidence();
              productList.add(
                      new Product( "", labels + i, confidence.toString() + i));

            }
          }


          bottomSheetTitleView.setText(
                  getResources()
                          .getQuantityString(
                                  R.plurals.bottom_sheet_title, labelsStr.size(), labelsStr.size()));

          productRecyclerView.setAdapter(new ProductAdapter(productList));
          Log.d(TAG, "searched : " + productList.size());

          ///
        }
      })
      .addOnFailureListener(new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception e) {
          //
          Log.e(TAG, "SEAN:FirebaseVisionImage===>onFailure");
          //
        }
      })
      ;
      //imageProcessor = new CloudImageLabelingProcessor();
      //imageProcessor.process(searchbitmap, graphicImageOverlay);
    }
    Log.e(TAG, "SEAN:FirebaseVisionImage===>end" );

  }
  private void stateChangeInAutoSearchMode(WorkflowState workflowState) {
    boolean wasPromptChipGone = (promptChip.getVisibility() == View.GONE);

    //sean searchButton.setVisibility(View.GONE);
    searchButton.setVisibility(View.GONE);
    searchProgressBar.setVisibility(View.GONE);
    switch (workflowState) {
      case DETECTING:
      case DETECTED:
      case CONFIRMING:
        promptChip.setVisibility(View.VISIBLE);
        promptChip.setText(
            workflowState == WorkflowState.CONFIRMING
                ? R.string.prompt_hold_camera_steady
                : R.string.prompt_point_at_an_object);
        startCameraPreview();
        break;
      case CONFIRMED:
        promptChip.setVisibility(View.VISIBLE);
        promptChip.setText(R.string.prompt_searching);



        stopCameraPreview();
        break;
      case SEARCHING:
        searchProgressBar.setVisibility(View.VISIBLE);
        promptChip.setVisibility(View.VISIBLE);
        promptChip.setText(R.string.prompt_searching);
        stopCameraPreview();
        break;
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
  }

  private void stateChangeInManualSearchMode(WorkflowState workflowState) {
    boolean wasPromptChipGone = (promptChip.getVisibility() == View.GONE);
    boolean wasSearchButtonGone = (searchButton.getVisibility() == View.GONE);

    searchProgressBar.setVisibility(View.GONE);
    switch (workflowState) {
      case DETECTING:
      case DETECTED:
      case CONFIRMING:
        promptChip.setVisibility(View.VISIBLE);
        promptChip.setText(R.string.prompt_point_at_an_object);
        searchButton.setVisibility(View.GONE);
        startCameraPreview();
        break;
      case CONFIRMED:
        promptChip.setVisibility(View.GONE);
        searchButton.setVisibility(View.VISIBLE);
        searchButton.setEnabled(true);
        searchButton.setBackgroundColor(Color.WHITE);
        startCameraPreview();
        break;
      case SEARCHING:
        promptChip.setVisibility(View.GONE);
        searchButton.setVisibility(View.VISIBLE);
        searchButton.setEnabled(false);
        searchButton.setBackgroundColor(Color.GRAY);
        searchProgressBar.setVisibility(View.VISIBLE);
        stopCameraPreview();
        break;
      case SEARCHED:
        promptChip.setVisibility(View.GONE);
        searchButton.setVisibility(View.GONE);
        stopCameraPreview();
        break;
      default:
        promptChip.setVisibility(View.GONE);
        searchButton.setVisibility(View.GONE);
        break;
    }

    boolean shouldPlayPromptChipEnteringAnimation =
        wasPromptChipGone && (promptChip.getVisibility() == View.VISIBLE);
    if (shouldPlayPromptChipEnteringAnimation && !promptChipAnimator.isRunning()) {
      promptChipAnimator.start();
    }

    boolean shouldPlaySearchButtonEnteringAnimation =
        wasSearchButtonGone && (searchButton.getVisibility() == View.VISIBLE);
    if (shouldPlaySearchButtonEnteringAnimation && !searchButtonAnimator.isRunning()) {
      searchButtonAnimator.start();
    }
  }
}
