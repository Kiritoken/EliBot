package com.eli.bot.entity.fund;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * 持仓占比
 *
 * @author Eli
 */
@Data
public class PositionSharesItem{

	@JSONField(name="stock_shares")
	private String stockShares;

	@JSONField(name="stock_name")
	private String stockName;

	@JSONField(name="stock_a_d")
	private String stockAD;
}