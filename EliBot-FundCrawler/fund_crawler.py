# coding=utf-8
"""
基金数据爬虫
@author Eli
@date 2021/2/22
@e-mail 357449971@qq.com
"""

import pathlib
from bs4 import BeautifulSoup
import time
import re
import json
import flask
from selenium import webdriver
from selenium.webdriver import ChromeOptions

# 设置options参数，以开发者模式运行
option = ChromeOptions()
option.add_experimental_option("excludeSwitches", ["enable-automation"])

# 解决报错，设置无界面运行
option.add_argument('--no-sandbox')
option.add_argument('--disable-dev-shm-usage')
option.add_argument('window-size=1920x3000')  # 指定浏览器分辨率
option.add_argument('--disable-gpu')  # 谷歌文档提到需要加上这个属性来规避bug
option.add_argument('--hide-scrollbars')  # 隐藏滚动条, 应对一些特殊页面
option.add_argument('blink-settings=imagesEnabled=false')  # 不加载图片, 提升速度
option.add_argument('--headless')  # 浏览器不提供可视化页面. linux下如果系统不支持可视化不加这条会启动失败
# option.binary_location = r"C:\Program Files (x86)\Google\Chrome\Application\chrome.exe"  # 手动指定使用的浏览器位置


def crawl(url):
    driver = webdriver.Chrome(options=option)
    driver.implicitly_wait(10)  # 隐性等待，最长等30秒
    driver.get(url)
    # 解析html
    html = BeautifulSoup(driver.page_source, "html.parser")
    driver.close()
    fund_name = \
        html.find_all('div', class_="fundDetail-tit")[0].find_all('div', style="float: left")[0].text.split('(')[0]
    # 基金代码
    fund_code = html.find_all('span', class_="ui-num")[0].text
    # 单位净值
    net_asset_value = html.find_all('dd', class_="dataNums")[1].find_all('span')[0].text
    # 日涨跌幅
    a_d = html.find_all('dd', class_="dataNums")[1].find_all('span')[1].text
    # T日
    t_day_text = html.find_all('dl', class_="dataItem02")[0].find_all('p')[0].text
    t_day = re.findall(re.compile(r'[(](.*?)[)]', re.S), t_day_text)[0]
    # 更新时间
    update_time = time.strftime("%Y-%m-%d %H:%M:%S", time.localtime())
    # 持仓信息
    position_shares = []
    position_shares_html = html.find_all('li', id="position_shares")[0].find_all('tr')
    for i in range(1, len(position_shares_html)):
        stock = position_shares_html[i].find_all('td')
        position_shares.append(
            {"stock_name": stock[0].text, "stock_shares": stock[1].text, "stock_a_d": stock[2].text})
    # 记录爬取的数据
    crawl_data = {t_day: {
        "fund_name": fund_name,
        "fund_code": fund_code,
        "net_asset_value": net_asset_value,
        "a_d": a_d,
        "t_day": t_day,
        "update_time": update_time,
        "position_shares": position_shares
    }}
    return crawl_data


def save(crawl_data, fund_code):
    file_path = "./fund_data/" + fund_code + ".json"
    file = pathlib.Path(file_path)
    if file.exists():
        with open(file_path, 'r', encoding='utf-8') as file_reader:
            his_data = json.load(file_reader)
        with open(file_path, "w", encoding='utf-8') as file_writer:
            for (key, value) in crawl_data.items():
                his_data[key] = value
            json.dump(crawl_data, file_writer, ensure_ascii=False)
    else:
        with open(file_path, "w", encoding='utf-8') as file_writer:
            json.dump(crawl_data, file_writer, ensure_ascii=False)
    pass

def crawlIndex(url):
    driver = webdriver.Chrome(options=option)
    driver.implicitly_wait(10)  # 隐性等待，最长等30秒
    driver.get(url)
    # 解析html
    html = BeautifulSoup(driver.page_source, "html.parser")
    driver.close()
    # <h2 class="header-title-h2 fl" id="name">上证指数</h2>
    index_name = \
        html.find_all('h2', class_="header-title-h2 fl", id="name")[0].text
    # 指数代码
    # <b class="header-title-c fl" id="code">000001</b>
    index_code = html.find_all('b', class_="header-title-c fl", id="code")[0].text
    # 涨跌幅
    # <b id="km2" class="xp4 green" style="">-0.63%</b>
    a_d = html.find_all('b', class_="xp4 green", id="km2")[0].text
    # 价格
    # <strong id="price9" class="xp1 green" style="">3483.07</strong>
    price = html.find_all('strong', class_="xp1 green", id="price9")[0].text
    # 涨跌额
    # <b id="km1" class="xp3 green" style="">-22.11</b>
    price_a_d = html.find_all('b', class_="xp3 green", id="km1")[0].text
    # 日期
    # <span id="hqday" class="hqday">（2021-01-29 星期五 15:39:50）</span>
    date = html.find_all('span', class_="hqday", id='hqday')[0].text.strip('()')
    # 更新时间
    update_time = time.strftime("%Y-%m-%d %H:%M:%S", time.localtime())
    # 记录爬取的数据
    crawl_data = {date: {
        "index_name": index_name,
        "index_code": index_code,
        "a_d": a_d,
        "price": price,
        "price_a_d": price_a_d,
        "update_time": update_time,
    }}
    return crawl_data


def saveIndex(crawl_data, index_code):
    file_path = "./index_data/" + index_code + ".json"
    with open(file_path, "w", encoding='utf-8') as file_writer:
        json.dump(crawl_data, file_writer, ensure_ascii=False)


server = flask.Flask(__name__)


@server.route('/getFundInfo', methods=['get', 'post'])
def get_fund_info():
    try:
        # 基金代码
        fund_code = flask.request.values.get('fundCode')
        fund_url = "http://fund.eastmoney.com/" + fund_code + ".html"
        data = crawl(fund_url)
        save(data, fund_code)
        res = {'msg': '获取基金信息成功', 'code': 200}
        for (_, value) in data.items():
            res['data'] = value
        return json.dumps(res, ensure_ascii=False)
    except Exception as e:
        print(e, "爬取异常")
        res = {'msg': '获取基金信息失败', 'code': 500}
        return json.dumps(res, ensure_ascii=False)


@server.route('/getIndexInfo', methods=['get', 'post'])
def get_index_info():
    try:
        # 指数代码
        index_code = flask.request.values.get('indexCode')
        index_url = "http://quote.eastmoney.com/zs" + index_code + ".html"
        data = crawlIndex(index_url)
        saveIndex(data, index_code)
        res = {'msg': '获取指数信息成功', 'code': 200}
        for (_, value) in data.items():
            res['data'] = value
        return json.dumps(res, ensure_ascii=False)
    except Exception as e:
        print(e, "爬取异常")
        res = {'msg': '获取指数信息失败', 'code': 500}
        return json.dumps(res, ensure_ascii=False)


if __name__ == "__main__":
    server.run(port=8081)
