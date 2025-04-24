package com.skiply.fee;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(
        title = "Fee Service API",
        version = "1.0",
        description = "API for fee collection and receipt generation"
))
public class FeeServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(FeeServiceApplication.class, args);
    }
}