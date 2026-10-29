package com.alojate.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.alojate")
@EntityScan(basePackages = "com.alojate")
@EnableJpaRepositories(basePackages = "com.alojate")
public class Ms01UsuariosAlojamientosApplication {

    public static void main(String[] args) {
        SpringApplication.run(Ms01UsuariosAlojamientosApplication.class, args);
    }
}
