package com.shravya.bankingapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class BankingappApplication {

	public static void main(String[] args) {
		SpringApplication.run(BankingappApplication.class, args);
	}

}
