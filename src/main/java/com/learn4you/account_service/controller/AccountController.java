package com.learn4you.account_service.controller;

import com.learn4you.account_service.model.*;
import com.learn4you.account_service.repository.AccountRepository;
import com.learn4you.account_service.repository.MessageRepository;
import com.learn4you.account_service.repository.StatisticRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaProducerException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
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

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    MessageRepository messageRepository;

    @Autowired
    StatisticRepository statisticRepository;

    @PostMapping("/new")
    public Account newAccount(@RequestBody Account account) {
        // Statistics information
        String message = "Account " + account.getEmail() + " has been created";
        Statistic statistic = new Statistic();
        statistic.setMessage(message);
        statistic.setCreatedDate(new Date());
        statistic.setStatus(false);
        statisticRepository.save(statistic);

        // Notification
        Message notification_message = new Message();
        notification_message.setTo(account.getEmail());
        notification_message.setToName(account.getName());
        notification_message.setSubject("POC Notification with JPA");
        notification_message.setContent("POC Kafka 3x and Spring Boot 3x");
        notification_message.setStatus(false);
        messageRepository.save(notification_message);

        // Account information
        accountRepository.save(account);
        return account;
    }

//    @PostMapping("/new_dto")
//    public AccountDTO createAccount(@RequestBody AccountDTO account) {
//        // Statistics information
//        String message = "Account " + account.getEmail() + " has been created";
//        StatisticDTO stat = new StatisticDTO(message, new Date());
//
//        // Send notification
//        MessageDTO messageDTO = MessageDTO.builder()
//                                          .to(account.getEmail())
//                                          .toName(account.getName())
//                                          .subject("Welcome to Learn4You")
//                                          .content("Learn4You has been created for Continuous Learning Platform...")
//                                          .build();
//
//        /* Kafka errors:
//        SerializationException → serializer issue
//        TimeoutException → broker unreachable
//        UnknownTopicOrPartition → topic missing
//        NotLeaderForPartition → cluster issue
//         */
//        for (int i = 0; i < 1; i++)
//            kafkaTemplate.send("notification", messageDTO).whenComplete((status, ex) -> {
//                if (ex != null) {
//                    // Fallback Logic
//                    // Handle failures, save to DB the failed events.
//                    // Then, using Job Scheduler to send these failed events again
//                    System.out.println("Error sending notification");
////                    ex.printStackTrace();
//                    handleFailure(messageDTO, ex);
//                } else {
//                    // Handle success
//                    handleSuccess(status);
//                }
//            });
//        kafkaTemplate.send("statistic", stat);
//        return account;
//    }

    private void handleSuccess(SendResult<String, Object> status) {
        System.out.println("Sent successfully : " + status.getRecordMetadata().offset());
    }

    private void handleFailure(Object message, Throwable throwable) {
        System.err.println("Error sending notification" + throwable.getMessage());

        // Fallback Strategies
//        saveToDatabase(message);
//        sendToDeadLetterTopic(message);
//        retrySend(message);

        throwable.printStackTrace();
    }
}