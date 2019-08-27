package br.uff.vAPcontroller;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author juan
 */
public class CommunicationHandler implements ReceiveCallback {

//    DatagramSocket socket;
    DatagramSocket asyncSocket;
    DatagramSocket synchronousSocket;

    MessageReceiver receiver;

    TransactionHandler tHandler;
    EventHandler eHandler;

    boolean listening = false;

    public CommunicationHandler(TransactionHandler tranHandler, EventHandler eventHandler) {
        try {
//            this.socket = new DatagramSocket();
            this.eHandler = eventHandler;
            this.tHandler = tranHandler;
            this.asyncSocket = new DatagramSocket(null);
            this.synchronousSocket = new DatagramSocket(null);
            this.synchronousSocket.bind(new InetSocketAddress(Csts.SEND_LISTEN_PORT_SYNC));
            this.synchronousSocket.setSoTimeout(Csts.SYNC_TIMEOUT_MILLIS);
        } catch (SocketException ex) {
            Logger.getLogger(CommunicationHandler.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void listen() {
        if (!listening) {
            try {
                asyncSocket.bind(new InetSocketAddress(Csts.SEND_LISTEN_PORT_ASYNC));
                receiver = new MessageReceiver(asyncSocket, this);
                receiver.start();
                listening = true;
            } catch (SocketException ex) {
                Log.print(Log.ERROR, "SocketException: " + ex.getMessage());
            }
        }
    }

    public void sendAsyncRequest(String tid, String msg, CtrlInterface iface) {
        try {
            byte[] msgBytes = buildRequestStructure(tid, msg, iface);
            Log.print(Log.DEBUG_INFO, "Sending message to " + iface.toString() + " :\n" + msg);
            DatagramPacket req_pckt = new DatagramPacket(msgBytes, msgBytes.length, iface.getIp(), iface.getPort());
            asyncSocket.send(req_pckt);
        } catch (IOException ex) {
            Log.print(Log.ERROR, "Error while sending message to " + iface.toString() + " :\n" + msg + "\n" + ex.getMessage());
        }
    }

    @Override
    public void receiveCallback(DatagramPacket dp) {

        String packetData = new String(dp.getData(), 0, dp.getLength());
        String sender = dp.getAddress().getHostAddress() + ":" + dp.getPort();
        Log.print(Log.DEBUG_INFO, "Received message from " + sender + ":\n" + packetData);

        if (packetData.startsWith("TID=")) {
            tHandler.processMessage(new String(dp.getData(), 0, dp.getLength()));
        } else {
            eHandler.receiveEvent(sender, packetData);
        }

    }

    String sendSyncRequest(String tid, String request, CtrlInterface destination) throws SocketTimeoutException {
        try {
            byte[] msgBytes = buildRequestStructure(tid, request, destination);
            Log.print(Log.DEBUG_INFO, "Sending message to " + destination.toString() + " :\n" + request);
            DatagramPacket req_pckt = new DatagramPacket(msgBytes, msgBytes.length, destination.getIp(), destination.getPort());
            synchronousSocket.send(req_pckt);
            byte[] resp_buffer = new byte[2048];
            DatagramPacket response = new DatagramPacket(resp_buffer, resp_buffer.length);
            synchronousSocket.receive(response);
            Log.print(Log.DEBUG_INFO, "Received message from "
                    + response.getAddress().getHostName() + ":" + response.getPort() + ":\n"
                    + new String(response.getData(), 0, response.getLength()));
            return new String(response.getData(), 0, response.getLength());
        } catch (SocketTimeoutException timeout) {
            throw timeout;
        } catch (IOException ex) {
            Log.print(Log.ERROR, "Error while sending message to " + destination.toString() + " :\n" + request
                    + "\n");
            ex.printStackTrace();
            return null;
        }
    }

    private byte[] buildRequestStructure(String tid, String msg, CtrlInterface iface) throws IOException {
        if (iface.isCookieSet()) {
            msg = "TID=" + tid + " " + iface.getCookie() + " " + msg;
        } else if (msg.contains(Csts.GET_COOKIE)) {
            msg = "TID=" + tid + " " + msg;
        } else {
            throw new IOException("Message Cookie is not set (Only cookie requests messages can be sent without a cookie)");
        }
        return msg.getBytes();
    }
}
