package com.eli.bot.service;

import com.eli.bot.entity.fund.Fund;
import com.eli.bot.entity.fundHoldings.LeptFund;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

/**
 * 基金信息查询服务
 *
 * @author Eli
 * @date 2021/1/23
 */
public interface IFundService {

    /**
     * 根据基金代码查询制定基金信息
     *
     * @param fundCode 基金代码
     * @return Optional<Fund>
     */
    Optional<Fund> findFundInfo(@NotNull String fundCode);

    Optional<List<LeptFund>> findFundsInfoList(@NotNull String fundCode);

}
