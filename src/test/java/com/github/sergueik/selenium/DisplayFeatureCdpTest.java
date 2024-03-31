package com.github.sergueik.selenium;

/**
 * Copyright 2023,2024 Serguei Kouzmine
 */


import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.openqa.selenium.UnsupportedCommandException;

/**
 * Selected test scenarios for Selenium 4 Chrome Developer Tools bridge
 * https://chromedevtools.github.io/devtools-protocol/tot/Emulation/#type-DisplayFeature
 * https://chromedevtools.github.io/devtools-protocol/tot/Emulation/#type-ScreenOrientation
 * NOTE: both are unsupported
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */
public class DisplayFeatureCdpTest extends BaseCdpTest {
	private static String baseURL = "https://www.wikipedia.org";
	private static String command = null;
	private static Map<String, Object> params = new HashMap<>();

	@Test(expected = UnsupportedCommandException.class)
	public void test1() throws UnsupportedEncodingException {
		// Arrange
		params = new HashMap<>();
		params.put("orientation", Arrays.asList("horizontal"));
		command = "Emulation.DisplayFeature";
		driver.executeCdpCommand(command, params);
		// Act
		driver.get(baseURL);
	}

	@Test(expected = UnsupportedCommandException.class)
	public void test2() throws UnsupportedEncodingException {
		// Arrange
		params = new HashMap<>();
		params.put("orientation", Arrays.asList("landscapePrimary"));
		command = "Emulation.ScreenOrientation";
		driver.executeCdpCommand(command, params);
		// Act
		driver.get(baseURL);
	}

}
