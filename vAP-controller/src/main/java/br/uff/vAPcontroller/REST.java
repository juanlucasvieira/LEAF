/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.uff.vAPcontroller;

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
    @PostMapping("/migrate/{vap_id}/from{ap_src_id}to{ap_dst_id}")
    public ResponseEntity migrateVAP(@PathVariable("ap_src_id") String ap_src_id,
                                    @PathVariable("vap_id") String vap_id,
                                    @PathVariable("ap_dst_id") String ap_dst_id) {

        AP ap = c.getAPById(id);
        if (ap == null) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.ok().build();
        }
    }
}
