package com.eli.bot.handler;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * #About
 * about 信息
 *
 * @author Eli
 */
@Order(3)
@Component
public class AboutHandler extends AbstractHandler {


    @Override
    public boolean isMatched(GroupMessageEvent event) {
        String message = event.getMessage().contentToString();
        // 判断是否符合查询格式
        return message.startsWith("#About");
    }

    @Override
    public void handle(GroupMessageEvent event) {
        String aboutMessage = "我起了,一枪秒了,有什么好说的\nGithub: https://github.com/Kiritoken/EliBot\n";
        event.getSubject().sendMessage(aboutMessage);
    }
}
