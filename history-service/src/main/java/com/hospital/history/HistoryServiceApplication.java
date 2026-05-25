package com.hospital.history;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableRabbit
public class HistoryServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(HistoryServiceApplication.class, args);
    }
}
