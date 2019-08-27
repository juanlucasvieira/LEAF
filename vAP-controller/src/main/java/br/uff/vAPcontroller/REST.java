/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.uff.vAPcontroller;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.logging.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
    @GetMapping("/teste/{id}")
    public ResponseEntity report(@PathVariable("id") long id) {
        String s = c.sendRequest();
        return ResponseEntity.ok(s + " " + id);
    }

    @ResponseBody
    @GetMapping("/ap/{id}")
    public ResponseEntity getAP(@PathVariable("id") String id) {
        AP ap = c.getAPById(id);
        if (ap == null) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.ok(ap);
        }
    }

    @ResponseBody
    @GetMapping("/ap/all")
    public ResponseEntity getAP() {
        ArrayList<AP> aps = c.getAllAPs();
        if (aps == null) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.ok(aps);
        }
    }

    @ResponseBody
    @PostMapping("/migrate/{vap_id}/from/{ap_src_id}/to/{ap_dst_id}")
    public ResponseEntity migrateVAP(@PathVariable("ap_src_id") String ap_src_id,
            @PathVariable("vap_id") String vap_id,
            @PathVariable("ap_dst_id") String ap_dst_id) throws InterruptedException {

        Instant start = Instant.now();
        int returnCode = c.migrateVAP(ap_src_id, ap_dst_id, vap_id);
        Instant finish = Instant.now();
        long timeElapsed = Duration.between(start, finish).toMillis();  //in millis
        System.out.println("MIGRATION TIME: " + timeElapsed);

        switch (returnCode) {
            case 0:
                return ResponseEntity.ok().body("Migration Successful! STA detected in new physical AP!");
            case 1:
                return ResponseEntity.ok().body("Migration Successful. Could not detect STA in new AP.");
            default:
                Log.print(Log.ERROR, "Migration Return Code: " + returnCode);
                return ResponseEntity.badRequest().body("Command failed: " + returnCodeToString(returnCode));
//            return ResponseEntity.notFound().build();
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
                s += "Migration VAP not found!";
                break;
        }
//        if (returnCode > 0) {
//            s += "Rollback successful!";
//        } else {
//            s += "Rollback failed!";
//        }
        return s;
    }
}
