# coding=utf-8
"""
基金数据爬虫
@author Eli
@date 2021/2/22
@e-mail 357449971@qq.com
"""

import abc
import flask
import json
import time
from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.support.wait import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver import ChromeOptions


class Crawler(metaclass=abc.ABCMeta):
    def __init__(self, query, driver):
        self._query = query
        self._driver = driver

    def carwl_info(self):
        try:
            self._url = self._get_url()
            self._data = self._get_data()
            self._driver.get('about:blank')
            response = {'msg': '获取信息成功', 'code': 200, 'data': self._data}
            return json.dumps(response, ensure_ascii=False)
        except Exception as e:
            print(e, "爬取异常[%s]" % self.__class__.__name__)
            response = {'msg': '获取信息失败', 'code': 500}
            return json.dumps(response, ensure_ascii=False)

    @abc.abstractmethod
    def _get_url(self):
        pass

    @abc.abstractmethod
    def _get_data(self):
        pass


class FundInfoCrawler(Crawler):
    def __init__(self, query, driver):
        super().__init__(query, driver)

    def _get_url(self):
        self.__url = "http://fund.eastmoney.com/" + self._query + ".html"

    def _get_data(self):
        self._driver.get(self.__url)
        # 基金名称
        fund_name = self._driver.find_element_by_class_name(
            'fundDetail-tit').find_element_by_tag_name('div').text.split('(')[0]
        # 基金代码
        fund_code = self._driver.find_element_by_class_name('ui-num').text
        # 单位净值 & 日涨跌幅
        data_of_fund = self._driver.find_element_by_class_name('dataItem02')
        net_asset_value = data_of_fund.find_elements_by_class_name(
            "ui-num")[0].text
        a_d = data_of_fund.find_elements_by_class_name("ui-num")[1].text
        # T日
        t_day = data_of_fund.find_element_by_tag_name('p').text[6:16]
        # 更新时间
        update_time = time.strftime("%Y-%m-%d %H:%M:%S", time.localtime())
        # 持仓信息
        shares = []
        # position_shares
        holdings = self._driver.find_element_by_id(
            "position_shares").find_elements_by_tag_name("tr")
        for i in range(1, len(holdings)):
            el = holdings[i].find_elements_by_tag_name("td")
            shares.append(
                {"name": el[0].text, "shares": el[1].text, "a_d": el[2].text})
        # position_bonds
        # 债券持仓
        self._driver.execute_script(
            "document.getElementById('position_bonds').style.display = 'block'")
        holdings = self._driver.find_element_by_id(
            "position_bonds").find_elements_by_tag_name("tr")
        for i in range(1, len(holdings)):
            el = holdings[i].find_elements_by_tag_name("td")
            if len(el) == 1:
                break
            shares.append(
                {"name": el[0].text, "shares": el[1].text, "a_d": "--"})
        # 记录爬取的数据
        data = {
            "fund_name": fund_name,
            "fund_code": fund_code,
            "net_asset_value": net_asset_value,
            "a_d": a_d,
            "t_day": t_day,
            "update_time": update_time,
            "shares": shares
        }
        return data


class FundsInfosCrawler(Crawler):
    def __init__(self, query, driver):
        super().__init__(query, driver)

    def _get_url(self):
        fund_codes = self._query.split(' ')
        self.__urls = []
        for code in fund_codes:
            self.__urls.append("http://fund.eastmoney.com/" + code + ".html")

    def _get_data(self):
        data = []
        for url in self.__urls:
            self._driver.get(url)
            self._driver.add_cookie({
                'domain': '.eastmoney.com',
                'path': '/',
                'name': 'AUTH_FUND.EASTMONEY.COM_GSJZ',
                'value': 'AUTH*TTJJ*TOKEN'})
            self._driver.refresh()
            wait = WebDriverWait(self._driver, 10)  # 显性等待
            # 基金名称
            fund_name = self._driver.find_element_by_class_name(
                'fundDetail-tit').find_element_by_tag_name('div').text.split('(')[0]
            # 基金代码
            fund_code = self._driver.find_element_by_class_name('ui-num').text
            # 净值估算
            net_asset_value = self._driver.find_element_by_id('gz_gsz')
            wait.until_not(EC.text_to_be_present_in_element(
                (By.ID, 'gz_gsz'), '--'))
            net_asset_value_text = net_asset_value.text
            # 涨跌幅
            a_d = self._driver.find_element_by_id('gz_gszzl').text
            # 涨跌额
            value_a_d = self._driver.find_element_by_id('gz_gszze').text
            # 估算时间
            update_time = self._driver.find_element_by_id('gz_gztime').text
            # 记录爬取的数据
            data.append({
                "fund_name": fund_name,
                "fund_code": fund_code,
                "net_asset_value": net_asset_value_text,
                "a_d": a_d,
                "value_a_d": value_a_d,
                "update_time": update_time,
            })
        return data


class IndexCrawler(Crawler):
    def __init__(self, query, driver):
        super().__init__(query, driver)

    def _get_url(self):
        self.__url = "http://quote.eastmoney.com/zs" + self._query + ".html"

    def _get_data(self):
        self._driver.get(self.__url)
        # 指数名称
        index_name = self._driver.find_element_by_id('name').text
        # 指数代码
        index_code = self._driver.find_element_by_id('code').text
        # 涨跌幅
        a_d = self._driver.find_element_by_id('km2').text
        # 价格
        price = self._driver.find_element_by_id('price9').text
        # 涨跌额
        price_a_d = self._driver.find_element_by_id('km1').text
        # 日期
        date = self._driver.find_element_by_id('hqday').text.strip('()')
        # 更新时间
        update_time = time.strftime("%Y-%m-%d %H:%M:%S", time.localtime())
        # 记录爬取的数据
        data = {
            "index_name": index_name,
            "index_code": index_code,
            "a_d": a_d,
            "price": price,
            "price_a_d": price_a_d,
            "update_time": update_time,
            "date": date  # 待解析
        }
        return data


def main():
    # 配置webdriver
    option = ChromeOptions()
    # 以开发者模式运行
    option.add_experimental_option("excludeSwitches", ["enable-automation"])
    # 解决报错，设置无界面运行
    option.add_argument('--no-sandbox')
    option.add_argument('--disable-dev-shm-usage')
    option.add_argument('window-size=1920x3000')  # 指定浏览器分辨率
    option.add_argument('--disable-gpu')  # 谷歌文档提到需要加上这个属性来规避bug
    option.add_argument('--hide-scrollbars')  # 隐藏滚动条, 应对一些特殊页面
    option.add_argument('blink-settings=imagesEnabled=false')  # 不加载图片, 提升速度
    option.add_argument('--headless')  # 浏览器不提供可视化页面. linux下如果系统不支持可视化不加这条会启动失败
    # 手动指定使用的浏览器位置
    option.binary_location = r"C:\Program Files (x86)\Google\Chrome\Application\chrome.exe"
    # chrome driver
    driver = webdriver.Chrome(options=option)
    driver.implicitly_wait(10)  # 隐性等待，最长等30秒

    # 配置flask
    server = flask.Flask(__name__)

    @server.route('/getFundInfo', methods=['get', 'post'])
    def get_fund_info():  # pylint: disable=unused-variable
        query = flask.request.values.get('fundCode')
        return FundInfoCrawler(query, driver).carwl_info()

    @server.route('/getFundsInfoList', methods=['get', 'post'])
    def get_funds_info_list():  # pylint: disable=unused-variable
        query = flask.request.values.get('fundCodes')
        return FundsInfosCrawler(query, driver).carwl_info()

    @server.route('/getIndexInfo', methods=['get', 'post'])
    def get_index_info():  # pylint: disable=unused-variable
        query = flask.request.values.get('indexCode')
        return IndexCrawler(query, driver).carwl_info()

    server.run(port=8081)

    driver.close()


if __name__ == "__main__":
    main()
