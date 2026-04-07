package com.learn4you.account_service.repository;

import com.learn4you.account_service.model.Statistic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StatisticRepository extends JpaRepository<Statistic, Integer> {
    public List<Statistic> findByStatus(boolean status);
}