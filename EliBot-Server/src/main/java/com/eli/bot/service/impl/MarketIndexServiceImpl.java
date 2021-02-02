package com.eli.bot.service.impl;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.eli.bot.entity.marketIndex.MarketIndex;
import com.eli.bot.service.IMarketIndexService;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

/**
 * @author Eli
 */
@Slf4j
@Service
public class MarketIndexServiceImpl implements IMarketIndexService {

    private static final String CODE = "code";

    private static final int SUCCESS = 200;

    @Value("${index.queryIndexInfo}")
    private String queryIndexInfoApi;

    private final RestTemplate restTemplate;

    public MarketIndexServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public Optional<MarketIndex> findIndexInfo(@NotNull String indexCode) {
        if (StringUtils.isEmpty(indexCode)) {
            return Optional.empty();
        }
        // 调用指数查询接口
        ResponseEntity<String> entity = restTemplate.getForEntity(queryIndexInfoApi + "?indexCode=" + indexCode, String.class);
        String body = entity.getBody();
        JSONObject res = JSON.parseObject(body);
        if (SUCCESS == res.getIntValue(CODE)) {
            MarketIndex index = res.getObject("data", MarketIndex.class);
            log.info("查询大盘指数{}成功,指数信息为{}", indexCode, JSON.toJSONString(index));
            return Optional.ofNullable(index);
        } else {
            log.error("查询指数{}信息失败", indexCode);
            return Optional.empty();
        }
    }
}
