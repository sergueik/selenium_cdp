package com.github.sergueik.selenium;

import static java.lang.System.err;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.codec.binary.Base64;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
// need to use branch cdp_codegen of SeleniumHQ/selenium
// https://github.com/SeleniumHQ/selenium/tree/cdp_codegen/java/client/src/org/openqa/selenium/devtools
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chromium.ChromiumDriver;
import org.openqa.selenium.devtools.DevTools;
//import org.openqa.selenium.devtools.Console;
// import org.openqa.selenium.devtools.Log;
import org.openqa.selenium.devtools.network.Network;
import org.openqa.selenium.devtools.network.model.Headers;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

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

public class ChromeDevToolsTest {

	private static ChromiumDriver driver;
	private static String osName = Utils.getOSName();
	private static DevTools chromeDevTools;

	private static String baseURL = "https://apache.org";
	private static int flexibleWait = 60;
	private static int pollingInterval = 500;

	private final static int id = (int) (java.lang.Math.random() * 1_000_000);
	public final static String consoleMessage = "message from test id #" + id;
	private static Map<String, Object> headers = new HashMap<>();
	private static WebElement element;
	private static WebDriverWait wait;
	private static Actions actions;

	@SuppressWarnings("deprecation")
	@BeforeClass
	public static void setUp() throws Exception {
		System.setProperty("webdriver.chrome.driver", Paths.get(System.getProperty("user.home")).resolve("Downloads")
				.resolve(osName.equals("windows") ? "chromedriver.exe" : "chromedriver").toAbsolutePath().toString());

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

	@Ignore
	@Test
	// https://chromedevtools.github.io/devtools-protocol/tot/Console#event-messageAdded
	// https://chromedevtools.github.io/devtools-protocol/tot/Log#event-entryAdded
	// https://chromedevtools.github.io/devtools-protocol/tot/Log#type-LogEntry
	public void consoleMessageAddTest() {
		// Assert
		// add event listener to verify the console message text
		// chromeDevTools.addListener(Log.entryAdded(), o ->
		// Assert.assertEquals(true,
		// o.getText().equals(consoleMessage)));

		// Act
		// write console message by executing Javascript
		Utils.executeScript("console.log('" + consoleMessage + "');");
	}

	// https://chromedevtools.github.io/devtools-protocol/tot/Network/#method-setExtraHTTPHeaders
	// see also:
	// https://stackoverflow.com/questions/15645093/setting-request-headers-in-selenium
	// see also:
	// https://github.com/SeleniumHQ/selenium/blob/master/java/client/test/org/openqa/selenium/devtools/ChromeDevToolsNetworkTest.java
	@Test
	public void addCustomHeadersTest() {
		// enable Network
		chromeDevTools.send(Network.enable(Optional.empty(), Optional.empty(), Optional.empty()));
		headers = new HashMap<>();
		headers.put("customHeaderName", "customHeaderValue");
		headers.put("customHeaderName", this.getClass().getName() + " addCustomHeadersTest");
		Headers headersData = new Headers(headers);
		chromeDevTools.send(Network.setExtraHTTPHeaders(headersData));
		// add event listener to log that requests are sending with the custom header
		chromeDevTools.addListener(Network.requestWillBeSent(),
				o -> Assert.assertEquals(o.getRequest().getHeaders().get("customHeaderName"), "customHeaderValue"));
		chromeDevTools.addListener(Network.requestWillBeSent(), o -> System.err.println(
				"addCustomHeaders Listener invoked with " + o.getRequest().getHeaders().get("customHeaderName")));
		// to test with a dummy server fire on locally and inspect the headers
		// server-side
		driver.get("http://127.0.0.1:8080/demo/Demo");
		// otherwise just hit a generic web site
		// driver.get("https://apache.org");
	}

	// https://en.wikipedia.org/wiki/Basic_access_authentication
	// https://examples.javacodegeeks.com/core-java/apache/commons/codec/binary/base64-binary/org-apache-commons-codec-binary-base64-example/
	@Test
	public void basicAuthenticationTest() {

		final String username = "guest";
		final String password = "guest";
		try {
			// Arrange
			// enable Network
			chromeDevTools.send(Network.enable(Optional.empty(), Optional.empty(), Optional.empty()));
			driver.get("https://jigsaw.w3.org/HTTP/");
			headers = new HashMap<>();
			headers.put("Authorization", "Basic "
					+ new String((new Base64()).encode(String.format("%s:%s", username, password).getBytes())));
			Headers headersData = new Headers(headers);
			chromeDevTools.send(Network.setExtraHTTPHeaders(headersData));

			// Act
			element = wait.until(ExpectedConditions
					.visibilityOf(driver.findElement(By.cssSelector("table td> a[href=\"Basic/\"]"))));
			element.click();
			wait.until(ExpectedConditions.urlToBe("https://jigsaw.w3.org/HTTP/Basic/"));

			element = driver.findElement(By.tagName("body"));
			assertThat("get past authentication", element.getAttribute("innerHTML"),
					containsString("Your browser made it!"));
			Utils.sleep(1000);
		} catch (WebDriverException e) {
			err.println("WebDriverException (ignored): " + Utils.processExceptionMessage(e.getMessage()));
		} catch (Exception e) {
			err.println("Exception: " + e.toString());
			throw (new RuntimeException(e));
		}
	}

	@Ignore
	// https://chromedevtools.github.io/devtools-protocol/tot/Network#method-enable
	// https://chromedevtools.github.io/devtools-protocol/tot/Network/#event-loadingFailed
	@Test
	public void setblockURLTestf() {
		/*
		 * chromeDevTools.send(Network.enable(Optional.of(100000000), Optional.empty(),
		 * Optional.empty())); chromeDevTools.send(Network.setBlockedURLs(
		 * Arrays.asList(
		 * "https://blog.testproject.io/wp-content/uploads/2019/10/pop-up-illustration.png"
		 * ))); BlockedReason blockReason = BlockedReason.inspector; // Cannot find
		 * symbol // symbol: variable inspector // location: class
		 * org.openqa.selenium.devtools.network.model.BlockedReason
		 * chromeDevTools.addListener(Network.loadingFailed(), e -> {
		 * assertThat(e.getBlockedReason(), is(blockReason)); });
		 * 
		 * driver.get(
		 * "https://blog.testproject.io/2019/11/26/next-generation-front-end-testing-using-webdriver-and-devtools-part-1/"
		 * ); chromeDevTools.send(Network.disable());
		 */
	}
}
