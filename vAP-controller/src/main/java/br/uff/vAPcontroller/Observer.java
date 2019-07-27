/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.uff.vAPcontroller;

/**
 *
 * @author juan
 */
public interface Observer {
    
    public String getId();
    
    public CtrlInterface getCtrlIface();
    
//    public String sendRequest();
    
    public void notify(Transaction t);
    
    //public void updateState();
    
}
