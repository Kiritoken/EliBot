package com.eli.bot.entity.fundHoldings;

import lombok.Data;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 持仓信息
 *
 * @author Eli
 */
@Data
public class FundHoldings {

    /**
     * 用户
     */
    private List<String> users;

    /**
     * 用户持仓
     */
    private Map<String, List<LeptFund>> holdings;

    public FundHoldings() {
        users = new LinkedList<>();
        holdings = new HashMap<>();
    }
}