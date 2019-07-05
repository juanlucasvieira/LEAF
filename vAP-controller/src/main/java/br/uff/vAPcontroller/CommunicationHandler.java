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
    
    Controller c;

    boolean listening = false;

    public CommunicationHandler(Controller c) {
        try {
//            this.socket = new DatagramSocket();
            this.c = c;
            this.recv_socket = new DatagramSocket(null);
        } catch (SocketException ex) {
            Logger.getLogger(CommunicationHandler.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void listen(InetAddress addr, int port) throws SocketException {
        if (!listening) {
            recv_socket.bind(new InetSocketAddress(addr, port));
            System.out.println("bla");
            receiver = new MessageReceiver(recv_socket, this);
            receiver.start();
            listening = true;
        }
    }

    public void receiveMsg(DatagramPacket answer, String requestID) {

    }

    public void sendMsg(String msg, CtrlInterface iface) {
        try {
            if (iface.getCookie().length() > 0) {
                msg = iface.getCookie() + " " + msg;
            }
            byte[] msg_byte = msg.getBytes();
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
    public void rcv_callback(DatagramPacket dp) {
        //  TODO: Do something with received answer.
        System.out.println("I received this: " + new String(dp.getData()));
        System.out.println("Sender: " + dp.getAddress().getHostName() + ":" + dp.getPort());
        c.processMessage(dp);
        //Send it to the correspondent CtrlInterface

//        System.exit(0);
//        receiver.run();
    }
}
