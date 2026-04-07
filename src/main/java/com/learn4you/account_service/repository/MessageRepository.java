package com.learn4you.account_service.repository;

import com.learn4you.account_service.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Integer> {
    public List<Message> findByStatus(boolean status);
}