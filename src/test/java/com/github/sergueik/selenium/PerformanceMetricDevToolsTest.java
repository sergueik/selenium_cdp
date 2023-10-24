package com.github.sergueik.selenium;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.greaterThan;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.devtools.v118.performance.Performance;
import org.openqa.selenium.devtools.v118.performance.model.Metric;


/**
 * Selected test scenarios for Selenium Chrome Developer Tools Selenium 4 bridge
 * see:
 * https://developer.mozilla.org/en-US/docs/Web/API/PerformanceNavigationTiming
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

public class PerformanceMetricDevToolsTest extends BaseDevToolsTest {

	private static List<Metric> metrics;
	private static HashSet<Object> metricKeys = new HashSet<>();
	private static String[] standardKeys = { "AdSubframes", "ArrayBufferContents",
			"AudioHandlers", "ContextLifecycleStateObservers", "DetachedScriptStates",
			"DevToolsCommandDuration", "Documents", "DomContentLoaded",
			"FirstMeaningfulPaint", "Frames", "JSEventListeners", "JSHeapTotalSize",
			"JSHeapUsedSize", "LayoutCount", "LayoutDuration", "LayoutObjects",
			"MediaKeySessions", "MediaKeys", "NavigationStart", "Nodes",
			"ProcessTime", "RTCPeerConnections", "RecalcStyleCount",
			"RecalcStyleDuration", "ResourceFetchers", "Resources", "ScriptDuration",
			"TaskDuration", "TaskOtherDuration", "ThreadTime", "Timestamp",
			"UACSSResources", "V8CompileDuration", "V8PerContextDatas",
			"WorkerGlobalScopes" };
	private static String baseURL = "https://developer.mozilla.org/en-US/docs/Web/API/PerformanceNavigationTiming";

	@Before
	public void before() throws Exception {
		// Arrange
		chromeDevTools.send(Performance.enable(Optional.empty()));
	}

	@Test
	public void test1() {
		// Arrange
		driver.get(baseURL);

		// Act
		metrics = chromeDevTools.send(Performance.getMetrics());
		// Assert
		assertThat(metrics, notNullValue());
		assertThat(metrics.size(), greaterThan(0));
		metrics.stream().map((Metric metric) -> String.format("%s: %s",
				metric.getName(), metric.getValue())).forEach(System.err::println);
		metrics
				.forEach((Metric metric) -> metricKeys.add((Object) metric.getName()));
		assertThat("Checking performance metrics", metricKeys,
				containsInAnyOrder((Object[]) standardKeys));

	}

	// slightly alternative key set validation
	// origin:
	// https://github.com/fugazi/carbonfour-selenium-4/blob/main/src/test/java/Selenium_4_Tests/TestDevToolsPerformance.java
	@Test
	public void test2() {
		// Arrange
		driver.get(baseURL);

		metrics = chromeDevTools.send(Performance.getMetrics());
		// Assert
		assertThat(metrics, notNullValue());
		// Act
		List<String> metricNames = metrics.stream().map(Metric::getName)
				.collect(Collectors.toList());
		System.err.println("MetricNames: " + metricNames);
		List<String> keyMetrics = Arrays.asList("Timestamp", "Documents", "Frames",
				"JSEventListeners", "Nodes", "LayoutCount", "RecalcStyleCount",
				"RecalcStyleDuration", "LayoutDuration", "MediaKeySessions",
				"Resources", "DomContentLoaded", "NavigationStart", "TaskDuration",
				"JSHeapUsedSize", "JSHeapTotalSize", "ScriptDuration");
		keyMetrics.forEach(metric -> System.err.println("Metric: " + metric + "\n"
				+ metrics.get(metricNames.indexOf(metric)).getValue()));
		// NOTE: hamcrest does not have "containsAll"
		String[] keyMetricsArray = new String[keyMetrics.size()];
		keyMetrics.toArray(keyMetricsArray);
		assertThat(CollectionUtils.containsAny(metricNames, keyMetrics), is(true));
		// does not validate in the "correct" order - commented
		// assertThat("Checking key performance metrics", metricNames,
		//		containsInAnyOrder(keyMetricsArray));

	}

	// TODO:
	// https://github.com/fugazi/carbonfour-selenium-4/blob/main/src/test/java/Selenium_4_Tests/TestLoginRelativeLocators.java
	@After
	// Disables performance tracking
	public void afterTest() throws Exception {
		chromeDevTools.send(Performance.disable());
	}

}
