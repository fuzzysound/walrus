package com.fuzzysound.walrus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class WalrusTaskWorkerApplication {
    public static void main(String[] args) {
        SpringApplication.run(WalrusTaskWorkerApplication.class, args);
    }
}
