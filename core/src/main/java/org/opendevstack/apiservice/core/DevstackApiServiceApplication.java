package org.opendevstack.apiservice.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication(scanBasePackages = { "org.opendevstack.apiservice" })
@EnableCaching
public class DevstackApiServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(DevstackApiServiceApplication.class, args);
	}

}
