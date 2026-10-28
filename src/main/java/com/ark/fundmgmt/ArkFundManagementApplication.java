package com.ark.fundmgmt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class ArkFundManagementApplication {
    public static void main(String[] args) {
        SpringApplication.run(ArkFundManagementApplication.class, args);
    }
}
