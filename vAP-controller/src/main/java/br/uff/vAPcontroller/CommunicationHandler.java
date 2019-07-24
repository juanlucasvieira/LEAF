package br.uff.vAPcontroller;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author juan
 */
public class CommunicationHandler implements ReceiveCallback {

//    DatagramSocket socket;
    DatagramSocket recv_socket;

    MessageReceiver receiver;

    TransactionHandler handler;

    boolean listening = false;

    public CommunicationHandler(TransactionHandler handler) {
        try {
//            this.socket = new DatagramSocket();
            this.handler = handler;
            this.recv_socket = new DatagramSocket(null);
        } catch (SocketException ex) {
            Logger.getLogger(CommunicationHandler.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void listen() {
        if (!listening) {
            try {
                recv_socket.bind(new InetSocketAddress(Cmds.SEND_LISTEN_PORT));
                receiver = new MessageReceiver(recv_socket, this);
                receiver.start();
                listening = true;
            } catch (SocketException ex) {
                Log.print(Cmds.ERROR, "SocketException: " + ex.getMessage());
            }
        }
    }

    public void sendRequest(String tid, String msg, CtrlInterface iface) {
        try {
            if (iface.isCookieSet()) {
                msg = "TID=" + tid + " " + iface.getCookie() + " " + msg;
            } else if (msg.contains(Cmds.GET_COOKIE)) {
                msg = "TID=" + tid + " " + msg;
            }
            byte[] msg_byte = msg.getBytes();
            Log.print(Cmds.DEBUG_INFO, "Sending message to " + iface.toString() + " :\n" + msg);
            DatagramPacket req_pckt = new DatagramPacket(msg_byte, msg_byte.length, iface.getIp(), iface.getPort());
            recv_socket.send(req_pckt);
        } catch (IOException ex) {
            Logger.getLogger(CommunicationHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

//    public void sendReq(String req, String req_id) {
//        SendMsgRequest smr = new SendMsgRequest(socket, req, req_id, this);
//        smr.run();
//    }
    @Override
    public void receiveCallback(DatagramPacket dp) {
        //  TODO: Do something with received answer.
        Log.print(Cmds.DEBUG_INFO, "Received message from "
                + dp.getAddress().getHostName() + ":" + dp.getPort() + ":\n"
                + new String(dp.getData(), 0, dp.getLength()));
        handler.processMessage(new String(dp.getData(), 0, dp.getLength()));
        //Send it to the correspondent CtrlInterface

//        System.exit(0);
//        receiver.run();
    }
}
