package com.hato;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HatoApplication {

    public static void main(String[] args) {
        SpringApplication.run(HatoApplication.class, args);
    }
}
