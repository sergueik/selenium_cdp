package com.github.sergueik.selenium;
/**
 * Copyright 2023 Serguei Kouzmine
 */

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.core.IsNot.not;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Selected test scenarios for Selenium Chrome Developer Tools Selenium 4 bridge
 * https://chromedevtools.github.io/devtools-protocol/tot/Overlay/#method-enable
 * https://chromedevtools.github.io/devtools-protocol/1-2/DOM/#type-RGBA
 * https://chromedevtools.github.io/devtools-protocol/1-2/DOM/#method-highlightNode
 * https://chromedevtools.github.io/devtools-protocol/1-2/DOM/#type-HighlightConfig
 * https://chromedevtools.github.io/devtools-protocol/1-2/DOM/#method-hideHighlight
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

public class OverlayHighlightCDPTest extends BaseDevToolsTest {
	private static String url = "https://www.wikipedia.org";
	private static final String selector = "*[id^='js-link-box'] > strong";

	private static String command = null;
	private static Map<String, Object> result = new HashMap<>();
	private static Map<String, Object> params = new HashMap<>();
	private static Map<String, Object> data = new HashMap<>();
	private static Map<String, Object> data2 = new HashMap<>();
	private static List<Object> data3 = new ArrayList<>();
	private static String dataString = null;
	private static List<Map<String, Object>> cookies = new ArrayList<>();
	public static Long nodeId = (long) -1;
	public static String isolationId = null;

	private static By locator = null;
	private static String baseURL = "about:blank";

	@Before
	public void before() {
		command = "DOM.enable";
		params.clear();
		params.put("includeWhitespace", "all");
		driver.executeCdpCommand(command, params);
		command = "Overlay.enable";
		driver.executeCdpCommand(command, new HashMap<>());
		driver.get(url);
	}

	@After
	public void after() {
		command = "Overlay.disable";
		driver.executeCdpCommand(command, new HashMap<>());
		command = "DOM.disable";
		driver.executeCdpCommand(command, new HashMap<>());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void nodeHighlightTest() {

		String command = "DOM.getDocument";
		params = new HashMap<>();
		params.put("pierce", false);
		params.put("depth", 0);
		try {
			// Act
			result = driver.executeCdpCommand(command, params);
			nodeId = Long.parseLong(
					((Map<String, Object>) result.get("root")).get("nodeId").toString());
			command = "DOM.querySelectorAll";
			params.clear();
			params.put("nodeId", nodeId);
			params.put("selector", selector);

			result = driver.executeCdpCommand(command, params);
			assertThat(result, hasKey("nodeIds"));
			List<Long> results = (List<Long>) result.get("nodeIds");
			results.stream().forEach((Long nodeId) -> {
				assertThat(nodeId, is(not(0)));
				System.err.println("nodeId: " + nodeId);

				data.clear();
				data.put("showInfo", true);
				data.put("showRulers", true);
				// DOM.RGBA
				data2 = new HashMap<>();
				data2.put("r", "255");
				data2.put("g", "128");
				data2.put("b", "64");
				data.put("contentColor", data2);
				data2.clear();
				data2.put("r", 64);
				data2.put("g", 128);
				data2.put("b", 64);

				data.put("marginColor", data2);
				data2.clear();
				data2.put("r", 0);
				data2.put("g", 0);
				data2.put("b", 128);
				data.put("borderColor", data2);
				params.clear();
				params.put("nodeId", nodeId);
				params.put("highlightConfig", data);
				dataString = null;
				result = driver.executeCdpCommand("DOM.highlightNode", params);
				params.clear();
				params.put("nodeId", nodeId);
				dataString = null;
				result = driver.executeCdpCommand("DOM.getOuterHTML", params);
				assertThat(result, notNullValue());
				assertThat(result, hasKey("outerHTML"));
				dataString = (String) result.get("outerHTML");
				assertThat(dataString, notNullValue());
				System.err.println("outerHTML: " + dataString);

				Utils.sleep(500);

				params.clear();
				result = driver.executeCdpCommand("DOM.hideHighlight", params);
			});
		} catch (WebDriverException e) {
			System.err.println("Web Driver exception in " + command + " (ignored): "
					+ Utils.processExceptionMessage(e.getMessage()));
		} catch (UnsupportedOperationException e) {
			System.err.println("UnsupportedOperation exception in " + command
					+ " (ignored): " + e.getMessage());
		} catch (Exception e) {
			System.err.println("Exception in " + command + "  " + e.toString());
			throw (new RuntimeException(e));
		}
	}

}
