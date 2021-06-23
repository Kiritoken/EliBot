import abc
import json
import time
import threading
from selenium.webdriver.support.wait import WebDriverWait
from selenium.webdriver.common.by import By
from selenium.webdriver.support import expected_conditions as EC
from crawler_driver import CrawlerDriver, StaticCrawlerDriver, DynamicCrawlerDriver


class Crawler(metaclass=abc.ABCMeta):
    def __init__(self, query, is_staic_web):
        self._query = query
        self._driver = StaticCrawlerDriver() if is_staic_web else DynamicCrawlerDriver()

    def crawl_info(self):
        try:
            self._get_url()
            self._data = self._get_data()
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
    def __init__(self, query):
        super().__init__(query, is_staic_web=False)

    def _get_url(self):
        self.__url = "http://fund.eastmoney.com/" + self._query + ".html"

    def _get_data(self):
        guard = self._driver.get(self.__url)  # pylint: disable=unused-variable
        document = self._driver.document

        fund_name = document.find_element_by_class_name(
            'fundDetail-tit').find_element_by_tag_name('div').text.split('(')[0]
        fund_code = document.find_element_by_class_name('ui-num').text
        data_of_fund = document.find_element_by_class_name('dataItem02')
        net_asset_value = data_of_fund.find_elements_by_class_name(
            "ui-num")[0].text
        a_d = data_of_fund.find_elements_by_class_name("ui-num")[1].text
        t_day = data_of_fund.find_element_by_tag_name('p').text[6:16]
        update_time = time.strftime("%Y-%m-%d %H:%M:%S", time.localtime())
        # 持仓信息
        shares = []
        holdings = document.find_element_by_id(
            "position_shares").find_elements_by_tag_name("tr")
        for i in range(1, len(holdings)):
            el = holdings[i].find_elements_by_tag_name("td")
            shares.append(
                {"name": el[0].text, "shares": el[1].text, "a_d": el[2].text})
        document.execute_script(
            "document.getElementById('position_bonds').style.display = 'block'")
        holdings = document.find_element_by_id(
            "position_bonds").find_elements_by_tag_name("tr")
        for i in range(1, len(holdings)):
            el = holdings[i].find_elements_by_tag_name("td")
            if len(el) == 1:
                break
            shares.append(
                {"name": el[0].text, "shares": el[1].text, "a_d": "--"})

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
    def __init__(self, query):
        super().__init__(query, is_staic_web=False)

    def _get_url(self):
        fund_codes = self._query.split(' ')
        self.__urls = []
        for code in fund_codes:
            self.__urls.append("http://fund.eastmoney.com/" + code + ".html")

    def _get_datum_from_url(self, url, data):
        guard = self._driver.detach_get(url)
        document = guard.document
        document.add_cookie({
            'domain': '.eastmoney.com',
            'path': '/',
            'name': 'AUTH_FUND.EASTMONEY.COM_GSJZ',
            'value': 'AUTH*TTJJ*TOKEN'})
        document.refresh()

        wait = WebDriverWait(document, 10)  # 显性等待

        # 基金名称
        fund_name = document.find_element_by_class_name(
            'fundDetail-tit').find_element_by_tag_name('div').text.split('(')[0]
        # 基金代码
        fund_code = document.find_element_by_class_name('ui-num').text
        # 净值估算
        net_asset_value = document.find_element_by_id('gz_gsz')
        wait.until_not(EC.text_to_be_present_in_element(
            (By.ID, 'gz_gsz'), '--'))
        net_asset_value_text = net_asset_value.text
        # 涨跌幅
        a_d = document.find_element_by_id('gz_gszzl').text
        # 涨跌额
        value_a_d = document.find_element_by_id('gz_gszze').text
        # 估算时间
        update_time = document.find_element_by_id('gz_gztime').text
        # 记录爬取的数据

        data.append({
            "fund_name": fund_name,
            "fund_code": fund_code,
            "net_asset_value": net_asset_value_text,
            "a_d": a_d,
            "value_a_d": value_a_d,
            "update_time": update_time,
        })

    def _get_data(self):
        data = []
        pool = []
        for url in self.__urls:
            t = threading.Thread(
                target=self._get_datum_from_url, args=(url, data))
            pool.append(t)
            t.start()
        for t in pool:
            t.join()
        return data


class FundIndexCrawler(Crawler):
    def __init__(self, query):
        super().__init__(query, False)

    def _get_url(self):
        self.__url = "http://quote.eastmoney.com/zs" + self._query + ".html"

    def _get_data(self):
        guard = self._driver.get(self.__url) # pylint: disable=unused-variable
        document = self._driver.document

        index_name = document.find_element_by_id('name').text
        index_code = document.find_element_by_id('code').text
        a_d = document.find_element_by_id('km2').text
        price = document.find_element_by_id('price9').text
        price_a_d = document.find_element_by_id('km1').text
        date = document.find_element_by_id('hqday').text.strip('()')
        update_time = time.strftime("%Y-%m-%d %H:%M:%S", time.localtime())

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


# todo
# NBA crawler