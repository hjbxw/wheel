package com.pgmerxw.eatwhat;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;



@MapperScan("com.pgmerxw.eatwhat.mapper")
@SpringBootApplication
public class EatWhatApplication {

    public static void main(String[] args) {
        SpringApplication.run(EatWhatApplication.class, args);
    }

}
