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

	@JSONField(name="shares")
	private String shares;

	@JSONField(name="name")
	private String name;

	@JSONField(name="a_d")
	private String aD;
}