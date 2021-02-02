package com.eli.bot.service;

import com.eli.bot.entity.fundHoldings.FundHoldings;
import com.eli.bot.entity.fundHoldings.LeptFund;
import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.List;


/**
 * #编辑持仓 基金代码 使用空格分隔多个基金代码
 * 编辑自选基金持仓信息，总是覆盖已有配置
 *
 * @author Eli
 */
@Slf4j
public class FundHoldingsEditorService {

    private volatile static FundHoldingsEditorService instance = null;

    private FundHoldings holdings;

    private TypeDescription typeDescription;

    private FundHoldingsEditorService() {
        try {
            typeDescription = new TypeDescription(LeptFund.class);
            typeDescription.setIncludes("fundCode", "fundName", "amounts");
            Yaml yml = new Yaml();
            yml.addTypeDescription(typeDescription);
            holdings = yml.loadAs(new FileInputStream("./src/main/resources/holdings.yml"),
                    FundHoldings.class);
        } catch (Exception e) {
            log.error("用户持仓信息YML加载异常");
        }
    }

    public static FundHoldingsEditorService getInstance() {
        if (instance == null) {
            synchronized (FundHoldingsEditorService.class) {
                if (instance == null) {
                    instance = new FundHoldingsEditorService();
                }
            }
        }
        return instance;
    }

    public List<LeptFund> getUserHoldings(String uid) {
        if (instance == null || holdings == null) return null;
        return holdings.getHoldings().get(uid);
    }

    public void setUserHoldings(String uid, List<LeptFund> fundList) {
        if (instance == null || holdings == null) return;
        holdings.getHoldings().put(uid, fundList);
    }

    public List<String> getUsers() {
        if (instance == null || holdings == null) return null;
        return holdings.getUsers();
    }

    public synchronized void save() {
        try {
            log.info("存储YML");
            Yaml yml = new Yaml();
            yml.addTypeDescription(typeDescription);
            yml.dump(holdings, new FileWriter("./src/main/resources/holdings.yml"));
        } catch (Exception e) {
            log.error("用户持仓信息YML保存异常");
        }
    }
}
