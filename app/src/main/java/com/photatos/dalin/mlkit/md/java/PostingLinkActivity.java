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
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.photatos.dalin.mlkit.R;
import com.photatos.dalin.mlkit.ghost.view.LoginActivity;
import com.photatos.dalin.mlkit.md.java.LiveObjectCloudDetectionActivity;

//import com.google.firebase.database.DataSnapshot;
//import com.google.firebase.database.DatabaseError;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.database.ValueEventListener;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//import android.support.design.widget.BottomNavigationView;
//import android.support.annotation.NonNull;


/** Entry activity to select the detection mode. */
public class PostingLinkActivity extends AppCompatActivity {

  private static final String TAG = "PostingLinkActivity";

  private TextView mTextMessage;
  private ImageView pointimage;
    private TextView mTextbody;

  private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
          = new BottomNavigationView.OnNavigationItemSelectedListener() {

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
      switch (item.getItemId()) {

        case R.id.navigation_ok:
          mTextMessage.setText(R.string.title_OK);
          Activity activity1 = PostingLinkActivity.this;
          activity1.startActivity(new Intent(activity1, com.photatos.dalin.mlkit.ghost.view.LoginActivity.class));
          return true;
          /*
        case R.id.navigation_cancel:
          mTextMessage.setText(R.string.title_BACK);
          return true;
          */
        case R.id.navigation_cancel:
          mTextMessage.setText(R.string.title_CANCEL);
          Activity activity2 = PostingLinkActivity.this;
          activity2.startActivity(new Intent(activity2, com.photatos.dalin.mlkit.md.java.LiveObjectCloudDetectionActivity.class));
          return true;
      }
      return false;
    }
  };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.d(TAG, "onCreate: ");
    setContentView(R.layout.activity_main_postinglink);
    BottomNavigationView navView = findViewById(R.id.nav_view);
    mTextMessage = findViewById(R.id.message);
    pointimage=(ImageView)findViewById(R.id.imageView3);
    mTextbody = findViewById(R.id.title_postingdetail);
    pointimage.setImageResource(R.drawable.mypoint);

    navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
  }

}
