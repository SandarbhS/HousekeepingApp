package com.braille.tesseract.sandarbh.iitihousekeeping;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.support.design.widget.TabLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class Login extends AppCompatActivity {

    public static Toolbar actionBar;
    private static EditText roomno,username,stupwd,suppwd;
    private static Button stulogin,suplogin;
    private static AlertDialog dialog;

    private boolean exit = false;
    private static final int STUDENT_LOGIN = 1,SUPERVISOR_LOGIN = 2;
    private static Activity thisActivity;

    private TabLayout chooseLogin;
    private ViewPager pager;

    private static FirebaseAuth authenticate;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        thisActivity = this;

        actionBar = findViewById(R.id.actionBar);
        setSupportActionBar(actionBar);
        getSupportActionBar().setTitle("Login");

        chooseLogin = findViewById(R.id.chooseLogin);
        pager = findViewById(R.id.pager);

        pager.setAdapter(new PagerAdapter(getSupportFragmentManager()));
        chooseLogin.setupWithViewPager(pager);
        pager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(chooseLogin));

        authenticate = FirebaseAuth.getInstance();
        dialog = Loading();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent redirect;
        user = authenticate.getCurrentUser();
        if (user != null) {
            if (user.getDisplayName().equals("SUPERVISOR"))
                redirect = new Intent(thisActivity, Supervisor_Activity.class);

            else
                redirect = new Intent(thisActivity, Student_Activity.class);
            //redirect.putExtra("User",user.getDisplayName());
            Log.e("DEBUG_Login",""+user.getDisplayName());
            startActivity(redirect);
            thisActivity.finish();
        }
    }

    public class PagerAdapter extends FragmentStatePagerAdapter{

        public PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            StudentLoginFragment loginFragment = StudentLoginFragment.newFragment(position);
            return loginFragment;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {

            switch (position){

                case 0 : return "Student";

                case 1 : return "Supervisor";
            }
            return "Tab : "+position;
        }
    }

    public static class StudentLoginFragment extends Fragment{

        public StudentLoginFragment(){

        }

        public static StudentLoginFragment newFragment(int position){

            StudentLoginFragment frag = new StudentLoginFragment();
            Bundle arguments = new Bundle();
            arguments.putInt("Code",position);

            frag.setArguments(arguments);
            return frag;
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

            int code = this.getArguments().getInt("Code");

            switch (code){

                case 0 : View student_login = inflater.inflate(R.layout.student_login,container,false);

                    roomno = student_login.findViewById(R.id.roomno);
                    stupwd = student_login.findViewById(R.id.stupwd);
                    stulogin = student_login.findViewById(R.id.stulogin);


                    stulogin.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            final String email = roomno.getText().toString().trim();
                            String password = stupwd.getText().toString();

                            if (email.isEmpty() || password.isEmpty()){
                                Log.e("DEBUG", "CANCELLED");
                                CustomToast invalid = new CustomToast(thisActivity);
                                invalid.showToast("Invalid Credentials!");
                            }

                            else {
                                dialog.show();
                                Log.e("DEBUG", "EXECUTING");
                                loginUser(email,password,STUDENT_LOGIN);
                            }
                        }
                    });

                    return student_login;

                case 1 : View supervisor_login = inflater.inflate(R.layout.supervisor_login,container,false);

                    username = supervisor_login.findViewById(R.id.username);
                    suppwd = supervisor_login.findViewById(R.id.suppwd);
                    suplogin = supervisor_login.findViewById(R.id.suplogin);

                    suplogin.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            String email = username.getText().toString().trim();
                            String password = suppwd.getText().toString();

                            if (email.isEmpty() || password.isEmpty()){
                                Log.e("DEBUG", "CANCELLED");
                                CustomToast invalid = new CustomToast(thisActivity);
                                invalid.showToast("Invalid Credentials!");
                            }

                            else {
                                dialog.show();
                                Log.e("DEBUG", "EXECUTING");
                                loginUser(email,password,SUPERVISOR_LOGIN);
                            }
                        }
                    });

                    return supervisor_login;
            }

            return null;
        }

    }

    private static void loginUser(final String email, String password, final int code){
        authenticate.signInWithEmailAndPassword(email.concat("@sandarbh.firebaseapp.com"), password).addOnCompleteListener(thisActivity, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.e("DEBUG", "SUCCESS");
                    dialog.dismiss();

                    FirebaseUser curr_user = authenticate.getCurrentUser();
                    UserProfileChangeRequest updateProfile = new UserProfileChangeRequest.Builder()
                            .setDisplayName(email).build();

                    curr_user.updateProfile(updateProfile);

                    Intent Enter = null;

                    switch (code){

                        case STUDENT_LOGIN : if (email.equals("SUPERVISOR")) {
                            CustomToast invalid = new CustomToast(thisActivity);
                            invalid.showToast("Invalid Credentials!");
                        }
                        else
                            Enter = new Intent(thisActivity,Student_Activity.class);

                            break;

                        case SUPERVISOR_LOGIN : if (email.equals("SUPERVISOR")) {
                            Enter = new Intent(thisActivity,Supervisor_Activity.class);
                        }
                        else {
                            CustomToast invalid = new CustomToast(thisActivity);
                            invalid.showToast("Invalid Credentials!");
                        }

                            break;
                    }

                    if (Enter!=null) {
                        Enter.putExtra("User", email);
                        thisActivity.startActivity(Enter);
                        thisActivity.finish();
                    }

                } else {
                    //task.getResult();
                    Log.e("DEBUG", "FAILED");
                    dialog.dismiss();
                    CustomToast invalid = new CustomToast(thisActivity);
                    invalid.showToast("Invalid Credentials!");
                }
            }

        });
    }

    private static AlertDialog Loading(){

        String msg = "Logging in...";
        RelativeLayout dialogLayout = new RelativeLayout(thisActivity);
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
        loadingMessage.setTextSize((float) getDPFromPixels(18));

        dialogLayout.addView(divider);
        dialogLayout.addView(loadingBar);
        dialogLayout.addView(loadingMessage);
        AlertDialog.Builder builder = new AlertDialog.Builder(thisActivity)
                .setView(dialogLayout)
                .setTitle("Loading")
                .setCancelable(false);

        return builder.create();
    }

    private static double getDPFromPixels(double pixels) {
        DisplayMetrics metrics = new DisplayMetrics();
        thisActivity.getWindowManager().getDefaultDisplay().getMetrics(metrics);

        switch(metrics.densityDpi){
            case DisplayMetrics.DENSITY_LOW:
                pixels = pixels * 0.75;
                break;
            case DisplayMetrics.DENSITY_MEDIUM:
                //pixels = pixels * 1;
                break;
            case DisplayMetrics.DENSITY_HIGH:
                pixels = pixels * 1.5;
                break;
        }
        return pixels;
    }

    private static class FirebaseLogin extends AsyncTask<Void,Void,Void>{

        String email,password;
        Context context;
        final CustomToast invalid;

        FirebaseLogin(Context c){
            context = c;
            invalid = new CustomToast(thisActivity);
        }
        @Override
        protected void onPreExecute() {

            email = roomno.getText().toString();
            password = stupwd.getText().toString();

            if (email.isEmpty() || password.isEmpty()){
                Log.e("DEBUG", "CANCELLED");
                cancel(true);
            }
            else{
                //cancel(false);
                Log.e("DEBUG", "LOGIN");
                dialog.show();
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {

            Log.e("DEBUG", "EXECUTING");
                authenticate.signInWithEmailAndPassword(email.concat("@sandarbh.firebaseapp.com"), password).addOnCompleteListener(thisActivity, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.e("DEBUG", "SUCCESS");

                        } else {
                            Log.e("DEBUG", "FAILED");
                            cancel(true);
                        }
                    }

                });

            return null;
        }

        @Override
        protected void onCancelled() {
            Log.e("DEBUG", "CANCELLED");
            invalid.showToast("Invalid Credentials!");
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Log.e("DEBUG", "POST EXECUTE");
            dialog.dismiss();

            Intent Enter = new Intent(context, Student_Activity.class);
            context.startActivity(Enter);
            thisActivity.finish();
        }
    }

    @Override
    public void onBackPressed() {

        if (exit){
            finish();
        }
        else{
            CustomToast back = new CustomToast(getBaseContext());
            back.showToast(getResources().getString(R.string.BACK_MSG));

            exit = true;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        exit = false;
                    }
                },3*1000);
        }
    }
}
