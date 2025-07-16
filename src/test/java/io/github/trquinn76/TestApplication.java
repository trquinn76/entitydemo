package io.github.trquinn76;

import org.springframework.boot.SpringApplication;

import io.github.trquinn76.entitydemo.Application;

/**
 * Run this application class to start your application locally, using Testcontainers for all external services. You
 * have to configure the containers in {@link TestcontainersConfiguration}.
 */
public class TestApplication {

    public static void main(String[] args) {
        SpringApplication.from(Application::main).with(TestcontainersConfiguration.class).run(args);
    }
}
