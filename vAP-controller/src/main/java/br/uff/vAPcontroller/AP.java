package br.uff.vAPcontroller;

import br.uff.vAPcontroller.Cmds;
import br.uff.vAPcontroller.CtrlInterface;
import br.uff.vAPcontroller.Observer;
import br.uff.vAPcontroller.TransactionHandler;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AP implements Observer {

    private CtrlInterface gci;
    private HashMap<String, VirtualAP> vaps;
    private String ap_id;
    private TransactionHandler handler;

    public AP(String id, InetAddress ip, int port) {

    }

    AP(String id, InetAddress ip, int port, TransactionHandler handler) {
        this.ap_id = id;
        this.gci = new CtrlInterface(ip, port);
        this.handler = handler;
        this.vaps = new HashMap<>();
    }

    @Override
    public String getId() {
        return ap_id;
    }

    public String getAddress() {
        return gci.getIp().getHostAddress() + "#" + gci.getPort();
    }

    public void vAPUpdate() {
        for (VirtualAP vap : vaps.values()) {
            vap.update(handler);
        }
    }

    public void requestInterfaces() {
        if (!handler.isObserverRegistered(this)) {
            handler.registerObserver(this);
        }
        if (!gci.isCookieSet()) {
            gci.requestCookie(handler);
        } else {
            handler.pushTransaction(new Transaction(this.ap_id, Cmds.GET_AP_IFACES, gci));
        }
    }

    @Override
    public void notify(Transaction t) {
        switch (t.getRequest()) {
            case Cmds.GET_AP_IFACES:
                updateIfaces(t.getResponse());
                break;
        }
    }

    private void updateIfaces(String response) {
        //wlp9s0 ctrl_iface=udp:8881
        String[] lines = response.split("\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            String vap_id = line.split(" ")[0];
            String port = line.substring(line.lastIndexOf(':') + 1);
            if (vaps.get(vap_id) == null) {
                vaps.put(vap_id, new VirtualAP(ap_id + "@" + vap_id, vap_id, gci.getIp(), Integer.parseInt(port)));
            }
        }
    }

}
