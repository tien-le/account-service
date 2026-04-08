package com.learn4you.account_service.repository;

import com.learn4you.account_service.model.Statistic;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StatisticRepository extends JpaRepository<Statistic, Integer> {
    List<Statistic> findByStatus(boolean status);

    Page<Statistic> findByStatus(boolean status, Pageable pageable);
}
