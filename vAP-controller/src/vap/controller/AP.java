package vap.controller;

import java.net.InetAddress;

public class AP {
    private CtrlInterface ctrl;
    private Interface[] ifaces;
    private VirtualAP[] vaps;

    public AP(InetAddress ip, short port) {
        ctrl = new CtrlInterface(ip, port);
    }

    public Interface[] getIfaces() {
        return ifaces;
    }

    public VirtualAP[] getVaps() {
        return vaps;
    }
    
    public void bind(){
        
    }
}
