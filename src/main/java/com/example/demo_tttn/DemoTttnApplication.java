package com.example.demo_tttn;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.example.demo_tttn")
public class DemoTttnApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoTttnApplication.class, args);
		System.out.println	("Ứng dụng Spring Boot đã khởi động!");

	}
}
