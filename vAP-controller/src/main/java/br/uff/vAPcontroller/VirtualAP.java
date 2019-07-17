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
    private int ctrl_port;
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
            case Cmds.REQ_ALL_STA_INFO:
                parseStaInfo(t.getResponse());
                break;
        }
    }

    void update(TransactionHandler handler) {
        handler.pushTransaction(new Transaction(this.vap_id, Cmds.REQ_STATUS_INFO, ctrl_iface));
        if (num_sta == 1) {
            handler.pushTransaction(new Transaction(this.vap_id, Cmds.REQ_ALL_STA_INFO, ctrl_iface));
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
        for (int i = 0; i < lines.length; i++) {
            if (startOfVAPInfo > 0 && endOfPhyIfaceInfo > 0) {
                if (lines[i].startsWith("bss[")) {
                    endOfPhyIfaceInfo = i;
                    String[] key_value = lines[i].split("=");
                    if (key_value[1].equals(v_iface_name)) {
                        startOfVAPInfo = i;
                        errors += parseVAPInfo(lines, startOfVAPInfo);
                        errors += parsePhyIfaceInfo(lines, endOfPhyIfaceInfo);
                    }
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
            System.out.println("Error in virtual AP information filling.");
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
                    supported_rates[j] = Integer.decode(rates[j]);
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
            System.out.println("Error in physical interface information filling.");
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
                    supported_rates[j] = Integer.decode(rates[j]);
                }
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
                sta.setSupportedRates(supported_rates);
                sta.setListenInterval(listen_interval);
            } else {
                System.out.println("Error while parsing STA information.");
            }
        }
    }

    public void changePhyIface(PhyIface newIface) {
        this.phy = newIface;
    }
}
