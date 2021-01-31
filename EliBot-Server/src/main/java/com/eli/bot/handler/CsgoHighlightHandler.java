package com.eli.bot.handler;

import com.eli.bot.task.DemoGenerateTask;
import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.message.data.QuoteReply;
import org.springframework.stereotype.Component;

import java.util.concurrent.*;

/**
 * #demo http://#####/.dem steam64Id
 *
 * @author Eli
 */
@Slf4j
@Component
public class CsgoHighlightHandler extends AbstractHandler {

    private static final int THREAD_NUM = 1;

    private static final int CAPACITY = 1024;

    private static final String ARGUMENT_SEPARATOR = " ";

    private static final int SEPARATOR_NUM = 3;

    private static final ThreadPoolExecutor EXECUTOR = new ThreadPoolExecutor(THREAD_NUM, THREAD_NUM, 5,
            TimeUnit.SECONDS, new LinkedBlockingQueue<>(CAPACITY), new ThreadPoolExecutor.AbortPolicy());


    @Override
    public boolean isMatched(GroupMessageEvent event) {
        String message = event.getMessage().contentToString();
        return message.startsWith("#demo");
    }

    @Override
    public void handle(GroupMessageEvent event) {
        String message = event.getMessage().contentToString();
        String[] s = message.split(ARGUMENT_SEPARATOR);
        final QuoteReply quote = new QuoteReply(event.getSource());
        if (s.length < SEPARATOR_NUM) {
            event.getGroup().sendMessage(quote.plus("回复#demo + 空格 + demo链接 + 空格 +steamId 试试吧"));
            return;
        }
        String demoUrl = s[1];
        String steam64Id = s[2];
        log.info("群:{}({}) 成员:{}({}) 查询demo:{}低光视频", event.getGroup().getName(), event.getGroup().getId(),
                event.getSender().getNick(), event.getSender().getId(), demoUrl);
        try {
            // 提交视频解析处理任务
            EXECUTOR.execute(new DemoGenerateTask(event.getGroup().getId(), event.getSender().getId(), steam64Id, demoUrl));
            int waitingTaskNum = EXECUTOR.getQueue().size();
            log.info("当前视频解析任务排队数:{}", waitingTaskNum);
            event.getGroup().sendMessage(quote.plus(String.format("demo解析任务已提交,还需排队等待%s人,请耐心等待", waitingTaskNum)));
        } catch (Exception e) {
            log.error("demo视频解析任务异常,demoUrl:{} steam64Id:{}", demoUrl, steam64Id, e);
            event.getGroup().sendMessage(quote.plus("demo解析异常,请稍后重试"));
        }
    }
}
