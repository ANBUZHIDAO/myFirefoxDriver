# myFirefoxDriver
Selenium Webdriver重新使用已打开的浏览器实例 适用于Selenium3.8.1  Firefox57 更新于2017-12-23

-------------TestCase 1--------
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
-----------TestCase 2--------------
import webtest.myFirefoxDriver;
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.JavascriptExecutor

//下面三行，取出保存的可用的浏览器的Webdriver Server的地址和SessionID，new一个Webdriver。
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

((JavascriptExecutor)driver).executeScript("alert(\"hello,this is a alert!\")");
 //driver.quit()
}
catch(Exception e)
{
log.info "Exception encountered : " + e.message
}

------- testCase3---------------
import webtest.myFirefoxDriver;
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.JavascriptExecutor
import net.sf.json.JSONObject;

//下面三行，取出保存的可用的浏览器的Webdriver Server的地址和SessionID，new一个Webdriver。
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

((JavascriptExecutor)driver).executeScript("alert(\"hello,this is a alert!\")");
 //driver.quit()
}
catch(Exception e)
{
log.info "Exception encountered : " + e.message
}
