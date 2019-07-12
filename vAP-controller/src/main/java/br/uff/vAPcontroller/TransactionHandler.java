/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.uff.vAPcontroller;

import java.util.ArrayList;
import java.util.HashMap;
import java.net.DatagramPacket;

/**
 * @author juan
 */
public class TransactionHandler {

    private CommunicationHandler comm;
    private HashMap<String, Observer> observers = new HashMap<>();
    private HashMap<String, Transaction> transactions = new HashMap();

    public TransactionHandler() {
        this.comm = new CommunicationHandler(this);
    }

    public void processMessage(DatagramPacket pckt) {
        String data = new String(pckt.getData());
        String tid = data.substring(4, 36);
        String response = data.substring(36, data.length());
        processTransactionAnswer(tid, response);
    }

    public void registerObserver(Observer o) {
        observers.put(o.getId(), o);
    }

    public void processTransactionAnswer(String tid, String response) {
        Transaction t = transactions.get(tid);
        Observer o = observers.get(t.getClaimantID());
        if(t == null){
            System.out.println("Received answer: Transaction not found.");
            return;
        }
        if(o != null){
            System.out.println("Received answer: Claimant not found.");
            return;
        }
        o.notify(t);
        transactions.remove(tid);
    }

    public Transaction getTransByID(String TID) {
        return transactions.get(TID);
    }

    public boolean pushTransaction(Transaction t) {
        if (observers.containsKey(t.getClaimantID())) {
            transactions.put(t.getTID(), t);
            comm.sendRequest(t.getRequest(), t.getDestination());
            return true;
        } else {
            System.out.println("Error: Push Transaction from unknown Claimant");
            return false;
        }
    }
}
