package br.uff.vAPcontroller;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CtrlInterface implements Observer {

//    private DatagramSocket udp_socket;
    private String cookie = null;
    private int port;
    private InetAddress ip;
    private String id;
//    DatagramPacket request;
//    DatagramPacket response;
    private AtomicBoolean attached;

    public CtrlInterface(InetAddress ip, int port) {
        this.ip = ip;
        this.port = port;
        id = ip.getHostAddress() + ":" + port;
        this.attached = new AtomicBoolean();
        this.attached.set(false);
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

    public boolean isAttached() {
        return attached.get();
    }

    public boolean isCookieSet() {
        return (cookie != null && cookie.length() > 0);
    }

    public void invalidate() {
        cookie = null;
    }

    private void setCookie(String c) {
        if (c.startsWith("COOKIE=")) {
            c.replaceFirst("COOKIE=", "").trim();
            this.cookie = c;
            Log.print(Log.INFO, "Connected to "+this.ip+":"+this.port);
        }
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void notify(Transaction t) {
        if (t.getRequest().equals(Csts.GET_COOKIE)) {
            setCookie(t.getResponse());
        } else if (t.getRequest().equals(Csts.ATTACH)) {
            if (t.getResponse().startsWith("OK")) {
                attached.set(true);
            }
        } else if (t.getRequest().equals(Csts.DETACH)) {
            if (t.getResponse().startsWith("OK")) {
                attached.set(false);
            }
        }
    }

    public void attach(TransactionHandler handler) {
        if (!handler.isObserverRegistered(this)) {
            handler.registerObserver(this);
        }
        handler.pushAsyncTransaction(new Transaction(this.id, Csts.ATTACH, this));
    }

    public void detach(TransactionHandler handler) {
        if (!handler.isObserverRegistered(this)) {
            handler.registerObserver(this);
        }
        handler.pushAsyncTransaction(new Transaction(this.id, Csts.DETACH, this));
    }

    public void requestCookie(TransactionHandler handler) {
        if (!handler.isObserverRegistered(this)) {
            handler.registerObserver(this);
        }
        handler.pushAsyncTransaction(new Transaction(this.id, Csts.GET_COOKIE, this));
    }

    public int attachSync(TransactionHandler handler) {
        if (!handler.isObserverRegistered(this)) {
            handler.registerObserver(this);
        }
        handler.pushAsyncTransaction(new Transaction(this.id, Csts.ATTACH, this));

        long elapsedTime = 0;
        Instant start = Instant.now();

        while (!(elapsedTime > Csts.SYNC_TIMEOUT_MILLIS)) {
            if (this.attached.get()) {
                return Csts.SYNC_REQUEST_OK;
            }
            elapsedTime = Duration.between(start, Instant.now()).toMillis();
        }
        return Csts.SYNC_REQUEST_TIMEOUT;
    }

    public int requestCookieSync(TransactionHandler handler) {
        Transaction t = handler.pushSynchronousTransaction(new Transaction(this.id, Csts.GET_COOKIE, this));
        if (t.getResponse() != null && t.getResponse().contains("COOKIE=")) {
            this.setCookie(t.getResponse());
            return Csts.SYNC_REQUEST_OK;
        } else if (t.getResponse().equals(Csts.TIMEOUT)) {
            return Csts.SYNC_REQUEST_TIMEOUT;
        }
        return Csts.SYNC_REQUEST_FAILED;
    }

    @JsonIgnore
    @Override
    public CtrlInterface getCtrlIface() {
        return this;
    }

    public void deinit(TransactionHandler handler) {
        if (handler.isObserverRegistered(this)) {
            handler.removeObserver(this);
        }
    }
}
