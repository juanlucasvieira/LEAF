package br.uff.vAPcontroller;

public class Station {
    private final String mac_address;
    private short aid;
    private int capabilities;
    private int[] supported_rates;
    private int listen_interval;
    private boolean associated;
    private boolean authenticated;
    private boolean authorized;

    public Station(String mac_address, short aid, int capabilities, int[] supported_rates, int listen_interval, boolean associated, boolean authenticated, boolean authorized) {
        this.mac_address = mac_address;
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
  
    public String getMacAddress() {
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
