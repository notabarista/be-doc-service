package org.notabarista;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import springfox.documentation.swagger2.annotations.EnableSwagger2;

@EnableSwagger2
@EnableScheduling
@EnableAutoConfiguration
@SpringBootApplication
public class DocServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(DocServiceApplication.class, args);
	}

}
