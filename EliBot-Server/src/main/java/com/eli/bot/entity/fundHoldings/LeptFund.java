package com.eli.bot.entity.fundHoldings;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * 持仓信息
 *
 * @author Eli
 */
@Data
public class LeptFund {

    /**
     * 基金代码
     */
    @JSONField(name = "fund_code")
    private String fundCode;

    /**
     * 基金名称
     */
    @JSONField(name = "fund_name")
    private String fundName;

    /**
     * 用户持仓
     */
    private double amounts;

    /**
     * 净值
     */
    @JSONField(name = "net_asset_value")
    private String netAssetValue;

    /**
     * 日涨跌幅
     */
    @JSONField(name = "a_d")
    private  String aD;

    /**
     * 日涨跌幅
     */
    @JSONField(name = "value_a_d")
    private  String valueAD;

    /**
     * 估值更新时间
     */
    @JSONField(name = "update_time")
    private String update_time;
}