package br.uff.vAPcontroller;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Controller {

    private ConcurrentHashMap<String, AP> phy_aps;

    private CommunicationHandler comm;
    private TransactionHandler thand;
    private EventHandler ehand;

    private static Controller c;

    private boolean suspendUpdate = false;
    private long updateTimeMillis = 1000;

    public Controller() {
        this.phy_aps = new ConcurrentHashMap<>();
    }

    public static Controller getInstance() {
        if (c == null) {
            c = new Controller();
        }
        return c;
    }

    public void begin(ConcurrentHashMap<String, AP> ap_list) throws InterruptedException {
//        try {
        ehand = new EventHandler(this);
        thand = new TransactionHandler();
        comm = new CommunicationHandler(thand, ehand);
        thand.setCommunicationHandler(comm);
        comm.listen();

        for (AP ap : ap_list.values()) {
            phy_aps.put(ap.getId(), new AP(ap.getId(), ap.getCtrlIface().getIp(), ap.getCtrlIface().getPort(), thand, ehand));
            Log.print(Log.INFO, "Registered AP: " + ap.getId() + " " + ap.getStringAddress());
        }
//            phy_aps.put("AP@1", new AP("AP@1", InetAddress.getByName("127.0.0.1"), 9000, thand, ehand));
//            phy_aps.put("AP@2", new AP("AP@2", InetAddress.getByName("192.168.1.143"), 9000, thand, ehand));
        updateLoop();
//        } catch (UnknownHostException ex) {
//            Log.print(Log.ERROR, "Unknown IP format");
//        } catch (IOException ex) {
//            Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }

    public void updateLoop() throws InterruptedException {
        while (true) {
            if (!suspendUpdate) {
                Log.print(Log.DEBUG, "Update loop is RUNNING");
                for (AP ap : phy_aps.values()) {
                    ap.loop();
                }
            }
            Thread.sleep(updateTimeMillis);
        }
//        for (AP phy_ap : phy_aps) {
//            phy_ap.update();
//        }
    }

    public int addAP(String ap_id, InetAddress ip, int port) {
        for (AP ap : phy_aps.values()) {
            if ((ap.getCtrlIface().getIp().getHostAddress().equals(ip.getHostAddress())
                    && ap.getCtrlIface().getPort() == port)) {
                return -2;
            }
        }
        if (!phy_aps.containsKey(ap_id)) {
            phy_aps.put(ap_id, new AP(ap_id, ip, port, thand, ehand));
            return 0;
        } else {
            return -1;
        }
    }

    public int deleteAP(String ap_id) {
        if (phy_aps.containsKey(ap_id)) {
            phy_aps.get(ap_id).deinitialize();
            phy_aps.remove(ap_id);
            return Csts.SYNC_REQUEST_OK;
        } else {
            return Csts.AP_NOT_FOUND;
        }
    }

    public int migrateVAPCommand(String src_ap_id, String dst_ap_id, String target_vap, String dst_phy_s) throws InterruptedException {

        Instant start = Instant.now();

        AP src = getAPById(src_ap_id);
        if (src == null) {
            return Csts.SOURCE_AP_NOT_FOUND;
        }
        PhyIface src_phy = src.getPhyByVAPId(target_vap);
        if (src_phy == null) {
            System.out.println("PHY IFACE NOT FOUND IN SOURCE AP");
            return Csts.VAP_NOT_FOUND; //Target not found in specified AP code.
        }
        VirtualAP target = src_phy.getVAPByID(target_vap);
        
        if(Csts.BLOCK_MAIN_VAP_OPERATIONS && target.isMainVAP()){
            return Csts.VAP_CANNOT_BE_MIGRATED;
        }

        AP dst = getAPById(dst_ap_id);
        if (dst == null) {
            System.out.println("DST AP NOT FOUND");
            return Csts.DST_AP_NOT_FOUND;
        }
        if (dst.isPhyIfacesEmpty()) {
            return Csts.UNAVAILABLE_PHY_IFACE;
        }

        PhyIface dst_phy;
        if (dst_phy_s == null) {
            dst_phy = dst.getBestPhyIface(target); //Choose best physical interface??
        } else {
            dst_phy = dst.getPhyByName(dst_phy_s);
            if (dst_phy == null) {
                return Csts.SPECIFIED_PHY_NOT_FOUND;
            }
        }

        return migrateVAP(src, dst, src_phy, dst_phy, target);
    }

    public int migrateVAP(AP src, AP dst, PhyIface src_phy, PhyIface dst_phy, VirtualAP target) throws InterruptedException {
        suspendUpdate = true;

        int newPort = dst.getNextAvailableCtrlIfacePort();

        String newVIfaceName = Csts.getNewIfaceName(target.getBssId(), dst_phy);

        int exitCode = dst.vAPReceiveRequest(target, dst_phy, newPort, newVIfaceName);

        if (exitCode == 0) {
            CtrlInterface newIface = new CtrlInterface(dst.getCtrlIface().getIp(), dst.getNextAvailableCtrlIfacePort());
            if (newIface.requestCookieSync(thand) == 0 && newIface.attachSync(thand) == 0) {
                Station movingSta = target.getSta();
                if (!Csts.DISABLE_STA_INJECTION && target.getSta() != null) {
                    exitCode = dst.STAReceiveRequest(thand, movingSta, newIface); //Send STA injection request
                    if (exitCode == 0) {
                        if (!Csts.DISABLE_CSA && !src_phy.channelEqualsTo(dst_phy)) { //Check different channels
                            exitCode = target.sendCSARequest(thand, dst_phy.getFrequency(), Csts.CSA_COUNT, true);
                            if (exitCode == 0) {
                                Log.print(Log.DEBUG, "sendSTAFrameRequest() exitCode: " + exitCode);
                                Thread.sleep(Csts.CSA_WAITING_TIME_MILLIS);
                                return finishMigration(src, dst, src_phy, dst_phy, target, newIface, newVIfaceName);
                            } else {
                                if (exitCode == Csts.SYNC_REQUEST_TIMEOUT) {
                                    return rollbackUnsuccessfulMigration(dst, newVIfaceName, target, Csts.SYNC_REQUEST_TIMEOUT);
                                } else {
                                    return rollbackUnsuccessfulMigration(dst, newVIfaceName, target, Csts.FAILED_TO_SEND_CSA);
                                }
                            }
                        } else {
                            //TODO: Check connectivity before deleting?
                            return finishMigration(src, dst, src_phy, dst_phy, target, newIface, newVIfaceName);
                        }
                    } else {
                        if (exitCode == Csts.SYNC_REQUEST_TIMEOUT) {
                            return rollbackUnsuccessfulMigration(dst, newVIfaceName, target, Csts.SYNC_REQUEST_TIMEOUT);
                        } else {
                            return rollbackUnsuccessfulMigration(dst, newVIfaceName, target, Csts.STA_INJECTION_FAILED);
                        }
                    }
                } else {
                    return finishMigration(src, dst, src_phy, dst_phy, target, newIface, newVIfaceName);
                }
            } else {
                return rollbackUnsuccessfulMigration(dst, newVIfaceName, target, Csts.VAP_INJECTION_FAILED);
            }
        } else {
            suspendUpdate = false;
            if (exitCode == Csts.SYNC_REQUEST_TIMEOUT) {
                return Csts.SYNC_REQUEST_TIMEOUT;
            } else {
                return Csts.VAP_INJECTION_FAILED;
            }
        }
    }

    public int finishMigration(AP src_ap, AP dst_ap, PhyIface src, PhyIface dst, VirtualAP target, CtrlInterface newIface, String newVIfaceName) {
        int returnCode;
        Station s = target.getSta();
        boolean pool_sta_ok = false;
        if (s != null) {
            if (dst_ap.pollSTA(s, newIface) != 0) {
                Log.print(Log.ERROR, "Poll STA failed!");
                pool_sta_ok = false;
            } else {
                Log.print(Log.INFO, "Poll STA successful!");
                pool_sta_ok = true;
            }
            if(!Csts.DISABLE_SEND_FRAME){
                if (dst_ap.sendSTAFrameRequest(thand, s, newIface) != 0) {
                    Log.print(Log.ERROR, "Send STA frame failed!");
                }
            }
        }
        returnCode = src_ap.deleteVAPRequest(src, target);
        if (returnCode == Csts.SYNC_REQUEST_OK) {
            if (pool_sta_ok) {
                returnCode = Csts.MIGRATION_SUCCESSFUL_STA_DETECTED;
            } else {
                returnCode = Csts.MIGRATION_SUCCESSFUL;
            }
            target.setCtrlIface(newIface);
            target.setvIfaceName(newVIfaceName);
            dst.addVAP(target);
        } else {
            returnCode = Csts.DEL_AP_FROM_OLD_VAP_FAILED;
        }
        suspendUpdate = false;
        return returnCode;
    }

    public int rollbackUnsuccessfulMigration(AP dst_ap, String newVIfaceName, VirtualAP target, int errorCode) {
        int returnCode = dst_ap.rollbackVAPRemove(newVIfaceName);
        if (returnCode == Csts.SYNC_REQUEST_OK) {
            returnCode = Csts.MIGRATION_ROLLBACK_SUCCESSFUL * errorCode;
        } else {
            returnCode = Csts.MIGRATION_ROLLBACK_FAILED * errorCode;
        }
        suspendUpdate = false;
        return returnCode;
    }

    private boolean wasSuccessful(int code) {
        return code == 0;
    }

    public AP getAPById(String id) {
        if (phy_aps.containsKey(id)) {
            return phy_aps.get(id);
        } else {
            return null;
        }
    }

    public int removeVAPRESTCmd(String vap_id) {
        VirtualAP vap = getVAPById(vap_id);
        if (vap != null) {
            if(Csts.BLOCK_MAIN_VAP_OPERATIONS && vap.isMainVAP()){
                return Csts.VAP_CANNOT_BE_REMOVED;
            }
            AP ap = getAPByVAPId(vap.getId());
            return ap.deleteVAPRequest(vap);
        }
        return Csts.VAP_NOT_FOUND;
    }

    public AP getAPByCtrlIfaceId(String ctrl_iface_id) {
        for (AP phy_ap : phy_aps.values()) {
            if (phy_ap.getCtrlIface().getId().equals(ctrl_iface_id)) {
                return phy_ap;
            }
        }
        return null;
    }

    public ArrayList<AP> getAllAPs() {
        if (phy_aps != null && phy_aps.size() > 0) {
            return new ArrayList<>(phy_aps.values());
        } else {
            return null;
        }
    }

    public VirtualAP getVAPById(String id) {
        for (AP ap : phy_aps.values()) {
            VirtualAP vap = ap.getVAPById(id);
            if (vap != null) {
                return vap;
            }
        }
        return null;
    }

    public AP getAPByVAPId(String id) {
        for (AP ap : phy_aps.values()) {
            VirtualAP vap = ap.getVAPById(id);
            if (vap != null) {
                return ap;
            }
        }
        return null;
    }

    public void phyIfaceEnabledEvent(String ctrl_iface_id) {
        AP ap = getAPByCtrlIfaceId(ctrl_iface_id);
        ap.setPhyIfaceState(ctrl_iface_id, true);
    }

    public void phyIfaceDisabledEvent(String ctrl_iface_id) {
        AP ap = getAPByCtrlIfaceId(ctrl_iface_id);
        ap.setPhyIfaceState(ctrl_iface_id, false);
    }

    int createDefaultVAPRESTCmd(String ap_id, String phy_name) {
        AP ap = phy_aps.get(ap_id);
        PhyIface phy = ap.getPhyByName(phy_name);
        HexAddress bssid = phy.getNextAvailableBSSID();
        if(bssid != null){
            return ap.createNewVAP(phy,bssid);
        } else {
            return Csts.SYNC_REQUEST_FAILED;
        }
    }
}
