package com.github.sergueik.selenium;

/* Copyright 2023 Serguei Kouzmine */

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.devtools.v122.dom.model.Rect;
import org.openqa.selenium.devtools.v122.emulation.Emulation;
import org.openqa.selenium.devtools.v122.page.Page;
import org.openqa.selenium.interactions.Actions;

/**
 * Selected test scenarios for Selenium Chrome Developer Tools Selenium 4 bridge
 * see also:
 * https://chromedevtools.github.io/devtools-protocol/tot/Page/#method-getLayoutMetrics
 * https://chromedevtools.github.io/devtools-protocol/tot/Emulation/#method-setDeviceMetricsOverride
 * https://chromedevtools.github.io/devtools-protocol/tot/Page#method-captureScreenshot
 * https://chromedevtools.github.io/devtools-protocol/tot/Emulation/#method-clearDeviceMetricsOverride
 *
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

public class DeviceMetricsOverrideDevToolsTest extends BaseDevToolsTest {
	private static Page.GetLayoutMetricsResponse result;
	private static Rect rect;
	private static int delay = 300;

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
	public void before() throws Exception {
	}

	@After
	public void clearPage() {
		chromeDevTools.send(Emulation.clearDeviceMetricsOverride());
	}

	@Test
	public void test1() {
		for (Integer device_width : widths.keySet()) {
			Integer viewport_width = widths.get(device_width);

			chromeDevTools.send(
				// @formatter:off
				Emulation.setDeviceMetricsOverride(
						device_width, 
					height,
					0,  // deviceScaleFactor
					true, // mobile
					Optional.empty(), // scale 
					Optional.empty(), // screenWidth
					Optional.empty(), // screenHeight
					Optional.empty(), // positionX
					Optional.empty(), // positionY
					Optional.empty(), // dontSetVisibleSize
					Optional.empty(), // screenOrientation
					Optional.empty(), // viewport 
					Optional.empty(),  // displayFeature
					Optional.empty() // devicePosture
				)
				// @formatter:on	
			);
			driver.get(baseURL);
			result = chromeDevTools.send(Page.getLayoutMetrics());
			rect = result.getContentSize();
			System.err.println("Page.getLayoutMetrics: " + rect);

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
