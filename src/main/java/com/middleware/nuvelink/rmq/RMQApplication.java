package com.middleware.nuvelink.rmq;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.middleware.nuvelink.rmq.service.RabbitMQSender;

@SpringBootApplication
public class RMQApplication implements CommandLineRunner {

    @Autowired
    private RabbitMQSender rabbitMQSender;

    public static void main( String[] args )
    {
        SpringApplication.run(RMQApplication.class, args);
    }

    @Override
    public void run( String... args ) throws Exception
    {
        System.out.println("main");
        rabbitMQSender.send("Hi. I'm AWS", "aws");
        System.out.println("AWS published");
        rabbitMQSender.send("Hi. I'm GCP", "gcp");
        System.out.println("GCP published");
        rabbitMQSender.send("Hi. I'm CSP", "csp");
        System.out.println("CSP published");
    }
}
