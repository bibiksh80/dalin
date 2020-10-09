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

import android.app.ListActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.photatos.dalin.mlkit.R;

import java.util.Calendar;
import com.photatos.dalin.mlkit.md.java.model.FriendlyMessage;
import com.photatos.dalin.mlkit.md.java.model.User;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;



//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;


/** Entry activity to select the detection mode. */
public class ReservationListActivity extends AppCompatActivity {

  private static final String TAG = "ReservationList";

  private  TextView titleView;
  private ImageView pointimage;

  private EditText mMessageEditText;

  private  TextView reservationdatetext;
  private String mEmail = "Anonymous";
  private String mName = "bibi";

  private static final String CHILD_USERS = "chat-users";
  private static final String CHILD_MESSAGES = "chat";
  private static final String UID = "id-12345";
  private DatabaseReference mRootRefMsg, mUsersRef, mMessageRef;
  private String mUsername;

  private ValueEventListener mValueEventListener;

  private String ReservationDate;

  DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
  DatabaseReference conditionRev = mRootRef.child("reservation");

  public static final String MESSAGES_CHILD = "chat";




  private ArrayAdapter<String> adapter;



  @Override
  protected void onCreate(@Nullable Bundle bundle) {
    super.onCreate(bundle);



    AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    setContentView(R.layout.activity_main_reservlist);


      addListenerOnButton();

    adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);

//    adapter.add("item1");
//    adapter.add("item2");
//    adapter.add("item3");

    ListView listView = (ListView) findViewById(R.id.listview);

    listView.setAdapter(adapter);

//    addListenerOnButton();

    MakeRealtimeInit();

    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ListView listView = (ListView) parent;
        // TODO 아이템 클릭시에 구현할 내용은 여기에.
        String item = (String) listView.getItemAtPosition(position);
//        Toast.makeText(ListViewSampleActivity.this, item, Toast.LENGTH_LONG).show();
      }
    });

    // 아이템을 [선택]시의 이벤트 리스너를 등록
    listView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parent, View view,
                                 int position, long id) {
        ListView listView = (ListView) parent;
        // TODO 아이템 선택시에 구현할 내용은 여기에.
        String item = (String) listView.getSelectedItem();
//        Toast.makeText(ListViewSampleActivity.this, item, Toast.LENGTH_LONG).show();
      }
      @Override
      public void onNothingSelected(AdapterView<?> parent) {
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


  public void MakeRealtimeInit(){

    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    mRootRefMsg = firebaseDatabase.getReference();
    mUsersRef = mRootRefMsg.child(CHILD_USERS);
    mMessageRef = mRootRefMsg.child(CHILD_MESSAGES);

    // default user name


    mUsersRef.child(UID).setValue(mUsername);

    mValueEventListener = new ValueEventListener() {
      @Override
      public void onDataChange(DataSnapshot dataSnapshot) {
//                mDialog.dismiss();
        mUsername = dataSnapshot.child(CHILD_USERS).child(UID).getValue(String.class);

//                mTextView.setText(getString(username, mUsername));

        if (TextUtils.isEmpty(mUsername)) {
//                    mButtonPush.setEnabled(false);
//                    mButtonUpdateChildren.setEnabled(false);
        } else {
//                    mButtonPush.setEnabled(true);
//                    mButtonUpdateChildren.setEnabled(true);
        }
        Iterable<DataSnapshot> children = dataSnapshot.child(CHILD_MESSAGES).getChildren();
        while(children.iterator().hasNext()){
          String key= children.iterator().next().getKey();
          FriendlyMessage friendlyMessage = dataSnapshot.child(CHILD_MESSAGES).child(key).getValue(FriendlyMessage.class);

          long now = System.currentTimeMillis();
          Log.d(TAG, "now: " + now);
          Calendar calendar = Calendar.getInstance();
          calendar.setTimeInMillis(now);

          int mYear = calendar.get(Calendar.YEAR);
          int mMonth = calendar.get(Calendar.MONTH);
          int mDay = calendar.get(Calendar.DAY_OF_MONTH);

          Log.d(TAG, "friendlyMessage.getText(): " + friendlyMessage.getText());
          long past = now - (60 * 60 * 24 * 45 * 1000L);
          Log.d(TAG, "past : " + past);
          String x = DateUtils.getRelativeTimeSpanString(past, now, DateUtils.MINUTE_IN_MILLIS).toString();
          //boolean k=DateUtils.isToday()
          Log.d(TAG, "x : " + x);

//                    mTextView.append("username: " + friendlyMessage.getUsername() + " | ");
//                    mTextView.append("text: " + friendlyMessage.getText() + " (" + x + ")" + "\n");
            if (checktoday( now,friendlyMessage.getText())){
              String madetxt = makekorean(friendlyMessage.getText(), true);
              adapter.add(madetxt);
          }else
          {
            String madetxt = makekorean(friendlyMessage.getText(), false);
            adapter.add(madetxt);
          }

        }
      }

      @Override
      public void onCancelled(DatabaseError databaseError) {
//                mDialog.dismiss();
//                mTextView.setText(getString(R.string.fail_read, databaseError.getMessage()));
      }
    };
    mRootRefMsg.addValueEventListener(mValueEventListener);

  }
  public boolean checktoday(long now,String resevation){

    long now2 = System.currentTimeMillis();
    Log.d(TAG, "now2: " + now2);
    Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(now2);

    int mYear = calendar.get(Calendar.YEAR);
    int mMonth = calendar.get(Calendar.MONTH);
    int mDay = calendar.get(Calendar.DAY_OF_MONTH);
    Log.d(TAG, "mYear: " + mYear);
    Log.d(TAG, "mMonth: " + mMonth+1);
    Log.d(TAG, "mDay: " + mDay);

    String year=resevation.substring(0,4);
    String month=resevation.substring(5,7);
    String day=resevation.substring(8,10);
    String time=resevation.substring(11,16);
    Log.d(TAG, "year: " + year);
    Log.d(TAG, "month: " + month);
    Log.d(TAG, "Integer.parseInt(month): " + Integer.parseInt(month));
    Log.d(TAG, "day: " + day);
    Log.d(TAG, "Integer.parseInt(day): " + Integer.parseInt(day));
    Log.d(TAG, "time: " + time);
    if (mYear> (Integer.parseInt(year)) ){
      Log.d(TAG, "myear: " + false);
       if ( (mMonth+1>(Integer.parseInt(month))) ){
          if ( mDay>=(Integer.parseInt(day))){
            return false;
          }else{
            return true;
          }
       }else{
         if ( (mMonth+1==(Integer.parseInt(month))) ){
           if ( mDay>(Integer.parseInt(day))){
             return false;
           } else
           {
             return true;
           }

         }else{
           return true;
         }


       }
    }else{
      if (mYear== (Integer.parseInt(year))){
        if ( (mMonth+1>(Integer.parseInt(month))) ){
           return false;
        }else{
           return true;
        }
      }else{
        return true;
      }

    }
    //2019:10:02:18:54

  }

    public String makekorean(String mTxt,boolean mEnd) {
      String year=mTxt.substring(0,4);
      String month=mTxt.substring(5,7);
      String day=mTxt.substring(8,10);
      String time=mTxt.substring(11,16);
      String blocks=mTxt.substring(17,18);
      String display="";
      Log.d(TAG, "year : " + year);
      Log.d(TAG, "month : " + month);
      Log.d(TAG, "day : " + day);
      Log.d(TAG, "time : " + time);
      Log.d(TAG, "blocks : " + blocks);
      if (mEnd){
        display = year + " 년 " + month + " 월 " + day + " 일 " + time + " " + blocks + " 개" + " 예약 되었습니다.";
      } else {
        display = year + " 년 " + month + " 월 " + day + " 일 " + time + " " + blocks + " 개" + " 예약이  끝났습니다.";
      }

      return display;
    }


  public void addListenerOnButton() {

    //
    titleView =(TextView)findViewById(R.id.title1);
    pointimage=(ImageView)findViewById(R.id.imageView1);

    //reservationdatetext =(TextView)findViewById(R.id.reservationid);



    conditionRev.addValueEventListener(new ValueEventListener() {
      @Override
      public void onDataChange(DataSnapshot dataSnapshot) {
//        ReservData Reservdata = dataSnapshot.getValue(ReservData.class);
        String text = dataSnapshot.getValue(String.class);
        Log.d(TAG, "Reservdata : " + text);

        String year=text.substring(0,4);
        String month=text.substring(5,7);
        String day=text.substring(8,10);
        String time=text.substring(11,16);
        String blocks=text.substring(17,18);

        String display="";
        Log.d(TAG, "year : " + year);
        Log.d(TAG, "month : " + month);
        Log.d(TAG, "day : " + day);
        Log.d(TAG, "time : " + time);
        Log.d(TAG, "blocks : " + blocks);
        display=year+" 년 "+month+" 월 "+day+" 일 "+time+" "+blocks+" 개"+" 예약 되었습니다.";


//        ReservationDate = text;
//        display=text+"예약 되었습니다";
        //reservationdatetext.setText(display);

        //reservationdatetext.setText(Reservdata.getMessage());
      }

      @Override
      public void onCancelled(DatabaseError databaseError) {

      }
    });


  }




}
