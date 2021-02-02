package com.eli.bot.handler;

import com.eli.bot.entity.fund.Fund;
import com.eli.bot.entity.fundHoldings.LeptFund;
import com.eli.bot.service.FundHoldingsEditorService;
import com.eli.bot.service.IFundService;
import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.QuoteReply;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.FileNotFoundException;
import java.util.*;

/**
 * #编辑持仓 基金代码 使用空格分隔多个基金代码
 * 编辑自选基金持仓信息，总是覆盖已有配置
 *
 * @author Eli
 */
@Slf4j
@Order(6)
@Component
public class FundHoldingsEditorHandler extends AbstractHandler {

    private final RestTemplate restTemplate;

    private final IFundService fundService;

    public FundHoldingsEditorHandler(IFundService fundService, RestTemplate restTemplate) {
        this.fundService = fundService;
        this.restTemplate = restTemplate;
    }

    @Override
    public boolean isMatched(GroupMessageEvent event) {
        String message = event.getMessage().contentToString();
        // 判断是否符合查询格式
        return message.startsWith("#编辑持仓 ");
    }

    @Override
    public void handle(GroupMessageEvent event) throws FileNotFoundException {
        String message = event.getMessage().contentToString().substring(6);
        String codeList[] = message.split(" +");
        log.info("群:{}({}) 成员:{}({}) 编辑持仓信息", event.getGroup().getName(), event.getGroup().getId(),
                event.getSender().getNick(), event.getSender().getId());
        List<String> request = new LinkedList<>();
        Set<String> existedFund = new HashSet<>();
        for (String code : codeList) {
            if (!code.matches("\\d{6}")) {
                event.getGroup().sendMessage("基金代码格式错误");
                return;
            }
            if (existedFund.contains(code)) continue;
            else existedFund.add(code);
            request.add(code);
        }
        try {
            // 查询用户持仓基金
            String fundInfo = "已添加基金:\n";
            List<LeptFund> fundList = fundService.findFundsInfoList(String.join(" ", request))
                    .orElse(new LinkedList<>());
            for (LeptFund fund : fundList) {
                fundInfo += String.format("%s(%s)\n", fund.getFundName(), fund.getFundCode());
            }
            FundHoldingsEditorService holdings = FundHoldingsEditorService.getInstance();
            holdings.setUserHoldings(String.valueOf(event.getSender().getId()), fundList);
            holdings.save();
            // 回复
            final QuoteReply quote = new QuoteReply(event.getSource());
            MessageChain messages = quote.plus(fundInfo);
            event.getGroup().sendMessage(messages);
        } catch (Exception e) {
            log.error("编辑持仓信息异常", e);
            event.getGroup().sendMessage("编辑持仓信息异常");
        }
    }
}
