package com.eli.bot.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.jetbrains.annotations.NotNull;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 缓存
 *
 * @author Eli
 * @date 2021/1/23
 */
public class CacheHolder {

    private static final Map<String, Cache<String, Object>> CACHE_MAP = new ConcurrentHashMap<>();

    private static final String DEFAULT = "default";

    public static Cache<String, Object> getCache(String key) {
        if (StringUtils.isEmpty(key)) {
            // 获取默认缓存
            if (null == CACHE_MAP.get(DEFAULT)) {
                Cache<String, Object> cache = CacheBuilder.newBuilder().maximumSize(1000)
                        .expireAfterWrite(100, TimeUnit.MINUTES)
                        .build();
                CACHE_MAP.put(DEFAULT, cache);
            }
            return CACHE_MAP.get(DEFAULT);
        }
        if (null == CACHE_MAP.get(key)) {
            Cache<String, Object> cache = CacheBuilder.newBuilder().maximumSize(1000)
                    .expireAfterWrite(100, TimeUnit.MINUTES)
                    .build();
            CACHE_MAP.put(key, cache);
        }
        return CACHE_MAP.get(key);
    }


}
