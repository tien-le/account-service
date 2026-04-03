package com.learn4you.account_service;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class AccountServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AccountServiceApplication.class, args);
    }

    @Bean
    NewTopic notification() {
        // Topic name, Partition numbers, Replication number=#brokers server
        return new NewTopic("notification", 2, (short) 3);
    }

    @Bean
    NewTopic statistic()  {
        // Topic name, Partition numbers, Replication number
        return new NewTopic("statistic", 1, (short) 1);
    }

}