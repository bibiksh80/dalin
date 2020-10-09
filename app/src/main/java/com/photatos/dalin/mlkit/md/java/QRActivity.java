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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.photatos.dalin.mlkit.R;
import com.photatos.dalin.mlkit.ghost.view.LoginActivity;

/** Entry activity to select the detection mode. */
public class QRActivity extends AppCompatActivity {

  private static final String TAG = "Main2Activity";

  private  TextView titleView;
  //
  private ImageButton Item1_1Button;
  private ImageButton Item1_2Button;

  private  TextView item1_1title;
  private  TextView item1_2title;



  @Override
  protected void onCreate(@Nullable Bundle bundle) {
    super.onCreate(bundle);



    AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    setContentView(R.layout.activity_main2_qr);


    titleView =(TextView)findViewById(R.id.title1);

    Item1_1Button = (ImageButton) findViewById(R.id.btnitem1_1);
    Item1_2Button = (ImageButton) findViewById(R.id.btnitem1_2);


    item1_1title = (TextView)findViewById(R.id.itemtitle1_1);
    item1_2title = (TextView)findViewById(R.id.itemtitle1_2);

    Item1_1Button.setOnClickListener(new Button.OnClickListener() {
      @Override
      public void onClick(View view) {
        // TODO : click event
        Activity activity = QRActivity.this;
        activity.startActivity(new Intent(activity, com.photatos.dalin.mlkit.md.java.LiveBarcodeScanningActivity.class));
      }
    });
    Item1_2Button.setOnClickListener(new Button.OnClickListener() {
      @Override
      public void onClick(View view) {
        // TODO : click event
        Activity activity = QRActivity.this;
        activity.startActivity(new Intent(activity, com.photatos.dalin.mlkit.md.java.LiveObjectCloudDetectionActivity.class));
      }
    });


  }

  @Override
  protected void onResume() {
    super.onResume();
    if (!Utils.allPermissionsGranted(this)) {
      Utils.requestRuntimePermissions(this);
    }
  }




}
