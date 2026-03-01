package com.system.batch.sybatchsystem;

import org.springframework.batch.core.job.Job;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import java.util.Arrays;

@SpringBootApplication
public class SyBatchSystemApplication {

    public static void main(String[] args) {
        // Jenkin 등 연동을 위해 변경
        System.exit(SpringApplication.exit(SpringApplication.run(SyBatchSystemApplication.class, args)));
    }

}
