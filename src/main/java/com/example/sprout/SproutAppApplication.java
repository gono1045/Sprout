package com.example.sprout;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.sprout.dao")
public class SproutAppApplication {

  public static void main(String[] args) {
    SpringApplication.run(SproutAppApplication.class, args);
  }

}
