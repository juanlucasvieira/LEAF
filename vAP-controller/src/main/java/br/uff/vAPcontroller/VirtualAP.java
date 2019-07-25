package br.uff.vAPcontroller;

import br.uff.vAPcontroller.Cmds;
import br.uff.vAPcontroller.CtrlInterface;
import br.uff.vAPcontroller.Observer;
import br.uff.vAPcontroller.TransactionHandler;
import java.net.InetAddress;

public class VirtualAP implements Observer {

    private CtrlInterface ctrl_iface;
    private PhyIface phy;
    private String v_iface_name;
    private String vap_id;
    private String bss_id;
    private String ssid;
    private Station sta;
    private short num_sta;
    private short max_sta_num;

    public VirtualAP(String vap_id, String v_iface_name, InetAddress ip, int port) {
        this.ctrl_iface = new CtrlInterface(ip, port);
        this.v_iface_name = v_iface_name;
        this.vap_id = vap_id;
        this.num_sta = 0;
        this.max_sta_num = 1;
    }

    @Override
    public String getId() {
        return vap_id;
    }

    @Override
    public void notify(Transaction t) {
        switch (t.getRequest()) {
            case Cmds.REQ_STATUS_INFO:
                parseStatusInfo(t.getResponse());
                break;
            case Cmds.REQ_FIRST_STA_INFO:
                parseStaInfo(t.getResponse());
                break;
        }
    }

    void update(TransactionHandler handler) {
        if (!handler.isObserverRegistered(this)) {
            handler.registerObserver(this);
        }
        if (!this.ctrl_iface.isCookieSet()) {
            ctrl_iface.requestCookie(handler);
        } else {
            handler.pushTransaction(new Transaction(this.vap_id, Cmds.REQ_STATUS_INFO, this.ctrl_iface));
            if (num_sta == 1) {
                handler.pushTransaction(new Transaction(this.vap_id, Cmds.REQ_FIRST_STA_INFO, ctrl_iface));
            }
        }
    }

    public void checkConnectivityToSTA() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void parseStatusInfo(String response) {
        String[] lines = response.split("\n");
        int errors = 0;
        int endOfPhyIfaceInfo = -1;
        int startOfVAPInfo = -1;
        boolean foundInfo = false;
        for (int i = 0; i < lines.length; i++) {
            if (!foundInfo) {
                if (lines[i].startsWith("bss[")) {
                    endOfPhyIfaceInfo = i;
                    String[] key_value = lines[i].split("=");
                    if (key_value[1].equals(v_iface_name)) {
                        startOfVAPInfo = i;
                        errors += parseVAPInfo(lines, startOfVAPInfo);
                        errors += parsePhyIfaceInfo(lines, endOfPhyIfaceInfo);
                    }
                }
                if (startOfVAPInfo > 0 && endOfPhyIfaceInfo > 0) {
                    foundInfo = true;
                }
            }
        }

    }

    private int parseVAPInfo(String[] lines, int startOfVAPInfo) {
        /*
        bss[0]=wlp9s0
        bssid[0]=2c:d0:5a:42:73:13
        ctrl_interface[0]=udp:8881
        ssid[0]=TESTE_CSA_CH1
        num_sta[0]=1
         */

        String bss_aux = "", ssid_aux = "";
        short sta_number = -1;
        for (int j = startOfVAPInfo; j < (startOfVAPInfo + 5); j++) {
            if (lines[j].startsWith("bssid[")) {
                bss_aux = lines[j].split("=")[1];
            } else if (lines[j].startsWith("ssid[")) {
                ssid_aux = lines[j].split("=")[1];
            } else if (lines[j].startsWith("num_sta[")) {
                sta_number = Short.parseShort(lines[j].split("=")[1]);
            }
        }
        if (sta_number >= 0 && bss_aux.length() > 0 && ssid_aux.length() > 0) {
            this.bss_id = bss_aux;
            this.ssid = ssid_aux;
            this.num_sta = sta_number;
            return 0;
        } else {
            Log.print(Cmds.ERROR, "Error in virtual AP information filling.");
            return 1;
        }
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
        String iface_name = "";
        boolean state = false;
        int frequency = -1;
        int channel = -1;
        int[] supported_rates = null;
        int max_txpower = -1;

        for (int i = 0; i < endOfPhyIfaceInfo; i++) {
            if (lines[i].startsWith("state")) {
                if (lines[i].split("=")[1].equalsIgnoreCase("enabled")) {
                    state = true;
                } else {
                    state = false;
                }
            } else if (lines[i].startsWith("phy")) {
                iface_name = lines[i].split("=")[1];
            } else if (lines[i].startsWith("freq")) {
                frequency = Integer.parseInt(lines[i].split("=")[1]);
            } else if (lines[i].startsWith("channel")) {
                channel = Integer.parseInt(lines[i].split("=")[1]);
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
                && max_txpower > 0 && iface_name.length() > 0
                && (supported_rates != null && supported_rates.length > 0)) {
            if (phy == null) {
                phy = new PhyIface(iface_name, frequency, channel, state, supported_rates, max_txpower);
            } else {
                phy.setIface_name(iface_name);
                phy.setFrequency(frequency);
                phy.setChannel(channel);
                phy.setState(state);
                phy.setSupported_rates(supported_rates);
                phy.setMax_txpower(max_txpower);
            }
            return 0;
        } else {
            Log.print(Cmds.ERROR, "Error in physical interface information filling.");
            return 1;
        }
    }

    private void parseStaInfo(String response) {
        /*
        a8:db:03:9e:03:03
        flags=[AUTH][ASSOC][AUTHORIZED][SHORT_PREAMBLE]
        aid=1
        capability=0x21
        listen_interval=10
        supported_rates=82 84 0b 16
        timeout_next=NULLFUNC POLL
        rx_packets=34039
        tx_packets=20377
        rx_bytes=2736076
        tx_bytes=9770641
        inactive_msec=1448
        signal=-37
        rx_rate_info=10
        tx_rate_info=110
        connected_time=4056
        supp_op_classes=51707374757c7d7e7f808182767778797a7b515354
        min_txpower=5
        max_txpower=19
        ext_capab=0000000000000040
         */
        String[] lines = response.split("\n");
        String mac = lines[0];
        boolean auth = false, assoc = false,
                authorized = false, short_preamble = false;
        short aid = -1;
        int capability = -1, listen_interval = -1;
        long rx_packets = -1, tx_packets = -1, rx_bytes = -1, tx_bytes = -1;
        long inactive_msec = -1;
        int signal = 0;
        int rx_rate_info = 0;
        int tx_rate_info = 0;
        long connected_time= -1;
        int[] supported_rates = null;
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i];
            if (line.startsWith("flags=")) {
                if (line.contains("AUTHORIZED")) {
                    authorized = true;
                } else if (line.contains("ASSOC")) {
                    assoc = true;
                } else if (line.contains("AUTH")) {
                    auth = true;
                } else if (line.contains("SHORT_PREAMBLE")) {
                    short_preamble = true;
                }
            } else if (line.startsWith("aid=")) {
                aid = Short.parseShort(line.split("=")[1]);
            } else if (line.startsWith("capability=")) {
                capability = Integer.decode(line.split("=")[1]);
            } else if (line.startsWith("listen_interval=")) {
                listen_interval = Integer.parseInt(line.split("=")[1]);
            } else if (line.startsWith("supported_rates=")) {
                String[] rates = line.split("=")[1].split(" ");
                supported_rates = new int[rates.length];
                for (int j = 0; j < rates.length; j++) {
                    supported_rates[j] = Integer.parseInt(rates[j], 16);
                }
            } else if (line.startsWith("rx_packets=")) {
                rx_packets = Long.parseLong(line.split("=")[1]);
            } else if (line.startsWith("tx_packets=")) {
                tx_packets = Long.parseLong(line.split("=")[1]);
            } else if (line.startsWith("rx_bytes=")) {
                rx_bytes = Long.parseLong(line.split("=")[1]);
            } else if (line.startsWith("tx_bytes=")) {
                tx_bytes = Long.parseLong(line.split("=")[1]);
            } else if (line.startsWith("inactive_msec=")) {
                inactive_msec = Long.parseLong(line.split("=")[1]);
            } else if (line.startsWith("signal=")) {
                signal = Integer.parseInt(line.split("=")[1]);
            } else if (line.startsWith("rx_rate_info=")) {
                rx_rate_info = Integer.parseInt(line.split("=")[1]);
            } else if (line.startsWith("tx_rate_info=")) {
                tx_rate_info = Integer.parseInt(line.split("=")[1]);
            } else if (line.startsWith("connected_time=")) {
                connected_time = Long.parseLong(line.split("=")[1]);
            }
        }
        if ((supported_rates != null && supported_rates.length > 0)
                && aid >= 0 && capability >= 0 && listen_interval >= 0) {
            if (sta == null) {
                sta = new Station(mac, aid, capability, supported_rates,
                        listen_interval, assoc, auth, authorized);
            } else if (sta.getMacAddress().equals(mac)) {
                sta.setAid(aid);
                sta.setAssociated(assoc);
                sta.setAuthenticated(auth);
                sta.setAuthorized(authorized);
                sta.setCapabilities(capability);
                sta.setShort_preamble(short_preamble);
                sta.setSupportedRates(supported_rates);
                sta.setListenInterval(listen_interval);
                sta.setRx_bytes(rx_bytes);
                sta.setTx_bytes(tx_bytes);
                sta.setRx_packets(rx_packets);
                sta.setTx_packets(tx_packets);
                sta.setInactive_msec(inactive_msec);
                sta.setSignal(signal);
                sta.setTx_rate_info(tx_rate_info);
                sta.setRx_rate_info(rx_rate_info);
                sta.setConnected_time(connected_time);
            } else {
                System.out.println("Error while parsing STA information.");
            }
        }
    }

    public void changePhyIface(PhyIface newIface) {
        this.phy = newIface;
    }

    public CtrlInterface getCtrl_iface() {
        return ctrl_iface;
    }

    public PhyIface getPhy() {
        return phy;
    }

    public String getV_iface_name() {
        return v_iface_name;
    }

    public String getVap_id() {
        return vap_id;
    }

    public String getBss_id() {
        return bss_id;
    }

    public String getSsid() {
        return ssid;
    }

    public Station getSta() {
        return sta;
    }

    public short getNum_sta() {
        return num_sta;
    }

    public short getMax_sta_num() {
        return max_sta_num;
    }
    
    
}
