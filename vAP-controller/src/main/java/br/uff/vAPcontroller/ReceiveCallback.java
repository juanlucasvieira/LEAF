/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.uff.vAPcontroller;

import java.net.DatagramPacket;

/**
 *
 * @author juan
 */
public interface ReceiveCallback {
    
    public void rcv_callback(DatagramPacket dp);
    
}
