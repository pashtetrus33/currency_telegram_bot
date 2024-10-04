package com.skillbox.cryptobot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class InactivityCheckService {

    private final SubscribeService subscribeService;

    @Scheduled(cron = "0 0 0 * * ?") // Запускается каждый день в полночь
    public void checkInactiveSubscriptions() {
        subscribeService.removeInactiveSubscriptions();
        log.info("Checking inactive subscriptions");
    }
}
