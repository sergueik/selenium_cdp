package com.github.sergueik.selenium;

/**
 * Copyright 2025 Serguei Kouzmine
 */
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.devtools.v144.deviceaccess.DeviceAccess;
import org.openqa.selenium.devtools.v144.page.Page;
import org.openqa.selenium.devtools.v144.page.model.JavascriptDialogClosed;
import org.openqa.selenium.devtools.v144.page.model.JavascriptDialogOpening;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.devtools.v144.deviceaccess.model.DeviceRequestPrompted;
import org.openqa.selenium.devtools.v144.deviceaccess.model.PromptDevice;
import org.openqa.selenium.devtools.v144.deviceaccess.model.RequestId;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * 
 * Selected test scenarios for Selenium Chrome Developer Tools Selenium 4 bridge
 * https://chromedevtools.github.io/devtools-protocol/1-2/Page/#event-javascriptDialogOpening
 * https://chromedevtools.github.io/devtools-protocol/1-3/Page/#event-javascriptDialogClosed
 * 
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

public class DeviceAccessDevToolsLocalTest extends BaseDevToolsTest {

	protected static WebDriverWait wait;
	public final static int flexibleWait = 10; // NOTE: 60 is quite long
	public final static Duration duration = Duration.ofSeconds(flexibleWait);
	public final static int implicitWait = 1;
	public final static int pollingInterval = 500;
	protected Alert alert = null;
	protected static String name = null;
	protected static WebElement element;
	private final String page = "device_access2.html";

	@Before
	public void before() {
		chromeDevTools.send(DeviceAccess.enable());
		chromeDevTools.send(Page.enable(Optional.of(false)));
		// TODO: demonstrate CDP never gets the event
		wait = new WebDriverWait(driver, duration);
		wait.pollingEvery(Duration.ofMillis(pollingInterval));
	}

	@After
	public void after() {
		chromeDevTools.clearListeners();
		chromeDevTools.send(DeviceAccess.disable());
		chromeDevTools.send(Page.disable());
		Utils.stopLocalHttpServer(1);
		Utils.sleep(3000);
		driver.get("about:blank");
	}

	@Ignore
	@Test
	public void test1() {
		// Arrange
		// register to Device Access events
		List<RequestId> data = new ArrayList<>();
		RequestId requestId = null;
		chromeDevTools.addListener(DeviceAccess.deviceRequestPrompted(), (DeviceRequestPrompted event) -> {
			PromptDevice device = event.getDevices().get(0);
			// local variables referenced from a lambda expression must be final or
			// effectively final
			data.add(event.getId());
			System.err.println(String.format("Page has Device Access prompt for device %s", device.getName()));
		});
		// register to page alert events
		chromeDevTools.addListener(Page.javascriptDialogOpening(),
				(JavascriptDialogOpening event) -> System.err
						.println(String.format("Page has dialog %s of type %s opening (%s)", event.getMessage(),
								event.getType(), event.getHasBrowserHandler().booleanValue())));
		// assert
		// Whether dialog was confirmed
		chromeDevTools.addListener(Page.javascriptDialogClosed(),
				(JavascriptDialogClosed event) -> assertThat(event.getResult(), is(true)));
		// Act
		System.err.println("Started local test 1 - deny microphone access.");
		driver.get(Utils.getPageContent("device_access1.html"));
		element = driver.findElement(By.tagName("button"));
		assertThat(element, notNullValue());
		System.err.println(String.format("Press: %s", element.getAttribute("outerHTML")));
		Utils.highlight(element);
		element.click();
		Utils.sleep(3000);
		// NOTE: id is required by cancelPrompt
		assertThat(data.size(), greaterThan(0));
		requestId = getRequestId(data);
		chromeDevTools.send(DeviceAccess.cancelPrompt(requestId));

		alert = driver.switchTo().alert();
		// Assert alert displayed
		assertThat("alert expected to be displayed", alert, notNullValue());
		System.err.println("accepting alert");
		alert.accept();
		element = driver.findElement(By.id("demo"));
		System.err.println(element.getAttribute("innerHTML"));
	}
	
	@Ignore
	@Test
	public void test2() {
		// Arrange
		List<RequestId> data = new ArrayList<>();
		RequestId requestId = null;
		chromeDevTools.addListener(DeviceAccess.deviceRequestPrompted(), (DeviceRequestPrompted event) -> {
			PromptDevice device = event.getDevices().get(0);
			data.add(event.getId());
			System.err.println(String.format("Page has Device Access prompt for device %s", device.getName()));
		});

		// Act
		System.err.println("Started local test 2 - deny USB Device access.");
		driver.get(Utils.getPageContent(page));
		element = driver.findElement(By.tagName("button"));
		assertThat(element, notNullValue());
		System.err.println(String.format("Press: %s", element.getAttribute("outerHTML")));
		Utils.highlight(element);
		element.click();
		Utils.sleep(3000);
		assertThat(data.size(), greaterThan(0));
		requestId = this.<RequestId> getRequestId(data);
		chromeDevTools.send(DeviceAccess.cancelPrompt(requestId));
		// Assert status updated
		element = driver.findElement(By.id("status"));
		System.err.println(element.getAttribute("innerHTML"));
	}

	@Ignore
	@Test
	public void test3() {
		List<RequestId> data = new ArrayList<>();
		RequestId requestId = null;
		chromeDevTools.addListener(DeviceAccess.deviceRequestPrompted(), (DeviceRequestPrompted event) -> {
			PromptDevice device = event.getDevices().get(0);
			data.add(event.getId());
			System.err.println(String.format("Page has Device Access prompt for device %s", device.getName()));
		});
		// Act
		System.err.println("Started local test 2 - deny USB Device access.");
		driver.get(Utils.getLocallyHostedPageContent(page));
		element = driver.findElement(By.tagName("button"));
		assertThat(element, notNullValue());
		System.err.println(String.format("Press: %s", element.getAttribute("outerHTML")));
		Utils.highlight(element);
		element.click();
		Utils.sleep(3000);
		assertThat(data.size(), greaterThan(0));
		requestId = getRequestId(data);
		chromeDevTools.send(DeviceAccess.cancelPrompt(requestId));
		// TODO: Assert status updated if the event is processed
		element = driver.findElement(By.id("status"));
		System.err.println(element.getAttribute("innerHTML"));
	}
	private <T> T getRequestId(List<T>data) {
		T result = 
	 (data.size() > 0) ? 
		data.get(0):null;
	
	return result; 
	}
}
