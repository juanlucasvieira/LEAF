package vap.controller;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

public class main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        ConcurrentLinkedQueue<String> requests = new ConcurrentLinkedQueue<>();
        ConcurrentLinkedQueue<String> answers = new ConcurrentLinkedQueue<>();
        ConcurrentLinkedQueue<String> ctrl_ifaces = new ConcurrentLinkedQueue<>();
        CommCoordinator comm = new CommCoordinator(requests, answers, ctrl_ifaces);
        comm.start();
    }

}
