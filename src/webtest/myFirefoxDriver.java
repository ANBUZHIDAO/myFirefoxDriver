package webtest;

import static org.openqa.selenium.remote.CapabilityType.SUPPORTS_JAVASCRIPT;
import static org.openqa.selenium.remote.CapabilityType.PROXY;

import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.ImmutableCapabilities;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.Command;
import org.openqa.selenium.remote.DriverCommand;
import org.openqa.selenium.remote.Response;
import org.openqa.selenium.remote.internal.WebElementToJsonConverter;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class myFirefoxDriver extends FirefoxDriver {
	
	private Capabilities mycapabilities;

	public myFirefoxDriver(String localserver, String sessionID) {

		mystartClient(localserver);
		mystartSession(sessionID);
	}
	
	public myFirefoxDriver(String localserver, String sessionID, Map<String, ?> capMap) {

		mystartClient(localserver);
		mystartSession(sessionID);
		mycapabilities = dropCapabilities(new MutableCapabilities(capMap)); 
	}

	protected void mystartClient(String localserver) {
		myHttpCommandExecutor delegate = null;

		try {
			URL driverserver = new URL(localserver);
			delegate = new myHttpCommandExecutor(driverserver);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		setCommandExecutor(delegate);
		System.out.println("Connect to the existing browser");

	}

	@Override
	protected void startClient() {

		// Do nothing
	}

	protected void mystartSession(String sessionID) {

		if (!sessionID.isEmpty()) {
			super.setSessionId(sessionID);
		}

		Command command = new Command(super.getSessionId(), DriverCommand.STATUS);

		Response response;
		try {
			response = ((myHttpCommandExecutor)getCommandExecutor()).execute(command);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			System.out.println("Can't use this Session");
			return;
		}
		
		System.out.println("response.getValue()" + response.getValue());
		if (response.getValue() instanceof Exception)
		{
			((Exception)response.getValue()).printStackTrace();
		}
		
		//为了能执行Script
		this.mycapabilities = dropCapabilities(new FirefoxOptions()) ;

	}

	@Override
	protected void startSession(Capabilities desiredCapabilities) {
		// Do Nothing
	}
	
	public Capabilities getCapabilities() {
		return mycapabilities;
	}
	
	  /**
	   * 复制FirefoxDriver中的方法过来
	   */
	private static Capabilities dropCapabilities(Capabilities capabilities) 
	{
		if (capabilities == null) {
			return new ImmutableCapabilities();
		}

		MutableCapabilities caps = new MutableCapabilities(capabilities);

		// Ensure that the proxy is in a state fit to be sent to the extension
		Proxy proxy = Proxy.extractFrom(capabilities);
		if (proxy != null) {
			caps.setCapability(PROXY, proxy);
		}

		return caps;
	}
	
	public Object executeScript(String script, Object... args) {
		if (!mycapabilities.is(SUPPORTS_JAVASCRIPT)) {
			throw new UnsupportedOperationException(
					"You must be using an underlying instance of WebDriver that supports executing javascript");
		}

		// Escape the quote marks
		script = script.replaceAll("\"", "\\\"");

		Iterable<Object> convertedArgs = Iterables.transform(Lists.newArrayList(args), new WebElementToJsonConverter());

		Map<String, ?> params = ImmutableMap.of("script", script, "args", Lists.newArrayList(convertedArgs));

		return execute(DriverCommand.EXECUTE_SCRIPT, params).getValue();
	}

	public Object executeAsyncScript(String script, Object... args) {
		if (!isJavascriptEnabled()) {
			throw new UnsupportedOperationException(
					"You must be using an underlying instance of " + "WebDriver that supports executing javascript");
		}

		// Escape the quote marks
		script = script.replaceAll("\"", "\\\"");

		Iterable<Object> convertedArgs = Iterables.transform(Lists.newArrayList(args), new WebElementToJsonConverter());

		Map<String, ?> params = ImmutableMap.of("script", script, "args", Lists.newArrayList(convertedArgs));

		return execute(DriverCommand.EXECUTE_ASYNC_SCRIPT, params).getValue();
	}

	private boolean isJavascriptEnabled() {
		return mycapabilities.is(SUPPORTS_JAVASCRIPT);
	}

	public WebElement loopFindElement(By by) {
		WebElement result = null;
		try {
			result = super.findElement(by);
			return result;
		} catch (Exception e) {
			//
		}

		//循环查找的同时也会自动切到对应的iframe去
		List<WebElement> frames = findElements(By.xpath("//iframe[string-length(@src)>1]"));
		for (int i = 0; i < frames.size(); i++) {
			this.switchTo().frame(frames.get(i));
			result = loopFindElement(by);

			if (result != null) {
				return result;
			}

			this.switchTo().parentFrame();
		}

		return result;
	}

	public WebElement myFindElement(By by) throws Exception {
		WebElement result;

		// 首先在当前iframe查找,找不到再切换到默认frame，遍历
		try {
			result = super.findElement(by);
			return result;
		} catch (Exception e) {
			//
		}

		// 如果没找到
		switchTo().defaultContent();
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // 首先等待100毫秒
		
		//防止iframe过多，没查找一次就等待一下，时间将会很长
		this.manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS);
		//开始循环查找
		result = loopFindElement(by);
		if (result == null) {
			throw new NoSuchElementException("Cannot locate an element using " + by.toString());
		} else{
			this.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
			return result;
		}
	}
	
	public WebElement myFindElement(WebElement frame,By by) throws Exception {
		WebElement result;
		
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // 首先等待100毫秒
		
		//先切换到指定框架，在指定框架下查找
		switchTo().frame(frame);
		// 首先在当前iframe查找,找不到再切换到默认frame，遍历
		try {
			result = super.findElement(by);
			return result;
		} catch (Exception e) {
			//
		}

		//防止iframe过多，没查找一次就等待一下，时间将会很长
		this.manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS);
		//开始循环查找
		result = loopFindElement(by);
		if (result == null) {
			throw new NoSuchElementException("Cannot locate an element using " + by.toString());
		} else{
			this.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
			return result;
		}
	}

}
