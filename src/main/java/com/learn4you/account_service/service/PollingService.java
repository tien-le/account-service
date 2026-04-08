package com.learn4you.account_service.service;

import com.learn4you.account_service.model.Message;
import com.learn4you.account_service.model.Statistic;
import com.learn4you.account_service.repository.MessageRepository;
import com.learn4you.account_service.repository.StatisticRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class PollingService {
    private static final int BATCH_SIZE = 100;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    MessageRepository messageRepository;

    @Autowired
    StatisticRepository statisticRepository;

    void handleFailure(Object message, Throwable throwable) {
        logger.error("Message - Error sending notification. Due to: {}", throwable.getMessage());
    }

    @Scheduled(fixedRate = 1000)
    public void producer() {
        processMessages();
        processStatistics();
        logger.info("Batch polling cycle completed");
    }

    private void processMessages() {
        Page<Message> messages = messageRepository.findByStatus(
                false,
                PageRequest.of(0, BATCH_SIZE, Sort.by("id").ascending())
        );
        if (messages.isEmpty()) {
            return;
        }

        List<Message> successfulMessages = Collections.synchronizedList(new ArrayList<>());
        List<CompletableFuture<SendResult<String, Object>>> futures = new ArrayList<>();

        for (Message message : messages.getContent()) {
            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send("notification", message);
            futures.add(future.whenComplete((status, ex) -> {
                if (ex != null) {
                    handleFailure(message, ex);
                } else {
                    successfulMessages.add(message);
                    logger.info("Re-Sent successfully : {}", status.getRecordMetadata().offset());
                }
            }));
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        if (!successfulMessages.isEmpty()) {
            successfulMessages.forEach(message -> message.setStatus(true));
            messageRepository.saveAll(successfulMessages);
        }
    }

    private void processStatistics() {
        Page<Statistic> statistics = statisticRepository.findByStatus(
                false,
                PageRequest.of(0, BATCH_SIZE, Sort.by("id").ascending())
        );
        if (statistics.isEmpty()) {
            return;
        }

        List<Statistic> successfulStatistics = Collections.synchronizedList(new ArrayList<>());
        List<CompletableFuture<SendResult<String, Object>>> futures = new ArrayList<>();

        for (Statistic statistic : statistics.getContent()) {
            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send("statistic", statistic);
            futures.add(future.whenComplete((status, ex) -> {
                if (ex != null) {
                    logger.error("Statistics - Error sending statistic. Due to: {}", ex.getMessage());
                } else {
                    successfulStatistics.add(statistic);
                    logger.info("Updated statistic successfully : {}", status.getRecordMetadata().offset());
                }
            }));
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        if (!successfulStatistics.isEmpty()) {
            successfulStatistics.forEach(statistic -> statistic.setStatus(true));
            statisticRepository.saveAll(successfulStatistics);
        }
    }

    @Scheduled(fixedRate = 60000)
    public void deleteMessageSuccess() {
        List<Message> messages = messageRepository.findByStatus(true);
        messageRepository.deleteAllInBatch(messages);

        List<Statistic> statistics = statisticRepository.findByStatus(true);
        statisticRepository.deleteAllInBatch(statistics);

        logger.info("All statistics and notifications deleted successfully");
    }
}
