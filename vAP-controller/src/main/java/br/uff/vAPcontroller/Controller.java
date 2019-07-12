package br.uff.vAPcontroller;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Controller {
    
    private ArrayList<AP> phy_aps;
    private CommunicationHandler comm;
    private TransactionHandler thand;
    private static Controller c;
    
    public Controller() {
        this.phy_aps = new ArrayList<>();
    }
    
    public static Controller getInstance(){
        if(c == null){
            c = new Controller();
        }
        return c;
    }
       
    public void begin(){
        try {
            thand = new TransactionHandler();
            phy_aps.add(new AP("AP@1",InetAddress.getByName("127.0.0.1"), 8888,thand));
            phy_aps.get(0).requestInterfaces();
        } catch (UnknownHostException ex) {
            System.err.println("Unknown IP format");
        } catch (IOException ex) {
            Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
        
    public void run() throws InterruptedException{
        while(true){
            System.out.println("RUNNING");
            Thread.sleep(1000);
        }
//        for (AP phy_ap : phy_aps) {
//            phy_ap.update();
//        }
    }
    

    
    public String sendRequest(){
        return "GOT A REQUEST!!";
    }
    
    
    
}
