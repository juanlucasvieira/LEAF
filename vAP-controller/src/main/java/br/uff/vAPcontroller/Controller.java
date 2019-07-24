package br.uff.vAPcontroller;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Controller {

    private ArrayList<AP> phy_aps;
    private CommunicationHandler comm;
    private TransactionHandler thand;
    private static Controller c;

    private long updateTimeMillis = 1000;

    public Controller() {
        this.phy_aps = new ArrayList<>();
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
            phy_aps.add(new AP("AP@1", InetAddress.getByName("127.0.0.1"), 8888, thand));
            updateLoop();
        } catch (UnknownHostException ex) {
            Log.print(Cmds.ERROR, "Unknown IP format");
        } catch (IOException ex) {
            Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void updateLoop() throws InterruptedException {
        while (true) {
            Log.print(Cmds.DEBUG_INFO, "Update loop is RUNNING");
            for (AP ap : phy_aps) {
                ap.requestInterfaces();
                ap.vAPUpdate();
            }
            Thread.sleep(updateTimeMillis);
        }
//        for (AP phy_ap : phy_aps) {
//            phy_ap.update();
//        }
    }

    public void migrateVAP(AP source, AP dest, VirtualAP target) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.

    }

    public String sendRequest() {
        return "GOT A REQUEST!!";
    }

}
