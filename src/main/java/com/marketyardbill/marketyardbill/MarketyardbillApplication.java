package com.marketyardbill.marketyardbill;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MarketyardbillApplication {

	public static void main(String[] args) {
		SpringApplication.run(MarketyardbillApplication.class, args);
	}

}



