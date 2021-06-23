import config
from queue import Queue
from selenium import webdriver
from selenium.webdriver import ChromeOptions
from selenium.webdriver.common.by import By
from selenium.webdriver.support.wait import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC


class WebDriverPool(object):
    def __init__(self, size=1):
        self.__pool = Queue(size)
        for _ in range(size):
            self.__pool.put(WebDriver())

    def get(self):
        return self.__pool.get(True)

    def put(self, driver):
        driver.get('about:blank')
        self.__pool.put(driver)


class WebDriver(object):
    def __init__(self):
        # 配置webdriver
        self.__option = ChromeOptions()
        # 以开发者模式运行
        self.__option.add_experimental_option(
            "excludeSwitches", ["enable-automation"])
        # 解决报错，设置无界面运行
        self.__option.add_argument('--no-sandbox')
        self.__option.add_argument('--disable-dev-shm-usage')
        self.__option.add_argument('window-size=1920x3000')  # 指定浏览器分辨率
        self.__option.add_argument('--disable-gpu')  # 谷歌文档提到需要加上这个属性来规避bug
        self.__option.add_argument('--hide-scrollbars')  # 隐藏滚动条, 应对一些特殊页面
        self.__option.add_argument(
            'blink-settings=imagesEnabled=false')  # 不加载图片, 提升速度
        # 浏览器不提供可视化页面. linux下如果系统不支持可视化不加这条会启动失败
        if not config.VisibleWebForDebug:
            self.__option.add_argument('--headless')
        # 手动指定使用的浏览器位置
        self.__option.binary_location = config.ChromeLocation
        # chrome driver
        self.__driver = webdriver.Chrome(options=self.__option)
        self.__driver.implicitly_wait(10)  # 隐性等待，最长等30秒

    def __del__(self):
        self.__driver.close()

    def get(self, url):
        self.__driver.get(url)

    @property
    def web_driver(self):
        return self.__driver


web_driver_pool = WebDriverPool(config.WebDriverPoolSize)
