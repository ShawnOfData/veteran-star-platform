package com.veteran;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.veteran.mapper")
@EnableAsync
@EnableScheduling
public class VeteranApplication {

    public static void main(String[] args) {
        SpringApplication.run(VeteranApplication.class, args);
    }
}