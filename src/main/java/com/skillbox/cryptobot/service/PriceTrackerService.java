package com.skillbox.cryptobot.service;

import com.skillbox.cryptobot.bot.CryptoBot;
import com.skillbox.cryptobot.model.Subscribe;
import com.skillbox.cryptobot.utils.TextUtil;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class PriceTrackerService {

    private final SubscribeService subscribeService;
    private final CryptoCurrencyService cryptoCurrencyService;
    private final CryptoBot cryptoBot;

    @Value("${telegram.bot.notify.delay.value}")
    private int notifyDelayValue;

    @Value("${telegram.bot.check.price.value}")
    private int checkDelayValue;

    @Value("${telegram.bot.unit}")
    private String notifyUnit;

    private long lastNotificationTime = 0;


    @PostConstruct
    public void init() {
        long fixedRateMillis = getDelayInMillis(checkDelayValue);
        System.setProperty("telegram.bot.check.price.value", String.valueOf(fixedRateMillis));
    }

    @Scheduled(fixedRateString = "${telegram.bot.check.price.value}")
    public void checkPrices() {
        try {
            double currentPrice = cryptoCurrencyService.getBitcoinPrice();
            log.info("Текущая цена биткоина: {} USD", TextUtil.toString(currentPrice));
            List<Subscribe> subscriptions = subscribeService.getAllSubscriptions();

            for (Subscribe subscribe : subscriptions) {
                if (subscribe.getDesiredPrice() != null && subscribe.getDesiredPrice() >= currentPrice) {
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - lastNotificationTime >= getDelayInMillis(notifyDelayValue)) {
                        sendNotification(subscribe, currentPrice);
                        lastNotificationTime = currentTime;
                    }
                }
            }
        } catch (IOException e) {
            log.error("Ошибка при получении текущей цены биткоина", e);
        }
    }

    private void sendNotification(Subscribe subscribe, double currentPrice) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(subscribe.getUserId()));
        message.setText("Пора покупать, стоимость биткоина: " + TextUtil.toString(currentPrice) + " USD");

        try {
            cryptoBot.execute(message);
            log.info("Уведомление отправлено пользователю: {}", subscribe.getUserId());
        } catch (TelegramApiRequestException e) {
            log.error("Бот заблокирован пользователем");
            subscribeService.deleteSubscribeByUserId(subscribe.getUserId());
            log.error("Удаляем пользователя из базы {}", subscribe.getUserId());
        } catch (Exception e) {
            log.error("Ошибка при отправке уведомления", e);
        }
    }


    private long getDelayInMillis(int delay) {
        return switch (notifyUnit.toLowerCase()) {
            case "minutes" -> Duration.ofMinutes(delay).toMillis();
            case "seconds" -> Duration.ofSeconds(delay).toMillis();
            case "hours" -> Duration.ofHours(delay).toMillis();
            default -> throw new IllegalArgumentException("Неверная единица времени: " + notifyUnit);
        };
    }
}