package br.uff.vAPcontroller;

import java.net.InetAddress;

public class VirtualAP implements Observer {

    private String id;

    private String v_iface_name;
    private String bss_id;
    private CtrlInterface ctrl_iface;
    private String ssid;
    private short num_sta;

    private Station sta;

    private short max_sta_num;

//    public VirtualAP(String vap_id, String v_iface_name, InetAddress ip, int port) {
//        this.ctrl_iface = new CtrlInterface(ip, port);
//        this.v_iface_name = v_iface_name;
//        this.id = vap_id;
//        this.num_sta = 0;
//        this.max_sta_num = 1;
//    }

    public VirtualAP(String id, String v_iface_name, String bss_id, CtrlInterface ctrl_iface, String ssid, short num_sta) {
        this.id = id;
        this.v_iface_name = v_iface_name;
        this.bss_id = bss_id;
        this.ctrl_iface = ctrl_iface;
        this.ssid = ssid;
        this.num_sta = num_sta;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void notify(Transaction t) {
        switch (t.getRequest()) {
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
            if (num_sta == 1) {
                handler.pushAsyncTransaction(new Transaction(this.id, Cmds.REQ_FIRST_STA_INFO, ctrl_iface, Transaction.ASYNC));
            }
        }
    }

    public void checkConnectivityToSTA() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
        long connected_time = -1;
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

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public void setNum_sta(short num_sta) {
        this.num_sta = num_sta;
    }

    

    public CtrlInterface getCtrl_iface() {
        return ctrl_iface;
    }

    public String getV_iface_name() {
        return v_iface_name;
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
