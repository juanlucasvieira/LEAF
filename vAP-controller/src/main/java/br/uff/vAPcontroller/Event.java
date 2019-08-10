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
public class Event {

    public static final String AP_STA_CONNECTED = "AP-STA-CONNECTED ";
    public static final String AP_STA_DISCONNECTED = "AP-STA-DISCONNECTED ";
    public static final String AP_STA_POSSIBLE_PSK_MISMATCH = "AP-STA-POSSIBLE-PSK-MISMATCH ";
    public static final String AP_STA_POLL_OK = "AP-STA-POLL-OK ";

    public static final String AP_REJECTED_MAX_STA = "AP-REJECTED-MAX-STA ";
    public static final String AP_REJECTED_BLOCKED_STA = "AP-REJECTED-BLOCKED-STA ";

    public static final String AP_EVENT_ENABLED = "AP-ENABLED ";
    public static final String AP_EVENT_DISABLED = "AP-DISABLED ";

    public static final String INTERFACE_ENABLED = "INTERFACE-ENABLED ";
    public static final String INTERFACE_DISABLED = "INTERFACE-DISABLED ";

    private String text;

    public Event(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
