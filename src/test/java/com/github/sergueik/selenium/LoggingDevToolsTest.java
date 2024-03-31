package com.github.sergueik.selenium;

/**
 * Copyright 2020,2021,2024 Serguei Kouzmine
 */

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.TimeZone;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.devtools.v123.log.Log;
import org.openqa.selenium.devtools.v123.log.model.LogEntry;
import org.openqa.selenium.devtools.v123.runtime.model.Timestamp;
import org.openqa.selenium.devtools.v123.page.Page;

/**
 * Selected test scenarios for Selenium Chrome Developer Tools Selenium 4 bridge
 * 
 * https://chromedevtools.github.io/devtools-protocol/tot/Log#method-enable
 * https://chromedevtools.github.io/devtools-protocol/tot/Log/#event-entryAdded
 * https://chromedevtools.github.io/devtools-protocol/1-3/Page/#method-navigate
 * 
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

@SuppressWarnings("deprecation")
public class LoggingDevToolsTest extends BaseDevToolsTest {

	private final static String baseURL = "https://www.google.com";

	@Before
	public void beforeTest() throws Exception {
		chromeDevTools.send(Log.enable());
		chromeDevTools.addListener(Log.entryAdded(),

				(LogEntry event) -> System.err.println(String.format(
						"time stamp: %s line number: %s url: \"%s\" text: %s", formatTimestamp(event.getTimestamp()),
						(event.getLineNumber().isPresent() ? event.getLineNumber().get() : ""),
						(event.getUrl().isPresent() ? event.getUrl().get() : ""), event.getText())));
	}

	@Test
	public void test1() {
		// add event listener to show in host console the browser console message
		chromeDevTools.addListener(Log.entryAdded(), (LogEntry event) -> {
			assertThat(event.getText(), notNullValue());
			assertThat(event.getLineNumber(), notNullValue());
			assertThat(event.getTimestamp(), notNullValue());
			assertThat(event.getSource(), notNullValue());

		});

		// chromeDevTools.addListener(Log.eventAdded(), System.err::println);
		// what it would print will not be too useful:
		// org.openqa.selenium.devtools.v123.log.model.LogEntry@5e77d702

		driver.get(baseURL);
		chromeDevTools
				.send(Page.navigate(baseURL, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()));
	}

	@Test
	public void test2() {
		final String consoleMessage = "Lorem ipsum";
		chromeDevTools.addListener(Log.entryAdded(),
				(LogEntry event) -> assertThat(event.getText(), containsString(consoleMessage)));
		driver.executeScript("console.log(arguments[0]);", consoleMessage);
	}

	// origin:
	// https://github.com/SaraMohamed2022/Selenium4_CDPPracticing/blob/main/src/test/java/selenium4_SelfPracticing/CDPLogs.java
	@Test
	public void test3() {
		chromeDevTools.addListener(Log.entryAdded(), logEntry -> {
			System.err.println(
					String.format("-----------" + "\n" + "Level: %s" + "\n" + "Text: %s" + "\n" + "Broken URL: %s",
							logEntry.getLevel(), logEntry.getText(), logEntry.getUrl()));
		});
		String url = "https://the-internet.herokuapp.com/broken_images";
		driver.get(url);
	}

	@After
	public void afterTest() throws Exception {
		chromeDevTools.clearListeners();
		chromeDevTools.send(Log.disable());
	}

	private String formatTimestamp(Timestamp timestamp) {
		final DateFormat gmtFormat = new SimpleDateFormat("E, dd-MMM-yyyy hh:mm:ss");
		final TimeZone timeZone = TimeZone.getDefault();
		gmtFormat.setTimeZone(timeZone);
		long time = Double.valueOf(timestamp.toString()).longValue();
		return gmtFormat.format(new Date(time)) + " " + timeZone.getDisplayName(false, TimeZone.SHORT);

	}

}
