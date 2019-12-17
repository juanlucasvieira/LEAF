/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.uff.vAPcontroller;

import java.util.UUID;

/**
 *
 * @author juan
 */
public class Csts {

    //Configuration
    public static boolean DEBUG_LOG_LEVEL = false;
    
    /*Creates a new vAP when all vAPs of an AP have a station associated or
     when a physical interface reaches zero APs.*/
    public static boolean CREATE_VAP_AUTOMATICALLY = true;
        
    // Disables the CSA IE injection during the migration phase;
    public static boolean DISABLE_CSA = false;
    
    // Disables sending client frame upon a migration.
    public static boolean DISABLE_SEND_FRAME = false;
    
    // Disables STA injection client upon a migration. For test purposes.
    public static boolean DISABLE_STA_INJECTION = false;
    
    // Sends Deauth frames when a BSS is removed
    public static boolean SEND_DEAUTH_ENABLED = false;
    
    public static boolean BLOCK_MAIN_VAP_OPERATIONS = true;

    public static final int SEND_LISTEN_PORT_ASYNC = 9999;
    public static final int SEND_LISTEN_PORT_SYNC = 9998;

    public static final int CSA_COUNT = 1;
    public static final long CSA_WAITING_TIME_MILLIS = 1000;

    public static final int SYNC_TIMEOUT_MILLIS = 5000;
    public static final int POLL_STA_TIMEOUT_MILLIS = 5000;

    //Commands
    public static final String REQ_STATUS_INFO = "STATUS";
    public static final String REQ_STA_INFO = "STA";
    public static final String REQ_FIRST_STA_INFO = "STA-FIRST";
    public static final String GET_AP_IFACES = "INTERFACES ctrl";
    public static final String GET_COOKIE = "GET_COOKIE";
    public static final String REMOVE_VAP = "REMOVE";
    public static final String ATTACH = "ATTACH";
    public static final String DETACH = "DETACH";

    //Success Codes
    public static final int MIGRATION_SUCCESSFUL_STA_DETECTED = 0;
    public static final int MIGRATION_SUCCESSFUL = 1;

    //Error codes
    public static final int VAP_INJECTION_FAILED = 2;
    public static final int STA_INJECTION_FAILED = 3;
    public static final int FAILED_TO_SEND_CSA = 4;
    public static final int DEL_AP_FROM_OLD_VAP_FAILED = 5;
    public static final int SOURCE_AP_NOT_FOUND = 6;
    public static final int VAP_NOT_FOUND = 7;
    public static final int DST_AP_NOT_FOUND = 8;
    public static final int UNAVAILABLE_PHY_IFACE = 9;
    public static final int SPECIFIED_PHY_NOT_FOUND = 11;
    public static final int VAP_CANNOT_BE_MIGRATED = 12;
    public static final int VAP_CANNOT_BE_REMOVED = 13;
    public static final int AP_NOT_FOUND = 14;

    public static final int SYNC_REQUEST_OK = 0;
    public static final int SYNC_REQUEST_FAILED = 1;
    public static final int SYNC_REQUEST_TIMEOUT = 10;

    public static final String INVALID_COOKIE = "INVALID COOKIE";
    public static final String TIMEOUT = "TIMEOUT";

    public static final int MIGRATION_ROLLBACK_SUCCESSFUL = 1;
    public static final int MIGRATION_ROLLBACK_FAILED = -1;

    public static String buildVAPReceiveRequest(VirtualAP target, PhyIface dst_phy, int port, String iface_name) {

        return "ADD_BSS bss_config=" + dst_phy.getIface_name()
                + ":bss_params=" + "\""
                + "interface=" + iface_name + " "
                + "bridge=" + "vapbridge" + " "
                + "ssid=" + target.getSsid() + " "
                + "bssid=" + target.getBssId() + " "
                + (SEND_DEAUTH_ENABLED ? "" : "broadcast_deauth=0 ")
                + "ctrl_interface=udp:" + port + " "
                + "ctrl_interface_group=0" + " "
                + "channel=" + dst_phy.getChannel() + "\"";
    }

    public static String buildSTAReceiveRequest(Station sta) {

        //add_sta_p b8:27:eb:b1:83:2d "aid=1 capability=33 supported_rates=130|132|11|22 listen_interval=10 flag_associated=1 flag_authenticated=1 flag_authorized=1"
        return "ADD_STA_P " + sta.getMacAddress() + " \""
                + "aid=" + sta.getAid() + " "
                + "capability=" + sta.getCapabilities() + " "
                + "supported_rates=" + suppRatesToParam(sta.getSupportedRates()) + " "
                + "listen_interval=" + sta.getListenInterval() + " "
                + "flag_associated=" + (sta.isAssociated() ? 1 : 0) + " "
                + "flag_authenticated=" + (sta.isAuthenticated() ? 1 : 0) + " "
                + "flag_authorized=" + (sta.isAuthorized() ? 1 : 0) + "\"";
    }

    public static String suppRatesToParam(int[] supp_rates) {
        String s = "";
        for (int i = 0; i < supp_rates.length; i++) {
            s += supp_rates[i];
            if (i != supp_rates.length - 1) {
                s += "|";
            }
        }
        return s;
    }

    public static String buildSendCSARequest(int frequency, int beacon_count, boolean blocktx) {
        String s = "SEND_CSA " + beacon_count + " " + frequency;
        if (blocktx) {
            s += " blocktx";
        }
        return s;
    }

    public static String buildSendFrameRequest(Station sta) {
        // SEND_FRAME <dst> <src> <ifname>
        return "SEND_FRAME ff:ff:ff:ff:ff:ff " + sta.getMacAddress() + " "
                + "ifname=vapbridge";
    }

    public static String buildPollStaRequest(Station sta) {
        //POLL_STA a8:db:03:9e:03:03
        return "POLL_STA " + sta.getMacAddress();
    }

    static String buildVAPDeleteRequest(String iface) {
        return Csts.REMOVE_VAP + " " + iface;
    }

    public static String getNewIfaceName(HexAddress bssid, PhyIface dst_phy) {
        if (dst_phy.isEnabled()) {
            return "wl" + bssid.toString().replaceAll(":", "");
        } else {
            return dst_phy.getIface_name();
        }
    }

    public static String generateRandomUUID() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    public static String defaultNewSSID(HexAddress bssid) {
        return "vAP-" + bssid.toString().replaceAll(":", "");
    }

    public static String buildNewVAPRequest(VirtualAP newVAP, PhyIface phy) {
        return buildVAPReceiveRequest(newVAP, phy, newVAP.getCtrlIface().getPort(), newVAP.getVirtualIfaceName());
    }

}
