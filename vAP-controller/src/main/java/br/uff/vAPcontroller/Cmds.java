/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.uff.vAPcontroller;

/**
 *
 * @author juan
 */
public class Cmds {

    //Commands
    public static final String REQ_STATUS_INFO = "STATUS";
    public static final String REQ_STA_INFO = "STA";
    public static final String REQ_FIRST_STA_INFO = "STA-FIRST";
    public static final String GET_AP_IFACES = "INTERFACES ctrl";
    public static final String GET_COOKIE = "GET_COOKIE";
    public static final String REMOVE_VAP = "REMOVE";
    public static final String ATTACH = "ATTACH";
    public static final String DETACH = "DETACH";

    public static final boolean DEBUG_LOG_LEVEL = true;

    public static final int SEND_LISTEN_PORT_ASYNC = 9999;
    public static final int SEND_LISTEN_PORT_SYNC = 9998;

    //Error codes
    public static final int MIGRATION_SUCCESSFUL = 0;
    public static final int VAP_INJECTION_FAILED = 1;
    public static final int STA_INJECTION_FAILED = 2;
    public static final int FAILED_TO_SEND_CSA = 3;
    public static final int DEL_AP_FROM_OLD_VAP_FAILED = 4;

    public static final int SYNC_REQUEST_OK = 0;
    public static final int SYNC_REQUEST_FAILED = 1;
    public static final int SYNC_REQUEST_TIMEOUT = 9;

    public static final String INVALID_COOKIE = "INVALID COOKIE";
    public static final String TIMEOUT = "TIMEOUT";
    public static final int SYNC_TIMEOUT_MILLIS = 5000;
    public static final int POLL_STA_TIMEOUT_MILLIS = 5000;

    public static final int MIGRATION_ROLLBACK_SUCCESSFUL = 1;
    public static final int MIGRATION_ROLLBACK_FAILED = -1;

    public static final int CSA_COUNT = 1;
    public static final long CSA_WAITING_TIME_MILLIS = 1000;

    public static String buildVAPReceiveRequest(VirtualAP target, PhyIface dst_phy, int port, String iface_name) {

        return "ADD_BSS bss_config=" + dst_phy.getIface_name()
                + ":bss_params=" + "\""
                + "interface=" + iface_name + " "
                + "bridge=" + "vapbridge" + " "
                + "ssid=" + target.getSsid() + " "
                + "bssid=" + target.getBss_id() + " "
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
        return Cmds.REMOVE_VAP + " " + iface;
    }

    public static String getNewIfaceName(VirtualAP target, PhyIface dst_phy) {
        if (dst_phy.isEnabled()) {
            return "wl" + target.getBss_id().replace(":", "");
        } else {
            return dst_phy.getIface_name();
        }
    }
}
