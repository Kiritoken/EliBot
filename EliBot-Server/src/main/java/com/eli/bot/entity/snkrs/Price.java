package com.eli.bot.entity.snkrs;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * 价格信息
 *
 * @author Eli
 */
@Data
public class Price {

    @JSONField(name = "fullRetailPrice")
    private int fullRetailPrice;

    @JSONField(name = "currentRetailPrice")
    private int currentRetailPrice;

    @JSONField(name = "msrp")
    private int msrp;

    @JSONField(name = "onSale")
    private boolean onSale;
}