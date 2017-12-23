# myFirefoxDriver
Selenium Webdriver重新使用已打开的浏览器实例 适用于Selenium3.8.1  Firefox57 更新于2017-12-23

# Selenium Webdriver 工作原理简介
    1）Selenium代码调用API实际上根据 The WebDriver Wire Protocol 发送不同的HttpRequest 到 WebdriverServer。
    IE 是 IEDriverServer.exe 
    Chrome是ChromerDriver，下载地址： https://sites.google.com/a/chromium.org/chromedriver/downloads 
    Firefox之前老版本是以插件的形式，直接在selenium-server-standalone-XXX.jar里了： 
    webdriver.xpi （selenium-server-standalone-2.48.2.jar中/org/openqa/selenium/firefox/目录下） 
    Firefox现在抛弃XPI插件框架了，现在最新的如Firefox57需要使用geckodriver。
    geckodriver下载地址：https://github.com/mozilla/geckodriver/releases

    WebDriver协议是RESTful风格的。W3C Webdriver标准协议内容：http://www.w3.org/TR/webdriver/

    2）WebdriverServer接收到HttpRequest之后，根据不同的命令在操作系统层面去触发浏览器的”native事件“， 
    模拟操作浏览器。WebdriverServer将操作结果Http Response返回给代码调用的客户端。

    可以参考 我之前在CSDN写的使用代理可以看到协议具体内容： 
    http://blog.csdn.net/wwwqjpcom/article/details/51232302

# 如何实现使用已打开的浏览器
    想要实现Webdriver重新使用已打开的浏览器实例。
    根据原理,需要WebDriverServer的地址、一个可用的已有Session。
    
    据此实现了myFirefoxDriver。
    使用中需要将一个已打开的WebDriver实例的WebDriverServer地址，SessionId保存起来，
    然后用于初始化myFirefoxDriver。
    另外执行JavaScript需要判断Capabilities，因此加了一点在myFirefoxDriver中设置Capabilities的逻辑。

# 测试例子
下面的例子是我在SoapUI中使用groovy脚本执行的，如以其他方式使用请勿直接照搬。  
用例1是正常情况下使用SeleniumWebDriver的情况。正常创建FirefoxDriver后不退出，不关闭浏览器。  
然后参考用例2用例3，创建myFirefoxDriver即可继续使用已打开的浏览器

## 用例1
```groovy
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.support.ui.ExpectedCondition
import org.openqa.selenium.support.ui.WebDriverWait
import org.openqa.selenium.OutputType
import org.apache.commons.io.FileUtils
import org.openqa.selenium.Keys
import net.sf.json.JSONObject;

System.setProperty("webdriver.gecko.driver", "D:\\geckodriver.exe");
WebDriver driver = new FirefoxDriver()   

try
{
    driver.get("https://www.baidu.com") // Url to be opened

    //下面两行将所需的地址和SessionID 保存起来。样例因为是在SoapUI中的两个Step，所以保存为了SoapUI中  
    //用例级别的属性，具体请根据自己的使用环境保存为系统参数或其他地方
    testRunner.testCase.setPropertyValue( "DriverServer", driver.getCommandExecutor().getAddressOfRemoteServer().toString() )
    testRunner.testCase.setPropertyValue( "CaseSession", driver.getSessionId().toString() )
    log.info driver.getSessionId().toString()
    log.info driver.getCapabilities()
    testRunner.testCase.setPropertyValue( "CaseCapabilities", JSONObject.fromObject(driver.getCapabilities().asMap()).toString())

    WebElement element = driver.findElement(By.id("kw"))

    File f1 = driver.getScreenshotAs(OutputType.FILE)
    FileUtils.copyFile(f1, new File("D:\\screenshot1.png")); // Location to save screenshot

}
catch(Exception e)
{
    log.info "Exception encountered : " + e.message
}
```
## 用例2
```groovy
import webtest.myFirefoxDriver;
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.JavascriptExecutor

//下面三行，取出保存的可用的浏览器的Webdriver Server的地址和SessionID，创建myFirefoxDriver。
def driverserver = testRunner.testCase.getPropertyValue( "DriverServer" )
def caseSession = testRunner.testCase.getPropertyValue( "CaseSession" )
WebDriver driver = new myFirefoxDriver(driverserver,caseSession)

log.info (driver.getCommandExecutor().getAddressOfRemoteServer())

try
{

    driver.findElement(By.id("kw")).sendKeys("SoapUI")
    driver.findElement(By.id("su")).click()

    log.info driver.getSessionId().toString()
    log.info driver.getCapabilities()

    ((JavascriptExecutor)driver).executeScript("alert(\"hello,this is an alert!\")");
    //driver.quit()
}
catch(Exception e)
{
    log.info "Exception encountered : " + e.message
}
```
## 用例3
```groovy
import webtest.myFirefoxDriver;
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.JavascriptExecutor
import net.sf.json.JSONObject;

//下面三行，取出保存的可用的浏览器的Webdriver Server的地址和SessionID以及之前保存的Capabilities，创建myFirefoxDriver。
def driverserver = testRunner.testCase.getPropertyValue( "DriverServer" )
def caseSession = testRunner.testCase.getPropertyValue( "CaseSession" )
def caseCapabilities = testRunner.testCase.getPropertyValue( "CaseCapabilities" )
Map capMap = (Map) JSONObject.fromObject(caseCapabilities);

WebDriver driver = new myFirefoxDriver(driverserver,caseSession,capMap)

log.info (driver.getCommandExecutor().getAddressOfRemoteServer())

try
{

    driver.findElement(By.id("kw")).sendKeys("SoapUI")
    driver.findElement(By.id("su")).click()

    log.info driver.getSessionId().toString()
    log.info driver.getCapabilities()

    ((JavascriptExecutor)driver).executeScript("alert(\"hello,this is an alert!\")");
    //driver.quit()
}
catch(Exception e)
{
    log.info "Exception encountered : " + e.message
}
```