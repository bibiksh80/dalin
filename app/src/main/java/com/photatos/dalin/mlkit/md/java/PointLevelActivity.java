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

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.photatos.dalin.mlkit.R;

//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;


/** Entry activity to select the detection mode. */
public class PointLevelActivity extends AppCompatActivity {


  private  TextView titleView;
  private ImageView pointimage;
  private  TextView point;


  private ImageButton PointPlusLevelButton;

  private ImageButton PointMinusLevelButton;

  //  private  TextView pointplus;
  private ImageButton PostPointButton;

  private  TextView postpointtitle;


  private  TextView postpoint;


  private  TextView pointlevel;

  private Integer PointValue;
  private Integer PostValue;

  private Integer PointLevelValue;


  DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
  DatabaseReference conditionRef = mRootRef.child("text");

  DatabaseReference conditionRefpost = mRootRef.child("post");

  DatabaseReference conditionRefpointlevel = mRootRef.child("pointlevel");

  @Override
  protected void onCreate(@Nullable Bundle bundle) {
    super.onCreate(bundle);



    AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    setContentView(R.layout.activity_main_point_level);

    addListenerOnButton();

  }

  @Override
  protected void onResume() {
    super.onResume();
    if (!Utils.allPermissionsGranted(this)) {
      Utils.requestRuntimePermissions(this);
    }
  }

  public void addListenerOnButton() {

    //
    titleView =(TextView)findViewById(R.id.title1);
    pointimage=(ImageView)findViewById(R.id.imageView1);
    point =(TextView)findViewById(R.id.point);


    PointPlusLevelButton = (ImageButton) findViewById(R.id.btnPointlevelplus);

    PointMinusLevelButton = (ImageButton) findViewById(R.id.btnPointlevelminus);


    //pointplus =(TextView)findViewById(R.id.point);
    PostPointButton = (ImageButton) findViewById(R.id.btnPostit);

    postpointtitle =(TextView)findViewById(R.id.posttitle);
    postpoint =(TextView)findViewById(R.id.postcount);


    pointlevel =(TextView)findViewById(R.id.pointlevelcount);


    pointimage.setImageResource(R.drawable.mypoint);
    //point.setText("100");


    conditionRef.addValueEventListener(new ValueEventListener() {
      @Override
      public void onDataChange(DataSnapshot dataSnapshot) {
        String text = dataSnapshot.getValue(String.class);
        if (text==null)
        {
          PointValue =100;
        } else
          {
          PointValue = Integer.parseInt(text);
        }
        point.setText(text);
      }

      @Override
      public void onCancelled(DatabaseError databaseError) {

      }
    });
    conditionRefpost.addValueEventListener(new ValueEventListener() {
      @Override
      public void onDataChange(DataSnapshot dataSnapshot) {
        String posttext = dataSnapshot.getValue(String.class);
        if (posttext==null)
        {
          PostValue =0;
        } else
        {
          PostValue = Integer.parseInt(posttext);
          //postpoint.setText(PostValue);
        }
        postpoint.setText(posttext);
      }

      @Override
      public void onCancelled(DatabaseError databaseError) {

      }
    });


    conditionRefpointlevel.addValueEventListener(new ValueEventListener() {
      @Override
      public void onDataChange(DataSnapshot dataSnapshot) {
        String pointleveltext = dataSnapshot.getValue(String.class);
        if (pointleveltext==null)
        {
          PointLevelValue =0;
        } else
        {
          PointLevelValue = Integer.parseInt(pointleveltext);
          //postpoint.setText(PostValue);
        }
        //postpoint.setText(posttext);
        pointlevel.setText(pointleveltext);
      }

      @Override
      public void onCancelled(DatabaseError databaseError) {

      }
    });

    PointPlusLevelButton.setOnClickListener(new Button.OnClickListener() {
      @Override
      public void onClick(View view) {
        // TODO : click event
        PointLevelValue=PointLevelValue+1;
        String text=Integer.toString(PointLevelValue);


        conditionRefpointlevel.setValue(text);


      }
    });

    PointMinusLevelButton.setOnClickListener(new Button.OnClickListener() {
      @Override
      public void onClick(View view) {
        // TODO : click event
        PointLevelValue=PointLevelValue-1;
        String text=Integer.toString(PointLevelValue);

        conditionRefpointlevel.setValue(text);

      }
    });

  }



}
