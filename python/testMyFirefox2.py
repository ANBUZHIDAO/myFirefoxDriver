from webdriver import myFirefox
import pickle

f = open("params.data", 'rb')
# 从文件中载入对象
params = pickle.load(f)
print(params)

browser = myFirefox.myWebDriver(service_url=params["server_url"],
    session_id=params["session_id"])

print(browser.capabilities)
print(browser.command_executor._url)
print(browser.session_id)

browser.find_element_by_id("kw").clear()
browser.find_element_by_id("kw").send_keys("python")
browser.find_element_by_id("su").click()

#browser.quit()