package com.skillbox.cryptobot.service;

import com.skillbox.cryptobot.model.Subscribe;
import com.skillbox.cryptobot.repository.SubscribeRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Service
@Slf4j
public class SubscribeService {

    private final SubscribeRepository subscribeRepository;

    @Value("${telegram.bot.inactive.days}")
    private int inActiveDays;



    public void createOrUpdateSubscribe(Long userId, Integer desiredPrice) {
        // Проверяем, существует ли подписка с данным userId
        Optional<Subscribe> existingSubscribe = subscribeRepository.findByUserId(userId);

        if (existingSubscribe.isPresent()) {
            // Если подписка существует, обновляем её
            Subscribe subscribe = existingSubscribe.get();
            subscribe.setDesiredPrice(desiredPrice);
            subscribe.setLastInteractionTime(System.currentTimeMillis());
            subscribeRepository.save(subscribe);
            log.info("Подписка обновлена для пользователя: {}", userId);
        } else {
            // Если подписки нет, создаем новую
            Subscribe subscribe = Subscribe.builder()
                    .userId(userId)
                    .desiredPrice(desiredPrice)
                    .lastInteractionTime(System.currentTimeMillis())
                    .build();
            updateLastInteractionTime(userId);
            subscribeRepository.save(subscribe);
            log.info("Создана новая подписка для пользователя: {}", userId);
        }
    }


    public Optional<Subscribe> getSubscribeByUserId(Long userId) {
        return subscribeRepository.findByUserId(userId);
    }

    public void deleteSubscribePriceByUserId(Long userId) {

        subscribeRepository.findByUserId(userId).ifPresent(subscribe -> {
            subscribe.setDesiredPrice(null);
            subscribeRepository.save(subscribe);
        });
    }

    public List<Subscribe> getAllSubscriptions() {
        return (List<Subscribe>) subscribeRepository.findAll();
    }

    public void removeInactiveSubscriptions() {
        long threshold = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(inActiveDays);
        List<Subscribe> subscriptions = getAllSubscriptions();
        for (Subscribe subscribe : subscriptions) {
            if (subscribe.getLastInteractionTime() < threshold) {
                deleteSubscribeByUserId(subscribe.getUserId());
                log.info("Удалена подписка для пользователя: {}", subscribe.getUserId());
            }
        }
    }


    public void deleteSubscribeByUserId(Long userId) {
        subscribeRepository.findByUserId(userId).ifPresent(subscribeRepository::delete);
    }

    public void updateLastInteractionTime(@NonNull Long id) {
        getSubscribeByUserId(id).ifPresent(subscribe -> {
            subscribe.setLastInteractionTime(System.currentTimeMillis());
            subscribeRepository.save(subscribe);
        });
    }
}