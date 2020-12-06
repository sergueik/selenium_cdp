package com.github.sergueik.selenium;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
// need to use branch cdp_codegen of SeleniumHQ/selenium
// https://github.com/SeleniumHQ/selenium/tree/cdp_codegen/java/client/src/org/openqa/selenium/devtools
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.chromium.ChromiumDriver;
import org.openqa.selenium.devtools.DevTools;
//import org.openqa.selenium.devtools.Console;
// import org.openqa.selenium.devtools.Log;
import org.openqa.selenium.devtools.network.Network;
import org.openqa.selenium.devtools.network.model.Headers;

/**
 * Selected test scenarios for Selenium Chrome Developer Tools Selenium 4 bridge
 * based on:
 * https://github.com/adiohana/selenium-chrome-devtools-examples/blob/master/src/test/java/ChromeDevToolsTest.java
 * https://codoid.com/selenium-4-chrome-devtools-log-entry-listeners/ etc. NOTE:
 * https://chromedevtools.github.io/devtools-protocol/tot/Console/ says The
 * Console domain is deprecated - use Runtime or Log instead.
 *
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

public class NetworkDevToolsTest {

	private static boolean runHeadless = false;
	private static String osName = Utils.getOSName();
	private static ChromiumDriver driver;
	private static DevTools chromeDevTools;
	private int cnt = 0;
	private static String baseURL = "about:blank";

	private static Map<String, Object> headers = new HashMap<>();

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
		chromeDevTools = driver.getDevTools();
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

	// https://chromedevtools.github.io/devtools-protocol/tot/Network/#method-setExtraHTTPHeaders
	// see also:
	// https://stackoverflow.com/questions/15645093/setting-request-headers-in-selenium
	// see also:
	// https://github.com/SeleniumHQ/selenium/blob/master/java/client/test/org/openqa/selenium/devtools/ChromeDevToolsNetworkTest.java
	// @Ignore
	@Test
	public void addCustomHeadersTest() {
		// enable Network
		chromeDevTools.send(
				Network.enable(Optional.empty(), Optional.empty(), Optional.empty()));
		headers = new HashMap<>();
		headers.put("customHeaderName", "customHeaderValue");
		/*
		headers.put("customHeaderName",
				this.getClass().getName() + " addCustomHeadersTest");
				*/
		Headers headersData = new Headers(headers);
		chromeDevTools.send(Network.setExtraHTTPHeaders(headersData));
		// add event listener to log that requests are sending with the custom
		// header
		chromeDevTools.addListener(Network.requestWillBeSent(),
				o -> assertThat(o.getRequest().getHeaders().get("customHeaderName"),
						is("customHeaderValue")));
		chromeDevTools.addListener(Network.requestWillBeSent(),
				o -> System.err.println("addCustomHeaders Listener invoked with "
						+ o.getRequest().getHeaders().get("customHeaderName")));
		// to test with a dummy server fire on locally and inspect the headers
		// server-side
		// driver.get("http://127.0.0.1:8080/demo/Demo");
		// otherwise just hit a generic web site
		driver.get("https://apache.org");
	}

	// https://chromedevtools.github.io/devtools-protocol/tot/Network#method-enable
	// https://chromedevtools.github.io/devtools-protocol/1-2/Network/#event-dataReceived
	@Test
	public void eventTest() {
		chromeDevTools.send(Network.enable(Optional.of(100000000), Optional.empty(),
				Optional.empty()));
		chromeDevTools.addListener(Network.dataReceived(), o -> {
			if (cnt++ < 10)
				System.err
						.println(String.format("Network request %s data received at %s",
								o.getRequestId(), o.getTimestamp()));
		});
		// https://chromedevtools.github.io/devtools-protocol/1-2/Network/#method-setCacheDisabled
		chromeDevTools.send(Network.setCacheDisabled(true));
		driver.get("https://apache.org");

	}
}

