package br.uff.vAPcontroller;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AP implements Observer{
    
    private CtrlInterface gci;
    private HashMap<String,VirtualAP> vaps;
    private String ap_id;
    private TransactionHandler handler;

    public AP(String id, InetAddress ip, int port) {

    }

    AP(String id, InetAddress ip, int port, TransactionHandler handler) {
        this.ap_id = id;
        this.gci = new CtrlInterface(ip, port);
        this.handler = handler;
    }

    @Override
    public String getId() {
        return ap_id;
    }
    
    public String getAddress(){
        return gci.getIp().getHostAddress() + "#" + gci.getPort();
    }
    
    public void vAPUpdate(){
        for(VirtualAP vap : vaps.values()){
            vap.update();
        }
    }
    
    public void requestInterfaces(){
        handler.pushTransaction(new Transaction(this.ap_id, Cmds.GET_AP_IFACES, gci));
    }

    @Override
    public void notify(Transaction t) {
        switch(t.getRequest()){
            case Cmds.GET_AP_IFACES:
                updateIfaces(t.getResponse());
        }
    }

    private void updateIfaces(String response) {
        //TODO: Fill fields using response
        String vap_id = "";
        String port = "";
        if(vaps.get(vap_id) == null){
            vaps.put(vap_id, new VirtualAP(ap_id + "@" + vap_id, gci.getIp(), Integer.parseInt(port)));
        }
    }
    
}
