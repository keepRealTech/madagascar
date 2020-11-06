package com.keepreal.madagascar.coua;

import com.keepreal.madagascar.common.snowflake.annotation.EnableIdGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @program: madagascar
 * @author: zhangxidong
 * @create: 2020-04-26
 **/

@SpringBootApplication
@EnableIdGenerator
@EnableJpaAuditing
@EnableScheduling
public class CouaApplication {

    public static void main(String[] args) {
        SpringApplication.run(CouaApplication.class, args);
    }
}
