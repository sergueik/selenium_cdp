package com.github.sergueik.selenium;

import java.nio.file.Paths;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
// need to use branch cdp_codegen of SeleniumHQ/selenium
// https://github.com/SeleniumHQ/selenium/tree/cdp_codegen/java/client/src/org/openqa/selenium/devtools
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chromium.ChromiumDriver;
// import org.openqa.selenium.devtools.Console;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.Log;
import org.openqa.selenium.devtools.network.Network;

import org.openqa.selenium.devtools.network.model.BlockedReason;
import org.openqa.selenium.devtools.network.model.InterceptionStage;
import org.openqa.selenium.devtools.network.model.RequestPattern;
import org.openqa.selenium.devtools.network.model.ResourceType;
import org.openqa.selenium.devtools.security.Security;

// as with selenium-devtools-4.0.0-alpha-3.jar the page.Page is not public yet
// nor is the handleJavaScriptDialog API proxy
// import org.openqa.selenium.devtools.page.Page;

import org.openqa.selenium.json.JsonInput;

import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Selected test scenarios for Selenium Chrome Developer Tools Selenium 4 bridge
 * based on:
 * https://github.com/adiohana/selenium-chrome-devtools-examples/blob/master/src/test/java/ChromeDevToolsTest.java
 * https://codoid.com/selenium-4-chrome-devtools-log-entry-listeners/
 * etc.
 * NOTE: https://chromedevtools.github.io/devtools-protocol/tot/Console/ says 
 * The Console domain is deprecated - use Runtime or Log instead.
 *
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

public class ChromeDevToolsTest {

	private static ChromiumDriver driver;
	private static String osName = Utils.getOSName();
	private static DevTools chromeDevTools;

	private static String baseURL = "https://apache.org";

	private final static int id = (int) (java.lang.Math.random() * 1_000_000);
	public final static String consoleMessage = "message from test id #" + id;

	@SuppressWarnings("deprecation")
	@BeforeClass
	public static void setUp() throws Exception {
		System
				.setProperty("webdriver.chrome.driver",
						Paths.get(System.getProperty("user.home"))
								.resolve("Downloads").resolve(osName.equals("windows")
										? "chromedriver.exe" : "chromedriver")
								.toAbsolutePath().toString());

		driver = new ChromeDriver();
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
		chromeDevTools.send(Log.enable());
		// add event listener to show in host console the browser console message
		chromeDevTools.addListener(Log.entryAdded(), System.err::println);
		driver.get(baseURL);
	}

	@AfterClass
	public static void tearDown() {
		if (driver != null) {
			driver.quit();
		}
	}

	@Test
	// https://chromedevtools.github.io/devtools-protocol/tot/Console#event-messageAdded
	// https://chromedevtools.github.io/devtools-protocol/tot/Log#event-entryAdded
	// https://chromedevtools.github.io/devtools-protocol/tot/Log#type-LogEntry
	public void consoleMessageAddTest() {
		// Assert
		// add event listener to verify the console message text
		chromeDevTools.addListener(Log.entryAdded(),
				o -> Assert.assertEquals(true, o.getText().equals(consoleMessage)));

		// Act
		// write console message by executing Javascript
		Utils.executeScript("console.log('" + consoleMessage + "');");
	}

	private static Map<String, String> headers = new HashMap<>();

	@Test
	public void addCustomHeaders() {

		// enable Network
		chromeDevTools.send(
				Network.enable(Optional.empty(), Optional.empty(), Optional.empty()));
		headers = new HashMap<>();
		headers.put("customHeaderName", "customHeaderValue");
		// set custom header
		chromeDevTools.send(Network.setExtraHTTPHeaders(headers));

		// add event listener to verify that requests are sending with the custom
		// header
		chromeDevTools.addListener(Network.requestWillBeSent(),
				requestWillBeSent -> Assert.assertEquals(
						requestWillBeSent.getRequest().getHeaders().get("customHeaderName"),
						"customHeaderValue"));

		driver.get("https://apache.org");

	}

	// https://chromedevtools.github.io/devtools-protocol/tot/Network#method-enable
	// https://chromedevtools.github.io/devtools-protocol/tot/Network/#event-loadingFailed
	@Test
	public void setblockURLTest() {
		chromeDevTools.send(Network.enable(Optional.of(100000000), Optional.empty(),
				Optional.empty()));
		chromeDevTools.send(Network.setBlockedURLs(Arrays.asList(
				"https://blog.testproject.io/wp-content/uploads/2019/10/pop-up-illustration.png")));

		chromeDevTools.addListener(Network.loadingFailed(), e -> {
			assertThat(e.getBlockedReason(), is(BlockedReason.inspector));
		});

		driver.get(
				"https://blog.testproject.io/2019/11/26/next-generation-front-end-testing-using-webdriver-and-devtools-part-1/");
		chromeDevTools.send(Network.disable());
	}
}
