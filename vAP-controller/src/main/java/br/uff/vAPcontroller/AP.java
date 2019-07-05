package br.uff.vAPcontroller;

import java.net.InetAddress;
import java.util.ArrayList;

public class AP {
    
    private CtrlInterface ctrl;
    private ArrayList<String> ifaces_names;
    private VirtualAP[] vaps;

    public AP(InetAddress ip, int port) {
        ctrl = new CtrlInterface(ip, port);
    }

    public ArrayList<String> getIfaces() {
        return ifaces_names;
    }

    public VirtualAP[] getVaps() {
        return vaps;
    }
    
    public String getId(){
        return ctrl.getIp().getHostAddress() + "#" + ctrl.getPort();
    }
    
    public void update(){
        
    }
    
}
