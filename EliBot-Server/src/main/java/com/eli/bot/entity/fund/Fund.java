package com.eli.bot.entity.fund;

import java.util.List;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * 基金信息
 *
 * @author Eli
 */
@Data
public class Fund {

    /**
     * 更新时间
     */
    @JSONField(name = "update_time")
    private String updateTime;

    /**
     * 日涨跌幅
     */
    @JSONField(name = "a_d")
    private String aD;

    /**
     * 净值
     */
    @JSONField(name = "net_asset_value")
    private String netAssetValue;

    /**
     * 基金名称
     */
    @JSONField(name = "fund_name")
    private String fundName;

    /**
     * 基金代码
     */
    @JSONField(name = "fund_code")
    private String fundCode;

    /**
     * 交易日
     */
    @JSONField(name = "t_day")
    private String tDay;

    /**
     * 持仓占比
     */
    @JSONField(name = "position_shares")
    private List<PositionSharesItem> positionShares;
}