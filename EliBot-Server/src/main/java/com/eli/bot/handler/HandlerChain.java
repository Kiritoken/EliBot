package com.eli.bot.handler;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * handlers
 * @author Eli
 */
@Component
public class HandlerChain {

    private final List<AbstractHandler> handlers;

    public HandlerChain(List<AbstractHandler> handlers) {
        this.handlers = handlers;
    }

    public void handle(Bot bot, GroupMessageEvent event) throws Exception {
        for (AbstractHandler handler : handlers) {
            if (handler.isMatched(bot, event)) {
                handler.handle(bot, event);
                return;
            }
        }
    }

}
