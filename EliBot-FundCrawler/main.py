# coding=utf-8
"""
基金数据爬虫
@author Eli
@date 2021/06/17
@e-mail 357449971@qq.com
"""

import flask
import logging

import crawler


def create_logger():
    log = logging.getLogger('crawler')
    if not log.handlers:
        log_handler = logging.StreamHandler()
        # log_handler.setLevel(logging.DEBUG)
        log_handler.setFormatter(logging.Formatter(
            '[%(asctime)s] %(levelname)s in %(module)s: %(message)s'))
        logging.basicConfig(level=logging.INFO, handlers=[log_handler])
        log.removeHandler(log_handler)
        log.addHandler(log_handler)
    return log


''' 
test case
getFundInfo: http://localhost:8081/getFundInfo?fundCode=001102
getFundsInfoList: http://localhost:8081/getFundsInfoList?fundCodes=005001%20161818%20000913%20008208%20161723%20006098
getIndexInfo: http://localhost:8081/getIndexInfo?indexCode=399006
getNBAInfo: http://localhost:8081/getNBAInfo
'''


def main():
    create_logger()

    server = flask.Flask(__name__)

    @server.route('/getFundInfo', methods=['get', 'post'])
    def get_fund_info():  # pylint: disable=unused-variable
        logging.info('GET %s' % flask.request.full_path)
        query = flask.request.values.get('fundCode')
        return crawler.FundInfoCrawler(query).crawl_info()

    @server.route('/getFundsInfoList', methods=['get', 'post'])
    def get_funds_info_list():  # pylint: disable=unused-variable
        logging.info('GET %s' % flask.request.full_path)
        query = flask.request.values.get('fundCodes')
        return crawler.FundsInfosCrawler(query).crawl_info()

    @server.route('/getIndexInfo', methods=['get', 'post'])
    def get_fund_index_info():  # pylint: disable=unused-variable
        logging.info('GET %s' % flask.request.full_path)
        query = flask.request.values.get('indexCode')
        return crawler.FundIndexCrawler(query).crawl_info()

    server.run(port=8081)


if __name__ == '__main__':
    main()
