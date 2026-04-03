package com.learn4you.account_service.controller;

import com.learn4you.account_service.model.AccountDTO;
import com.learn4you.account_service.model.MessageDTO;
import com.learn4you.account_service.model.StatisticDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
@RequestMapping("/account")
public class AccountController {
    @Autowired
    KafkaTemplate<String, Object> kafkaTemplate; //key, value=JSON

    @PostMapping("/new")
    public AccountDTO createAccount(@RequestBody AccountDTO account) {
        String message = "Account " + account.getEmail() + " has been created";
        StatisticDTO stat = new StatisticDTO(message, new Date());

        // Send notification
        MessageDTO messageDTO = MessageDTO.builder()
                                          .to(account.getEmail())
                                          .toName(account.getName())
                                          .subject("Welcome to Learn4You")
                                          .content("Learn4You has been created for Continuous Learning Platform...")
                                          .build();

        kafkaTemplate.send("notification", messageDTO);
        kafkaTemplate.send("statistic", stat);

        return account;
    }
}