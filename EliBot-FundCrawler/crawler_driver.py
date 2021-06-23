from abc import ABCMeta, abstractmethod
import urllib.request
from bs4 import BeautifulSoup
from webdriver_pool import web_driver_pool


class CrawlerDriver(metaclass=ABCMeta):
    def __init__(self):
        pass


class StaticCrawlerDriver(CrawlerDriver):
    def __init__(self):
        super().__init__()

    def get(self, url):
        req = urllib.request.Request(url, method="GET")
        document = urllib.request.urlopen(
            req, timeout=3).read().decode("utf-8")
        self.__html = BeautifulSoup(document, "html.parser")
        return WebDriverGuard()

    @property
    def document(self):
        return self.__html


class DynamicCrawlerDriver(CrawlerDriver):
    def __init__(self):
        super().__init__()

    def get(self, url):
        self.__driver = web_driver_pool.get()
        self.__driver.get(url)
        return WebDriverGuard(self.__driver)

    def detach_get(self, url):
        driver = web_driver_pool.get()
        driver.get(url)
        return WebDriverGuard(driver)

    @property
    def document(self):
        return self.__driver.web_driver


class WebDriverGuard(object):
    def __init__(self, driver=None):
        self.__driver = driver

    @property
    def document(self):
        return self.__driver.web_driver

    def __del__(self):
        if not self.__driver == None:
            web_driver_pool.put(self.__driver)
