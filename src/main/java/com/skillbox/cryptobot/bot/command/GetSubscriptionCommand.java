package com.skillbox.cryptobot.bot.command;

import com.skillbox.cryptobot.model.Subscribe;
import com.skillbox.cryptobot.service.SubscribeService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.util.Optional;

@Service
@Slf4j
@AllArgsConstructor
public class GetSubscriptionCommand implements IBotCommand {

    private final SubscribeService service;
    private final SubscribeService subscribeService;


    private static final String SUBSCRIPTION_MESSAGE = "Вы подписаны на стоимость биткоина %s USD";
    private static final String NO_SUBSCRIPTION_MESSAGE = "Активные подписки отсутствуют";

    @Override
    public String getCommandIdentifier() {
        return "get_subscription";
    }

    @Override
    public String getDescription() {
        return "Возвращает текущую подписку";
    }

    @Override
    public void processMessage(AbsSender absSender, Message message, String[] arguments) {
        SendMessage answer = new SendMessage();
        answer.setChatId(message.getChatId());

        subscribeService.updateLastInteractionTime(message.getFrom().getId());

        Optional<Subscribe> subscribeByUserId = service.getSubscribeByUserId(message.getFrom().getId());
        if (subscribeByUserId.isPresent() && subscribeByUserId.get().getDesiredPrice() != null) {
            Subscribe subscribe = subscribeByUserId.get();
            String responseMessage = String.format(SUBSCRIPTION_MESSAGE, subscribe.getDesiredPrice());
            sendMessage(absSender, answer, responseMessage);
            log.info("Пользователь {} запрашивает подписку: {}", message.getFrom().getId(), subscribe.getDesiredPrice());
        } else {
            sendMessage(absSender, answer, NO_SUBSCRIPTION_MESSAGE);
            log.info("Пользователь {} запрашивает подписку, но активные подписки отсутствуют.", message.getFrom().getId());
        }
    }

    private void sendMessage(AbsSender absSender, SendMessage message, String text) {
        try {
            message.setText(text);
            absSender.execute(message);
        } catch (Exception e) {
            log.error("Ошибка при отправке сообщения", e);
        }
    }
}