package com.project.ProjectS;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ProjectSApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProjectSApplication.class, args);
	}

}
