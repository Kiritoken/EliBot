package com.eli.bot;

import com.eli.bot.core.BotCenter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


/**
 * Eli群聊天机器人
 *
 * @author Eli
 * @date 2021/1/22
 * @email 357449971@qq.com
 */
@Slf4j
@SpringBootApplication
public class EliBotApplication implements CommandLineRunner {

    @Autowired
    private BotCenter botCenter;

    public static void main(String[] args) {
        SpringApplication.run(EliBotApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        botCenter.run();
    }
}
