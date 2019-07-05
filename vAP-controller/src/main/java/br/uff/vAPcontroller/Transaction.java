package br.uff.vAPcontroller;

import java.util.UUID;

public class Transaction {
    
    String id;
    String request;
    
    public Transaction(String request){
        this.id = UUID.randomUUID().toString();
        this.request = request;
    }
    
}
