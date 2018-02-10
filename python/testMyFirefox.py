from webdriver import myFirefox

browser = myFirefox.myWebDriver(service_url="http://127.0.0.1:4444",
    session_id="ed6d43ee-3451-42ef-b10e-1d993402bae5")

print(browser.capabilities)
print(browser.command_executor._url)
print(browser.session_id)

browser.find_element_by_id("kw").clear()
browser.find_element_by_id("kw").send_keys("python")
browser.find_element_by_id("su").click()
