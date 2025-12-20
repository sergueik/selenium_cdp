package com.github.sergueik.selenium;

/* Copyright 2023,2024 Serguei Kouzmine */

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

/**
 * Selected test scenarios for Selenium 4 Chrome Developer Tools bridge
 * https://chromedevtools.github.io/devtools-protocol/tot/Page/#method-getLayoutMetrics
 * https://chromedevtools.github.io/devtools-protocol/tot/Emulation/#method-setDeviceMetricsOverride
 * https://chromedevtools.github.io/devtools-protocol/tot/Emulation/#method-clearDeviceMetricsOverride
 *
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

public class DeviceMetricsOverrideCdpTest extends BaseCdpTest {

	private static int delay = 300;

	private static String command = null;
	private static Map<String, Object> result = new HashMap<>();
	private static Map<String, Object> params = new HashMap<>();

	public static Long nodeId = (long) -1;

	private static String baseURL = "https://www.whatismybrowser.com/detect/what-http-headers-is-my-browser-sending";
	private Actions actions;
	private WebElement element;

	private static Map<Integer, Integer> widths = new HashMap<>();
	static {
		widths.put(600, 480);
		widths.put(480, 384);
		widths.put(250, 200);
	}
	private static Integer height = 640;

	@Before
	public void beforeTest() throws Exception {
		driver.get("about:blank");
	}

	@After
	public void clearDeviceMetricsOverride() {
		command = "Emulation.clearDeviceMetricsOverride";
		driver.executeCdpCommand(command, new HashMap<String, Object>());
		// Assert
	}

	@SuppressWarnings("unchecked")
	@Test
	public void test() {
		// Assert
		params = new HashMap<>();

		for (Integer device_width : widths.keySet()) {
			Integer viewport_width = widths.get(device_width);
			command = "Emulation.setDeviceMetricsOverride";
			// Arrange
			params.clear();
			params.put("deviceScaleFactor", 0);
			params.put("width", device_width);
			params.put("height", height);
			params.put("mobile", true);
			System.err.println(String.format("Set device metrics to %02d %02d",
					viewport_width, height));
			params.put("scale", 1);
			driver.executeCdpCommand(command, params);
			// Act
			driver.get(baseURL);
			command = "Page.getLayoutMetrics";
			result = driver.executeCdpCommand(command, new HashMap<>());
			System.err.println("Page.getLayoutMetrics: " + result.get("contentSize"));
			Map<String, Long> rect = (Map<String, Long>) result.get("contentSize");
			element = driver.findElement(By.xpath(
					"//*[@id=\"content-base\"]//table//th[contains(text(),\"VIEWPORT-WIDTH\")]/../td"));
			assertThat(element, notNullValue());
			assertThat(element.getText(),
					containsString(String.format("%d", viewport_width)));
			actions = new Actions(driver);
			actions.moveToElement(element).build().perform();

			Utils.highlight(element);
			Utils.sleep(delay);
		}
	}
}
