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
public class Cmds {
    
    public static final String REQ_STATUS_INFO = "STATUS";
    public static final String REQ_STA_INFO = "STA";
    public static final String REQ_ALL_STA_INFO = "ALL_STA";
    public static final String GET_AP_IFACES = "INTERFACES ctrl";
    public static final String GET_COOKIE = "GET_COOKIE";
    
    public static final boolean DEBUG_LOG_LEVEL = true;
    
    public static int INFO = 1;
    public static int DEBUG_INFO = 2;
    public static int ERROR = 3;
    
    public static int SEND_LISTEN_PORT = 9999;
}
