package br.uff.vAPcontroller;

import br.uff.vAPcontroller.CtrlInterface;
import java.util.UUID;

public class Transaction {
    
    private String tid;
    private String claimant;
    private String request;
    private String response;
    private CtrlInterface destination;
    
    public Transaction(String claimant, String request, CtrlInterface destination){
        this.claimant = claimant;
        this.tid = UUID.randomUUID().toString();
        this.request = request;
    }

    public CtrlInterface getDestination() {
        return destination;
    }

    public String getTID() {
        return tid;
    }

    public String getClaimantID() {
        return claimant;
    }

    public String getRequest() {
        return request;
    }
    
    public String getResponse() {
        return request;
    }
}
