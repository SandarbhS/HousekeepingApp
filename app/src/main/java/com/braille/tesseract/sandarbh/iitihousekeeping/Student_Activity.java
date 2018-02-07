package com.braille.tesseract.sandarbh.iitihousekeeping;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.scalified.fab.ActionButton;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.braille.tesseract.sandarbh.iitihousekeeping.Login.toolbar;

public class Student_Activity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private View reqView,timeSetter;
    private NumberPicker hour,min,period;
    private AlertDialog.Builder request;
    private AlertDialog retry;
    private RadioGroup radioGroup;
    private EditText fromTime,toTime,remarks;
    private CustomToast info,toast;
    private ActionButton fab;
    private static TextView NAmsg;
    private TextView roomNum;
    public static DrawerLayout drawer;

    private final String ADD_REQUEST = "Add New Request",MIN_TIME = "9 : 00  am",MAX_TIME = "4 : 59  pm",NO_TEXT = "N/A";
    private final int FROM_TIME = 1,TO_TIME= 2,VALID = 3,INVALID = 4,INVALID_DIFFERENCE = 5,UPDATE = 6,WRAP = 7;
    private final long VALID_DIFFERENCE = 1800000;

    private String currentTime,USERNAME;
    private boolean exit = false,RETRY = false;

    private ArrayList<Request> requestsList;
    private RecyclerView recyclerView;
    private RecyclerAdapter rAdapter;
    public static SwipeRefreshLayout refreshLayout;

    private DatabaseReference DB,roomUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student);

        toast = new CustomToast(getBaseContext());
        info = new CustomToast(getBaseContext());
        NAmsg = findViewById(R.id.naMsg);
        NAmsg.setText(R.string.blank_page_msg);
        NAmsg.setVisibility(GONE);

        requestsList = new ArrayList<>();
        if (savedInstanceState != null)
            requestsList = (ArrayList<Request>) savedInstanceState.getSerializable("Requests");

        refreshLayout = findViewById(R.id.refresh);
        refreshLayout.setOnRefreshListener(this);
        refreshLayout.setNestedScrollingEnabled(true);
        refreshLayout.setRefreshing(true);

        initActionBar();
        initDrawer();
        initFAB();
        initRecyclerView();
        initTimeDialog();
        initDatabase();
        initNewRequest();
        retry= Loading();
        getAvailableRequests();
    }

    public void initDatabase(){
        DB = FirebaseDatabase.getInstance().getReference();
        USERNAME = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();

        assert USERNAME != null;
        roomUser = DB.child(USERNAME);

        Log.e("DEBUG_Main","RoomNo: "+USERNAME);
    }

    public void getAvailableRequests(){

        if (isConnectedToNetwork()) {
            roomUser.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.e("DEBUG","Retrieving....");
                    @SuppressWarnings("unchecked")
                    HashMap<String, HashMap> map = (HashMap<String, HashMap>) dataSnapshot.getValue();
                    if (map == null) {
                        refreshLayout.setRefreshing(false);
                        NAmsg.setVisibility(View.VISIBLE);
                    } else {
                        NAmsg.setVisibility(GONE);
                        updateRequestlist(map);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e("DEBUG_Main", "FIREBASE ERROR : " + databaseError.getDetails());
                    CustomToast error = new CustomToast(getBaseContext());
                    error.showToast("Unexpected Error occured! Please try again.");
                }
            });
        }
        else{
            Log.e("DEBUG","NO NETWORK");
            CustomToast noNetwork = new CustomToast(this);
            noNetwork.showToast("No Internet Connection. Please retry!");
            refreshLayout.setRefreshing(false);
            NAmsg.setVisibility(View.VISIBLE);

            if (retry.isShowing()){
                retry.dismiss();
                uploadDataToDatabase();
            }
        }

    }

    public void updateRequestlist(HashMap<String,HashMap> requestsMap){

        requestsList.clear();
        for (Map.Entry<String,HashMap> entry : requestsMap.entrySet()){

            HashMap final_map = entry.getValue();
            String key = entry.getKey();
            Log.e("DEBUG","Key : "+entry.getKey());
            requestsList.add(new Request(final_map,key));
        }
        sortRequestsList();
        if (RETRY){
            RETRY = false;
            uploadDataToDatabase();
        }
        else {
            if (refreshLayout.isRefreshing())
                refreshLayout.setRefreshing(false);
        }
    }

    public void initActionBar(){
        toolbar = findViewById(R.id.actionBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.app_name));
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    private void initDrawer() {
        drawer = findViewById(R.id.drawer);
        DrawerFragment drawerFragment = (DrawerFragment) getSupportFragmentManager().findFragmentById(R.id.drawer_fragment);
        drawerFragment.initDrawer(drawer, toolbar);
    }

    public void initFAB(){
        fab = findViewById(R.id.fab);
        fab.setType(ActionButton.Type.BIG);
        fab.setImageResource(R.drawable.fab_plus_icon);
        //Resources.getSystem().getDisplayMetrics().heightPixels; //(getting screen size)
        fab.setImageSize(35.0f);
        fab.setRippleEffectEnabled(true);

        fab.setButtonColor(getResources().getColor(R.color.fab_appthemeorange_500));
        fab.setButtonColorPressed(getResources().getColor(R.color.fab_appthemeorange_900));

        fab.setShowAnimation(ActionButton.Animations.JUMP_FROM_DOWN);
        fab.setHideAnimation(ActionButton.Animations.JUMP_TO_DOWN);

        fab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                info.setToastPosition(Gravity.END|Gravity.RELATIVE_LAYOUT_DIRECTION,50,520);
                info.showToast(ADD_REQUEST);

                return true;
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fab.playHideAnimation();
                fab.setVisibility(GONE);
                addNewRequest();
            }
        });
    }

    public void initRecyclerView(){

        Collections.reverse(requestsList);

        recyclerView = findViewById(R.id.recyclerView);
        rAdapter = new RecyclerAdapter(Student_Activity.this,requestsList);
        recyclerView.setAdapter(rAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(Student_Activity.this));

        recyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (fab.getVisibility() == GONE){
                    fab.playShowAnimation();
                    fab.setVisibility(View.VISIBLE);
                    exit = false;
                }
                return false;
            }
        });
    }

    public void initTimeDialog(){

        timeSetter = getLayoutInflater().inflate(R.layout.time,(ViewGroup)findViewById(R.id.timeRoot),false);
        final MediaPlayer ticksound = MediaPlayer.create(Student_Activity.this,R.raw.timepicker_tap);

        hour = timeSetter.findViewById(R.id.hour);
        min = timeSetter.findViewById(R.id.min);
        period = timeSetter.findViewById(R.id.period);
        hour.setMinValue(1);
        hour.setMaxValue(12);
        hour.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                timeSetter.setSoundEffectsEnabled(true);
                final MediaPlayer ticksound = MediaPlayer.create(Student_Activity.this,R.raw.timepicker_tap);
                ticksound.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        ticksound.release();
                    }
                });
                ticksound.start();
            }
        });

        min.setMinValue(0);
        min.setMaxValue(59);
        min.setFormatter(new NumberPicker.Formatter() {
            @Override
            public String format(int i) {
                if (i/10==0)
                    return String.format("%02d",i);
                else
                    return String.format("%d",i);
            }
        });
        min.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                timeSetter.setSoundEffectsEnabled(true);
                final MediaPlayer ticksound = MediaPlayer.create(Student_Activity.this,R.raw.timepicker_tap);
                ticksound.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        ticksound.release();
                    }
                });
                ticksound.start();
            }
        });

        String periodValues[] = {"am","pm"};
        period.setDisplayedValues(periodValues);
        period.setMinValue(0);
        period.setMaxValue(1);
        period.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                timeSetter.setSoundEffectsEnabled(true);
                final MediaPlayer ticksound = MediaPlayer.create(Student_Activity.this,R.raw.timepicker_tap);
                ticksound.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        ticksound.release();
                    }
                });
                ticksound.start();
            }
        });

        hour.setWrapSelectorWheel(true);
        min.setWrapSelectorWheel(true);
        period.setWrapSelectorWheel(true);

        hour.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        min.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        period.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
    }

    public void initNewRequest(){

        reqView = getLayoutInflater().inflate(R.layout.request_dialog,(ViewGroup)findViewById(R.id.reqroot),false);
        request = new AlertDialog.Builder(Student_Activity.this);

        radioGroup = reqView.findViewById(R.id.radiogrp);
        radioGroup.check(R.id.any);

        final RelativeLayout timeLayout = reqView.findViewById(R.id.customTimeRoot);
        timeLayout.setVisibility(GONE);
        roomNum = reqView.findViewById(R.id.roomnum);
        roomNum.setText(String.format(getResources().getString(R.string.username),USERNAME));
        remarks = reqView.findViewById(R.id.remarks);

        fromTime = reqView.findViewById(R.id.fromTime);
        fromTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String time = fromTime.getText().toString();
                hour.setValue(Integer.parseInt(time.substring(0,1)));
                min.setValue(Integer.parseInt(time.substring(4,6)));
                if (time.substring(8,10).equals("am"))
                    period.setValue(0);
                else
                    period.setValue(1);
                showTimeDialog(FROM_TIME);
            }
        });

        toTime = reqView.findViewById(R.id.toTime);
        toTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String time = toTime.getText().toString();
                hour.setValue(Integer.parseInt(time.substring(0,1)));
                min.setValue(Integer.parseInt(time.substring(4,6)));
                if (time.substring(8,10).equals("am"))
                    period.setValue(0);
                else
                    period.setValue(1);
                showTimeDialog(TO_TIME);
            }
        });

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
                if(radioGroup.getCheckedRadioButtonId() == R.id.customTime){
                    timeLayout.setVisibility(View.VISIBLE);
                    @SuppressLint("SimpleDateFormat") DateFormat df = new SimpleDateFormat("h : mm  a");
                    currentTime = df.format(Calendar.getInstance().getTime());
                    int hours = new Date().getHours();
                    if (hours>=9 && hours<17)
                        fromTime.setText(currentTime);
                    else
                        fromTime.setText(MIN_TIME);

                    toTime.setText(MAX_TIME);
                }
                else
                    timeLayout.setVisibility(GONE);
            }
        });

        request.setTitle("New Request");
        request.setPositiveButton("ADD REQUEST",null);
        request.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                fab.playShowAnimation();
                fab.setVisibility(View.VISIBLE);
            }
        });
        request.setCancelable(false);

    }

    public void addNewRequest(){
        if (reqView.getParent()!=null){
            ((ViewGroup)reqView.getParent()).removeView(reqView);
        }
        request.setView(reqView);
        final AlertDialog newRequest = request.create();

        newRequest.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialogInterface) {

                Button reqButton = newRequest.getButton(AlertDialog.BUTTON_POSITIVE);
                reqButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        CustomToast invalidTimeWarning = new CustomToast(Student_Activity.this);
                        switch (enteredTimeValidity()){

                            case VALID : Log.e("correct","Done!");
                                String Time,rem;
                                if (radioGroup.getCheckedRadioButtonId() == R.id.customTime)
                                   Time  = fromTime.getText().toString()+" to "+toTime.getText().toString();
                                else
                                    Time = "ANY";

                                rem = remarks.getText().toString();
                                if (rem.equals(""))
                                    rem = NO_TEXT;

                                String reqKey = "Request "+(requestsList.size()+1);
                                final Request newRequest = new Request(reqKey,USERNAME,Time,rem,false,false,R.color.PendingRequest,new Date().getTime());
                                requestsList.add(newRequest);
                                sortRequestsList();
                                rAdapter.notifyDataSetChanged();

                                if (isConnectedToNetwork()) {
                                    roomUser.child(newRequest.Key).setValue(newRequest).addOnCompleteListener(Student_Activity.this, new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (!task.isSuccessful()) {
                                                CustomToast noNetwork = new CustomToast(Student_Activity.this);
                                                noNetwork.showToast("Couldn't upload request.");
                                                requestsList.remove(0);
                                                rAdapter.notifyItemRangeChanged(0, requestsList.size());
                                            }
                                        }
                                    });
                                }
                                else{
                                    CustomToast noNetwork = new CustomToast(Student_Activity.this);
                                    noNetwork.showToast("No Internet Connection. Please retry!");
                                    requestsList.remove(0);
                                    rAdapter.notifyItemRangeChanged(0, requestsList.size());
                                }
                                //rAdapter.notifyItemInserted(0);
                                if (NAmsg.getVisibility() == View.VISIBLE)
                                    NAmsg.setVisibility(View.INVISIBLE);

                                dialogInterface.dismiss();
                                fab.playShowAnimation();
                                fab.setVisibility(View.VISIBLE);
                                break;

                            case INVALID_DIFFERENCE :invalidTimeWarning.showToast("The slot must be of atleast half an hour!");
                                break;

                            case INVALID :invalidTimeWarning.showToast("Please choose a valid time slot!");
                                break;
                        }

                    }
                });
            }
        });
        newRequest.show();
    }

    public void showTimeDialog(final int code){

        AlertDialog.Builder timeBuilder = new AlertDialog.Builder(Student_Activity.this);
        timeBuilder.setTitle("Set Time");

        if (timeSetter.getParent()!=null){
            ((ViewGroup)timeSetter.getParent()).removeView(timeSetter);
        }
        timeBuilder.setView(timeSetter);
        timeBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                switch (code){
                    case FROM_TIME : if (getTime()!=null)
                                        fromTime.setText(getTime());
                        break;

                    case TO_TIME : if (getTime()!=null)
                                        toTime.setText(getTime());
                        break;
                }

            }
        });

        timeBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        AlertDialog timeDialog = timeBuilder.create();
        timeDialog.show();

    }

    public String getTime(){

        String selectedTime = hour.getValue()+" : "+String.format("%02d",min.getValue())+"  "+
                period.getDisplayedValues()[period.getValue()];
        try {
            Date d = new SimpleDateFormat("h : mm  a").parse(selectedTime);
            int hours = d.getHours();

            if (hours<9 || hours>=17){
                CustomToast warning = new CustomToast(this);
                warning.showToast("Time limit exceeded.");
                return null;
            }

            else
                return selectedTime;

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int enteredTimeValidity(){

        String from = fromTime.getText().toString();
        String to = toTime.getText().toString();
        long t1,t2;
        try {
            t1 = new SimpleDateFormat("h : mm  a").parse(from).getTime();
            t2 = new SimpleDateFormat("h : mm  a").parse(to).getTime();

            Log.e("time",t1+" "+t2);

            if (t1<t2 && t2-t1>=VALID_DIFFERENCE)
                return VALID;

            else if (t1<t2 && t2-t1<VALID_DIFFERENCE)
                return INVALID_DIFFERENCE;

            else
                return INVALID;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static void toggleMessageVisibility(){
            NAmsg.setVisibility(View.VISIBLE);
    }

    public void sortRequestsList(){
        Collections.sort(requestsList, new Comparator<Request>() {
            @Override
            public int compare(Request request, Request t1) {
                return (int)(long)(t1.uptime-request.uptime);
            }
        });
    }

    public void uploadDataToDatabase(){

        if (isConnectedToNetwork()) {
            Map<String, Request> uploadMap = new HashMap<>();

                for (Request tmp : requestsList) {
                    uploadMap.put(tmp.Key, tmp);
                }
                roomUser.setValue(uploadMap);
                //roomUser.updateChildren(uploadMap);

                if (retry.isShowing()) {
                    retry.dismiss();
                    CustomToast done = new CustomToast(this);
                    done.showToast("Done!");
                }
                finish();

        }
        else {
            Log.e("DEBUG","NO NETWORK");
            if (retry.isShowing())
                retry.dismiss();

            AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setTitle("Warning")
                    .setMessage("No internet connection was detected. If you exit now all the changes made by you may be lost.")
                    .setPositiveButton("EXIT ANYWAY", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                        }
                    })
                    .setNegativeButton("RETRY", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            showRetryDialog();
                            RETRY = true;
                            getAvailableRequests();
                            //uploadDataToDatabase();
                        }
                    })
                    .setCancelable(false);
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    public boolean isConnectedToNetwork(){

        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if (cm.getActiveNetworkInfo() != null) {
            Log.e("DEBUG", "OK");
            return true;
        }
        else
            return false;
    }

    private AlertDialog Loading(){

        String msg = "Reconnecting...";
        RelativeLayout dialogLayout = new RelativeLayout(this);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        dialogLayout.setLayoutParams(params);

        View divider = new View(dialogLayout.getContext());
        RelativeLayout.LayoutParams dividerParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,1);
        dividerParams.topMargin = 20;
        dividerParams.leftMargin = 20;
        dividerParams.rightMargin = 20;
        dividerParams.bottomMargin = 20;
        divider.setLayoutParams(dividerParams);

        divider.setBackgroundResource(R.color.Background);
        divider.setId(R.id.uptime);

        ProgressBar loadingBar = new ProgressBar(dialogLayout.getContext());
        RelativeLayout.LayoutParams barParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        barParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        barParams.leftMargin = 70;
        barParams.topMargin = 50;
        barParams.bottomMargin = 50;
        loadingBar.setIndeterminate(true);
        loadingBar.setLayoutParams(barParams);
        loadingBar.setId(R.id.refresh);

        TextView loadingMessage = new TextView(dialogLayout.getContext());
        RelativeLayout.LayoutParams msgParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        msgParams.addRule(RelativeLayout.CENTER_IN_PARENT);

        loadingMessage.setText(msg);
        loadingMessage.setLayoutParams(msgParams);
        loadingMessage.setTextSize(18);

        dialogLayout.addView(divider);
        dialogLayout.addView(loadingBar);
        dialogLayout.addView(loadingMessage);
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setView(dialogLayout)
                .setTitle("Loading")
                .setCancelable(false);

        return builder.create();
    }

    private void showRetryDialog(){
        retry.show();
    }

    @Override
    public void onBackPressed() {

        if (drawer.isDrawerOpen(Gravity.START)) {
            Log.e("DEBUG", "DRAWER OPEN");
            drawer.closeDrawer(Gravity.START);
        }

        if (exit){
            Log.e("dg",""+toast.isVisible()+" "+View.VISIBLE);
            //FirebaseAuth.getInstance().signOut();
            uploadDataToDatabase();
        }
        else{

            //toast.setToastPosition(Gravity.CENTER_HORIZONTAL,0,520);
            fab.playHideAnimation();
            fab.setVisibility(GONE);
            toast.showToast(getResources().getString(R.string.BACK_MSG));
            exit = true;

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    exit = false;
                    if (fab.getVisibility() == GONE){
                        fab.playShowAnimation();
                        fab.setVisibility(View.VISIBLE);
                    }
                }
            },3*1000);
        }
    }

    @Override
    public void onRefresh() {
        Log.w("R","Refreshing...");
        if (isConnectedToNetwork()){
            getAvailableRequests();
        }
        else {
            refreshLayout.setRefreshing(false);
            CustomToast noNetwork = new CustomToast(this);
            noNetwork.showToast("No Internet Connection. Please retry!");
        }
        rAdapter.notifyItemRangeChanged(0,requestsList.size());
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);

        Log.e("DEBUG","DATA SAVED!");
        outState.putSerializable("Requests",requestsList);
    }

}
