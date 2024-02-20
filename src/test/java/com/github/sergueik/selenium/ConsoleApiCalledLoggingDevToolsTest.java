package com.github.sergueik.selenium;

/**
 * Copyright 2024 Serguei Kouzmine
 */

import java.time.Duration;
import java.util.concurrent.CopyOnWriteArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import org.openqa.selenium.devtools.v121.runtime.Runtime;
import org.openqa.selenium.devtools.v121.runtime.model.StackTrace;
import org.openqa.selenium.devtools.v121.runtime.model.ConsoleAPICalled;
// NOTE: import org.openqa.selenium.bidi.log.StackTrace;
import org.openqa.selenium.devtools.v121.runtime.model.RemoteObject;

/**
 * Selected test scenarios for Selenium Chrome Developer Tools Selenium 4 bridge
 * based on:
 * https://www.selenium.dev/documentation/webdriver/bidirectional/chrome_devtools/cdp_api/#console-logs
 * see also:
 * https://chromedevtools.github.io/devtools-protocol/tot/Runtime/#event-consoleAPICalled
 * https://chromedevtools.github.io/devtools-protocol/tot/Runtime/#type-RemoteObject
 * https://chromedevtools.github.io/devtools-protocol/tot/Runtime/#type-StackTrace 
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

@SuppressWarnings("deprecation")
public class ConsoleApiCalledLoggingDevToolsTest extends BaseDevToolsTest {

	private final static String baseURL = "https://www.selenium.dev/selenium/web/bidi/logEntryAdded.html";

	private WebElement element;
	protected static WebDriverWait wait;
	public final static int flexibleWait = 10;
	public final static Duration duration = Duration.ofSeconds(flexibleWait);
	public final static int implicitWait = 1;
	public final static int pollingInterval = 50;

	private CopyOnWriteArrayList<String> logs = new CopyOnWriteArrayList<>();
	private CopyOnWriteArrayList<StackTrace> traces = new CopyOnWriteArrayList<>();

	@Before
	public void beforeTest() throws Exception {
		wait = new WebDriverWait(driver, duration);
		wait.pollingEvery(Duration.ofMillis(pollingInterval));
		chromeDevTools.send(Runtime.enable());
		driver.get(baseURL);
	}

	@Ignore
	@Test
	public void test1() {

		chromeDevTools.addListener(Runtime.consoleAPICalled(),
				(ConsoleAPICalled event) -> logs
						.add((String) event.getArgs().get(0).getValue().orElse("")));

		element = wait.until(
				ExpectedConditions.visibilityOfElementLocated(By.id("consoleLog")));
		element.click();
		Utils.sleep(1000);
		logs.stream().forEach(System.err::println);
	}

	@Test
	public void test2() {

		chromeDevTools.addListener(Runtime.consoleAPICalled(),
				(ConsoleAPICalled event) -> {
					System.err.println("Processing event");
					traces.add(event.getStackTrace().get());
					logs.add((String) event.getArgs().get(0).getValue().get());
				});

		element = wait.until(ExpectedConditions
				.visibilityOfElementLocated(By.id("logWithStacktrace")));
		element.click();
		Utils.sleep(1000);
		System.err.println(String.format("test2 traces: %d", traces.size()));
		traces.stream().map(o -> o.getDescription()).forEach(System.err::println);
		System.err.println(String.format("test2 logs: %d", logs.size()));
		logs.stream().forEach(System.err::println);
	}

	@After
	public void afterTest() throws Exception {
		logs.clear();
		traces.clear();
		chromeDevTools.clearListeners();
		chromeDevTools.send(Runtime.disable());
	}

}
