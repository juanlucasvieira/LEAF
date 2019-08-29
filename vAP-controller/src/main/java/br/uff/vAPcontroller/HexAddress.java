/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.uff.vAPcontroller;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Arrays;

/**
 *
 * @author juan
 */
public class HexAddress implements Comparable<HexAddress> {

    private int[] macAddr;

    public HexAddress(String mac) {
        this.macAddr = stringToMACAddress(mac);
    }

    public HexAddress(int[] mac) {
        this.macAddr = mac;
    }

    public static int[] stringToMACAddress(String mac) {
        String[] sAddr = mac.split(":");
        return stringHexToInt(sAddr);
    }

    public static int[] stringHexToInt(String[] s) {
        int[] aux = new int[6];
        for (int j = 0; j < aux.length; j++) {
            aux[j] = Integer.parseInt(s[j], 16);
        }
        return aux;
    }

    @JsonGetter("address")
    @Override
    public String toString() {
        String s = "";
        for (int i = 0; i < macAddr.length; i++) {
            String aux = Integer.toHexString(macAddr[i]);
            s += (aux.length() % 2 == 0 ? "" : "0") + aux;
            if (i < macAddr.length - 1) {
                s += ":";
            }
        }
        return s;
    }

    @JsonIgnore
    public int[] getMac() {
        return macAddr;
    }

    @JsonIgnore
    public static HexAddress getNextAddr(HexAddress mac) {
        int[] aux = mac.getMac().clone();
        boolean done = false;
        for (int i = aux.length - 1; i > 0; i--) {
            if (!done && aux[i] < 255) {
                aux[i] = aux[i] + 1;
                done = true;
            }
        }
        //Check if the MAC is FF:FF:FF:FF:FF:FF
        if(aux[0] == 255 && aux[1] == 255 && aux[2] == 255 && aux[3] == 255 && aux[4] == 255 && aux[5] == 255){
            return null;
        }
        if (done) {
            return new HexAddress(aux);
        } else {
            return null;
        }
    }

    @Override
    public int compareTo(HexAddress t) {
        return Arrays.compare(this.macAddr, t.getMac());
    }

    public boolean equals(HexAddress t) {
        return Arrays.equals(this.macAddr, t.getMac());
    }

}
