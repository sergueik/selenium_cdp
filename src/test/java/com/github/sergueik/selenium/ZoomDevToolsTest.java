package com.github.sergueik.selenium;

/**
 * Copyright 2023 Serguei Kouzmine
 */

import java.nio.file.Paths;
import java.util.Optional;

import org.junit.AfterClass;
import org.junit.BeforeClass;
// import org.junit.Ignore;
import org.junit.Test;
// https://github.com/SeleniumHQ/selenium/tree/cdp_codegen/java/client/src/org/openqa/selenium/devtools

import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.HasDevTools;
import org.openqa.selenium.devtools.v122.input.Input;
import org.openqa.selenium.devtools.v122.input.Input.DispatchKeyEventType;

/**
 * Selected test scenarios for Selenium Chrome Developer Tools Selenium 4 bridge
 * https://chromedevtools.github.io/devtools-protocol/tot/Input/#method-dispatchKeyEvent * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 * see also:
 * https://github.com/ChromeDevTools/devtools-protocol/issues/74
 * https://github.com/puppeteer/puppeteer/blob/main/src/common/Input.ts#L118
 */

public class ZoomDevToolsTest {

	private final static int delay = 1000;
	private static boolean runHeadless = false;
	private static String osName = Utils.getOSName();
	private static ChromeDriver driver;
	private static DevTools chromeDevTools;
	private static final int modifiers = 2;
	// Bit field representing pressed modifier keys. Alt=1, Ctrl=2,
	// Meta/Command=4, Shift=8 (default: 0)
	// NOTE: 2 has no effect
	private static String baseURL = "https://www.wikipedia.org";

	@BeforeClass
	public static void setUp() throws Exception {

		if (System.getenv().containsKey("HEADLESS")
				&& System.getenv("HEADLESS").matches("(?:true|yes|1)")) {
			runHeadless = true;
		}
		// force the headless flag to be true to support Unix console execution
		if (!(Utils.getOSName().equals("windows"))
				&& !(System.getenv().containsKey("DISPLAY"))) {
			runHeadless = true;
		}
		System
				.setProperty("webdriver.chrome.driver",
						Paths.get(System.getProperty("user.home"))
								.resolve("Downloads").resolve(osName.equals("windows")
										? "chromedriver.exe" : "chromedriver")
								.toAbsolutePath().toString());

		if (runHeadless) {
			ChromeOptions options = new ChromeOptions();
			options.addArguments("--headless", "--disable-gpu");
			driver = new ChromeDriver(options);
		} else {
			driver = new ChromeDriver();
		}
		Utils.setDriver(driver);
		chromeDevTools = ((HasDevTools) driver).getDevTools();
		// https://www.baeldung.com/java-static-default-methods#:~:text=Why%20Default%20Methods%20in%20Interfaces,and%20they%20provide%20an%20implementation
		chromeDevTools.createSession();
	}

	@BeforeClass
	// https://chromedevtools.github.io/devtools-protocol/tot/Console#method-enable
	// https://chromedevtools.github.io/devtools-protocol/tot/Log#method-enable
	public static void beforeClass() throws Exception {
		// NOTE:
		// the github location of package org.openqa.selenium.devtools.console
		// is uncertain
		// enable Console
		// chromeDevTools.send(Log.enable());
		// add event listener to show in host console the browser console message
		// chromeDevTools.addListener(Log.entryAdded(), System.err::println);
		driver.get(baseURL);
	}

	@AfterClass
	public static void tearDown() {
		if (driver != null) {
			driver.quit();
		}
	}

	@Test
	public void zoomTest() {
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
					Optional.empty(), Optional.empty(), Optional.empty(),
					Optional.empty(), Optional.empty(), Optional.empty(),
					Optional.empty(), Optional.empty(), Optional.empty(),
					Optional.empty(), Optional.empty()));

			Utils.sleep(delay);
		}
	}
}
