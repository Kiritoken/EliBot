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
import urllib.request
import json
import flask


def crawl(url):
    headers = {
        "User-Agent": "Mozilla / 5.0(Windows NT 10.0;Win64;x64) AppleWebKit / 537.36(KHTML, likeGecko) Chrome / "
                      "87.0.4280.141Safari / 537.36 "
    }
    data = bytes()
    req = urllib.request.Request(url, data, headers, method="GET")
    document = urllib.request.urlopen(req, timeout=3).read().decode("utf-8")
    # 解析html
    html = BeautifulSoup(document, "html.parser")
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
        for (key, value) in data.items():
            res['data'] = value
        return json.dumps(res, ensure_ascii=False)
    except Exception as e:
        print(e, "爬取异常")
        res = {'msg': '获取基金信息失败', 'code': 500}
        return json.dumps(res, ensure_ascii=False)


if __name__ == "__main__":
    server.run(port=8081)
