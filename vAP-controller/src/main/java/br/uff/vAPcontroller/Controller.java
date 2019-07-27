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

    private boolean suspendUpdate = false;
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
            if (!suspendUpdate) {
                Log.print(Log.DEBUG_INFO, "Update loop is RUNNING");
                for (AP ap : phy_aps.values()) {
                    ap.requestInfo();
                }
            }
            Thread.sleep(updateTimeMillis);
        }
//        for (AP phy_ap : phy_aps) {
//            phy_ap.update();
//        }
    }

    public int migrateVAP(String src_ap_id, String dst_ap_id, String target_vap) {
        AP src = getAPById(src_ap_id);
        if (src == null) {
            return -1;
        }
        PhyIface src_phy = src.getPhyByVAPId(target_vap);
        if (src_phy == null) {
            return -2; //Target not found in specified AP code.
        }
        VirtualAP target = src_phy.getVAPByID(target_vap);

        AP dst = getAPById(dst_ap_id);
        if (dst == null) {
            return -3;
        }
        PhyIface dst_phy = dst.choosePhyIface(target); //Choose best physical interface??

        suspendUpdate = true;

        if (dst.VAPReceiveRequest(target, dst_phy)) {
            Station movingSta = target.getSta();
            if (target.getSta() != null) {
                if (target.STAReceiveRequest(thand, movingSta)) { //Send STA injection request
                    if (src_phy.channelEqualsTo(dst_phy)) { //Check fifferent channels
                        if (target.sendCSARequest(thand, dst_phy.getFrequency(), Cmds.CSA_COUNT, true)) {
                            return finishMigration(src, target);
                        } else {
                            return rollbackUnsuccessfulMigration(dst, target, Cmds.FAILED_TO_SEND_CSA);
                        }
                    } else {
                        //TODO: Check connectivity before deleting?
                        return finishMigration(src, target);
                    }
                } else {
                    return rollbackUnsuccessfulMigration(dst, target, Cmds.STA_INJECTION_FAILED);
                }
            } else {
                return finishMigration(src, target);
            }
        } else {
            return rollbackUnsuccessfulMigration(dst, target, Cmds.VAP_INJECTION_FAILED);
        }
    }

    //TODO: Verify correctness by request info update?
    public int finishMigration(AP src_ap, VirtualAP target) {
        int returnCode = (src_ap.deleteVAPRequest(target) ? Cmds.MIGRATION_SUCCESSFUL : Cmds.DEL_AP_FROM_OLD_VAP_FAILED);
        suspendUpdate = false;
        return returnCode;
    }

    public int rollbackUnsuccessfulMigration(AP dst, VirtualAP target, int errorCode) {
        int returnCode = (dst.deleteVAPRequest(target) ? Cmds.MIGRATION_ROLLBACK_SUCCESSFUL * errorCode : Cmds.MIGRATION_ROLLBACK_FAILED * errorCode);
        suspendUpdate = false;
        return returnCode;
    }

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
