package com.eli.bot.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.eli.bot.entity.Fund;
import com.eli.bot.entity.PositionSharesItem;
import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.QuoteReply;
import net.mamoe.mirai.utils.ExternalResource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.regex.Pattern;

/**
 * #基金查询 008888
 * 查询基金信息
 *
 * @author Eli
 */
@Slf4j
@Order(1)
@Component
public class FundInfoSearchHandler extends AbstractHandler {

    private static final String NUM_REGEX = "[0-9]+";

    private static final String CODE = "code";

    private static final int SUCCESS = 200;

    @Value("${fund.queryFundInfo}")
    private String queryFundInfoApi;

    private final RestTemplate restTemplate;

    public FundInfoSearchHandler(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public boolean isMatched(Bot bot, GroupMessageEvent event) {
        String message = event.getMessage().contentToString();
        // 判断是否符合查询格式
        return message.startsWith("#基金查询");
    }

    @Override
    public void handle(Bot bot, GroupMessageEvent event) {
        String message = event.getMessage().contentToString();
        String fundCode = message.substring(5).trim();
        log.info("群:{}({}) 成员:{}({}) 查询基金{}信息", event.getGroup().getName(), event.getGroup().getId(),
                event.getSender().getNick(), event.getSender().getId(), fundCode);
        if (Pattern.matches(NUM_REGEX, fundCode)) {
            // 查询当天基金信息
            try {
                // 基金图片
                Image image = uploadFundImage(fundCode, event);

                Fund fund = queryFundInfo(fundCode);
                List<PositionSharesItem> positionShares = fund.getPositionShares();
                // 基金信息
                String fundInfo = String.format("%s(%s)\n日涨跌幅: %s\n净值: %s(%s)\n更新时间: %s\n", fund.getFundName(), fund.getFundCode(), fund.getAD()
                        , fund.getNetAssetValue(), fund.getTDay(), fund.getUpdateTime());
                // 持仓信息
                StringBuilder sharesInfo = new StringBuilder();
                if (CollectionUtils.isEmpty(positionShares)) {
                    sharesInfo.append("暂无持仓信息\n");
                } else {
                    sharesInfo.append("持仓信息如下:\n");
                    for (PositionSharesItem item : positionShares) {
                        sharesInfo.append(item.getStockName()).append("  ").append(item.getStockShares()).append("  ").append(item.getStockAD()).append("\n");
                    }
                }

                // 引用回复
                final QuoteReply quote = new QuoteReply(event.getSource());
                MessageChain messages = quote.plus(fundInfo).plus(image).plus(sharesInfo.toString());
                // 发送图片
                event.getGroup().sendMessage(messages);
            } catch (Exception e) {
                log.error("获取图片异常", e);
                event.getGroup().sendMessage("你在瞎几把报点");
            }
        } else {
            event.getGroup().sendMessage("你在瞎几把报点");
        }
    }

    private Image uploadFundImage(String fundCode, GroupMessageEvent event) throws Exception {
        // 查询当天基金信息
        URL url = new URL("http://j4.dfcfw.com/charts/pic6/" + fundCode + ".png");
        HttpURLConnection connection;
        connection = (HttpURLConnection) url.openConnection();
        //设置请求方式
        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
        //连接
        connection.connect();
        return event.getGroup().uploadImage(ExternalResource.create(connection.getInputStream()));
    }

    private Fund queryFundInfo(String fundCode) {
        // 调用基金查询接口
        ResponseEntity<String> entity = restTemplate.getForEntity(queryFundInfoApi + "?fundCode=" + fundCode, String.class);
        String body = entity.getBody();
        JSONObject res = JSON.parseObject(body);
        if (SUCCESS == res.getIntValue(CODE)) {
            Fund fund = res.getObject("data", Fund.class);
            log.info("查询基金{}成功,基金信息为{}", fundCode, JSON.toJSONString(fund));
            return fund;
        } else {
            log.error("查询基金{}信息失败", fundCode);
            throw new RuntimeException(String.format("查询基金%s信息失败", fundCode));
        }
    }


}
