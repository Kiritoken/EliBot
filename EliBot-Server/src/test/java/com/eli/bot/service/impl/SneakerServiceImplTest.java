package com.eli.bot.service.impl;

import com.eli.bot.entity.snkrs.Product;
import com.eli.bot.entity.snkrs.Sneaker;
import com.eli.bot.service.ISneakerService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
public class SneakerServiceImplTest {

    @Autowired
    private ISneakerService sneakerService;

    @Test
    public void test() throws ParseException {
        List<Sneaker> latestSneakerInfo = sneakerService.findLatestSneakerInfo();
        List<Sneaker> filter = latestSneakerInfo.stream().filter(sneaker -> {
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
                return !StringUtils.isEmpty(sneaker.getTitle()) && (DateUtils.isSameDay(startSellDate, nowDay) || startSellDate.getTime() > nowDay.getTime());
            } catch (ParseException e) {
                log.error("", e);
                throw new RuntimeException("球鞋信息查询时间解析错误");
            }
        }).collect(Collectors.toList());
        log.info("{}", filter);
    }

}