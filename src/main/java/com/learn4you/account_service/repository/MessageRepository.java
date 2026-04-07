package com.learn4you.account_service.repository;

import com.learn4you.account_service.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Integer> {
    public List<Message> findByStatus(boolean status);
}