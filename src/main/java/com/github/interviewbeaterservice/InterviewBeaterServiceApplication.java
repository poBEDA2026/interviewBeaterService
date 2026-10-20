package com.github.interviewbeaterservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationPropertiesScan
@SpringBootApplication
public class InterviewBeaterServiceApplication {

  /**
   * Точка входа Spring Boot приложения.
   *
   * @param args аргументы командной строки, передаваемые в SpringApplication
   */
  public static void main(String[] args) {
    SpringApplication.run(InterviewBeaterServiceApplication.class, args);
  }
}
