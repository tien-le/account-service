package com.learn4you.account_service.service;

import com.learn4you.account_service.model.Message;
import com.learn4you.account_service.model.Statistic;
import com.learn4you.account_service.repository.AccountRepository;
import com.learn4you.account_service.repository.MessageRepository;
import com.learn4you.account_service.repository.StatisticRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PollingService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    MessageRepository messageRepository;

    @Autowired
    StatisticRepository statisticRepository;

    void handleSuccess(Message message, SendResult<String, Object> status) {
        logger.info("Re-Sent successfully : {}", status.getRecordMetadata().offset());
        // Update status to true
        message.setStatus(true);
        messageRepository.save(message);
        logger.info("Updated status successfully : {}", status.getRecordMetadata().offset());
    }

    void handleFailure(Object message, Throwable throwable) {
        logger.error("Message - Error sending notification. Due to: {}", throwable.getMessage());
    }

    @Scheduled(fixedRate = 1000)
    public void producer() {
        // messages
        List<Message> messages = messageRepository.findByStatus(false);
        for (Message message : messages) {
            kafkaTemplate.send("notification", message).whenComplete((status, ex) -> {
                if (ex != null) {
                    // Fallback Logic
                    // Handle failures, save to DB the failed events.
                    // Then, using Job Scheduler to send these failed events again
                    handleFailure(message, ex);
                } else {
                    // Handle success
                    handleSuccess(message, status);
                }
            });
        }

        // statistic
        List<Statistic> statistics = statisticRepository.findByStatus(false);
        for (Statistic statistic : statistics) {
            kafkaTemplate.send("statistic", statistic).whenComplete((status, ex) -> {
                if (ex != null) {
                    // Handle failures
                    logger.error("Statistics - Error sending statistic. Due to: {}", ex.getMessage());
                } else {
                    statistic.setStatus(true);
                    statisticRepository.save(statistic);
                    logger.info("Updated statistic successfully : {}", status.getRecordMetadata().offset());
                }
            });
        }
        logger.info("All statistics and notification updated successfully");
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