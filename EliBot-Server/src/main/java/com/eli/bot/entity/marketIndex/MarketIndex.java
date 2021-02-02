package com.eli.bot.entity.marketIndex;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * 指数信息
 *
 * @author Eli
 */
@Data
public class MarketIndex {

    /**
     * 更新时间
     */
    @JSONField(name = "update_time")
    private String updateTime;

    /**
     * 涨跌幅
     */
    @JSONField(name = "a_d")
    private String aD;

    /**
     * 价格
     */
    @JSONField(name = "price")
    private String price;

    /**
     * 涨跌额
     */
    @JSONField(name = "price_a_d")
    private String priceAD;

    /**
     * 指数名称
     */
    @JSONField(name = "index_name")
    private String indexName;

    /**
     * 指数代码
     */
    @JSONField(name = "index_code")
    private String indexCode;
}