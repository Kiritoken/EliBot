package com.eli.bot.service.impl;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.eli.bot.entity.fund.Fund;
import com.eli.bot.entity.fundHoldings.LeptFund;
import com.eli.bot.service.IFundService;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

/**
 * @author Eli
 */
@Slf4j
@Service
public class FundServiceImpl implements IFundService {

    private static final String CODE = "code";

    private static final int SUCCESS = 200;

    @Value("${fund.queryFundInfo}")
    private String queryFundInfoApi;

    @Value("${fund.queryFundsInfoList}")
    private String queryFundsInfoListApi;

    private final RestTemplate restTemplate;

    public FundServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public Optional<Fund> findFundInfo(@NotNull String fundCode) {
        if (StringUtils.isEmpty(fundCode)) {
            return Optional.empty();
        }
        // 调用基金查询接口
        ResponseEntity<String> entity = restTemplate.getForEntity(queryFundInfoApi + "?fundCode=" + fundCode, String.class);
        String body = entity.getBody();
        JSONObject res = JSON.parseObject(body);
        if (SUCCESS == res.getIntValue(CODE)) {
            Fund fund = res.getObject("data", Fund.class);
            log.info("查询基金{}成功,基金信息为{}", fundCode, JSON.toJSONString(fund));
            return Optional.ofNullable(fund);
        } else {
            log.error("查询基金{}信息失败", fundCode);
            return Optional.empty();
        }
    }

    @Override
    public Optional<List<LeptFund>> findFundsInfoList(@NotNull String fundCodes) {
        if (StringUtils.isEmpty(fundCodes)) {
            return Optional.empty();
        }
        // 调用基金查询接口
        ResponseEntity<String> entity = restTemplate.getForEntity(queryFundsInfoListApi + "?fundCodes=" + fundCodes, String.class);
        String body = entity.getBody();
        JSONObject res = JSON.parseObject(body);
        if (SUCCESS == res.getIntValue(CODE)) {
            JSONArray infoJSONArray = res.getJSONArray("data");
            List<LeptFund> infoList = JSONObject.parseArray(infoJSONArray.toJSONString(), LeptFund.class);
            for (LeptFund fund : infoList)
                log.info("查询基金{}成功,基金信息为{}", fund.getFundCode(), JSON.toJSONString(fund));
            return Optional.ofNullable(infoList);
        } else {
            log.error("查询基金{}信息失败", fundCodes);
            return Optional.empty();
        }
    }
}
