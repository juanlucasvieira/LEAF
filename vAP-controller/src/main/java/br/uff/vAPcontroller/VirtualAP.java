package br.uff.vAPcontroller;

import java.net.InetAddress;

public class VirtualAP implements Observer{
    private CtrlInterface ctrl_iface;
    private String v_interface;
    private String bss_id;
    private int ctrl_port;
    private String ssid;
    private Station sta;
    private short max_sta_num;

    public VirtualAP(String v_interface, InetAddress ip, int port) {
        this.ctrl_iface = new CtrlInterface(ip, port);
        this.v_interface = v_interface;
        this.max_sta_num = 1;
    }
    
    public void update(){
        
    }

    @Override
    public String getId() {
        return v_interface;
    }

    @Override
    public void notify(Transaction t) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}

