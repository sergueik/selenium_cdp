package com.github.sergueik.selenium;

/**
 * Copyright 2023,2024 Serguei Kouzmine
 */


import java.time.Duration;

/**
 * Copyright 2024 Serguei Kouzmine
 */

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.devtools.v141.console.Console;
import org.openqa.selenium.devtools.v141.console.model.ConsoleMessage;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Selected test scenarios for Selenium Chrome Developer Tools Selenium 4 bridge
 * NOTE: the Console domain is deprecated - using Runtime or Log instead is advised
 * https://chromedevtools.github.io/devtools-protocol/tot/Console/#method-disable
 * https://chromedevtools.github.io/devtools-protocol/tot/Console/#method-enable
 * https://chromedevtools.github.io/devtools-protocol/tot/Console/#event-messageAdded
 * https://chromedevtools.github.io/devtools-protocol/tot/Console/#type-ConsoleMessage
 * 
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

@SuppressWarnings("deprecation")
public class ConsoleMessagesDevToolsTest extends BaseDevToolsTest {

	private final static String baseURL = "https://www.selenium.dev/selenium/web/bidi/logEntryAdded.html";
	private WebElement element;
	protected static WebDriverWait wait;
	public final static int flexibleWait = 10;
	public final static Duration duration = Duration.ofSeconds(flexibleWait);
	public final static int implicitWait = 1;
	public final static int pollingInterval = 50;

	@Before
	public void beforeTest() throws Exception {
		wait = new WebDriverWait(driver, duration);
		wait.pollingEvery(Duration.ofMillis(pollingInterval));

		chromeDevTools.send(Console.enable());
		chromeDevTools.addListener(Console.messageAdded(),
				(ConsoleMessage message) -> System.err.println(String.format(
						"level: %s" + "\n" + "source: %s" + "\n" + "line number: %s" + "\n"
								+ "column number: %s" + "\n" + "url: \"%s\" text: %s",
						message.getLevel(), message.getSource(),
						(message.getLine().isPresent() ? message.getLine().get() : ""),
						(message.getColumn().isPresent() ? message.getColumn().get() : ""),
						(message.getUrl().isPresent() ? message.getUrl().get() : ""),
						message.getText())));
		driver.get(baseURL);
	}

	// see also:
	// https://github.com/SaraMohamed2022/Selenium4_CDPPracticing/blob/main/src/test/java/selenium4_SelfPracticing/GetJavaScriptLogs.java
	@Test
	public void test1() {
		element = wait.until(
				ExpectedConditions.visibilityOfElementLocated(By.id("consoleLog")));
		element.click();
		Utils.highlight(element);
	}

	// NOTE: nothing will be logged
	@Test
	public void test2() {
		element = wait.until(ExpectedConditions
				.visibilityOfElementLocated(By.id("logWithStacktrace")));
		element.click();
		Utils.highlight(element);
	}

	@After
	public void afterTest() throws Exception {
		chromeDevTools.clearListeners();
		chromeDevTools.send(Console.disable());
	}

}
