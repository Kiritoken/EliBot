package com.eli.bot.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.eli.bot.service.IDemoParserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Eli
 */
@Slf4j
@Service
public class DemoParserServiceImpl implements IDemoParserService {

    @Value("${demo.getLowlightTick}")
    private String demoParserUrl;


    private final RestTemplate restTemplate;

    public DemoParserServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }


    @Override
    public List<Long> getLowlightTicks(String demoPath, String steam64Id) {
        // 构造请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        Map<String, Object> paramMap = new HashMap<>(2);
        paramMap.put("demoPath", demoPath);
        paramMap.put("steam64Id", steam64Id);
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(paramMap, headers);
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(demoParserUrl, requestEntity, String.class);
        JSONObject jsonObject = JSONObject.parseObject(responseEntity.getBody());
        if (200 == jsonObject.getInteger("code")) {
            JSONArray ticks = jsonObject.getJSONArray("ticks");
            return ticks.toJavaList(Long.class);
        } else {
            throw new RuntimeException("文件解析异常");
        }
    }
}
