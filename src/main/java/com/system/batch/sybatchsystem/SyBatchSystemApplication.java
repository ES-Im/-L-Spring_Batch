package com.system.batch.sybatchsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SyBatchSystemApplication {

    public static void main(String[] args) {
        // Jenkin 등 연동을 위해 변경
        System.exit(SpringApplication.exit(SpringApplication.run(SyBatchSystemApplication.class, args)));
    }

}
