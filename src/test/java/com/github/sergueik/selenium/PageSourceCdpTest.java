package com.github.sergueik.selenium;

import static java.lang.System.err;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebDriverException;

/**
 * Selected test scenarios for Selenium 4 Chrome Developer Tools bridge
 * https://chromedevtools.github.io/devtools-protocol/tot/DOM/#method-enable
 * https://chromedevtools.github.io/devtools-protocol/tot/DOM/#method-getDocument 
 * https://chromedevtools.github.io/devtools-protocol/tot/DOM/#method-getOuterHTML
 * https://chromedevtools.github.io/devtools-protocol/tot/DOM/#method-disable
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

// https://github.com/estromenko/driverless-selenium/blob/init-project/driverless_selenium/webdriver.py#L183
public class PageSourceCdpTest extends BaseCdpTest {

	private static String command = "Browser.setDownloadBehavior";
	private static String url = "https://www.wikipedia.org";
	private static Map<String, Object> result = new HashMap<>();
	private static Map<String, Object> params = new HashMap<>();
	public static Long nodeId = (long) -1;

	@Before
	public void before() {
		command = "DOM.enable";
		driver.executeCdpCommand(command, new HashMap<>());
		driver.get(url);
	}

	@Test
	public void test() {

		// Arrange
		baseURL = "https://www.wikipedia.org";
		driver.get(baseURL);
		String command = "DOM.getDocument";
		params = new HashMap<>();
		params.put("pierce", false);
		params.put("depth", 1);
		result = driver.executeCdpCommand(command, params);
		nodeId = Long.parseLong(
				((Map<String, Object>) result.get("root")).get("nodeId").toString());
		command = "DOM.getOuterHTML";
		params.clear();
		params.put("nodeId", nodeId);
		// Act
		result = driver.executeCdpCommand(command, params);
		String pageSource = (String) result.get("outerHTML");
		System.err.println("page source: " + pageSource);
	}

	@After
	public void after() {
		command = "DOM.disable";
		try {
			// Act
			driver.executeCdpCommand(command, new HashMap<>());
		} catch (Exception e) {
			err.println("Exception: in " + command + " (ignored): " + e.toString());
		}
	}

}
