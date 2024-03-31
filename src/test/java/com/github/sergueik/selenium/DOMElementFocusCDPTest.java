package com.github.sergueik.selenium;
/**
 * Copyright 2023,2024 Serguei Kouzmine
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
 * https://chromedevtools.github.io/devtools-protocol/tot/Overlay/#method-disable
 * https://chromedevtools.github.io/devtools-protocol/tot/DOM/#method-enable
 * https://chromedevtools.github.io/devtools-protocol/tot/DOM/#method-disable
 * https://chromedevtools.github.io/devtools-protocol/tot/DOM/#method-focus
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

public class DOMElementFocusCDPTest extends BaseDevToolsTest {
	private static String url = "https://formy-project.herokuapp.com/form";
	private static final String selector = "input";

	private static String command = null;
	private static Map<String, Object> result = new HashMap<>();
	private static Map<String, Object> params = new HashMap<>();
	private static Map<String, Object> data = new HashMap<>();
	private static String dataString = null;
	public static Long nodeId = -1l;
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
	public void test() {

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

				params.clear();
				params.put("nodeId", nodeId);
				driver.executeCdpCommand("DOM.focus", params);
				result = driver.executeCdpCommand("DOM.getOuterHTML", params);
				assertThat(result, notNullValue());
				assertThat(result, hasKey("outerHTML"));
				dataString = (String) result.get("outerHTML");
				assertThat(dataString, notNullValue());
				System.err.println("Highlighted outerHTML: " + dataString);

				Utils.sleep(500);
				driver.executeCdpCommand("DOM.hideHighlight", new HashMap<>());
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
