/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.uff.vAPcontroller;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author juan
 */
public class EventHandler {
    
    private Controller c;

    private ConcurrentHashMap<String, Event> waitEvents;

    public EventHandler(Controller c) {
        this.c = c;
        waitEvents = new ConcurrentHashMap<>();
    }

    public void receiveEvent(String senderId, String pcktData) {
        if(waitEvents.containsKey(senderId)){
            waitEvents.put(senderId, new Event(pcktData));
        } else {
            processEvent(senderId, pcktData);
        }
    }
    
    public void registerWaitIface(CtrlInterface iface){
        waitEvents.put(iface.getId(), new Event(""));
    }

    public String waitEvent(String eventType, CtrlInterface iface, int timeout) {
        long elapsedTime = 0;
        Instant start = Instant.now();

        while (!(elapsedTime > timeout)) {
            Event e = waitEvents.get(iface.getId());
            if (e != null && e.getText().contains(eventType)) {
                waitEvents.remove(iface.getId());
                return e.getText();
            }
            elapsedTime = Duration.between(start, Instant.now()).toMillis();
        }
        return null;
    }

    private void processEvent(String senderId, String pcktData) {
        //TODO: Process Event correctly
        Log.print(Log.INFO, "[EVENT] " +senderId+ " : " + pcktData + "");
    }

}
