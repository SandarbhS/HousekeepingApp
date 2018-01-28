package com.braille.tesseract.sandarbh.iitihousekeeping;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

/**
 * Created by sandarbh on 10/1/18.
 */

public class CustomToast {

    private Toast infoToast;
    private TextView toastMsg;
    private View toastView;

    CustomToast(Context context){
        infoToast = new Toast(context);
        infoToast.setDuration(Toast.LENGTH_SHORT);

        LayoutInflater inflater = LayoutInflater.from(context);
        toastView = inflater.inflate(R.layout.toast,null);
        toastMsg = toastView.findViewById(R.id.toastMsg);
        infoToast.setView(toastView);
    }

    public void showToast(String Message){

        toastMsg.setText(Message);
        infoToast.show();
    }

    public void setToastPosition(int gravity,int X,int Y){

        infoToast.setGravity(gravity,X,Y);
    }

    public boolean isVisible(){
        return infoToast.getView().isShown();

    }

    public void setToastVisibility(int visibility){
        infoToast.getView().setVisibility(visibility);

    }
}
