package com.skillbox.cryptobot.repository;

import com.skillbox.cryptobot.model.Subscribe;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface SubscribeRepository extends CrudRepository<Subscribe, Long> {
    Optional<Subscribe> findByUserId(Long userId);
}