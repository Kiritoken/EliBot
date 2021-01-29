package com.eli.bot.handler;

import com.eli.bot.cache.CacheHolder;
import com.eli.bot.entity.snkrs.Product;
import com.eli.bot.entity.snkrs.Sneaker;
import com.eli.bot.service.ISneakerService;
import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.message.data.QuoteReply;
import net.mamoe.mirai.utils.ExternalResource;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 最近球鞋查询
 *
 * @author Eli
 */
@Slf4j
@Order(4)
@Component
public class SneakersSearchHandler extends AbstractHandler {

    private final RestTemplate restTemplate;

    private final ISneakerService sneakerService;

    public SneakersSearchHandler(ISneakerService sneakerService, RestTemplate restTemplate) {
        this.sneakerService = sneakerService;
        this.restTemplate = restTemplate;
    }

    @Override
    public boolean isMatched(Bot bot, GroupMessageEvent event) {
        String message = event.getMessage().contentToString();
        // 判断是否符合查询格式
        return message.startsWith("#今日球鞋");
    }

    @Override
    public void handle(Bot bot, GroupMessageEvent event) {
        log.info("群:{}({}) 成员:{}({}) 查询球鞋信息", event.getGroup().getName(), event.getGroup().getId(),
                event.getSender().getNick(), event.getSender().getId());
        // 引用回复
        final QuoteReply quote = new QuoteReply(event.getSource());
        try {
            List<Sneaker> sneakers = filterSneakers(sneakerService.findLatestSneakerInfo());
            if (!CollectionUtils.isEmpty(sneakers)) {
                MessageChainBuilder chainBuilder = new MessageChainBuilder();
                chainBuilder.append(quote).append("今日球鞋发售信息:\n");
                for (int i = 0; i < sneakers.size(); i++) {
                    Sneaker sneaker = sneakers.get(i);
                    // 发售日期
                    String startSellDateStr = sneaker.getProduct().getStartSellDate() + " UTC";
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS Z");
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date startSellDate = simpleDateFormat.parse(startSellDateStr);

                    String sneakerInfoMsg = "No " + (i + 1) + "\n" + sneaker.getTitle() + "(" + sneaker.getSubtitle() + ")\n" +
                            "发售时间: " + dateFormat.format(startSellDate) + "：\n" + "发售价格: " + sneaker.getProduct().getPrice().getFullRetailPrice() + "\n";
                    chainBuilder.append(sneakerInfoMsg);

                    // 球鞋图片
                    String sneakerImageId = uploadSneakerImageAndGetImageId(sneaker, event);
                    chainBuilder.append(Image.fromId(sneakerImageId));
                }
                event.getSubject().sendMessage(chainBuilder.build());
            } else {
                // 查询无信息
                event.getSubject().sendMessage(quote.plus("今日暂无球鞋发售信息"));
            }
        } catch (Exception e) {
            log.error("查询球鞋信息异常", e);
            event.getSubject().sendMessage("查询球鞋信息异常");
        }
    }

    private List<Sneaker> filterSneakers(List<Sneaker> latestSneakerInfo) {
        return latestSneakerInfo.stream().filter(sneaker -> {
            Product product = sneaker.getProduct();
            if (StringUtils.isEmpty(sneaker.getTitle()) || StringUtils.isEmpty(product.getStartSellDate())) {
                return false;
            }
            // 发售日期
            String startSellDateStr = product.getStartSellDate() + " UTC";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS Z");
            try {
                Date startSellDate = simpleDateFormat.parse(startSellDateStr);
                Date nowDay = new Date();
                return DateUtils.isSameDay(startSellDate, nowDay) || startSellDate.getTime() > nowDay.getTime();
//                return DateUtils.isSameDay(startSellDate, nowDay);
            } catch (ParseException e) {
                log.error("", e);
                throw new RuntimeException("球鞋信息查询时间解析错误");
            }
        }).collect(Collectors.toList());
    }

    private String uploadSneakerImageAndGetImageId(Sneaker sneaker, GroupMessageEvent event) {
        String imageUrl = sneaker.getImageUrl();
        // 查询缓存获取snkrs图片ID缓存
        String imageId = (String) CacheHolder.getCache("snkrs").getIfPresent(imageUrl);
        if (StringUtils.isEmpty(imageId)) {
            log.info("snkrs: {} cache miss", imageUrl);
            // 上传图片
            ResponseEntity<byte[]> res = restTemplate.getForEntity(imageUrl, byte[].class);
            byte[] image = res.getBody();
            if (null != image) {
                Image uploadedImage = event.getGroup().uploadImage(ExternalResource.create(image));
                CacheHolder.getCache("snkrs").put(imageUrl, uploadedImage.getImageId());
                return uploadedImage.getImageId();
            } else {
                throw new RuntimeException("下载球鞋图片" + imageUrl + "异常");
            }
        } else {
            log.info("snkrs: {} cache hitted", imageUrl);
            return imageId;
        }
    }
}
