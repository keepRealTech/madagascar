package com.keepreal.madagascar.fossa;

import com.keepreal.madagascar.common.snowflake.annotation.EnableIdGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @program: madagascar
 * @author: zhangxidong
 * @create: 2020-04-27
 **/

@SpringBootApplication
@EnableIdGenerator
public class FossaApplication {

    public static void main(String[] args) {
        SpringApplication.run(FossaApplication.class, args);
    }
}
