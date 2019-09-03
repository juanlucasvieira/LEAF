/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.uff.vAPcontroller;

import java.util.ArrayList;
import java.util.HashMap;
import java.net.DatagramPacket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author juan
 */
public class TransactionHandler {

    private CommunicationHandler comm;
    private ConcurrentHashMap<String, Observer> observers = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Transaction> asyncTransactions = new ConcurrentHashMap<>();

    public TransactionHandler() {

    }

    public void processMessage(String pcktData) {
        String tid = pcktData.substring(4, 36);
        String response = pcktData.substring(37, pcktData.length());
        processTransactionAnswer(tid, response);
    }

    public void registerObserver(Observer o) {
        observers.put(o.getId(), o);
    }

    public boolean processTransactionAnswer(String tid, String response) {
        Transaction t = asyncTransactions.get(tid);
        t.setResponse(response);
        Observer o = observers.get(t.getClaimantID());
        if (t == null) {
            Log.print(Log.ERROR, "Received answer: Transaction not found.");
            return false;
        }
        if (o == null) {
            Log.print(Log.ERROR, "Received answer: Claimant not found.");
            return false;
        }
        if (response.contains(Csts.INVALID_COOKIE)) {
            t.getDestination().invalidate();
        } else {
            o.notify(t);
        }
        asyncTransactions.remove(tid);
        return true;
    }

    public Transaction getTransByID(String TID) {
        return asyncTransactions.get(TID);
    }

    public boolean isObserverRegistered(Observer o) {
        Observer observer = observers.get(o.getId());
        return observer != null;
    }

    public boolean pushAsyncTransaction(Transaction t) {
        if (observers.containsKey(t.getClaimantID())) {
            asyncTransactions.put(t.getTID(), t);
            Log.print(Log.DEBUG, "Sending Transaction: \n" + t.toString());
            comm.sendAsyncRequest(t.getTID(), t.getRequest(), t.getDestination());
            return true;
        } else {
            Log.print(Log.ERROR, "Error: Push Transaction from unknown Claimant: \n" + t.toString());
            return false;
        }
    }

    public Transaction pushSynchronousTransaction(Transaction t) {
        Log.print(Log.DEBUG, "Sending Synchronous Transaction: \n" + t.toString());
        String reply;
        try {
            reply = comm.sendSyncRequest(t.getTID(), t.getRequest(), t.getDestination());
            Log.print(Log.DEBUG, "Sync Transaction Response:\n" + reply);
            String tid = reply.substring(4, 36);
            if (t.getTID().equals(tid)) {
                String response = reply.substring(37, reply.length());
                t.setResponse(response);
            } else {
                Log.print(Log.ERROR, "TID mismatch in synchronous transaction.");
            }
        } catch (SocketTimeoutException ex) {
            t.setResponse(Csts.TIMEOUT);
        }
        return t;
    }

    public int sendSyncRequest(Observer o, String request, CtrlInterface iface) {
        Transaction t = pushSynchronousTransaction(new Transaction(o.getId(), request, iface));
        if (t.getResponse() != null && t.getResponse().contains("OK")) {
            return Csts.SYNC_REQUEST_OK;
        } else if (t.getResponse().equals(Csts.TIMEOUT)) {
            return Csts.SYNC_REQUEST_TIMEOUT;
        } else {
            return Csts.SYNC_REQUEST_FAILED;
        }
    }

    public int sendSyncRequest(Observer o, String request) {
        return sendSyncRequest(o, request, o.getCtrlIface());
    }

    void setCommunicationHandler(CommunicationHandler comm) {
        if (this.comm == null) {
            this.comm = comm;
        }
    }

}
