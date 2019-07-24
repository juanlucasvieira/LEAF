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
        this.comm.listen();
    }

    public void processMessage(String pcktData) {
        String tid = pcktData.substring(4, 36);
        String response = pcktData.substring(37, pcktData.length());
        processTransactionAnswer(tid, response);
    }

    public void registerObserver(Observer o) {
        observers.put(o.getId(), o);

    }

    public void processTransactionAnswer(String tid, String response) {
        Transaction t = transactions.get(tid);
        t.setResponse(response);
        Observer o = observers.get(t.getClaimantID());
        if (t == null) {
            Log.print(Cmds.ERROR, "Received answer: Transaction not found.");
            return;
        }
        if (o == null) {
            Log.print(Cmds.ERROR, "Received answer: Claimant not found.");
            return;
        }
        o.notify(t);
        transactions.remove(tid);
    }

    public Transaction getTransByID(String TID) {
        return transactions.get(TID);
    }

    public boolean isObserverRegistered(Observer o) {
        Observer observer = observers.get(o.getId());
        return observer != null;
    }

    public boolean pushTransaction(Transaction t) {
        if (observers.containsKey(t.getClaimantID())) {
            transactions.put(t.getTID(), t);
            Log.print(Cmds.DEBUG_INFO, "Sending Transaction: \n" + t.toString());
            comm.sendRequest(t.getTID(), t.getRequest(), t.getDestination());
            return true;
        } else {
            Log.print(Cmds.ERROR, "Error: Push Transaction from unknown Claimant: \n" + t.toString());
            return false;
        }
    }
}
