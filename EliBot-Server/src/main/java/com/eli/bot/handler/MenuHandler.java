package com.eli.bot.handler;

import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.message.data.QuoteReply;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 菜单
 *
 * @author Eli
 */
@Slf4j
@Order(1000)
@Component
public class MenuHandler extends AbstractHandler {

    @Override
    public boolean isMatched(GroupMessageEvent event) {
        Bot bot = event.getBot();
        String message = event.getMessage().contentToString();
        String atId = "@" + bot.getId();
        String atNick = "@" + bot.getNick();
        return message.contains(atId) || message.contains(atNick);
    }

    @Override
    public void handle(GroupMessageEvent event) {
        log.info("群:{}({}) 成员:{}({}) 信息:{}", event.getGroup().getName(), event.getGroup().getId(),
                event.getSender().getNick(), event.getSender().getId(), event.getMessage().contentToString());
        // 引用回复
        final QuoteReply quote = new QuoteReply(event.getSource());
        String menu = "尝试回复以下内容\n" +
                "#基金查询+基金代码\n" +
                "#今日球鞋\n" +
                "#上线\n" +
                "#About";
        event.getGroup().sendMessage(quote.plus(menu));
    }
}
