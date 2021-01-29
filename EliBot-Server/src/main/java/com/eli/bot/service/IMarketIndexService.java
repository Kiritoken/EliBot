package com.eli.bot.service;

import com.eli.bot.entity.marketIndex.MarketIndex;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * 指数信息服务
 *
 * @author Eli
 * @date 2021/1/23
 */
public interface IMarketIndexService {

    /**
     * 根据指数代码查询指数信息
     *
     * @param indexCode 指数代码
     * @return Optional<MarketIndex>
     */
    Optional<MarketIndex> findIndexInfo(@NotNull String indexCode);

}
