package com.github.sergueik.selenium;

import static java.lang.System.err;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasKey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.openqa.selenium.WebDriverException;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.arrayContainingInAnyOrder;

/**
 * Selected test scenarios for Selenium 4 Chrome Developer Tools bridge
 * https://chromedevtools.github.io/devtools-protocol/tot/Performance#method-setTimeDomain
 * https://chromedevtools.github.io/devtools-protocol/tot/Performance#method-enable
 * https://chromedevtools.github.io/devtools-protocol/tot/Performance#method-getMetrics
 * https://chromedevtools.github.io/devtools-protocol/tot/Performance/#type-Metric
 * 
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

public class PerformanceMetricsCdpTest extends BaseCdpTest {

	private static String command = null;
	private static Map<String, Object> result = new HashMap<>();
	private static Map<String, Object> params = new HashMap<>();
	private static List<Object> metrics = new ArrayList<>();
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

	@SuppressWarnings("unchecked")
	@Test
	// based on:
	// https://github.com/SrinivasanTarget/selenium4CDPsamples/blob/master/src/test/java/DevToolsTest.java
	// see also:
	// https://github.com/ShamaUgale/Selenium4Examples/blob/master/src/main/java/com/devtools/GetMetrics.java
	public void getPerformanceMetricsTest() {
		command = "Performance.setTimeDomain";
		params.put("timeDomain", "timeTicks");
		try {
			driver.executeCdpCommand(command, params);
			// Act
			command = "Performance.enable";
			driver.executeCdpCommand(command, new HashMap<>());
			baseURL = "https://developer.mozilla.org/en-US/docs/Web/API/PerformancePaintTiming";
			driver.get(baseURL);
			// Act
			command = "Performance.getMetrics";
			result = driver.executeCdpCommand(command, new HashMap<>());
			// Assert
			assertThat(result, notNullValue());
			assertThat(result, hasKey("metrics"));
			metrics = (List<Object>) result.get("metrics");
			assertThat(metrics, notNullValue());
			assertThat(metrics.size(), greaterThan(0));
			System.err.println("Metric dimension: " + metrics.size());
			for (int cnt = 0; cnt != metrics.size(); cnt++) {
				Object metricEntry = metrics.get(cnt);
				assertThat(metricEntry, notNullValue());
				Map<String, Integer> metricData = (Map<String, Integer>) metricEntry;
				assertThat(metricData, hasKey("name"));
				assertThat(metricData, hasKey("value"));
				System.err.println(String.format("%s: %s", metricData.get("name"),
						metricData.get("value")));
				metricKeys.add((Object) metricData.get("name"));
			}
			assertThat("Checking example array and direct arguments", metricKeys,
					containsInAnyOrder((Object[]) standardKeys));

			// Act
			command = "Performance.disable";
			driver.executeCdpCommand(command, new HashMap<>());
		} catch (WebDriverException e) {
			err.println("Exception in command " + command + " (ignored): "
					+ Utils.processExceptionMessage(e.getMessage()));
		} catch (Exception e) {
			err.println("Exception: in " + command + "  " + e.toString());
			throw (new RuntimeException(e));
		}
	}
}
