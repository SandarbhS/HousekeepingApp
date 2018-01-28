package com.braille.tesseract.sandarbh.iitihousekeeping;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by sandarbh on 25/1/18.
 */

public class FirebaseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        //FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
