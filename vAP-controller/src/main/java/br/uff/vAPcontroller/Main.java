package br.uff.vAPcontroller;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.ResponseEntity;

@ComponentScan({"br.uff.vAPcontroller"})
@SpringBootApplication
public class Main {

    public static void main(String[] args) throws InterruptedException {
        ConcurrentHashMap<String, AP> ap_list = new ConcurrentHashMap<>();

        int i = 0;
        while (i < args.length) {
            switch (args[i]) {
                case "-ap":
                    AP ap = checkAPParam(i, args);
                    if (ap == null) {
                        System.out.println("AP argument error! -ap <id> <IP> <GCI_Port>");
                        return;
                    } else {
                        ap_list.put(ap.getId(), ap);
                    }
                    i += 3;
                    break;
                case "-d":
                    Csts.DEBUG_LOG_LEVEL = true;
                    break;
                case "-noauto":
                    Csts.CREATE_VAP_AUTOMATICALLY = false;
                    break;
                default:
                    printHelpMessage();
                        break;
            }
            i++;
        }
        SpringApplication.run(Main.class, args);
        if (Csts.DEBUG_LOG_LEVEL) {
            System.out.println("Debug Log Level is ON");
        }
        Controller c = Controller.getInstance();
        c.begin(ap_list);
    }

    private static AP checkAPParam(int i, String[] args) {
        if (i + 3 < args.length) {
            String ap_id = args[i + 1];
            String ip_string = args[i + 2];
            String port_string = args[i + 3];

            try {
                int port = Integer.parseInt(port_string);
                if (port > 65535 || port < 0) {
                    return null;
                }
                InetAddress ip = InetAddress.getByName(ip_string);
                return new AP(ap_id, ip, port);
            } catch (UnknownHostException ex) {
                return null;
            }
        }
        return null;
    }

    private static void printHelpMessage() {
        System.out.println("ARGS: -ap <id> <IP> <port> - Sets AP on startup\n"
                + "\t-d - Enables Debug Logging\n"
                + "\t-noauto - Disables VAP auto generation\n");
    }
}
