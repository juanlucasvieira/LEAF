package br.uff.vAPcontroller;

public class Station {
    private final HexAddress mac_address;
    private short aid;
    private int capabilities;
    private int[] supported_rates;
    private int listen_interval;
    private boolean associated;
    private boolean authenticated;
    private boolean authorized;
    private boolean short_preamble;
    
    //These (probably) will not be passed to a new VAP.
    private long rx_packets;
    private long tx_packets;
    private long rx_bytes;
    private long tx_bytes;
    private long inactive_msec;
    private int signal;
    private int rx_rate_info;
    private int tx_rate_info;
    private long connected_time;

    public Station(HexAddress mac, short aid, int capabilities, int[] supported_rates, int listen_interval, boolean associated, boolean authenticated, boolean authorized) {
        this.mac_address = mac;
        this.aid = aid;
        this.capabilities = capabilities;
        this.supported_rates = supported_rates;
        this.listen_interval = listen_interval;
        this.associated = associated;
        this.authenticated = authenticated;
        this.authorized = authorized;
    }

    public void setAid(short aid) {
        this.aid = aid;
    }

    public void setCapabilities(int capabilities) {
        this.capabilities = capabilities;
    }

    public void setSupportedRates(int[] supported_rates) {
        this.supported_rates = supported_rates;
    }

    public void setListenInterval(int listen_interval) {
        this.listen_interval = listen_interval;
    }

    public void setAssociated(boolean associated) {
        this.associated = associated;
    }

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }

    public void setAuthorized(boolean authorized) {
        this.authorized = authorized;
    }

    public void setShortPreamble(boolean short_preamble) {
        this.short_preamble = short_preamble;
    }

    public void setRxPackets(long rx_packets) {
        this.rx_packets = rx_packets;
    }

    public void setTxPackets(long tx_packets) {
        this.tx_packets = tx_packets;
    }

    public void setRxBytes(long rx_bytes) {
        this.rx_bytes = rx_bytes;
    }

    public void setTxBytes(long tx_bytes) {
        this.tx_bytes = tx_bytes;
    }

    public void setInactiveMillis(long inactive_msec) {
        this.inactive_msec = inactive_msec;
    }

    public void setSignal(int signal) {
        this.signal = signal;
    }

    public void setRxRateInfo(int rx_rate_info) {
        this.rx_rate_info = rx_rate_info;
    }

    public void setTxRateInfo(int tx_rate_info) {
        this.tx_rate_info = tx_rate_info;
    }

    public void setConnectedTime(long connected_time) {
        this.connected_time = connected_time;
    }
  
    public HexAddress getMacAddress() {
        return mac_address;
    }

    public short getAid() {
        return aid;
    }

    public int getCapabilities() {
        return capabilities;
    }

    public int[] getSupportedRates() {
        return supported_rates;
    }

    public int getListenInterval() {
        return listen_interval;
    }

    public boolean isShortPreamble() {
        return short_preamble;
    }

    public long getRxPackets() {
        return rx_packets;
    }

    public long getTxPackets() {
        return tx_packets;
    }

    public long getRxBytes() {
        return rx_bytes;
    }

    public long getTxBytes() {
        return tx_bytes;
    }

    public long getInactiveMillis() {
        return inactive_msec;
    }

    public int getSignal() {
        return signal;
    }

    public int getRxRateInfo() {
        return rx_rate_info;
    }

    public int getTxRateInfo() {
        return tx_rate_info;
    }

    public long getConnectedTime() {
        return connected_time;
    }

    public boolean isAssociated() {
        return associated;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public boolean isAuthorized() {
        return authorized;
    }
        
}
