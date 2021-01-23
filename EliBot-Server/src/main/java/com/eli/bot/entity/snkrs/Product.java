package com.eli.bot.entity.snkrs;

import java.util.List;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * 商品信息
 *
 * @author Eli
 */
@Data
public class Product {

    @JSONField(name = "available")
    private boolean available;

    @JSONField(name = "stopSellDate")
    private String stopSellDate;

    @JSONField(name = "genders")
    private List<String> genders;

    @JSONField(name = "selectionEngine")
    private String selectionEngine;

    @JSONField(name = "title")
    private String title;

    @JSONField(name = "startSellDate")
    private String startSellDate;

    @JSONField(name = "merchStatus")
    private String merchStatus;

    @JSONField(name = "publishType")
    private String publishType;

    @JSONField(name = "price")
    private Price price;

    @JSONField(name = "interestId")
    private String interestId;

    @JSONField(name = "globalPid")
    private String globalPid;

    @JSONField(name = "subtitle")
    private String subtitle;

    @JSONField(name = "imageUrl")
    private String imageUrl;

    @JSONField(name = "quantityLimit")
    private int quantityLimit;

    @JSONField(name = "style")
    private String style;

    @JSONField(name = "colorCode")
    private String colorCode;

    @JSONField(name = "id")
    private String id;

    @JSONField(name = "colorDescription")
    private String colorDescription;

    @JSONField(name = "productType")
    private String productType;

    @JSONField(name = "upcoming")
    private boolean upcoming;
}