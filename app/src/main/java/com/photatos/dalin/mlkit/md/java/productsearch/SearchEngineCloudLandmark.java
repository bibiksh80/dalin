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

package com.photatos.dalin.mlkit.md.java.productsearch;


import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.label.FirebaseVisionCloudImageLabelerOptions;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler;
import com.photatos.dalin.mlkit.md.java.objectdetection.DetectedObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//import com.google.firebase.ml.md.java.objectdetection.DetectedObject;


/** A fake search engine to help simulate the complete work flow. */
public class SearchEngineCloudLandmark {

  private static final String TAG = "SearchEngineCloudland";


    private Bitmap searchbitmap;
    private final FirebaseVisionImageLabeler detector;


    public interface SearchResultListener {
    void onSearchCompleted(DetectedObject object, List<Product> productList);
  }

  private final RequestQueue searchRequestQueue;
  private final ExecutorService requestCreationExecutor;

  public SearchEngineCloudLandmark(Context context) {
    searchRequestQueue = Volley.newRequestQueue(context);
    requestCreationExecutor = Executors.newSingleThreadExecutor();

    //initial

      FirebaseVisionCloudImageLabelerOptions.Builder optionsBuilder =
              new FirebaseVisionCloudImageLabelerOptions.Builder()
              ;

      detector = FirebaseVision.getInstance().getCloudImageLabeler(optionsBuilder.build());

  }

  public void search(DetectedObject object, SearchResultListener listener) {
    // Crops the object image out of the full image is expensive, so do it off the UI thread.

       Tasks.call(requestCreationExecutor, () -> createRequest(object))
               .addOnSuccessListener(new OnSuccessListener<JsonObjectRequest>() {
                   @Override
                   public void onSuccess(JsonObjectRequest jsonObjectRequest) {
                       //sean

                       searchbitmap = object.getBitmap();
                       FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(searchbitmap);
                       detector.processImage(image)
                         .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionImageLabel>>() {
                             @Override
                             public void onSuccess(List<FirebaseVisionImageLabel> firebaseVisionImageLabels) {
                                 Log.d(TAG, "cloud label size: " + firebaseVisionImageLabels.size());

                                 List<Product> productList = new ArrayList<>();
                                 for (int i = 0; i < firebaseVisionImageLabels.size(); i++) {
                                     FirebaseVisionImageLabel label = firebaseVisionImageLabels.get(i);

                                     Log.d(TAG, "cloud label: " + label);
                                     if (label.getText() != null) {
                                         //labelsStr.add((label.getText()));

                                         String labels=label.getText();
                                         Float confidence=label.getConfidence();
                                         productList.add(
                                                 new Product("", labels , confidence.toString() +" %" ));
                                     } else {
                                         productList.add(
                                                 new Product("", "No Name " + i, "NO Confidence " + i));
                                     }
                                 }
                                 listener.onSearchCompleted(object, productList);
                             }
                         })
                         .addOnFailureListener(new OnFailureListener() {
                             @Override
                             public void onFailure(@NonNull Exception e) {

                                 List<Product> productList = new ArrayList<>();
                                 for (int i = 0; i < 5; i++) {
                                     productList.add(
                                             new Product( "", "MLkit Fail " + i, "Product subtitle " + i));
                                 }
                                 listener.onSearchCompleted(object, productList);

                             }
                         })
                        ;

                   }
               })
               /*
        .addOnSuccessListener(productRequest -> searchRequestQueue.add(productRequest.setTag(TAG))

        )*/
        .addOnFailureListener(
            e -> {
              Log.e(TAG, "Failed to create product search request!", e);
              // Remove the below dummy code after your own product search backed hooked up.
              List<Product> productList = new ArrayList<>();
              for (int i = 0; i < 3; i++) {
                productList.add(
                    new Product( "", "Search Fail " + i, "Product subtitle " + i));
              }
              listener.onSearchCompleted(object, productList);
            });

  }
  /*
    private void getFromCloud(DetectedObject object) {

        Log.e(TAG, "SEAN:FirebaseVisionImage===>start" );
        searchbitmap=object.getBitmap();
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(searchbitmap);
        imageProcessor = new CloudImageLabelingProcessor();
        imageProcessor.process(searchbitmap,null);
        Log.e(TAG, "SEAN:FirebaseVisionImage===>end" );

    }
*/



  private  JsonObjectRequest createRequest(DetectedObject searchingObject)  {
    byte[] objectImageData = searchingObject.getImageData();

    if (objectImageData == null) {
      //throw new Exception("Failed to get object image data!");
    }

    // Hooks up with your own product search backend here.
    Log.e(TAG, "SEAN:image lenght" +objectImageData.length);
    //inputBitmap=searchingObject.getBitmap();
      List<Product> productList = new ArrayList<>();
      for (int i = 0; i < 10; i++) {
          productList.add(
                  new Product( "", "Product title " + i, "Product subtitle " + i));
      }

      return null;
    //throw new Exception("Hooks up with your own product search backend.");
  }

  public void shutdown() {
    searchRequestQueue.cancelAll(TAG);
    requestCreationExecutor.shutdown();
  }






}
