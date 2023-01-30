package com.github.sergueik.selenium;

/**
 * Copyright 2021-2023 Serguei Kouzmine
 */

import java.io.IOException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Paths;

import org.junit.AfterClass;
import org.junit.BeforeClass;
// need to use branch cdp_codegen of SeleniumHQ/selenium
// https://github.com/SeleniumHQ/selenium/tree/cdp_codegen/java/client/src/org/openqa/selenium/devtools
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.chromium.ChromiumDriver;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.HasDevTools;
import org.openqa.selenium.remote.DesiredCapabilities;

/**
 * Base class for selected test scenarios for Selenium 4 Chrome Developer Tools bridge
 *
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

public class BaseDevToolsTest {

	protected static boolean runHeadless = false;
	protected static String osName = Utils.getOSName();
	protected static ChromiumDriver driver;
	protected static DevTools chromeDevTools;
	protected static String baseURL = "about:blank";

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

			// see: http://barancev.github.io/slow-loading-pages/
			// https://stackoverflow.com/questions/43734797/page-load-strategy-for-chrome-driver-updated-till-selenium-v3-12-0
			DesiredCapabilities desiredCapabilities = new DesiredCapabilities();
			desiredCapabilities.setCapability("pageLoadStrategy", "eager");
			ChromeOptions options = new ChromeOptions();
			options.merge(desiredCapabilities);

		if (runHeadless) {
			options.addArguments("--headless", "--disable-gpu");
			// alternatively,
			// options.setHeadless(false)
		}
		driver = new ChromeDriver(options);

		Utils.setDriver(driver);

		chromeDevTools = ((HasDevTools) driver).getDevTools();

		chromeDevTools.createSession();
		// TODO: switch to
		// chromeDevTools.createSessionIfThereIsNotOne();
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
		chromeDevTools.close();
		if (driver != null) {
			driver.quit();
		}
	}

	// origin:
	// https://stackoverflow.com/questions/3584210/preferred-java-way-to-ping-an-http-url-for-availability
	public boolean pingHost(String host, int port, int timeout) {
		try (Socket socket = new Socket()) {
			System.err.println(
					String.format("Trying to connect to host %s port %d", host, port));
			socket.connect(new InetSocketAddress(host, port), timeout);
			return true;
		} catch (IOException e) {
			System.err.println("timeout or unreachable or failed DNS lookup.");
			return false; // timeout or unreachable or failed DNS lookup.
		}
	}
}
