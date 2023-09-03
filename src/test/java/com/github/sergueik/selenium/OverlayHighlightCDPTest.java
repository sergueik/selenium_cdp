package com.github.sergueik.selenium;
/**
 * Copyright 2023 Serguei Kouzmine
 */

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.core.IsNot.not;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebDriverException;

/**
 * Selected test scenarios for Selenium Chrome Developer Tools Selenium 4 bridge
 * https://chromedevtools.github.io/devtools-protocol/tot/Overlay/#method-enable
 * https://chromedevtools.github.io/devtools-protocol/tot/DOM/#type-RGBA
 * https://chromedevtools.github.io/devtools-protocol/tot/Overlay/#method-highlightNode
 * https://chromedevtools.github.io/devtools-protocol/tot/Overlay/#type-HighlightConfig
 * https://chromedevtools.github.io/devtools-protocol/tot/Overlay/#method-hideHighlight
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

public class OverlayHighlightCDPTest extends BaseDevToolsTest {
	private static String url = "https://www.wikipedia.org";
	private static final String selector = "*[id^='js-link-box'] > strong";

	private static String command = null;
	private static Map<String, Object> result = new HashMap<>();
	private static Map<String, Object> params = new HashMap<>();
	private static Map<String, Object> data = new HashMap<>();
	private static String dataString = null;
	public static Long nodeId = (long) -1;
	public static String isolationId = null;

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

				Map<String, Object> contentColor = new HashMap<>();
				contentColor.put("r", 128);
				contentColor.put("g", 192);
				contentColor.put("b", 255);
				contentColor.put("a", 0.5);
				data.put("contentColor", contentColor);

				Map<String, Object> marginColor = new HashMap<>();
				marginColor.clear();
				marginColor.put("r", 64);
				marginColor.put("g", 128);
				marginColor.put("b", 64);
				contentColor.put("a", 0.5);
				data.put("marginColor", marginColor);

				Map<String, Object> borderColor = new HashMap<>();
				borderColor.clear();
				borderColor.put("r", 0);
				borderColor.put("g", 0);
				borderColor.put("b", 128);
				data.put("borderColor", borderColor);

				params.clear();
				params.put("nodeId", nodeId);
				params.put("highlightConfig", data);
				dataString = null;
				result = driver.executeCdpCommand("DOM.highlightNode", params);
				params.clear();
				params.put("nodeId", nodeId);
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
