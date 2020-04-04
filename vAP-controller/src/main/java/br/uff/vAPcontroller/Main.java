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
                case "-nocsa":
                    Csts.DISABLE_CSA = true;
                    break;
                case "-nosendframe":
                    Csts.DISABLE_SEND_FRAME = true;
                    break;
                case "-noaddsta":
                    Csts.DISABLE_STA_INJECTION = true;
                    break;
                case "-deauth":
                    Csts.SEND_DEAUTH_ENABLED = true;
                    break;
                case "-h":
                    printHelpMessage();
                    System.exit(0);
                    break;
                default:
                    printHelpMessage();
                    break;
            }
            i++;
        }
        SpringApplication.run(Main.class, args);
        System.out.println("Debug Log Level is " + (Csts.DEBUG_LOG_LEVEL ? "ENABLED" : "DISABLED"));
        System.out.println("VAP auto creation is " + (Csts.CREATE_VAP_AUTOMATICALLY ? "ENABLED" : "DISABLED"));
        System.out.println("CSA IE sending is " + (Csts.DISABLE_CSA ? "DISABLED" : "ENABLED"));
        System.out.println("Client Send Frame " + (Csts.DISABLE_SEND_FRAME ? "DISABLED" : "ENABLED"));
        System.out.println("Station Injection is " + (Csts.DISABLE_STA_INJECTION ? "DISABLED" : "ENABLED"));
        System.out.println("Send deauth is " + (Csts.SEND_DEAUTH_ENABLED ? "ENABLED" : "DISABLED"));
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
        System.out.println("VAP-SDN Controller\n"
                + "----------------------\n"
                + "OPTIONS:\n"
                + "\t-ap <AP_Identifier> <AP_IP> <AP_Port> - Sets AP on startup\n"
                + "\t-d - Enables Debug Logging\n"
                + "\t-noauto - Disables VAP auto generation\n"
                + "\t-nocsa - Disables CSA in the migration procedure\n"
                + "\nTESTING OPTIONS:\n"
                + "\t-nosendframe - Disables Client's Frame Sending for rerouting purposes\n"
                + "\t-noaddsta - Disables station injection in destination AP (for testing purposes)\n"
                + "\t-deauth - Source AP sends deauth frame to client upon handover (for testing purposes) \n"
                + "");
    }
}
