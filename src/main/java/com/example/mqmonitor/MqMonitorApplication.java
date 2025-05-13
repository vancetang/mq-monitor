package com.example.mqmonitor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@EnableScheduling
@SpringBootApplication
public class MqMonitorApplication {

	public static void main(String[] args) {
		SpringApplication.run(MqMonitorApplication.class, args);
		log.info("MQ Monitor Application started");
	}

}
