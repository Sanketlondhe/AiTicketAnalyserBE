package com.example.aiticketanalyser;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy
public class AiTicketAnalyserApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiTicketAnalyserApplication.class, args);
    }

}
