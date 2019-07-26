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
    public static final String REQ_FIRST_STA_INFO = "STA-FIRST";
    public static final String GET_AP_IFACES = "INTERFACES ctrl";
    public static final String GET_COOKIE = "GET_COOKIE";
    
    public static final boolean DEBUG_LOG_LEVEL = true;
    
    public static final int SEND_LISTEN_PORT_ASYNC = 9999;
    public static final int SEND_LISTEN_PORT_SYNC = 9998;
}
