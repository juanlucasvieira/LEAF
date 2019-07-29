package br.uff.vAPcontroller;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CtrlInterface implements Observer {

//    private DatagramSocket udp_socket;
    private String cookie = null;
    private int port;
    private InetAddress ip;
    private String id;
    DatagramPacket request;
    DatagramPacket response;

    public CtrlInterface(InetAddress ip, int port) {
        this.ip = ip;
        this.port = port;
        id = ip.getHostAddress() + ":" + port;
    }

//    public void bind() {
//        try {
//            udp_socket = new DatagramSocket();
//            String answer = sendReceive("GET_COOKIE");
//            if (sendReceive("GET_COOKIE").contains("COOKIE=")){
//                cookie = answer;
//            }
//            System.out.println(cookie);
//        } catch (SocketException e) {
//            System.err.println(e);
//        }
//    }
    public int getPort() {
        return port;
    }

    public InetAddress getIp() {
        return ip;
    }

    @Override
    public String toString() {
        return id;
    }

    public String getCookie() {
        if (cookie != null) {
            return cookie;
        }
        return null;
    }

    public boolean isCookieSet() {
        return (cookie != null && cookie.length() > 0);
    }

    private void setCookie(String c) {
        if (c.startsWith("COOKIE=")) {
            c.replaceFirst("COOKIE=", "").trim();
        }
        this.cookie = c;
    }

//    public Transaction buildCookieRequest(){
//        
//    }
//    public void cookieRequest(){
//        try {
//            byte[] msg = "GET_COOKIE".getBytes();
//            request = new DatagramPacket(msg, msg.length, ip, port);
//            udp_socket.send(request);
//            byte[] buffer = new byte[512];
//            response = new DatagramPacket(buffer, buffer.length);
//            udp_socket.receive(response);
//            String answer = new String(buffer, 0, response.getLength());
//            if (sendReceive("GET_COOKIE").contains("COOKIE=")){
//                cookie = answer;
//            }
//        } catch (SocketException e) {
//            System.err.println(e);
//        } catch (IOException ex) {
//            Logger.getLogger(CtrlInterface.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }
//    public String sendReceive(String command){
//        try {
//            command = cookie + " " + command;
//            byte[] msg = command.getBytes();
//            request = new DatagramPacket(msg, msg.length, ip, port);
//            udp_socket.send(request);
//            byte[] buffer = new byte[512];
//            response = new DatagramPacket(buffer, buffer.length);
//            udp_socket.receive(response);
//            return new String(buffer, 0, response.getLength());
//        } catch (SocketException e) {
//            System.err.println(e);
//        } catch (IOException ex) {
//            Logger.getLogger(CtrlInterface.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return null;
//    }
//    public boolean checkConnectivity(){
//        return sendReceive("PING").equals("PONG");
//    }
    @Override
    public String getId() {
        return id;
    }

    @Override
    public void notify(Transaction t) {
        if (t.getRequest().equals(Cmds.GET_COOKIE)) {
            setCookie(t.getResponse());
        }
    }

    public void requestCookie(TransactionHandler handler) {
        if (!handler.isObserverRegistered(this)) {
            handler.registerObserver(this);
        }
        handler.pushAsyncTransaction(new Transaction(this.id, Cmds.GET_COOKIE, this));
    }
    
    public int requestCookieSync(TransactionHandler handler) {
        Transaction t = handler.pushSynchronousTransaction(new Transaction(this.id, Cmds.GET_COOKIE, this));
        if(t.getResponse() != null && t.getResponse().contains("COOKIE=")){
            this.setCookie(t.getResponse());
            return Cmds.SYNC_REQUEST_OK;
        } else if (t.getResponse().equals(Cmds.TIMEOUT)){
            return Cmds.SYNC_REQUEST_TIMEOUT;
        }
        return Cmds.SYNC_REQUEST_FAILED;
    }
    
    @JsonIgnore
    @Override
    public CtrlInterface getCtrlIface() {
        return this;
    }
}
