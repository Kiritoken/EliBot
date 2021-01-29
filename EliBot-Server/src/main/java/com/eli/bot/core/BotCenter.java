package com.eli.bot.core;

import com.eli.bot.event.MessageEvent;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.BotFactory;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.utils.BotConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 机器人
 *
 * @author Eli
 */
@Slf4j
@Data
@Component
public class BotCenter {

    private Bot bot;

    /**
     * qq号
     */
    @Value("${bot.id}")
    private Long id;

    /**
     * qq密码
     */
    @Value("${bot.password}")
    private String password;

    private final MessageEvent messageEvent;

    public BotCenter(MessageEvent messageEvent) {
        this.messageEvent = messageEvent;
    }

    public synchronized void run() {
        bot = BotFactory.INSTANCE.newBot(id, password, new BotConfiguration() {
            {
                // 设备缓存信息
                fileBasedDeviceInfo(String.format("%s.json", id));
            }
        });
        bot.login();
        // 注册事件
        bot.getEventChannel().registerListenerHost(messageEvent);
        Group group = bot.getGroup(116868731);
        log.info("qq:{} 已成功登录", id);
    }
}