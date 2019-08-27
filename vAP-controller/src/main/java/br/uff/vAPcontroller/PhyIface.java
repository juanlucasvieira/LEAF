/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.uff.vAPcontroller;

import java.util.HashMap;

/**
 *
 * @author juan
 */
public class PhyIface {

    private String iface_name;
    private boolean state;
    private int frequency;
    private int channel;
    private int[] supported_rates;
    private int max_txpower;
    private int max_vap_num;
    private HashMap<String, VirtualAP> vaps;

    public PhyIface(String iface_name, int frequency, int channel, boolean state, int[] supported_rates, int max_txpower, int max_vap_num) {
        this.iface_name = iface_name;
        this.frequency = frequency;
        this.channel = channel;
        this.supported_rates = supported_rates;
        this.max_txpower = max_txpower;
        this.max_vap_num = max_vap_num;
        this.state = state;
        this.vaps = new HashMap<>();
    }

    public String getIface_name() {
        return iface_name;
    }

    public void setIface_name(String iface_name) {
        this.iface_name = iface_name;
    }

    public boolean isEnabled() {
        return state;
    }

    public HashMap<String, VirtualAP> getVaps() {
        return vaps;
    }

    public VirtualAP getVAPByID(String vap_id) {
        if (vaps.containsKey(vap_id)) {
            return vaps.get(vap_id);
        } else {
            return null;
        }
    }

    public VirtualAP getVAPByName(String vap_name) {
        for (VirtualAP vap : vaps.values()) {
            if (vap.getVirtualIfaceName().equals(vap_name)) {
                return vap;
            }
        }
        return null;
    }

    public void addVAP(VirtualAP vap) {
        vaps.put(vap.getId(), vap);
        if (!vaps.isEmpty()) {
            state = true;
        }
    }

    public void setState(boolean state) {
        this.state = state;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public int getChannel() {
        return channel;
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }

    public int[] getSupported_rates() {
        return supported_rates;
    }

    public void setSupported_rates(int[] supported_rates) {
        this.supported_rates = supported_rates;
    }

    public int getMax_txpower() {
        return max_txpower;
    }

    public void setMax_txpower(int max_txpower) {
        this.max_txpower = max_txpower;
    }

    public int getMaxVapNum() {
        return max_vap_num;
    }

    public void setMaxVapNum(int max_vap_num) {
        this.max_vap_num = max_vap_num;
    }

    public int getNumberOfVAPs() {
        return vaps.size();
    }
    
    public boolean isFilled(){
        return max_vap_num == vaps.size();
    }

    public HexAddress getNextAvailableBSSID() {
        HexAddress greater = vaps.values().iterator().next().getBssId();
        for (VirtualAP vap : vaps.values()) {
            HexAddress actual = vap.getBssId();
            if (greater.compareTo(actual) < 0) {
                greater = actual;
            }
        }
        return HexAddress.getNextAddr(greater);
    }

    void update(TransactionHandler handler) {
        for (VirtualAP vap : vaps.values()) {
            vap.update(handler);
        }
    }

    public boolean channelEqualsTo(PhyIface other) {
        return (this.channel == other.channel);
    }

    void removeVAP(String vapId) {
        this.vaps.remove(vapId);
        if (vaps.isEmpty()) {
            state = false;
        }
    }

    void removeVAP(VirtualAP vap) {
        this.vaps.remove(vap.getId());
        if (vaps.isEmpty()) {
            state = false;
        }
    }

    public boolean isVAPsFilledWithSTAs() {
        for (VirtualAP vap : vaps.values()) {
            if (!vap.reachedMaximum()) {
                return false;
            }
        }
        return true;
    }
}
