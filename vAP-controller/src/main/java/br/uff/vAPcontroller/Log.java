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
    
    public static void print(int messageType, String msg){
        if(messageType == Cmds.DEBUG_INFO && Cmds.DEBUG_LOG_LEVEL){
            System.out.println(" > " + msg);
        } else {
            System.out.println(" > " + msg);
        }
    }
    
}
