/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.uff.vAPcontroller;

/**
 *
 * @author juan
 */
public class Log {

    public static final int INFO = 1;
    public static final int DEBUG_INFO = 2;
    public static final int ERROR = 3;

    public static void print(int messageType, String msg) {
        if (messageType == DEBUG_INFO && Cmds.DEBUG_LOG_LEVEL) {
            System.out.println(" > " + msg);
        } else {
            System.out.println(" > " + msg);
        }
    }

}
