package br.uff.vAPcontroller;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Controller {

    private HashMap<String, AP> phy_aps;
    private CommunicationHandler comm;
    private TransactionHandler thand;
    private static Controller c;

    private long updateTimeMillis = 1000;

    public Controller() {
        this.phy_aps = new HashMap<>();
    }

    public static Controller getInstance() {
        if (c == null) {
            c = new Controller();
        }
        return c;
    }

    public void begin() throws InterruptedException {
        try {
            thand = new TransactionHandler();
            phy_aps.put("AP@1", new AP("AP@1", InetAddress.getByName("127.0.0.1"), 8888, thand));
//            phy_aps.put("AP@2", new AP("AP@2", InetAddress.getByName("192.168.1.145"), 8888, thand));
            updateLoop();
        } catch (UnknownHostException ex) {
            Log.print(Log.ERROR, "Unknown IP format");
        } catch (IOException ex) {
            Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void updateLoop() throws InterruptedException {
        while (true) {
            Log.print(Log.DEBUG_INFO, "Update loop is RUNNING");
            for (AP ap : phy_aps.values()) {
                ap.requestInfo();
            }
            Thread.sleep(updateTimeMillis);
        }
//        for (AP phy_ap : phy_aps) {
//            phy_ap.update();
//        }
    }

//    public int migrateVAP(String src_ap_id, String dst_ap_id, String target_vap) {
//        AP src = phy_aps.get(src_ap_id);
//        VirtualAP target = src.getVAPByID(target_vap);
//        AP dst = phy_aps.get(dst_ap_id);
//        
//    }

    public String sendRequest() {
        return "GOT A REQUEST!!";
    }

    public AP getAPById(String id) {
        if (phy_aps.containsKey(id)) {
            return phy_aps.get(id);
        } else {
            return null;
        }
    }

    public ArrayList<AP> getAllAPs() {
        if (phy_aps != null && phy_aps.size() > 0) {
            return new ArrayList<>(phy_aps.values());
        } else {
            return null;
        }
    }
}
