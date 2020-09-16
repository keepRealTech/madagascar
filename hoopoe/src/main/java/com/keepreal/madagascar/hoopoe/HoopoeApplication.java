package com.keepreal.madagascar.hoopoe;

import com.keepreal.madagascar.common.snowflake.annotation.EnableIdGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableIdGenerator
@EnableJpaAuditing
public class HoopoeApplication {

    public static void main(String[] args) {
        SpringApplication.run(HoopoeApplication.class, args);
    }

}
