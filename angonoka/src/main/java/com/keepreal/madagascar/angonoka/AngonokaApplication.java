package com.keepreal.madagascar.angonoka;

import com.keepreal.madagascar.common.snowflake.annotation.EnableIdGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableIdGenerator
@EnableJpaAuditing
@SpringBootApplication
public class AngonokaApplication {

	public static void main(String[] args) {
		SpringApplication.run(AngonokaApplication.class, args);
	}

}
