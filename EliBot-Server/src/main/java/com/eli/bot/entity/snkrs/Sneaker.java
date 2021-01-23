package com.eli.bot.entity.snkrs;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * 球鞋信息
 *
 * @author Eli
 */
@Data
public class Sneaker {

    @JSONField(name = "seoSlug")
    private String seoSlug;

    @JSONField(name = "product")
    private Product product;

    @JSONField(name = "seoDescription")
    private String seoDescription;

    @JSONField(name = "title")
    private String title;

    @JSONField(name = "seoTitle")
    private String seoTitle;

    @JSONField(name = "tags")
    private Object tags;

    @JSONField(name = "threadId")
    private String threadId;

    @JSONField(name = "squareImageUrl")
    private String squareImageUrl;

    @JSONField(name = "feed")
    private String feed;

    @JSONField(name = "lastUpdatedDate")
    private String lastUpdatedDate;

    @JSONField(name = "restricted")
    private boolean restricted;

    @JSONField(name = "portraitImageUrl")
    private String portraitImageUrl;

    @JSONField(name = "interestId")
    private String interestId;

    @JSONField(name = "subtitle")
    private String subtitle;

    @JSONField(name = "imageUrl")
    private String imageUrl;

    @JSONField(name = "name")
    private String name;

    @JSONField(name = "lastUpdatedTime")
    private String lastUpdatedTime;

    @JSONField(name = "id")
    private String id;

    @JSONField(name = "publishedDate")
    private String publishedDate;

    @JSONField(name = "expirationDate")
    private String expirationDate;
}