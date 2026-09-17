package com.github.sergueik.selenium;

/**
 * Copyright 2023,2024 Serguei Kouzmine
 */

import org.openqa.selenium.devtools.DevToolsException;
import org.openqa.selenium.WebDriverException;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.devtools.v153.extensions.Extensions;
import org.openqa.selenium.devtools.v153.extensions.model.ExtensionInfo;
import org.openqa.selenium.devtools.v153.network.model.TimeSinceEpoch;
import org.openqa.selenium.devtools.v153.performancetimeline.PerformanceTimeline;
import org.openqa.selenium.devtools.v153.performancetimeline.model.TimelineEvent;

/**
 * Selected test scenarios for Selenium Chrome Developer Tools Selenium 4 bridge
 * see: https://chromedevtools.github.io/devtools-protocol/#/PerformanceTimeline
 * https://w3c.github.io/performance-timeline/#dom-performanceentry-entrytype
 * https://github.com/w3c/performance-timeline
 * https://github.com/mdn/content/issues/2667 -
 * `PerformanceObserver.supportedEntryTypes` is undocumented
 * https://github.com/mdn/content/commit/08d338941a1667a284ea446fa29eb394ca39a136
 * https://developer.mozilla.org/en-US/docs/Web/API/PerformanceEntry
 * https://developer.mozilla.org/en-US/docs/Web/API/PerformanceObserver/supportedEntryTypes_static
 *
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

public class PerformanceTimelineDevToolsTest extends BaseDevToolsTest {

	private static HashSet<Object> metricKeys = new HashSet<>();
	// @formatter:off
	private static String[] supportedEntryTypes = {
		"element",
		"event",
		"first-input",
		"largest-contentful-paint",
		"layout-shift",
		"long-animation-frame",
		"longtask",
		"mark",
		"measure",
		"navigation",
		"paint",
		"resource",
		"visibility-state"
	};
	// @formatter:on

	private static String baseURL = "https://developer.mozilla.org/en-US/docs/Web/API/PerformanceEntry";

	private static Gson gson = new Gson();

	@Before
	public void before() throws Exception {
		// Arrange
	}

	@Test(expected = DevToolsException.class)
	public void test1() {

		// Arrange
		try {
			chromeDevTools.send(PerformanceTimeline.enable(Arrays.asList(supportedEntryTypes)));
			chromeDevTools.addListener(PerformanceTimeline.timelineEventAdded(), (

					timelineEvent) -> {
				String name = timelineEvent.getName();
				TimeSinceEpoch timeSinceEpoch = timelineEvent.getTime();
				System.err.println(String.format("Event %s added: %d", name, timeSinceEpoch));
			});
		} catch (DevToolsException e) {
			// TODO: find
			// org.openqa.selenium.devtools.DevToolsException:
			// {"id":5,"error":{"code":-32602,"message":"Unknown or unsupported entry
			// type"},"sessionId":"F9AC510B3011602B7F7019CB34F6C575"}
			String message = e.getCause().getMessage();
			String typeName = e.getCause().getClass().getTypeName();
			WebDriverException webDriverException = (WebDriverException) e.getCause();
			String rawMessage = webDriverException.getRawMessage();
			System.err.println("DevToolsException message: " + message);
			try {
				Map<String, Object> data = gson.fromJson(rawMessage, Map.class);
				System.err.println(String.format("WebDriverException message: %s", data.get("error")));
			} catch (JsonSyntaxException e2) {
				System.err.println(
						String.format("Could not parse WebDriverException raw message json: %s", e2.getMessage()));
			}
			throw e;
		}
	}

	@After
	// Disables performance timeline - effectively finishing the test will do
	public void after() throws Exception {
		// NOTE: the method disable() is undefined for the type PerformanceTimeline
		// chromeDevTools.send(PerformanceTimeline.disable());
	}

}
