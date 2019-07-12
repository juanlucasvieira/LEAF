/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.uff.vAPcontroller;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * @author juan
 */
public class MessageReceiver extends Thread {

    private ReceiveCallback rc;
    private DatagramSocket socket;

    public MessageReceiver(DatagramSocket socket, ReceiveCallback rc) {
        this.rc = rc;
        this.socket = socket;
    }

    @Override
    public void run() {
        while (true) {
            try {
                System.out.println("Listening for messages...");
                byte[] resp_buffer = new byte[512];
                DatagramPacket response = new DatagramPacket(resp_buffer, resp_buffer.length);
                socket.receive(response);
                System.out.println("End");
                rc.receiveCallback(response);
            } catch (IOException e) {
                rc.receiveCallback(null);
            }
        }
    }

}
