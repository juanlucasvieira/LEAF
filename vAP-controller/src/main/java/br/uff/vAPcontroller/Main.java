package br.uff.vAPcontroller;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan ({"br.uff.vAPcontroller"})
@SpringBootApplication
public class Main {

	public static void main(String[] args) throws InterruptedException {
		SpringApplication.run(Main.class, args);
                Controller c = Controller.getInstance();
                c.begin();
                c.run();
	}

}
