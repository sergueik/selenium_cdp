package com.github.sergueik.selenium;

/**
 * Copyright 2026 Serguei Kouzmine
 */

import static java.lang.System.err;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriverException;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * Selected test scenarios for Selenium 4 Chrome Developer Tools bridge
 *
 * https://chromedevtools.github.io/devtools-protocol/#/PerformanceTimeline.enable
 *
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

public class PerformanceTimelineCdpTest extends BaseCdpTest {

	private static String command = "PerformanceTimeline.enable";
	private static Map<String, Object> result = new HashMap<>();
	private Map<String, Object> data = new HashMap<>();
	private static Map<String, Object> params = new HashMap<>();
	private static String baseURL = "https://developer.mozilla.org/en-US/docs/Web/API/PerformanceEntry";
	private static Gson gson = new Gson();

	// @formatter:off
	private static String[] performanceEntryTypes = { "unloadEventStart", "unloadEventEnd", "redirectStart",
			"redirectEnd", "fetchStart", "domainLookupStart", "domainLookupEnd", "connectStart", "connectEnd",
			"secureConnectionStart", "requestStart", "responseStart", "responseEnd", "domLoading", "domInteractive",
			"domContentLoadedEventStart", "domContentLoadedEventEnd", "domComplete", "loadEventStart", "initiatorUrl" };

	@Before
	public void before() throws Exception {
		// Arrange
		try {
			driver.navigate().to(baseURL);
		} catch (TimeoutException e) {
			System.err.println("continue after timeout exception");
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void test1() {
		// Act
		try {
			params.clear();
			params.put("eventTypes", new String[] {});
			result = driver.executeCdpCommand(command, params);
			// Assert
			assertThat(result, notNullValue());
			System.err.println("Result: " + result.keySet());
		} catch (WebDriverException webDriverException) {
			err.println("Exception in command " + command + " (ignored): "
					+ Utils.processExceptionMessage(webDriverException.getMessage()));
			System.err.println("raw message: " + webDriverException.getRawMessage());
		} catch (Exception e) {
			err.println("Exception: in " + command + " " + e.toString());
			throw (new RuntimeException(e));
		}

	}

	@Ignore
	@SuppressWarnings("unchecked")
	@Test
	public void test2() {
		try {
			// Act
			params.clear();
			params.put("eventTypes", performanceEntryTypes);
			result = driver.executeCdpCommand(command, params);
			// Assert
			assertThat(result, notNullValue());
		} catch (WebDriverException webDriverException) {

			err.println("Exception in command " + command + " (ignored): "
					+ Utils.processExceptionMessage(webDriverException.getMessage()));
			System.err.println("raw message: " + webDriverException.getRawMessage());

		} catch (Exception e) {
			err.println("Exception: in " + command + " " + e.toString());
			throw (new RuntimeException(e));
		}
	}

	@Ignore
	@SuppressWarnings("unchecked")
	@Test
	public void test3() {

		for (String performanceEntryType : performanceEntryTypes) {
			try {
				// Act
				params.clear();
				params.put("eventTypes", Arrays.asList(performanceEntryType));
				result = driver.executeCdpCommand(command, params);
				// Assert
				assertThat(result, notNullValue());
			} catch (WebDriverException webDriverException) {

				err.println("Exception in command " + command + " (ignored): "
						+ Utils.processExceptionMessage(webDriverException.getMessage()));
				System.err.println("raw message: " + webDriverException.getRawMessage());

			} catch (Exception e) {
				err.println("Exception: in " + command + " " + e.toString());
				throw (new RuntimeException(e));
			}
		}
	}
}
