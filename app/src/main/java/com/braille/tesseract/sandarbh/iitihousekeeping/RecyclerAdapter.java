package com.braille.tesseract.sandarbh.iitihousekeeping;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;
import java.util.List;

/**
 * Created by sandarbh on 16/1/18.
 */

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.RequestHolder> {

    private List<Request> dataList;
    private Context context;
    private final int UPDATED = 1,UPDATE_ERROR = 2;

    public RecyclerAdapter(Context c, List<Request> data){
        context = c;
        dataList = data;
    }
    @Override
    public RequestHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View requestView = inflater.inflate(R.layout.request,parent,false);

        Log.e("R","ONCREATE_VH....");
        RequestHolder requestHolder = new RequestHolder(requestView);
        return requestHolder;
    }

    @Override
    public void onBindViewHolder(final RequestHolder holder, int position) {

        final int index = position;
        final Request tmp = dataList.get(position);

        holder.RoomNo.setText(tmp.RoomNo);
        holder.TimeSlot.setText(tmp.TimeSlot);
        holder.Remarks.setText(tmp.Remarks);
        holder.checkStu.setChecked(tmp.Stu_Check);
        holder.checkSup.setChecked(tmp.Sup_Check);

        if (holder.checkStu.isChecked() && holder.checkSup.isChecked()) {
            Log.e("R","Color Changed");
            tmp.StatusColor = R.color.ClearedRequest;
            holder.header.setBackgroundResource(R.drawable.request_top_cleared);
            holder.cancel.setTextColor(context.getResources().getColor(R.color.ClearedRequest));
        }
        else {
            tmp.StatusColor = R.color.PendingRequest;
            holder.header.setBackgroundResource(R.drawable.request_top_pending);
            holder.cancel.setTextColor(context.getResources().getColor(R.color.PendingRequest));
        }


        long timeDifference = (new Date().getTime()-tmp.uptime);

        if (timeDifference < 60000)
            holder.upTime.setText(R.string.sec_ago);
        else if (timeDifference <= 3600000)
            holder.upTime.setText(String.format(context.getResources().getString(R.string.mins_ago),timeDifference/60000));
        else if (timeDifference <= 86400000)
            holder.upTime.setText(String.format(context.getResources().getString(R.string.hours_ago),timeDifference/3600000));
        else if (timeDifference > 86400000)
            holder.upTime.setText(String.format(context.getResources().getString(R.string.days_ago),timeDifference/86400000));
        Log.e("R","ADDING...");

        if (context.getClass() == Supervisor_Activity.class){
            holder.cancel.setVisibility(View.INVISIBLE);
            holder.checkStu.setEnabled(false);

            holder.checkSup.setEnabled(!tmp.Sup_Check);
            holder.checkSup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(context)
                            .setTitle("Confirm Approval")
                            .setMessage("Are you sure you you want to mark this request complete?\nThis cannot be undone!")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    tmp.Sup_Check = true;
                                    holder.checkSup.setChecked(true);
                                    holder.checkSup.setEnabled(false);

                                    DatabaseReference request = FirebaseDatabase.getInstance().getReference()
                                            .child(tmp.RoomNo).child(tmp.Key).child("Sup_Check");
                                    Log.e("DEBUG","Updating : "+request.getKey());

                                    request.setValue(true).addOnCompleteListener((Activity) context, new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                Log.e("DEBUG","UPDATED");
                                                notifyDataSetChanged();
                                            }
                                            else {
                                                Log.e("DEBUG","FAILED_UPDATE");
                                                CustomToast failed = new CustomToast(context);
                                                failed.showToast("Error while updating! Please refresh.");
                                            }
                                        }
                                    });
                                    //holder.header.setBackgroundResource(student_main.StatusColor);
                                }
                            })
                            .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    holder.checkSup.setChecked(false);
                                    dialogInterface.dismiss();
                                }
                            });
                    AlertDialog warning = alert.create();
                    warning.show();
                }
            });

            if (Supervisor_Activity.refreshLayout.isRefreshing() && position == dataList.size() - 1) {
                Log.w("R", "Refreshed!");
                Supervisor_Activity.refreshLayout.setRefreshing(false);
            }
        }
        else {
            holder.cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showWarning(index);
                }
            });
            holder.checkSup.setEnabled(false);
            holder.checkStu.setEnabled(!tmp.Stu_Check);

            holder.checkStu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(context)
                            .setCancelable(false)
                            .setTitle("Confirm Approval")
                            .setMessage("Are you sure you you want to mark this request complete?\nThis cannot be undone!")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    tmp.Stu_Check = true;
                                    holder.checkStu.setChecked(true);
                                    holder.checkStu.setEnabled(false);
                                    DatabaseReference request = FirebaseDatabase.getInstance().getReference()
                                            .child(tmp.RoomNo).child(tmp.Key).child("Stu_Check");
                                    Log.e("DEBUG","Updating : "+request.getKey());

                                    request.setValue(true).addOnCompleteListener((Activity) context, new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                Log.e("DEBUG","UPDATED");
                                                notifyDataSetChanged();
                                            }
                                            else {
                                                Log.e("DEBUG","FAILED_UPDATE");
                                                CustomToast failed = new CustomToast(context);
                                                failed.showToast("Error while updating! Please refresh.");
                                            }
                                        }
                                    });
                                    //holder.header.setBackgroundResource(student_main.StatusColor);
                                }
                            })
                            .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    holder.checkStu.setChecked(false);
                                    dialogInterface.dismiss();
                                }
                            });
                    AlertDialog warning = alert.create();
                    warning.show();
                }
            });

            if (Student_Activity.refreshLayout.isRefreshing() && position == dataList.size() - 1) {
                Log.w("R", "Refreshed!");
                Student_Activity.refreshLayout.setRefreshing(false);
            }
        }

    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public class RequestHolder extends RecyclerView.ViewHolder {

        TextView RoomNo,TimeSlot,Remarks,upTime;
        CheckBox checkStu,checkSup;
        Button cancel;
        LinearLayout header;

        public RequestHolder(View itemView) {
            super(itemView);

            Log.e("R","CREATING...");
             RoomNo = itemView.findViewById(R.id.roomnum);
             TimeSlot = itemView.findViewById(R.id.timeslot);
             Remarks = itemView.findViewById(R.id.remarks);
            upTime = itemView.findViewById(R.id.uptime);

             checkStu = itemView.findViewById(R.id.checkStu);
             checkSup = itemView.findViewById(R.id.checkSup);

             cancel = itemView.findViewById(R.id.cancel);

             header = itemView.findViewById(R.id.req_header);
        }
    }

    private void showWarning(final int index){
        AlertDialog.Builder alert = new AlertDialog.Builder(context)
                .setTitle("Delete Request")
                .setMessage("Are you sure you want to delete the request?")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        if (isConnectedToNetwork()){
                            updateDatabase(index);
                        }
                        else {
                            CustomToast noNetwork = new CustomToast(context);
                            noNetwork.showToast("No Internet Connection. Please retry!");
                        }
                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
        AlertDialog warning = alert.create();
        warning.show();
    }

    public boolean isConnectedToNetwork(){

        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm.getActiveNetworkInfo() != null) {
            Log.e("DEBUG", "OK");
            return true;
        }
        else
            return false;
    }

    private void updateDatabase(final int index){

        DatabaseReference request = FirebaseDatabase.getInstance().getReference().child(dataList.get(index).RoomNo).child(dataList.get(index).Key);
        request.removeValue().addOnCompleteListener((Activity) context, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    dataList.remove(index);
                    if (dataList.size() == 0)
                        Student_Activity.toggleMessageVisibility();

                    notifyItemRemoved(index);
                    notifyItemRangeChanged(0,dataList.size());
                }

                else {
                    CustomToast failure = new CustomToast(context);
                    failure.showToast("Failed to delete the request!");
                }
            }
        });

    }

}
