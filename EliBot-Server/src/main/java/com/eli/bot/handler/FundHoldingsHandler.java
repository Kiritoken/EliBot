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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * #持仓
 * 自选基金持仓信息
 *
 * @author Eli
 */
@Slf4j
@Order(5)
@Component
public class FundHoldingsHandler extends AbstractHandler {

    private final RestTemplate restTemplate;

    private final IFundService fundService;

    public FundHoldingsHandler(IFundService fundService, RestTemplate restTemplate) {
        this.fundService = fundService;
        this.restTemplate = restTemplate;
    }

    @Override
    public boolean isMatched(GroupMessageEvent event) {
        String message = event.getMessage().contentToString();
        // 判断是否符合查询格式
        return message.equals("#持仓");
    }

    @Override
    public void handle(GroupMessageEvent event) throws FileNotFoundException {
        log.info("群:{}({}) 成员:{}({}) 查询持仓信息", event.getGroup().getName(), event.getGroup().getId(),
                event.getSender().getNick(), event.getSender().getId());
        try {
            // 查询用户配置文件
            FundHoldingsEditorService holdings = FundHoldingsEditorService.getInstance();
            List<LeptFund> fundList = holdings.getUserHoldings(String.valueOf(event.getSender().getId()));
            if (fundList == null) {
                event.getGroup().sendMessage("你持仓空啦，跑的真快");
                return;
            }
            // 查询用户持仓基金
            String holdingsInfo = "名称(代码)\n净值估算    涨跌额    涨跌幅\n";
            holdingsInfo += String.join("", Collections.nCopies(10, "-"));
            holdingsInfo += "\n";
            List<String> fundCodeList = new LinkedList<>();
            for (LeptFund fund : fundList) {
                fundCodeList.add(fund.getFundCode());
            }
            fundList = fundService.findFundsInfoList(String.join(" ", fundCodeList))
                    .orElse(new LinkedList<>());
            for (LeptFund fund : fundList) {
                holdingsInfo += String.format("%s(%s)\n%s    %s    %s\n", fund.getFundName(), fund.getFundCode(),
                        fund.getNetAssetValue(), fund.getValueAD(), fund.getAD());
            }
            // 回复
            final QuoteReply quote = new QuoteReply(event.getSource());
            MessageChain messages = quote.plus(holdingsInfo);
            event.getGroup().sendMessage(messages);
        } catch (Exception e) {
            log.error("查询持仓信息异常", e);
            event.getGroup().sendMessage("查询持仓信息异常");
        }
    }
}
