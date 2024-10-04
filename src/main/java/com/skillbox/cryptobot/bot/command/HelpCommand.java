package com.skillbox.cryptobot.bot.command;

import com.skillbox.cryptobot.service.SubscribeService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;

@Service
@Slf4j
@RequiredArgsConstructor
public class HelpCommand implements IBotCommand {

    private final SubscribeService subscribeService;

    @Override
    public String getCommandIdentifier() {
        return "help";
    }

    @Override
    public String getDescription() {
        return "Показывает список доступных команд";
    }

    @Override
    public void processMessage(AbsSender absSender, Message message, String[] arguments) {
        SendMessage answer = new SendMessage();
        answer.setChatId(message.getChatId());

        subscribeService.updateLastInteractionTime(message.getFrom().getId());

        answer.setText(
                """
                         Поддерживаемые команды:
                         /subscribe [число] - подписаться на стоимость биткоина в USD
                         /get_price - получить стоимость биткоина
                         /get_subscription - получить текущую подписку
                         /unsubscribe - отменить подписку на стоимость
                         /help - список доступных команд
                        """

        );

        try {
            absSender.execute(answer);
        } catch (Exception e) {
            log.error("Ошибка при отправке сообщения", e);
        }
    }
}
