package vap.controller;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author juan
 */
public class CommCoordinator extends Thread {
    DatagramChannel channel;
    InetSocketAddress ip_sock;
    Selector selector;
    SelectionKey key;
    ConcurrentLinkedQueue<String> requests;
    ConcurrentLinkedQueue<String> answers;
    ConcurrentLinkedQueue<String> ctrl_ifaces;
    
    public CommCoordinator(ConcurrentLinkedQueue<String> requests, 
                            ConcurrentLinkedQueue<String> answers,
                            ConcurrentLinkedQueue<String> ctrl_ifaces) throws IOException {
        this.selector = Selector.open();
        this.requests = requests;
        this.answers = answers;
        this.ctrl_ifaces = ctrl_ifaces;
    }
    
    @Override
    public void run(){
        try {
            while(true){
                checkNewIface();
                if (selector.select() <= 0){
                    continue;
                }
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectedKeys.iterator();
                while(iterator.hasNext()){
                    key = (SelectionKey) iterator.next();
                    if(key.isReadable()){
                        DatagramChannel c = (DatagramChannel) key.channel();
                        ByteBuffer bb = ByteBuffer.allocate(2048);
                        c.read(bb);
                        String result = new String(bb.array()).trim();
                        processAnswer(result, key.attachment());
                    }
                    if(key.isWritable()){
                        String msg = getMessageToWrite(key.attachment());
                        if(!msg.isEmpty()){
                            DatagramChannel c = (DatagramChannel) key.channel();
                            ByteBuffer bb = ByteBuffer.wrap(msg.getBytes());
                            c.write(bb);
                        }
                    }
                    iterator.remove();
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(CommCoordinator.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    private void registerComm(InetAddress ip, int port, String id){
        try {
            InetSocketAddress socket_address = new InetSocketAddress(ip, port);
            DatagramChannel dchannel = DatagramChannel.open();
            dchannel.bind(socket_address);
            dchannel.configureBlocking(false);
            dchannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE).attach(id);
        } catch(IOException e) {
            
        }
    }

    private void processAnswer(String result, Object attachment) {
        answers.offer(((String) attachment) + "" + result);
    }

    private String getMessageToWrite(Object attachment) {
        String id = (String) attachment;
        if(requests.peek().contains(id)){
            return requests.poll();
        }
        return null;
    }

    private void checkNewIface() throws UnknownHostException {
        if(ctrl_ifaces.peek() != null){
            String[] iface = ctrl_ifaces.poll().split("#");
            registerComm(InetAddress.getByName(iface[0]), Integer.parseInt(iface[1]), iface[2]);
        }
    }
}
