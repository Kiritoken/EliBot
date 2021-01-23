package com.eli.bot.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.eli.bot.core.SystemClock;
import com.eli.bot.entity.snkrs.Sneaker;
import com.eli.bot.service.ISneakerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * @author Eli
 */
@Slf4j
@Service
public class SneakerServiceImpl implements ISneakerService {

    @Value("${snkrs.querySneakerInfo}")
    private String querySneakerInfoApi;

    private final RestTemplate restTemplate;

    public SneakerServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public List<Sneaker> findLatestSneakerInfo() {
        log.info("开始查询snkrs最新发布的球鞋信息...");
        long startTime = SystemClock.now();
        ResponseEntity<String> responseEntity = restTemplate.getForEntity(querySneakerInfoApi, String.class);
        String body = responseEntity.getBody();
        JSONObject jsonObject = JSON.parseObject(body);
        JSONArray threads = jsonObject.getJSONArray("threads");
        log.info("查询snkrs最新发布的球鞋信息结束，耗时{}ms", SystemClock.now() - startTime);
        return threads.toJavaList(Sneaker.class);
    }
}
