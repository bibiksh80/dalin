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
public class ReservationmainActivity extends AppCompatActivity {

  private static final String TAG = "ReservmainActivity";

  private  TextView titleView;
  //
;
  private ImageButton Item7Button;
  private ImageButton Item8Button;


  private  TextView item7title;
  private  TextView item8title;






  @Override
  protected void onCreate(@Nullable Bundle bundle) {
    super.onCreate(bundle);



    AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    setContentView(R.layout.activity_main_reservation);


    titleView =(TextView)findViewById(R.id.title1);


    Item7Button = (ImageButton) findViewById(R.id.btnitem7);
    Item8Button = (ImageButton) findViewById(R.id.btnitem8);


    item7title = (TextView)findViewById(R.id.itemtitle7);
    item8title = (TextView)findViewById(R.id.itemtitle8);



    Item7Button.setOnClickListener(new Button.OnClickListener() {
      @Override
      public void onClick(View view) {
        // TODO : click event
        Activity activity = ReservationmainActivity.this;
        //activity.startActivity(new Intent(activity, com.photatos.dalin.mlkit.md.java.QRActivity.class));
        activity.startActivity(new Intent(activity, com.photatos.dalin.mlkit.md.java.ReservationListActivity.class));
      }
    });
    Item8Button.setOnClickListener(new Button.OnClickListener() {
      @Override
      public void onClick(View view) {
        // TODO : click event
        Activity activity = ReservationmainActivity.this;
       // activity.startActivity(new Intent(activity, com.photatos.dalin.mlkit.ghost.view.LoginActivity.class));
        activity.startActivity(new Intent(activity, com.photatos.dalin.mlkit.md.java.ReservationActivity.class));
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
