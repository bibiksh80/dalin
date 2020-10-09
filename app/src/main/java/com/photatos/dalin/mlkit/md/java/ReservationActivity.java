package com.photatos.dalin.mlkit.md.java;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;


//import android.support.annotation.NonNull;
//import android.support.v7.app.AlertDialog;
//import android.support.v7.app.AppCompatActivity;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;


import com.photatos.dalin.mlkit.md.java.model.FriendlyMessage;
import com.photatos.dalin.mlkit.md.java.model.User;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;


import com.photatos.dalin.mlkit.R;

public class ReservationActivity extends AppCompatActivity {

    private CalendarView calendar;
    private TimePicker timePicker;
    private EditText dateET;
    private EditText timeET;
    private EditText numberofPeopleET;
    private Button addBT;
    private Button subBT;
    private Button resBT;
    private int numberofPeople;

    private String strDate;
    private String strSaveDate;

    public Calendar c = Calendar.getInstance();
    DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    DatabaseReference conditionRev = mRootRef.child("reservation");


    private static final String CHILD_USERS = "chat-users";
    private static final String CHILD_MESSAGES = "chat";
    private static final String UID = "id-12345";
    private DatabaseReference mRootRefMsg, mUsersRef, mMessageRef;
    private String mUsername="bibi";

    private ValueEventListener mValueEventListener;



//    private EditText mMessageEditText;
    private String mEmail = "Anonymous";
    private String mName = "bibi";
    private String mMyEmail = "bibiksh78@gmail.com";

    private String ReservValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reservation_activity_main);

        calendar = findViewById(R.id.calendarView);
        timePicker = findViewById(R.id.timePicker);
        dateET = findViewById(R.id.editText1);
        timeET = findViewById(R.id.editText2);
        numberofPeopleET = findViewById(R.id.editText);
        addBT = findViewById(R.id.Addbutton);
        subBT = findViewById(R.id.Subbutton);
        resBT = findViewById(R.id.Reservebutton);
        numberofPeople = 0;

        calendar.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView calendarView, int i, int i1, int i2) {
                //strDate = String.format(("%d년 %d월 %d일"), i, i1, i2);
                strDate = String.format(("%d년 %d월 %d일"), i, i1+1, i2);
                strSaveDate= String.format(("%d:%02d:%02d"), i, i1+1, i2);
                dateET.setText(String.format(("%d : %d : %d"), i, i1+1, i2));
            }
        });

        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker timePicker, int i, int i1) {
                timeET.setText(String.format(("%d:%d"), i, i1));
            }
        });

        addBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                numberofPeople++;
                numberofPeopleET.setText(numberofPeople + " 개");
            }
        });

        subBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (numberofPeople == 0) numberofPeople = 0;
                else {
                    numberofPeople--;
                    numberofPeopleET.setText(numberofPeople + " 개");
                }
            }
        });

        resBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(dateET.getText().toString().equals("") || dateET.getText() == null ||
                        timeET.getText().toString().equals("") || timeET.getText() == null ||
                        numberofPeopleET.getText().toString().equals("") || numberofPeopleET.getText() == null) Toast.makeText(ReservationActivity.this, "양식을 채워주세요.", Toast.LENGTH_SHORT).show();
                else open(view);





            }
        });
        //Database listener
        addListenerOnButton();

        MakeRealtimeInit();



    }
    public void addListenerOnButton() {

        conditionRev.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String text = dataSnapshot.getValue(String.class);
                if (text==null)
                {
                    ReservValue ="2019-09-27 00:00";
                } else
                {
                    ReservValue = text;
                }
                //point.setText(text);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


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
                    long past = now - (60 * 60 * 24 * 45 * 1000L);
                    String x = DateUtils.getRelativeTimeSpanString(past, now, DateUtils.MINUTE_IN_MILLIS).toString();

//                    mTextView.append("username: " + friendlyMessage.getUsername() + " | ");
//                    mTextView.append("text: " + friendlyMessage.getText() + " (" + x + ")" + "\n");
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

    public void MakeRealtimeDB(String mtxt){

        FriendlyMessage friendlyMessage = new FriendlyMessage(mtxt, mUsername);
        mMessageRef.push().setValue(friendlyMessage);

    }


    public void open(View view) {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        if(android.os.Build.VERSION.SDK_INT < 23){

            String mHour=String.format(("%02d:%02d"), timePicker.getCurrentHour(), timePicker.getCurrentMinute());
            alertDialogBuilder.setMessage(strDate + " 시간" + mHour + "에 예약하시겠습니까?");
//            alertDialogBuilder.setMessage(strDate + " 시간" + timePicker.getCurrentHour() + ":" + timePicker.getCurrentMinute() + "에 예약하시겠습니까?");
        }else
        {
            String mHour=String.format(("%02d:%02d"), timePicker.getHour(), timePicker.getMinute());
            alertDialogBuilder.setMessage(strDate + " 시간" + mHour + "에 예약하시겠습니까?");
//            alertDialogBuilder.setMessage(strDate + " 시간" + timePicker.getHour() + ":" + timePicker.getMinute() + "에 예약하시겠습니까?");
        }
        ///reservation
        if(android.os.Build.VERSION.SDK_INT < 23){
            String mHour=String.format(("%02d:%02d"), timePicker.getCurrentHour(), timePicker.getCurrentMinute());
            String date=strSaveDate+":" +mHour+":"+ Integer.toString(numberofPeople);
            //String date=strSaveDate+":" +timePicker.getCurrentHour() + ":" + timePicker.getCurrentMinute()+":"+ Integer.toString(numberofPeople);
//            ReservData rData = new ReservData("reservation", date);
//            conditionRev.push().setValue(rData);
            conditionRev.setValue(date);
            MakeRealtimeDB(date);

            Activity activity = ReservationActivity.this;
            activity.startActivity(new Intent(activity, com.photatos.dalin.mlkit.md.java.ReservationListActivity.class));


        } else{
            String mHour=String.format(("%02d:%02d"), timePicker.getHour(), timePicker.getMinute());
            String date=strSaveDate+":" +mHour+":"+ Integer.toString(numberofPeople);
            //String date=strSaveDate+":" +timePicker.getHour() + ":" + timePicker.getMinute()+":"+ Integer.toString(numberofPeople);
//            ReservData rData = new ReservData("reservation", date);
//            conditionRev.push().setValue(rData);
            conditionRev.setValue(date);

            MakeRealtimeDB(date);

            Activity activity = ReservationActivity.this;
            activity.startActivity(new Intent(activity, com.photatos.dalin.mlkit.md.java.ReservationListActivity.class));

        }
        alertDialogBuilder.setPositiveButton("예약", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(ReservationActivity.this, "예약이 완료되었습니다.", Toast.LENGTH_LONG).show();
            }
        });
        alertDialogBuilder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(ReservationActivity.this,"예약이 취소되었습니다.", Toast.LENGTH_LONG).show();
                finish();
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.clear:
                calendar.setDate(c.getTimeInMillis());
                if(android.os.Build.VERSION.SDK_INT < 23){
                    timePicker.setCurrentHour(c.get(Calendar.HOUR));
                    timePicker.setCurrentMinute(c.get(Calendar.MINUTE));
                }else
                {
                    timePicker.setHour(c.get(Calendar.HOUR));
                    timePicker.setMinute(c.get(Calendar.MINUTE));
                }

                dateET.setText("");
                timeET.setText("");
                numberofPeopleET.setText("");
                numberofPeople = 0;
                return true;
            case R.id.white:
                calendar.setBackgroundColor(Color.WHITE);
                return true;
            case R.id.red:
                calendar.setBackgroundColor(Color.RED);
                return true;
            case R.id.green:
                calendar.setBackgroundColor(Color.GREEN);
                return true;
            case R.id.blue:
                calendar.setBackgroundColor(Color.BLUE);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }




}

