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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author juan
 */
public class TransactionHandler {

    private CommunicationHandler comm;
    private HashMap<String, Observer> observers = new HashMap<>();
    private HashMap<String, Transaction> asyncTransactions = new HashMap();

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
        Transaction t = asyncTransactions.get(tid);
        t.setResponse(response);
        Observer o = observers.get(t.getClaimantID());
        if (t == null) {
            Log.print(Log.ERROR, "Received answer: Transaction not found.");
            return;
        }
        if (o == null) {
            Log.print(Log.ERROR, "Received answer: Claimant not found.");
            return;
        }
        o.notify(t);
        asyncTransactions.remove(tid);
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
            Log.print(Log.DEBUG_INFO, "Sending Transaction: \n" + t.toString());
            comm.sendAsyncRequest(t.getTID(), t.getRequest(), t.getDestination());
            return true;
        } else {
            Log.print(Log.ERROR, "Error: Push Transaction from unknown Claimant: \n" + t.toString());
            return false;
        }
    }

    public Transaction pushSynchronousTransaction(Transaction t) {
        Log.print(Log.DEBUG_INFO, "Sending Synchronous Transaction: \n" + t.toString());
        String reply;
        try {
            reply = comm.sendSyncRequest(t.getTID(), t.getRequest(), t.getDestination());
            String tid = reply.substring(4, 36);
            if (t.getTID().equals(tid)) {
                String response = reply.substring(37, reply.length());
                Log.print(Log.DEBUG_INFO, "Sync Transaction Response:" + response);
                t.setResponse(response);
            } else {
                Log.print(Log.ERROR, "TID missmatch in synchronous transaction.");
            }
        } catch (SocketTimeoutException ex) {
            t.setResponse(Cmds.TIMEOUT);
        }
        return t;
    }

    public int sendSyncRequest(Observer o, String request, CtrlInterface iface) {
        Transaction t = pushSynchronousTransaction(new Transaction(o.getId(), request, iface));
        if (t.getResponse() != null && t.getResponse().contains("OK")) {
            return Cmds.SYNC_REQUEST_OK;
        } else if (t.getResponse().equals(Cmds.TIMEOUT)) {
            return Cmds.SYNC_REQUEST_TIMEOUT;
        } else {
            return Cmds.SYNC_REQUEST_FAILED;
        }
    }

    public int sendSyncRequest(Observer o, String request) {
        return sendSyncRequest(o, request, o.getCtrlIface());
    }

}
