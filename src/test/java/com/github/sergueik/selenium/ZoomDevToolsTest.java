package com.github.sergueik.selenium;

/**
 * Copyright 2023,2024 Serguei Kouzmine
 */

import java.nio.file.Paths;
import java.util.Optional;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
// import org.junit.Ignore;

// import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang.SystemUtils;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.CoreMatchers.startsWith;


import org.junit.Test;
// https://github.com/SeleniumHQ/selenium/tree/cdp_codegen/java/client/src/org/openqa/selenium/devtools

import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.HasDevTools;
import org.openqa.selenium.devtools.v134.input.Input;
import org.openqa.selenium.devtools.v134.input.Input.DispatchKeyEventType;

/**
 * Selected test scenarios for Selenium Chrome Developer Tools Selenium 4 bridge
 * https://chromedevtools.github.io/devtools-protocol/tot/Input/#method-dispatchKeyEvent * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 * see also:
 * https://github.com/ChromeDevTools/devtools-protocol/issues/74
 * https://github.com/puppeteer/puppeteer/blob/main/src/common/Input.ts#L118
 */

public class ZoomDevToolsTest extends BaseDevToolsTest {

	private final static int delay = 1000;
	private static final int modifiers = 2;
	// Bit field representing pressed modifier keys. Alt=1, Ctrl=2,
	// Meta/Command=4, Shift=8 (default: 0)
	// NOTE: 2 has no effect
	private static String baseURL = "https://www.wikipedia.org";

	@Before
	// https://chromedevtools.github.io/devtools-protocol/tot/Console#method-enable
	// https://chromedevtools.github.io/devtools-protocol/tot/Log#method-enable
	public void before() throws Exception {
		// https://www.baeldung.com/junit-conditional-assume
		Assume.assumeThat(System.getProperty("os.name"), startsWith("Windows"));
		Assume.assumeThat(Integer.parseInt(System.getProperty("os.version").split("\\.")[0]), greaterThan(9));
		getWindowsVersion();
		driver.get(baseURL);
	}

	@After
	public void after() {
		if (driver != null) {
			driver.quit();
		}
	}

	@Test
	public void test() {
		// Act
		for (int cnt = 0; cnt != 5; cnt++) {

			chromeDevTools.send(Input.dispatchKeyEvent(DispatchKeyEventType.KEYDOWN,
					Optional.of(modifiers), Optional.empty(), Optional.of("-"),
					Optional.empty(), Optional.empty(), Optional.empty(),
					Optional.empty(), Optional.empty(), Optional.empty(),
					Optional.empty(), Optional.empty(), Optional.empty(),
					Optional.empty(), Optional.empty()));

			chromeDevTools.send(Input.dispatchKeyEvent(DispatchKeyEventType.KEYUP,
					Optional.of(modifiers), Optional.empty(), Optional.empty(),
					Optional.	empty(), Optional.empty(), Optional.empty(),
					Optional.empty(), Optional.empty(), Optional.empty(),
					Optional.empty(), Optional.empty(), Optional.empty(),
					Optional.empty(), Optional.empty()));

			Utils.sleep(delay);
		}
	}
	public static void getWindowsVersion() {
		String osName = System.getProperty("os.name");
		String osVersion = System.getProperty("os.version");
		System.out.println("Operating System Name: " + osName);
		System.out.println("Operating System Version: " + osVersion);
		String os = SystemUtils.OS_NAME;
		System.out.println("Using SystemUtils: " + os);
	}

}

