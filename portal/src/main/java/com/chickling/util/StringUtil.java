package com.chickling.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by gl08 on 2015/12/4.
 */
public class StringUtil {
private static final HashMap<String,Integer> TimeIndex=new HashMap<>();
    /**
     * Encode SHA-256 to Generate Token
     * @param text
     * @return SHA-256 Token
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException
     */
    public static String sha256(String text) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        //Encode SHA-256
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(text.getBytes("UTF-8"));
        byte[] digest = md.digest();
        return String.format("%064x", new java.math.BigInteger(1, digest));
    }

    public static String cronGenerator(String type, String[] args,Date startDate) {
            String[] crone=new String[6];
        int[] DateFormat=new int[3];
        HashMap<String,Integer> TimeIndex=new HashMap<>();
        TimeIndex.put("Minute",1);
        TimeIndex.put("Hour",2);
        TimeIndex.put("Day",3);
        HashMap<Integer,String> WeekDay=new HashMap<>();
        WeekDay.put(0,"SUN");
        WeekDay.put(1,"MON");
        WeekDay.put(2,"TUE");
        WeekDay.put(3,"WED");
        WeekDay.put(4,"THU");
        WeekDay.put(5,"FRI");
        WeekDay.put(6,"SAT");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        DateFormat[2] = calendar.get(Calendar.HOUR_OF_DAY);
        DateFormat[1] = calendar.get(Calendar.MINUTE);
        DateFormat[0] = calendar.get(Calendar.SECOND);

            if(type.equals("interval")){
                int unit=TimeIndex.get(args[1]);
                crone[unit]=DateFormat[unit]+"/"+args[0];
                crone[crone.length-1]="?";

                for(int i=0;i<crone.length-1;i++){
                    if(i<unit){
                        crone[i]=Integer.toString(DateFormat[i]);
                    }
                    else if(i>unit){
                            crone[i]="*";
                        }

                }
            }
            else{
                char[] week=args[1].toCharArray();
                String w="";
                int diff=7-week.length;

                for(int i=week.length-1;i>=0;i--){

                    if(week[i]=='1'){
                        w=WeekDay.get(i+diff)+","+w;
                    }
                }
                w=w.substring(0,w.lastIndexOf(","));
                int hour=Integer.parseInt(args[0])/60;
                int minute=Integer.parseInt(args[0])%60;
                crone[0]="0";
                crone[1]=Integer.toString(minute);
                crone[2]=Integer.toString(hour);
                crone[3]="?";
                crone[4]="*";
                crone[5]=w;

            }
        String rtn="";
        for(int i=0;i<crone.length;i++){
            rtn=rtn+crone[i]+" ";
        }

        return rtn.trim();
    }
}
