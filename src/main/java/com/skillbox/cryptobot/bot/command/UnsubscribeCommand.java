package com.skillbox.cryptobot.bot.command;

import com.skillbox.cryptobot.model.Subscribe;
import com.skillbox.cryptobot.service.SubscribeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.util.Optional;

/**
 * Обработка команды отмены подписки на курс валюты
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UnsubscribeCommand implements IBotCommand {

    private static final String SUBSCRIPTION_CANCELLATION_MESSAGE = "Подписка отменена";
    private static final String NO_SUBSCRIPTION_MESSAGE = "Активные подписки отсутствуют";

    private final SubscribeService subscribeService;


    @Override
    public String getCommandIdentifier() {
        return "unsubscribe";
    }

    @Override
    public String getDescription() {
        return "Отменяет подписку пользователя";
    }

    @Override
    public void processMessage(AbsSender absSender, Message message, String[] arguments) {
        SendMessage answer = new SendMessage();
        answer.setChatId(message.getChatId());

        subscribeService.updateLastInteractionTime(message.getFrom().getId());

        Long userId = message.getFrom().getId();
        Optional<Subscribe> subscribeByUserId = subscribeService.getSubscribeByUserId(userId);

        if (subscribeByUserId.isPresent() && subscribeByUserId.get().getDesiredPrice() != null) {

            subscribeService.deleteSubscribePriceByUserId(userId);
            sendMessage(absSender, answer, SUBSCRIPTION_CANCELLATION_MESSAGE);
            log.info("Пользователь {}: {}", userId, SUBSCRIPTION_CANCELLATION_MESSAGE);
        } else {
            sendMessage(absSender, answer, NO_SUBSCRIPTION_MESSAGE);
            log.info("Пользователь {}: {}", userId, NO_SUBSCRIPTION_MESSAGE);
        }
    }

    private void sendMessage(AbsSender absSender, SendMessage message, String text) {
        try {
            message.setText(text);
            absSender.execute(message);
        } catch (Exception e) {
            log.error("Ошибка при отправке сообщения пользователю: {}", message.getChatId(), e);
        }
    }
}