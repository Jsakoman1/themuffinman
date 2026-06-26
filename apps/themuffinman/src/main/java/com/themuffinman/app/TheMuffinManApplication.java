package com.themuffinman.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class TheMuffinManApplication {

	public static void main(String[] args) {
		SpringApplication.run(TheMuffinManApplication.class, args);
	}

}
