package com.github.sergueik.selenium;

/**
 * Copyright 2026 Serguei Kouzmine
 */

import org.openqa.selenium.devtools.DevToolsException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import org.openqa.selenium.devtools.v153.network.model.TimeSinceEpoch;
import org.openqa.selenium.devtools.v153.performancetimeline.PerformanceTimeline;
import org.openqa.selenium.devtools.v153.performancetimeline.model.TimelineEvent;

/**
 * Selected test scenarios for Selenium Chrome Developer Tools Selenium 4 bridge
 * see: https://chromedevtools.github.io/devtools-protocol/#/PerformanceTimeline
 *
 * https://github.com/debug-tips/timing2
 * https://w3c.github.io/performance-timeline/#dom-performanceentry-entrytype
 * https://w3c.github.io/performance-timeline/#getentries-method
 * https://github.com/w3c/performance-timeline
 * https://github.com/mdn/content/issues/2667 -
 * `PerformanceObserver.performanceEntryTypess` is undocumented
 * https://github.com/mdn/content/commit/08d338941a1667a284ea446fa29eb394ca39a136
 * https://developer.mozilla.org/en-US/docs/Web/API/PerformanceEntry
 * https://developer.mozilla.org/en-US/docs/Web/API/PerformanceObserver/performanceEntryTypess_static
 *
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

@SuppressWarnings("unchecked")
public class PerformanceTimelineDevToolsTest extends BaseDevToolsTest {

	private WebDriverException webDriverException = null;
	private Map<String, Object> data = new HashMap<>();
	private static HashSet<Object> metricKeys = new HashSet<>();
	// @formatter:off
	private static String[] performanceEntryTypess = { "unloadEventStart", "unloadEventEnd", "redirectStart",
			"redirectEnd", "fetchStart", "domainLookupStart", "domainLookupEnd", "connectStart", "connectEnd",
			"secureConnectionStart", "requestStart", "responseStart", "responseEnd", "domLoading", "domInteractive",
			"domContentLoadedEventStart", "domContentLoadedEventEnd", "domComplete", "loadEventStart", "initiatorUrl"

//			[name, entryType, startTime, duration, navigationId, initiatorType, deliveryType, nextHopProtocol, renderBlockingStatus, contentType, contentEncoding, workerStart, workerRouterEvaluationStart, workerCacheLookupStart, workerMatchedSourceType, workerFinalSourceType, initiatorUrl, redirectStart, redirectEnd, fetchStart, domainLookupStart, domainLookupEnd, connectStart, secureConnectionStart, connectEnd, requestStart, responseStart, firstInterimResponseStart, finalResponseHeadersStart, responseEnd, transferSize, encodedBodySize, decodedBodySize, responseStatus, serverTiming, unloadEventStart, unloadEventEnd, domInteractive, domContentLoadedEventStart, domContentLoadedEventEnd, domComplete, loadEventStart, loadEventEnd, type, redirectCount, activationStart, criticalCHRestart, notRestoredReasons, confidence

	};

//	private static String[] performanceEntryTypess = { "frame" };
// @formatter:on

	private static String baseURL = "https://developer.mozilla.org/en-US/docs/Web/API/PerformanceEntry";
	private static Gson gson = new Gson();
	private static final Map<String, String> param = new HashMap<>();

	@Before
	public void before() throws Exception {
		// Arrange
		try {
			driver.navigate().to(baseURL);
		} catch (TimeoutException e) {
			System.err.println("continue after timeout exception");
		}
	}

	@Test
	public void test1() {
		for (String performanceEntryTypes : performanceEntryTypess) {
			try {
				chromeDevTools.send(PerformanceTimeline.enable(Arrays.asList(performanceEntryTypes)));
				System.err.println(
						String.format("Performance Timeline enabled for Event type %s", performanceEntryTypes));
				chromeDevTools.addListener(PerformanceTimeline.timelineEventAdded(), (TimelineEvent timelineEvent) -> {
					String name = timelineEvent.getName();
					TimeSinceEpoch timeSinceEpoch = timelineEvent.getTime();
					System.err.println(String.format("Event %s added at %d", name, timeSinceEpoch));
				});
			} catch (DevToolsException e) {
				if (e.getCause() instanceof WebDriverException) {
					webDriverException = (WebDriverException) e.getCause();

					try {
						data = gson.fromJson(webDriverException.getRawMessage(), Map.class);

						System.err.println(String.format("Unsupported entry type: %s: %s", performanceEntryTypes,
								data.get("error")));
					} catch (JsonSyntaxException e2) {
						System.err
								.println(String.format("Exception(ignored) parsing message json: %s", e2.getMessage()));
					}
				}
			}
		}
	}

	@Test
	public void test2() {
		try {
			chromeDevTools.send(PerformanceTimeline.enable(new ArrayList<String>()));
			System.err.println("Performance Timeline enabled for empty eventTypes");
			chromeDevTools.addListener(PerformanceTimeline.timelineEventAdded(), (TimelineEvent timelineEvent) -> {
				String name = timelineEvent.getName();
				TimeSinceEpoch timeSinceEpoch = timelineEvent.getTime();
				System.err.println(String.format("Event %s added at %d", name, timeSinceEpoch));
			});
		} catch (DevToolsException e) {
			if (e.getCause() instanceof WebDriverException) {
				webDriverException = (WebDriverException) e.getCause();

				try {
					data = gson.fromJson(webDriverException.getRawMessage(), Map.class);

					System.err.println(String.format("Exception enabling Performance Timeline for empty eventTypes: %s",
							data.get("error")));
				} catch (JsonSyntaxException e2) {
					System.err.println(String.format("Exception(ignored) parsing message json: %s", e2.getMessage()));
				}
			}
		}
	}

	@Test(expected = NullPointerException.class)
	public void test3() {
		try {
			chromeDevTools.send(PerformanceTimeline.enable(null));
			System.err.println("Performance Timeline enabled for null eventTypes");
			chromeDevTools.addListener(PerformanceTimeline.timelineEventAdded(), (TimelineEvent timelineEvent) -> {
				String name = timelineEvent.getName();
				TimeSinceEpoch timeSinceEpoch = timelineEvent.getTime();
				System.err.println(String.format("Event %s added at %d", name, timeSinceEpoch));
			});
			driver.navigate().refresh();
		} catch (NullPointerException e) {
			System.err.println(String.format(
					"NullPointerException enabling Performance Timeline for null eventTypes: %s", e.getMessage()));
			// NullPointerException enabling Performance Timeline for null eventTypes:
			// eventTypes is required
			throw e;
		} catch (Exception e) {
			System.err.println(
					String.format("Exception enabling Performance Timeline for null eventTypes: %s", e.getMessage()));
		}
	}

	// ignore to reduce logging - tests are not failing
	@Ignore
	@Test
	public void test4() {
		String script = "if (typeof window.performance.getEntries === 'function') return window.performance.getEntries();";
		List<String> results = (List<String>) executeScript(script);
		System.err.println(results);
	}

	@Ignore
	@Test
	public void test5() {
		String script = "return JSON.stringify(window.performance.getEntries());";
		List<Map<String, Object>> result = gson.fromJson((String) executeScript(script), List.class);
		System.err.println(result.size());
		System.err.println(result.get(0).keySet().toString());
	}

	@Ignore
	@Test
	public void test6() {
		for (String performanceEntryTypes : performanceEntryTypess) {
			String script = "var entryType = arguments[0]; return JSON.stringify(window.performance.getEntriesByType(entryType));";
			String result = (String) executeScript(script, performanceEntryTypes);
			System.err.println(String.format("%s %s", performanceEntryTypes, result));
		}
	}

	@Ignore
	@Test
	public void test7() {
		String script = "return JSON.stringify({'now': window.performance.now(), 'timeOrigin': window.performance.timeOrigin});";
		Map<String, Object> result = gson.fromJson((String) executeScript(script), Map.class);
		System.err.println(result);
	}

	@Ignore
	// see
	// https://github.com/sergueik/selenium_tests/blob/master/src/test/java/com/github/sergueik/selenium/PageTimingTest.java
	@Test
	public void test8() {
		param.clear();
		param.put("ladder", "true");
		String result = (String) executeScript(getScriptContent("compute-timing.js"));
		System.err.println("Result (raw)" + result);
	}

	@After
	// Disables performance timeline - effectively finishing the test will do
	public void after() throws Exception {
		// NOTE: the method disable() is undefined for the type PerformanceTimeline
		// chromeDevTools.send(PerformanceTimeline.disable());
	}

	protected String getScriptContent(String scriptName) {
		try {
			final InputStream stream = this.getClass().getClassLoader().getResourceAsStream(scriptName);
			final byte[] bytes = new byte[stream.available()];
			stream.read(bytes);
			return new String(bytes, "UTF-8");
		} catch (IOException e) {
			throw new RuntimeException(scriptName);
		}
	}

	// http://www.javawithus.com/tutorial/using-ellipsis-to-accept-variable-number-of-arguments
	// see also:
	// https://github.com/handakumbura/Seleniumuntil/blob/master/src/main/java/io/github/handakumbura/JavaScriptHelper.java
	// for binding events listeners to DOM
	public Object executeScript(String script, Object... arguments) {
		if (driver instanceof JavascriptExecutor) {
			JavascriptExecutor javascriptExecutor = JavascriptExecutor.class.cast(driver);
			/*
			 *
			 * // currently unsafe err.println(arguments.length + " arguments received.");
			 * String argStr = "";
			 *
			 * for (int i = 0; i < arguments.length; i++) { argStr = argStr + " " +
			 * (arguments[i] == null ? "null" : arguments[i].toString()); }
			 *
			 * err.println("Calling " + script.substring(0, 40) + "..." + \n" + "with
			 * arguments: " + argStr);
			 */
			return javascriptExecutor.executeScript(script, arguments);
		} else {
			throw new RuntimeException("Script execution failed.");
		}
	}

	public Object executeScript(WebDriver driver, String script, Object... arguments) {
		if (driver instanceof JavascriptExecutor) {
			JavascriptExecutor javascriptExecutor = JavascriptExecutor.class.cast(driver);
			return javascriptExecutor.executeScript(script, arguments);
		} else {
			throw new RuntimeException("Script execution failed.");
		}
	}

}
