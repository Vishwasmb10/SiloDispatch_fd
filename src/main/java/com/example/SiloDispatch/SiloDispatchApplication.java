package com.example.SiloDispatch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SiloDispatchApplication {

	public static void main(String[] args) {
		SpringApplication.run(SiloDispatchApplication.class, args);
	}

}
