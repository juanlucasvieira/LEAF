/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.uff.vAPcontroller;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 *
 * @author juan
 */
public class Log {

    public static final int INFO = 1;
    public static final int DEBUG = 2;
    public static final int ERROR = 3;
    
    public static DateFormat formatter = new SimpleDateFormat("HH:mm:ss.SSS");
    

    public static void print(int messageType, String msg) {
        formatter.setTimeZone(TimeZone.getDefault());
        String dateFormatted = formatter.format(new Date(System.currentTimeMillis()));
        if (messageType == DEBUG && Csts.DEBUG_LOG_LEVEL) {
            System.out.println(dateFormatted + " > " + msg);
        } else if (messageType == ERROR) {
            System.out.println(dateFormatted + "!!ERROR!! : " + msg);
        } else if (messageType == Log.INFO){
            System.out.println(dateFormatted + " > " + msg);
        }
    }

}
