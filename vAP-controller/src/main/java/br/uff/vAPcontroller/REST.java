/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.uff.vAPcontroller;

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

}
