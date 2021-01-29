package com.eli.bot.handler;

import com.eli.bot.entity.marketIndex.MarketIndex;
import com.eli.bot.service.IMarketIndexService;
import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.QuoteReply;
import net.mamoe.mirai.utils.ExternalResource;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * #大盘 [上证|深证|创业板]
 * 查询大盘信息，默认上证指数
 *
 * @author Eli
 */
@Slf4j
@Order(2)
@Component
public class MarketIndexHandler extends AbstractHandler {

    private enum ComponentIndex {
        // 默认值
        Unknown {
            @Override
            public String getCode() {
                return "000000";
            }
        },
        // 上证指数
        ShangHai {
            @Override
            public String getCode() {
                return "000001";
            }
        },
        // 深证成指
        Shenzhen {
            @Override
            public String getCode() {
                return "399001";
            }
        },
        // 创业板指
        GrowthEnterprise {
            @Override
            public String getCode() {
                return "399006";
            }
        };

        public abstract String getCode();


        public static ComponentIndex getIndex(String board) {
            switch (board) {
                case "":
                case "上证":
                    return ShangHai;
                case "深证":
                    return Shenzhen;
                case "创业板":
                    return GrowthEnterprise;
                default:
                    return Unknown;
            }
        }
    }

    private final RestTemplate restTemplate;

    private final IMarketIndexService indexService;

    public MarketIndexHandler(IMarketIndexService indexService, RestTemplate restTemplate) {
        this.indexService = indexService;
        this.restTemplate = restTemplate;
    }

    @Override
    public boolean isMatched(GroupMessageEvent event) {
        String message = event.getMessage().contentToString();
        // 判断是否符合查询格式
        return message.startsWith("#大盘");
    }

    @Override
    public void handle(GroupMessageEvent event) {
        String message = event.getMessage().contentToString();
        String board = message.substring(3).trim();
        log.info("群:{}({}) 成员:{}({}) 查询大盘{}信息", event.getGroup().getName(), event.getGroup().getId(),
                event.getSender().getNick(), event.getSender().getId(), board);
        ComponentIndex index = ComponentIndex.getIndex(board);
        if (index == ComponentIndex.Unknown) {
            event.getGroup().sendMessage("该板块跳楼了");
            return;
        }
        // 查询大盘指数信息
        try {
            // 指数图片
            Image image = uploadIndexImage(index, event);
            // 指数信息
            MarketIndex entity = indexService.findIndexInfo(index.getCode()).orElse(new MarketIndex());
            String indexInfo = String.format("%s(%s)\n涨跌幅: %s\n价格: %s\n涨跌额: %s\n更新时间: %s",
                    entity.getIndexName(), entity.getIndexCode(),
                    entity.getAD(),
                    entity.getPrice(),
                    entity.getPriceAD(),
                    entity.getUpdateTime());
            // 引用回复
            final QuoteReply quote = new QuoteReply(event.getSource());
            MessageChain messages = quote.plus(indexInfo).plus(image);
            // 发送图片
            event.getGroup().sendMessage(messages);
        } catch (Exception e) {
            log.error("查询大盘指数信息异常", e);
            event.getGroup().sendMessage("查询大盘指数信息异常");
        }
    }

    private Image uploadIndexImage(ComponentIndex index, GroupMessageEvent event) {
        String url = "http://webquotepic.eastmoney.com/GetPic.aspx?nid=";
        // 上证
        if (index == ComponentIndex.ShangHai) {
            url += "1.";
        } else {
            url += "0.";
        }
        url += index.getCode() + "&imageType=r";
        ResponseEntity<byte[]> res = restTemplate.getForEntity(url, byte[].class);
        byte[] image = res.getBody();
        if (null != image) {
            return event.getGroup().uploadImage(ExternalResource.create(image));
        } else {
            throw new RuntimeException("下载大盘指数" + index.toString() + "图片异常");
        }
    }
}
