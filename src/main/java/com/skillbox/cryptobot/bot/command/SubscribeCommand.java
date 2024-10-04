package com.skillbox.cryptobot.bot.command;

import com.skillbox.cryptobot.service.CryptoCurrencyService;
import com.skillbox.cryptobot.service.SubscribeService;
import com.skillbox.cryptobot.utils.TextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.io.IOException;

/**
 * Обработка команды подписки на курс валюты.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SubscribeCommand implements IBotCommand {

    private final SubscribeService subscribeService;
    private final CryptoCurrencyService cryptoCurrencyService;

    private static final String INVALID_ARGUMENTS_MSG = "Должен быть один аргумент - положительное целое число до 2 млрд.";
    private static final String NOT_AN_INTEGER_MSG = "Ошибка: %s не является допустимым целым числом.";
    private static final String NOT_POSITIVE_MSG = "Ошибка: %s должно быть больше нуля.";
    private static final String TOO_LARGE_MSG = "Ошибка: %s должно быть меньше 2 млрд.";
    private static final String SUCCESS_MESSAGE = "Новая подписка создана на стоимость %s USD";
    private static final String CURRENT_BITCOIN_PRICE = "Текущая цена биткоина: %s USD";

    @Override
    public String getCommandIdentifier() {
        return "subscribe";
    }

    @Override
    public String getDescription() {
        return "Подписывает пользователя на стоимость биткоина";
    }

    @Override
    public void processMessage(AbsSender absSender, Message message, String[] arguments) {

        SendMessage answer = new SendMessage();
        answer.setChatId(message.getChatId());

        if (arguments.length != 1) {
            sendMessage(absSender, answer, INVALID_ARGUMENTS_MSG);
            return;
        }

        Integer subscriptionValue = parseInteger(arguments[0]);
        if (subscriptionValue == null) {
            sendMessage(absSender, answer, String.format(NOT_AN_INTEGER_MSG, arguments[0]));
        } else if (subscriptionValue < 1) {
            sendMessage(absSender, answer, String.format(NOT_POSITIVE_MSG, arguments[0]));
        } else if (subscriptionValue >= 2_000_000_000) {
            sendMessage(absSender, answer, String.format(TOO_LARGE_MSG, arguments[0]));
        } else {
            handleSubscription(absSender, answer, message.getChatId(), subscriptionValue);
        }
    }

    private void handleSubscription(AbsSender absSender, SendMessage answer, Long chatId, Integer subscriptionValue) {
        try {
            String currentPrice = TextUtil.toString(cryptoCurrencyService.getBitcoinPrice());
            sendMessage(absSender, answer, String.format(CURRENT_BITCOIN_PRICE, currentPrice));
            subscribeService.createOrUpdateSubscribe(chatId, subscriptionValue);
            sendMessage(absSender, answer, String.format(SUCCESS_MESSAGE, subscriptionValue));

            String successLogMessage = String.format("Новая подписка создана на стоимость %d USD", subscriptionValue);
            log.info(successLogMessage);


        } catch (IOException e) {
            log.error("Ошибка при получении цены биткоина", e);
            sendMessage(absSender, answer, "Ошибка при получении цены биткоина. Пожалуйста, попробуйте позже.");
        } catch (Exception e) {
            log.error("Ошибка при обработке подписки", e);
            sendMessage(absSender, answer, "Произошла ошибка. Попробуйте снова.");
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

    public static Integer parseInteger(String input) {
        try {
            int value = Integer.parseInt(input);
            return value > 0 ? value : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}