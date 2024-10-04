package com.skillbox.cryptobot.bot.command;

import com.skillbox.cryptobot.service.CryptoCurrencyService;
import com.skillbox.cryptobot.service.SubscribeService;
import com.skillbox.cryptobot.utils.TextUtil;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;

/**
 * Обработка команды получения текущей стоимости валюты
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class GetPriceCommand implements IBotCommand {

    private final CryptoCurrencyService service;
    private final SubscribeService subscribeService;

    @Override
    public String getCommandIdentifier() {
        return "get_price";
    }

    @Override
    public String getDescription() {
        return "Возвращает цену биткоина в USD";
    }

    @Override
    public void processMessage(AbsSender absSender, Message message, String[] arguments) {

        SendMessage answer = new SendMessage();
        answer.setChatId(message.getChatId());

        subscribeService.updateLastInteractionTime(message.getFrom().getId());

        try {
            String bitcoinPrice = TextUtil.toString(service.getBitcoinPrice());
            answer.setText("Текущая цена биткоина: " + bitcoinPrice + " USD");
        } catch (Exception e) {
            log.error("Ошибка возникла в методе /get_price", e);
            answer.setText("Не удалось получить текущую цену биткоина. Попробуйте позже.");
        }

        try {
            absSender.execute(answer);
        } catch (Exception e) {
            log.error("Ошибка при отправке сообщения", e);
        }
    }
}