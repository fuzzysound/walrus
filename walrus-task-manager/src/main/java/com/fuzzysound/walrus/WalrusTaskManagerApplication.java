package com.fuzzysound.walrus;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class WalrusTaskManagerApplication {
    public static void main(String[] args) {
        SpringApplication.run(WalrusTaskManagerApplication.class, args);
    }
}
