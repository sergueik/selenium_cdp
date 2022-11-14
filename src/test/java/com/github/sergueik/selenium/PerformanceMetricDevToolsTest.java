package com.github.sergueik.selenium;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.greaterThan;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.devtools.v107.page.Page;
import org.openqa.selenium.devtools.v107.performance.Performance;
import org.openqa.selenium.devtools.v107.performance.model.Metric;

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

	@Before
	public void before() throws Exception {
		// Arrange
		chromeDevTools.send(Performance.enable(Optional.empty()));
	}

	@Test
	public void test() {
		// Act
		baseURL = "https://developer.mozilla.org/en-US/docs/Web/API/PerformanceNavigationTiming";
		driver.get(baseURL);

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

	@After
	public void afterTest() throws Exception {
		chromeDevTools.send(Performance.disable());
	}

}
