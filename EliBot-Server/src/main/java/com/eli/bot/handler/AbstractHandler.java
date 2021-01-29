package com.eli.bot.handler;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.event.events.GroupMessageEvent;

/**
 * @author Eli
 */
public abstract class AbstractHandler {

    /**
     * 符合条件
     *
     * @param event 群消息事件
     * @return true:符合条件；false：不符合条件
     */
    public abstract boolean isMatched(GroupMessageEvent event);

    /**
     * 处理事件
     *
     * @param event 群消息事件
     * @throws Exception e
     */
    public abstract void handle(GroupMessageEvent event) throws Exception;

}
