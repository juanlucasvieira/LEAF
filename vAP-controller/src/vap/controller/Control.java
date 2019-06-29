/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vap.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 *
 * @author juan
 */
public class Control {

    ConcurrentLinkedQueue<String> requests;
    ConcurrentLinkedQueue<String> answers;
    ConcurrentLinkedQueue<String> ctrl_ifaces;
    ArrayList<String> registered_ifaces;
    CommCoordinator comm;

    public Control() throws IOException {
        ConcurrentLinkedQueue<String> requests = new ConcurrentLinkedQueue<>();
        ConcurrentLinkedQueue<String> answers = new ConcurrentLinkedQueue<>();
        ConcurrentLinkedQueue<String> ctrl_ifaces = new ConcurrentLinkedQueue<>();
        comm = new CommCoordinator(requests, answers, ctrl_ifaces);
        registered_ifaces = new ArrayList<>();
    }

    public void loop() {
        comm.start();
    }

    public void sendMessage(CtrlInterface iface, String msg) {
        if (registered_ifaces.contains(iface.id)) {
            requests.offer(iface.id + "#" + msg);
        }
    }
    
    public void getMessage(CtrlInterface iface, String msg) {
    }

}
