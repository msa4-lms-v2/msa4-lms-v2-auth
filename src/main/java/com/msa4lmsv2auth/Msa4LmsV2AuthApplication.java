package com.msa4lmsv2auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class Msa4LmsV2AuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(Msa4LmsV2AuthApplication.class, args);
    }

}
