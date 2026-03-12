package org.opendevstack.apiservice.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = { "org.opendevstack.apiservice" })
@EnableJpaRepositories(basePackages = "org.opendevstack.apiservice.persistence.repository")
@EntityScan(basePackages = "org.opendevstack.apiservice.persistence.entity")
@EnableCaching
public class DevstackApiServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(DevstackApiServiceApplication.class, args);
	}

}
