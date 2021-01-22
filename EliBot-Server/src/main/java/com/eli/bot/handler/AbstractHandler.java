package com.eli.bot.handler;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.event.events.GroupMessageEvent;

public abstract class AbstractHandler {

    public abstract boolean isMatched(Bot bot, GroupMessageEvent event);

    public abstract void handle(Bot bot, GroupMessageEvent event) throws Exception;

}
