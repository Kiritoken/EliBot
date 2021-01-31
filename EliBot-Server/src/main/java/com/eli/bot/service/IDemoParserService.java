package com.eli.bot.service;

import java.util.List;

/**
 * DEMO 解析
 *
 * @author Eli
 */
public interface IDemoParserService {

    /**
     * 获取所有低光时刻tick
     *
     * @param demoPath  demo路径
     * @param steam64Id 64位steamId
     * @return ticks
     */
    List<Long> getLowlightTicks(String demoPath, String steam64Id);

}
