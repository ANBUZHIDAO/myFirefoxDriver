from selenium import webdriver
from selenium.webdriver.common.desired_capabilities import DesiredCapabilities
import pickle

driver = webdriver.remote.webdriver.WebDriver(command_executor="http://127.0.0.1:4444",
    desired_capabilities=DesiredCapabilities.FIREFOX)  
driver.get('http://www.baidu.com/')

print(driver.capabilities)
print(driver.command_executor._url)
print(driver.session_id)
print(driver.command_executor.keep_alive)

params = {}
params["session_id"] = driver.session_id
params["server_url"] = driver.command_executor._url

f = open("params.data", 'wb')
# 转储对象至文件
pickle.dump(params, f)
f.close()
