package com.eli.bot.event;

import com.eli.bot.handler.HandlerChain;
import kotlin.coroutines.CoroutineContext;
import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.ListeningStatus;
import net.mamoe.mirai.event.SimpleListenerHost;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * @author Eli
 */
@Slf4j
@Component
public class MessageEvent extends SimpleListenerHost {

    @Autowired
    private HandlerChain handlerChain;

    @Override
    public void handleException(@NotNull CoroutineContext context, @NotNull Throwable exception) {
        super.handleException(context, exception);
    }

    @EventHandler
    public ListeningStatus onMessage(@NotNull GroupMessageEvent event) throws Exception { // 可以抛出任何异常, 将在 handleException 处理
        String message = event.getMessage().contentToString();
        Bot bot = event.getBot();
        log.info("接收到群{}的消息{}", event.getGroup().getId(), message);
        if (bot.getId() == (event.getSender().getId())) {
            // 自己发送的消息
            return ListeningStatus.LISTENING;
        }
        handlerChain.handle(bot, event);
        // 表示继续监听事件
        return ListeningStatus.LISTENING;
    }


}
