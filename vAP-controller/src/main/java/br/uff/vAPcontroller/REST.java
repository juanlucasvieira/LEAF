/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.uff.vAPcontroller;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author juan
 */
@RestController
public class REST {

    Controller c;

    public REST() {
        c = Controller.getInstance();
    }

    @ResponseBody
    @GetMapping("/ap/{id}")
    public ResponseEntity getAP(@PathVariable("id") String id) {
        AP ap = c.getAPById(id);
        if (ap == null) {
            return ResponseEntity.badRequest().body("AP not found!");
        } else {
            return ResponseEntity.ok(ap);
        }
    }

    @ResponseBody
    @GetMapping("/ap/all")
    public ResponseEntity getAllAP() {
        ArrayList<AP> aps = c.getAllAPs();
        if (aps == null) {
            return ResponseEntity.badRequest().body("No AP registered in controller.");
        } else {
            return ResponseEntity.ok(aps);
        }
    }

    @ResponseBody
    @GetMapping("/vap/{id}")
    public ResponseEntity getVAP(@PathVariable("id") String id) {
        VirtualAP vap = c.getVAPById(id);
        if (vap == null) {
            return ResponseEntity.badRequest().body("VAP not found!");
        } else {
            return ResponseEntity.ok(vap);
        }
    }

    @PostMapping("/create/vap/at/{ap}/{phy}")
    public ResponseEntity createDefaultVAP(@PathVariable("ap") String ap_id, @PathVariable("phy") String phy_id) {
        return buildResponse(c.createDefaultVAPRESTCmd(ap_id, phy_id));
    }

    @DeleteMapping("/vap/{id}")
    public ResponseEntity deleteVAP(@PathVariable("id") String vap_id) {
        return buildResponse(c.removeVAPRESTCmd(vap_id));
    }

    @DeleteMapping("/ap/{id}")
    public ResponseEntity deleteAP(@PathVariable("id") String ap_id) {
        return buildResponse(c.deleteAP(ap_id));
    }

//    @ResponseBody
    @PostMapping("/register/ap/{ap_id}/{ap_ip}/{ap_gci_port}")
    public ResponseEntity registerAP(@PathVariable("ap_id") String ap_id,
            @PathVariable("ap_ip") String ap_ip,
            @PathVariable("ap_gci_port") String ap_gci_port) {

        try {
            int port = Integer.parseInt(ap_gci_port);
            if (port > 65535 || port < 0) {
                return ResponseEntity.badRequest().body("Wrong Port parameter!");
            }
            InetAddress ip = InetAddress.getByName(ap_ip);
            int returnCode = c.addAP(ap_id, ip, port);
            switch (returnCode) {
                case 0:
                    return ResponseEntity.ok().build();
                case -1:
                    return ResponseEntity.badRequest().body("AP already exists!");
                case -2:
                    return ResponseEntity.badRequest().body("This AP address is already registered!");
            }

        } catch (UnknownHostException ex) {
            return ResponseEntity.badRequest().body("Wrong IP parameter!");
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body("Wrong Port parameter!");
        }
        return ResponseEntity.badRequest().build();
    }

    @ResponseBody
    @PostMapping("/migrate/{vap_id}/from/{ap_src_id}/to/{ap_dst_id}")
    public ResponseEntity migrateVAP(@PathVariable("ap_src_id") String ap_src_id,
            @PathVariable("vap_id") String vap_id,
            @PathVariable("ap_dst_id") String ap_dst_id) throws InterruptedException {

        return migrateVAPRequest(ap_src_id, vap_id, ap_dst_id, null);
    }

    @ResponseBody
    @PostMapping("/migrate/{vap_id}/from/{ap_src_id}/to/{ap_dst_id}/at/{dst_phy}")
    public ResponseEntity migrateVAPPhy(@PathVariable("ap_src_id") String ap_src_id,
            @PathVariable("vap_id") String vap_id,
            @PathVariable("ap_dst_id") String ap_dst_id, @PathVariable("dst_phy") String dst_phy) throws InterruptedException {

        return migrateVAPRequest(ap_src_id, vap_id, ap_dst_id, dst_phy);
    }

    public ResponseEntity migrateVAPRequest(String ap_src_id, String vap_id, String ap_dst_id, String dst_phy) throws InterruptedException {
        Instant start = Instant.now();
        int returnCode = c.migrateVAPCommand(ap_src_id, ap_dst_id, vap_id, dst_phy);
        Instant finish = Instant.now();
        long timeElapsed = Duration.between(start, finish).toMillis();  //in millis
        System.out.println("MIGRATION TIME: " + timeElapsed);

        switch (returnCode) {
            case Csts.MIGRATION_SUCCESSFUL_STA_DETECTED:
                return ResponseEntity.ok().body("Migration Successful! STA detected in new physical AP!");
            case Csts.MIGRATION_SUCCESSFUL:
                return ResponseEntity.ok().body("Migration Successful. Could not detect STA in new AP.");
            default:
                return buildResponse(returnCode);
        }
    }

    private String returnCodeToString(int returnCode) {
        String s = "";
        switch (returnCode) {
            case Csts.STA_INJECTION_FAILED:
                s += "STA injection procedure failed!";
                break;
            case Csts.VAP_INJECTION_FAILED:
                s += "VAP injection procedure failed!";
                break;
            case Csts.FAILED_TO_SEND_CSA:
                s += "CSA send procedure failed!";
                break;
            case Csts.DEL_AP_FROM_OLD_VAP_FAILED:
                s += "Old BSS deletion procedure failed!";
                break;
            case Csts.SOURCE_AP_NOT_FOUND:
                s += "Source AP not found!";
                break;
            case Csts.DST_AP_NOT_FOUND:
                s += "Destination AP not found!";
                break;
            case Csts.VAP_NOT_FOUND:
                s += "VAP not found!";
                break;
            case Csts.VAP_CANNOT_BE_MIGRATED:
                s += "The specified VAP cannot be migrated.";
                break;
            case Csts.VAP_CANNOT_BE_REMOVED:
                s += "The specified VAP cannot be removed.";
                break;
            case Csts.SYNC_REQUEST_FAILED:
                s += "Command failed.";
                break;
            case Csts.SYNC_REQUEST_TIMEOUT:
                s += "Command timeout.";
                break;
        }
//        if (returnCode > 0) {
//            s += "Rollback successful!";
//        } else {
//            s += "Rollback failed!";
//        }
        return s;
    }

    private ResponseEntity buildResponse(int returnCode) {
        switch (returnCode) {
            case Csts.SYNC_REQUEST_OK:
                return ResponseEntity.ok().build();
            default:
                Log.print(Log.ERROR, "Command Return: " + returnCodeToString(returnCode));
                return ResponseEntity.badRequest().body(returnCodeToString(returnCode));
        }
    }
}
