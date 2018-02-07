package com.braille.tesseract.sandarbh.iitihousekeeping;

import java.util.HashMap;

/**
 * Created by sandarbh on 16/1/18.
 */

public class Request {

    public String RoomNo;
    public String TimeSlot;
    public String Remarks;
    public boolean Stu_Check,Sup_Check;
    public Integer StatusColor;
    public long uptime;
    public String Key;

    public Request(String key,String room,String time,String rem,boolean stu,boolean sup,int color,long up){
        Key = key;
        RoomNo = room;
        TimeSlot = time;
        Remarks = rem;
        Stu_Check = stu;
        Sup_Check = sup;
        StatusColor = color;
        uptime = up;
    }
    public Request(HashMap map,String key){

        Key = key;
        RoomNo = (String) map.get("RoomNo");
        TimeSlot = (String) map.get("TimeSlot");
        Remarks = (String) map.get("Remarks");
        Stu_Check = (boolean) map.get("Stu_Check");
        Sup_Check = (boolean) map.get("Sup_Check");
        StatusColor = (int)(long) map.get("StatusColor");
        uptime = (long) map.get("uptime");
    }
}
