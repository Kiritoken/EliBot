package com.eli.bot.service;

import com.eli.bot.entity.snkrs.Sneaker;

import java.util.List;


/**
 * 球鞋信息服务
 *
 * @author Eli
 */
public interface ISneakerService {

    /**
     * 返回snkrs上最新的50条商品数据
     *
     * @return List<Sneaker>
     */
    List<Sneaker> findLatestSneakerInfo();

}
