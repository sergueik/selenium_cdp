package com.github.sergueik.selenium;

/**
 * Copyright 2024 Serguei Kouzmine
 */

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

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

import org.openqa.selenium.devtools.v143.runtime.Runtime;
import org.openqa.selenium.devtools.v143.runtime.model.StackTrace;
import org.openqa.selenium.devtools.v143.runtime.model.ConsoleAPICalled;
import org.openqa.selenium.devtools.v143.runtime.model.ExceptionDetails;
import org.openqa.selenium.devtools.v143.runtime.model.ExceptionThrown;
import org.openqa.selenium.devtools.v143.runtime.model.Timestamp;
// NOTE: import org.openqa.selenium.bidi.log.StackTrace;
import org.openqa.selenium.devtools.v143.runtime.model.RemoteObject;

/**
 * Selected test scenarios for Selenium Chrome Developer Tools Selenium 4 bridge
 * based on:
 * https://www.selenium.dev/documentation/webdriver/bidirectional/chrome_devtools/cdp_api/#console-logs
 * see also:
 * https://chromedevtools.github.io/devtools-protocol/tot/Runtime/#event-consoleAPICalled
 * https://chromedevtools.github.io/devtools-protocol/tot/Runtime/#event-exceptionThrown
 * https://chromedevtools.github.io/devtools-protocol/tot/Runtime/#type-RemoteObject
 * https://chromedevtools.github.io/devtools-protocol/tot/Runtime/#type-ExceptionDetails
 * https://chromedevtools.github.io/devtools-protocol/tot/Runtime/#type-StackTrace 
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

public class RuntimeLoggingDevToolsTest extends BaseDevToolsTest {

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

	@Test
	public void test1() {

		chromeDevTools.addListener(Runtime.consoleAPICalled(),
				(ConsoleAPICalled event) -> logs
						.add((String) event.getArgs().get(0).getValue().orElse("")));

		element = wait.until(
				ExpectedConditions.visibilityOfElementLocated(By.id("consoleLog")));
		element.click();
		Utils.highlight(element);
		System.err.println(String.format("test1 console logs: %d", logs.size()));
		logs.stream().forEach(System.err::println);
	}

	@Test
	public void test2() {

		chromeDevTools.addListener(Runtime.exceptionThrown(),
				(ExceptionThrown event) -> {
					System.err.println("Processing the exception");
					ExceptionDetails exceptionDetails = event.getExceptionDetails();
					RemoteObject exception = exceptionDetails.getException().get();
					logs.add(String.format(
							"time stamp: %s line: %s column: %s url: \"%s\" text: %s exception: %s",
							formatTimestamp(event.getTimestamp()),
							exceptionDetails.getLineNumber(),
							exceptionDetails.getColumnNumber(),
							(exceptionDetails.getUrl().isPresent()
									? exceptionDetails.getUrl().get() : ""),
							exceptionDetails.getText(), exception.getDescription().get()));
					traces.add(exceptionDetails.getStackTrace().get());
				});

		element = wait.until(ExpectedConditions
				.visibilityOfElementLocated(By.id("logWithStacktrace")));
		element.click();
		Utils.highlight(element);
		System.err.println(String.format("test2 exception logs: %d", logs.size()));
		logs.stream().forEach(System.err::println);
		System.err
				.println(String.format("test2 exception traces: %d", traces.size()));
		traces.stream().map(o -> o.getDescription()).forEach(System.err::println);
	}

	private String formatTimestamp(Timestamp timestamp) {
		final DateFormat gmtFormat = new SimpleDateFormat(
				"E, dd-MMM-yyyy hh:mm:ss");
		final TimeZone timeZone = TimeZone.getDefault();
		gmtFormat.setTimeZone(timeZone);
		long time = Double.valueOf(timestamp.toString()).longValue();
		return gmtFormat.format(new Date(time)) + " "
				+ timeZone.getDisplayName(false, TimeZone.SHORT);

	}

	@After
	public void afterTest() throws Exception {
		logs.clear();
		traces.clear();
		chromeDevTools.clearListeners();
		chromeDevTools.send(Runtime.disable());
	}

}

