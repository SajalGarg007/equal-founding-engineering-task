package com.task.founding.engineer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
    "com.task.founding.engineer"  // Scans all packages: dto, service, repository, api.controller
})
@EnableJpaRepositories(basePackages = "com.task.founding.engineer.repository")
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}

