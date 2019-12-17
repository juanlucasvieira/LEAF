package br.uff.vAPcontroller;

import br.uff.vAPcontroller.Csts;
import br.uff.vAPcontroller.CtrlInterface;
import br.uff.vAPcontroller.Observer;
import br.uff.vAPcontroller.TransactionHandler;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AP implements Observer {

    private CtrlInterface gci;
    private ConcurrentHashMap<String, PhyIface> phy_ifaces;
    private ConcurrentHashMap<String, CtrlInterface> availableCtrlIfaces;
    private String ap_id;
    private TransactionHandler handler;
    private EventHandler eHandler;
//    private String ether_ifname;

    public AP(String id, InetAddress ip, int port) {
        this.ap_id = id;
        this.gci = new CtrlInterface(ip, port);
    }

    AP(String id, InetAddress ip, int port, TransactionHandler handler, EventHandler eHandler) {
        this.ap_id = id;
        this.gci = new CtrlInterface(ip, port);
        this.handler = handler;
        this.eHandler = eHandler;
        this.availableCtrlIfaces = new ConcurrentHashMap<>();
        this.phy_ifaces = new ConcurrentHashMap<>();
//        this.ether_ifname = ether_ifname;
    }

    @Override
    public String getId() {
        return ap_id;
    }

    public String getStringAddress() {
        return gci.getIp().getHostAddress() + ":" + gci.getPort();
    }

    @Override
    public CtrlInterface getCtrlIface() {
        return gci;
    }

//    public void vAPUpdate() {
//        for (VirtualAP vap : vaps.values()) {
//            vap.update(handler);
//        }
//    }
    //The parameter handler is not necessary!
    public int STAReceiveRequest(TransactionHandler handler, Station movingSta, CtrlInterface iface) {
        String request = Csts.buildSTAReceiveRequest(movingSta);
        return handler.sendSyncRequest(this, request, iface);
    }

    //The parameter handler is not necessary!
    public int sendSTAFrameRequest(TransactionHandler handler, Station movingSta, CtrlInterface iface) {
        String request = Csts.buildSendFrameRequest(movingSta);
        return handler.sendSyncRequest(this, request, iface);
    }

    public int pollSTA(Station sta, CtrlInterface iface) {
        String request = Csts.buildPollStaRequest(sta);

        long elapsedTime = 0;
        Instant start = Instant.now();
        eHandler.registerWaitIface(iface);
        while (!(elapsedTime > Csts.POLL_STA_TIMEOUT_MILLIS)) {
            if (handler.sendSyncRequest(this, request, iface) != Csts.SYNC_REQUEST_OK) {
                return Csts.SYNC_REQUEST_FAILED;
            }
            String s = eHandler.waitEvent(Event.AP_STA_POLL_OK, iface, 100);
            if (s != null && s.contains(sta.getMacAddress().toString())) {
                return Csts.SYNC_REQUEST_OK;
            }
            elapsedTime = Duration.between(start, Instant.now()).toMillis();
        }
        return Csts.SYNC_REQUEST_TIMEOUT;
    }

    public synchronized void loop() {
        if (!handler.isObserverRegistered(this)) {
            handler.registerObserver(this);
        }
        if (!gci.isCookieSet()) {
            gci.requestCookie(handler);
        } else {
            handler.pushAsyncTransaction(new Transaction(this.ap_id, Csts.GET_AP_IFACES, gci));
            for (CtrlInterface ctrl_iface : availableCtrlIfaces.values()) {
                if (!ctrl_iface.isCookieSet()) {
                    ctrl_iface.requestCookie(handler);
                } else {
                    handler.pushAsyncTransaction(new Transaction(this.ap_id, Csts.REQ_STATUS_INFO, ctrl_iface));
                }
                for (PhyIface phy : phy_ifaces.values()) {
                    phy.update(handler);
                }
            }
            if (Csts.CREATE_VAP_AUTOMATICALLY && isAPFilledWithSTAs() && !isAPFilledWithVAPs()) {
                PhyIface phy = getNextAvailableIface();
                if (phy != null && phy.getNextAvailableBSSID() != null) {
                    Log.print(Log.INFO, "Creating new VAP automatically. All current vAPs have an STA.");
                    createNewVAP(phy, phy.getNextAvailableBSSID());
                }
            }
        }
    }

    @Override
    public void notify(Transaction t) {
        switch (t.getRequest()) {
            case Csts.GET_AP_IFACES:
                discoverCtrlIfaces(t.getResponse(), t.getDestination());
                break;
            case Csts.REQ_STATUS_INFO:
                discoverPhysicalAndVirtualIfaces(t.getResponse());
                break;
        }
    }

    private void discoverCtrlIfaces(String response, CtrlInterface iface) {
        //wlp9s0 ctrl_iface=udp:8881
        String[] lines = response.split("\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            String interfaceName = line.split(" ")[0];
            int port = Integer.parseInt(line.substring(line.lastIndexOf(':') + 1));
            String iface_id = this.gci.getIp().getHostAddress() + ":" + port;
            if (!availableCtrlIfaces.containsKey(iface_id)) {
                availableCtrlIfaces.put(iface_id, new CtrlInterface(this.gci.getIp(), port));
            }
        }
    }
    
    private void removeAvailableCtrlIface(CtrlInterface ctrl){
        ctrl.detach(handler);
        ctrl.deinit(handler);
        availableCtrlIfaces.remove(ctrl.getId());
    }

    private void discoverPhysicalAndVirtualIfaces(String response) {
//        availableCtrlIfaces.get(iface.getId());
        String[] lines = response.split("\n");
        int errors = 0;
        int endOfPhyIfaceInfo = -1;
        boolean foundInfo = false;
        for (int i = 0; i < lines.length; i++) {
            if (!foundInfo) {
                if (lines[i].startsWith("bss[")) {
                    endOfPhyIfaceInfo = i;
                    errors += parsePhyIfaceInfo(lines, endOfPhyIfaceInfo);
                    foundInfo = true;
                }
            }
        }

    }

    public void setPhyIfaceState(String ctrl_iface_id, boolean state) {
        PhyIface disabled = getPhyByCtrlIfaceId(ctrl_iface_id);
        if (disabled != null) {
            disabled.setState(false);
        } else {
            Log.print(Log.ERROR, "Could not set phy enabled / disabled!!");
        }
    }

    private PhyIface getPhyByCtrlIfaceId(String ctrl_iface_id) {
        for (Map.Entry<String, CtrlInterface> set : availableCtrlIfaces.entrySet()) {
            if (set.getValue().getId().equals(ctrl_iface_id)) {
                return getPhyByName(set.getKey());
            }
        }
        return null;
    }

    public ConcurrentHashMap<String, PhyIface> getPhy_ifaces() {
        return phy_ifaces;
    }

    public ConcurrentHashMap<String, CtrlInterface> getAvailableCtrlIfaces() {
        return availableCtrlIfaces;
    }

    private int parsePhyIfaceInfo(String[] lines, int endOfPhyIfaceInfo) {
        /*
        state=ENABLED
        phy=wlp9s0
        freq=2412
        num_sta_non_erp=0
        num_sta_no_short_slot_time=1
        num_sta_no_short_preamble=0
        olbc=0
        num_sta_ht_no_gf=0
        num_sta_no_ht=0
        num_sta_ht_20_mhz=0
        num_sta_ht40_intolerant=0
        olbc_ht=0
        ht_op_mode=0x0
        cac_time_seconds=0
        cac_time_left_seconds=N/A
        channel=1
        secondary_channel=0
        ieee80211n=0
        ieee80211ac=0
        beacon_int=100
        dtim_period=2
        supported_rates=02 04 0b 16
        max_txpower=20
         */
        String phy_iface_name = "";
        boolean state = false;
        int frequency = -1;
        int channel = -1;
        int[] supported_rates = null;
        int max_txpower = -1;
        int max_vap_num = -1;

        for (int i = 0; i < endOfPhyIfaceInfo; i++) {
            if (lines[i].startsWith("state")) {
                if (lines[i].split("=")[1].equalsIgnoreCase("enabled")) {
                    state = true;
                } else {
                    state = false;
                }
            } else if (lines[i].startsWith("phy")) {
                phy_iface_name = lines[i].split("=")[1];
            } else if (lines[i].startsWith("freq")) {
                frequency = Integer.parseInt(lines[i].split("=")[1]);
            } else if (lines[i].startsWith("channel")) {
                channel = Integer.parseInt(lines[i].split("=")[1]);
            } else if (lines[i].startsWith("max_ap_num=")) {
                max_vap_num = Integer.parseInt(lines[i].split("=")[1]);
            } else if (lines[i].startsWith("supported_rates")) {
                String[] rates = lines[i].split("=")[1].split(" ");
                supported_rates = new int[rates.length];
                for (int j = 0; j < rates.length; j++) {
                    supported_rates[j] = Integer.parseInt(rates[j], 16);
                }
            } else if (lines[i].startsWith("max_txpower")) {
                max_txpower = Integer.parseInt(lines[i].split("=")[1]);
            }
        }
        if (frequency > 0 && channel > 0
                && max_txpower > 0 && phy_iface_name.length() > 0
                && (supported_rates != null && supported_rates.length > 0)) {
            PhyIface phy;
            if (!phy_ifaces.containsKey(phy_iface_name)) {
                phy = new PhyIface(phy_iface_name, frequency, channel, state, supported_rates, max_txpower, max_vap_num);
                phy_ifaces.put(phy.getIface_name(), phy);
            } else {
                phy = phy_ifaces.get(phy_iface_name);
                phy.setIface_name(phy_iface_name);
                phy.setFrequency(frequency);
                phy.setChannel(channel);
                phy.setState(state);
                phy.setSupported_rates(supported_rates);
                phy.setMax_txpower(max_txpower);
                phy.setMaxVapNum(max_vap_num);
            }
            parseVAPsInfo(phy, lines, endOfPhyIfaceInfo);
            return 0;
        } else {
            Log.print(Log.ERROR, "Error in physical interface information filling.");
            return 1;
        }
    }

    public VirtualAP getVAPById(String vap_id) {
        for (PhyIface phy : phy_ifaces.values()) {
            VirtualAP vap = phy.getVAPByID(vap_id);
            if (vap != null) {
                return vap;
            } else {
//                Log.print(Log.ERROR, "VAP not found!");
                return null;
            }
        }
        return null;
    }

    public PhyIface getPhyByName(String phy_name) {
        return phy_ifaces.get(phy_name);
    }

    private void parseVAPsInfo(PhyIface phy, String[] lines, int startOfVAPInfo) {
        /*
        bss[0]=wlp9s0
        bssid[0]=2c:d0:5a:42:73:13
        ctrl_interface[0]=udp:8881
        ssid[0]=TESTE_CSA_CH1
        num_sta[0]=1
         */
        int j = 0;
        while (j < lines.length) {
            String v_iface_name = "", bss = "", ssid = "";
            int port = -1;
            short sta_number = -1;
            boolean first_bss = false;
            for (j = startOfVAPInfo; j < (startOfVAPInfo + 5); j++) {
                if (lines[j].startsWith("bss[")) {
                    if(lines[j].startsWith("bss[0]")){
                        first_bss = true;
                    }
                    v_iface_name = lines[j].split("=")[1];
                } else if (lines[j].startsWith("bssid[")) {
                    bss = lines[j].split("=")[1];
                } else if (lines[j].startsWith("ctrl_interface[")) {
                    port = Integer.parseInt(lines[j].split(":")[1]);
                } else if (lines[j].startsWith("ssid[")) {
                    ssid = lines[j].split("=")[1];
                } else if (lines[j].startsWith("num_sta[")) {
                    sta_number = Short.parseShort(lines[j].split("=")[1]);
                }
            }
            if (sta_number >= 0 && bss.length() > 0 && ssid.length() > 0) {
                VirtualAP vap = phy.getVAPByName(v_iface_name);
                if (vap == null) {
                    String ctrlIfaceID = this.gci.getIp().getHostAddress() + ":" + port;
                    CtrlInterface vapIface = availableCtrlIfaces.get(ctrlIfaceID);
                    phy.addVAP(new VirtualAP(Csts.generateRandomUUID(),
                            v_iface_name, new HexAddress(bss), vapIface, ssid, sta_number, first_bss));
                } else {
                    vap.setvIfaceName(v_iface_name);
                    vap.setStaNumber(sta_number);
                    vap.setSsid(ssid);
                }
            } else {
                Log.print(Log.ERROR, "Error in virtual AP information filling.");
            }
            startOfVAPInfo = j;
        }
    }

    //TODO: Choose best physical iface
    public PhyIface getBestPhyIface(VirtualAP target) {
        return getNextAvailableIface();
    }

    //TODO: Algoritmo de escolha? Escolher interface mais vazia.
    public PhyIface getNextAvailableIface() {
        PhyIface chosen = null;
        for (PhyIface phy : phy_ifaces.values()) {
            if (!phy.isFilled()) {
                if (chosen == null || chosen.getNumberOfVAPs() > phy.getNumberOfVAPs()) {
                    chosen = phy;
                }
            }
        }
        return chosen;
    }

    public boolean isPhyIfacesEmpty() {
        return phy_ifaces.isEmpty();
    }

    public int getNextAvailableCtrlIfacePort() {
        int greater = 0;
        for (CtrlInterface c : availableCtrlIfaces.values()) {
            if (c.getPort() > greater) {
                greater = c.getPort();
            }
        }
        return greater + 1;
    }

    public PhyIface getPhyByVAPId(String vap_id) {
        for (PhyIface phy : phy_ifaces.values()) {
            if (phy.getVAPByID(vap_id) != null) {
                return phy;
            } else {
                Log.print(Log.ERROR, "Phy not found!");
                return null;
            }
        }
        return null;
    }

    int vAPReceiveRequest(VirtualAP target, PhyIface phy, int newPort, String new_iface_name) {
        String request = Csts.buildVAPReceiveRequest(target, phy, newPort, new_iface_name);
        return handler.sendSyncRequest(this, request);
    }

    int deleteVAPRequest(PhyIface phy, VirtualAP vap) {
        String request = Csts.buildVAPDeleteRequest(vap.getVirtualIfaceName());
        int replyCode = handler.sendSyncRequest(this, request);
        if (replyCode == 0) {
            HexAddress latestBSSID = vap.getBssId();
            this.removeAvailableCtrlIface(vap.getCtrlIface());
            phy.removeVAP(vap.getId());
            if (phy.getNumberOfVAPs() == 0 && Csts.CREATE_VAP_AUTOMATICALLY) {
                Log.print(Log.INFO, "Phy has no VAPs. Creating new VAP automatically. ");
                createNewVAP(phy, latestBSSID);
            }
        }
        return replyCode;
    }

    int rollbackVAPRemove(String v_iface_name) {
        String request = Csts.buildVAPDeleteRequest(v_iface_name);
        return handler.sendSyncRequest(this, request);
    }

    int deleteVAPRequest(VirtualAP vap) {
        PhyIface phy = getPhyByVAPId(vap.getId());
        return deleteVAPRequest(phy, vap);
    }

    public int createNewVAP(PhyIface phy, HexAddress bssid) {
        if (phy.isFilled()) {
            Log.print(Log.ERROR, "The specified physical interface cannot handle more vAPs!");
            return Csts.SYNC_REQUEST_FAILED;
        }
        CtrlInterface ctrl = new CtrlInterface(this.gci.getIp(), getNextAvailableCtrlIfacePort());
        String vIfaceName = Csts.getNewIfaceName(bssid, phy);
        boolean main_vap = false;
        if(phy.getNumberOfVAPs() == 0){
            main_vap = true;
        }
        VirtualAP newVAP = new VirtualAP(Csts.generateRandomUUID(), vIfaceName, bssid, ctrl, Csts.defaultNewSSID(bssid), (short) 0, main_vap);
        String request = Csts.buildNewVAPRequest(newVAP, phy);
        int response = handler.sendSyncRequest(this, request);
        if (response == Csts.SYNC_REQUEST_OK) {
            phy.addVAP(newVAP);
        }
        return response;
    }

    public boolean isAPFilledWithSTAs() {
        System.out.println("PhyIfaces size:: " + phy_ifaces.size());
        if (phy_ifaces.isEmpty()) {
            return false;
        }
        for (PhyIface phy : phy_ifaces.values()) {
            if (!phy.isVAPsFilledWithSTAs()) {
                return false;
            }
        }
        return true;
    }

    public boolean isAPFilledWithVAPs() {
        return getNextAvailableIface() == null;
    }

    void deinitialize() {
        for (CtrlInterface c : availableCtrlIfaces.values()) {
            c.deinit(handler);
        }
        for (PhyIface phy : phy_ifaces.values()) {
            phy.deinit(handler);
        }
        gci.deinit(handler);
        if (handler.isObserverRegistered(this)) {
            handler.removeObserver(this);
        }
    }
}
