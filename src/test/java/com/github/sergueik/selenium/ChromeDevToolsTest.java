package com.github.sergueik.selenium;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThan;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.IllegalFormatConversionException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
// need to use branch cdp_codegen of SeleniumHQ/selenium
// https://github.com/SeleniumHQ/selenium/tree/cdp_codegen/java/client/src/org/openqa/selenium/devtools
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.chromium.ChromiumDriver;
import org.openqa.selenium.devtools.Command;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.HasDevTools;
import org.openqa.selenium.devtools.v106.browser.Browser;
import org.openqa.selenium.devtools.v106.browser.Browser.GetWindowForTargetResponse;
import org.openqa.selenium.devtools.v106.browser.model.Bounds;
import org.openqa.selenium.devtools.v106.browser.model.WindowID;
import org.openqa.selenium.devtools.v106.input.Input;
import org.openqa.selenium.devtools.v106.input.Input.DispatchKeyEventType;
import org.openqa.selenium.devtools.v106.log.Log;
import org.openqa.selenium.devtools.v106.network.Network;
import org.openqa.selenium.devtools.v106.network.model.Headers;
import org.openqa.selenium.devtools.v106.network.model.RequestId;
import org.openqa.selenium.devtools.v106.overlay.Overlay;
import org.openqa.selenium.devtools.v106.page.Page;
import org.openqa.selenium.devtools.v106.page.model.ScriptIdentifier;
import org.openqa.selenium.devtools.v106.performance.Performance;
import org.openqa.selenium.devtools.v106.performance.model.Metric;
// import org.openqa.selenium.devtools.v106.target.model.SessionID;
import org.openqa.selenium.devtools.idealized.target.model.SessionID;
import org.openqa.selenium.devtools.v106.page.model.FrameTree;
import org.openqa.selenium.devtools.v106.dom.model.RGBA;

import static org.openqa.selenium.devtools.v106.performance.Performance.disable;
import static org.openqa.selenium.devtools.v106.performance.Performance.enable;
import static org.openqa.selenium.devtools.v106.performance.Performance.getMetrics;

import org.openqa.selenium.TimeoutException;

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

	private static boolean runHeadless = false;
	private static String osName = Utils.getOSName();
	private static ChromeDriver driver;
	private static DevTools chromeDevTools;

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

	// @Ignore
	@Test
	// https://chromedevtools.github.io/devtools-protocol/tot/Console#event-messageAdded
	// https://chromedevtools.github.io/devtools-protocol/tot/Log#event-entryAdded
	// https://chromedevtools.github.io/devtools-protocol/tot/Log#type-LogEntry
	public void consoleMessageAddTest() {
		// Assert
		final String consoleMessage = "message from the test";

		// add event listener to verify the console message text
		chromeDevTools.addListener(Log.entryAdded(),
				o -> Assert.assertEquals(true, o.getText().equals(consoleMessage)));

		// Act
		// write console message by executing Javascript
		Utils.executeScript("console.log('" + consoleMessage + "');");
		System.err
				.println("Successfully captured console log messge: " + consoleMessage);
	}

	@Test
	// https://chromedevtools.github.io/devtools-protocol/tot/Browser#method-getWindowForTarget
	public void broserGetWindowBoundsTest() {
		GetWindowForTargetResponse response = chromeDevTools
				.send(Browser.getWindowForTarget(Optional.empty()));
		WindowID windowId = response.getWindowId();
		Bounds bounds = response.getBounds();
		System.err.println(String.format(
				"Method Browser.getWindowForTarget result: windowId: %d"
						+ "\nBounds: top: %d, left: %d, width: %d, height: %d",
				Long.parseLong(windowId.toString()), bounds.getLeft().get(),
				bounds.getTop().get(), bounds.getWidth().get(),
				bounds.getHeight().get()));
		@SuppressWarnings("unused")
		Optional<WindowID> windowIdArg = Optional.of(windowId);
		try {
			bounds = chromeDevTools.send(Browser.getWindowBounds(windowId));
			chromeDevTools.createSessionIfThereIsNotOne();
			@SuppressWarnings("unused")
			SessionID id = chromeDevTools.getCdpSession();
		} catch (TimeoutException e) {
			System.err.println("Exception (ignored): " + e.toString());
			bounds = null;
		}
		// https://github.com/SeleniumHQ/selenium/issues/7369
		chromeDevTools.send(
				Network.enable(Optional.empty(), Optional.empty(), Optional.empty()));
		// https://github.com/SeleniumHQ/selenium/blob/master/java/client/src/org/openqa/selenium/devtools/DevTools.java
		// https://github.com/SeleniumHQ/selenium/blob/master/java/client/test/org/openqa/selenium/devtools/ChromeDevToolsNetworkTest.java
		// but Browser has no events, Network has
		chromeDevTools.addListener(Network.dataReceived(), o -> {
			Assert.assertNotNull(o.getRequestId());
			// TODO: Command<GetResponseBodyResponse> - get something practical
			System.err.println("Response body: "
					+ Network.getResponseBody(o.getRequestId()).getMethod());
		});
		driver.get("https://apache.org");
		if (bounds != null) {
			System.err.print(String.format("Method Browser.getWindowBounds(%d): ",
					Long.parseLong(windowId.toString())));
			if (bounds.getTop().isPresent()) {
				System.err
						.println(String.format("top: %d, left: %d, width: %d, height: %d",
								bounds.getTop().get(), bounds.getLeft().get(),
								bounds.getWidth().get(), bounds.getHeight().get()));

			} else {
				System.err.println("undefined");
			}
		} else {
			System.err.println("Method Browser.getWindowBounds failed");
		}
	}

	// https://stackoverflow.com/questions/60409219/how-do-you-disable-navigator-webdriver-in-chromedriver
	// https://intoli.com/blog/not-possible-to-block-chrome-headless/chrome-headless-test.js
	// https://chromedevtools.github.io/devtools-protocol/tot/Page/#method-addScriptToEvaluateOnNewDocument
	// https://chromedevtools.github.io/devtools-protocol/tot/Page/#type-ScriptIdentifier
	@Test
	public void scriptOnNewDocumentTest() {
		// Arrange
		final String script = "Object.defineProperty(navigator, 'webdriver', { get: () => undefined });";
		ScriptIdentifier response = chromeDevTools
				.send(Page.addScriptToEvaluateOnNewDocument(script, Optional.empty(),
						Optional.empty()));
		System.err.println(String.format(
				"Method Page.addScriptToEvaluateOnNewDocument result: %s", response));

		// chromeDevTools.send(Debugger.getScriptSource((ScriptId) response));
		// Cannot cast from ScriptIdentifier to ScriptId
		driver.get(
				"https://intoli.com/blog/not-possible-to-block-chrome-headless/chrome-headless-test.html");
		Utils.sleep(100);
		chromeDevTools.send(Page.removeScriptToEvaluateOnNewDocument(response));
	}

	// https://chromedevtools.github.io/devtools-protocol/tot/Network/#method-setExtraHTTPHeaders
	// see also:
	// https://stackoverflow.com/questions/15645093/setting-request-headers-in-selenium
	// see also:
	// https://github.com/SeleniumHQ/selenium/blob/master/java/client/test/org/openqa/selenium/devtools/ChromeDevToolsNetworkTest.java
	@Ignore
	@Test
	public void addCustomHeadersTest() {
		// enable Network
		chromeDevTools.send(
				Network.enable(Optional.empty(), Optional.empty(), Optional.empty()));
		headers = new HashMap<>();
		headers.put("customHeaderName", "customHeaderValue");
		headers.put("customHeaderName",
				this.getClass().getName() + " addCustomHeadersTest");
		Headers headersData = new Headers(headers);
		chromeDevTools.send(Network.setExtraHTTPHeaders(headersData));
		// add event listener to log that requests are sending with the custom
		// header
		chromeDevTools.addListener(Network.requestWillBeSent(),
				o -> Assert.assertEquals(
						o.getRequest().getHeaders().get("customHeaderName"),
						"customHeaderValue"));
		chromeDevTools.addListener(Network.requestWillBeSent(),
				o -> System.err.println("addCustomHeaders Listener invoked with "
						+ o.getRequest().getHeaders().get("customHeaderName")));
		// to test with a dummy server fire on locally and inspect the headers
		// server-side
		// driver.get("http://127.0.0.1:8080/demo/Demo");
		// otherwise just hit a generic web site
		driver.get("https://apache.org");
	}

	// origin:
	// https://github.com/SeleniumHQ/selenium/blob/cdp_codegen/java/client/test/org/openqa/selenium/devtools/ChromeDevToolsPerformanceTest.java#L72

	// https://chromedevtools.github.io/devtools-protocol/tot/Performance/#method-setTimeDomain
	// https://chromedevtools.github.io/devtools-protocol/tot/Performance/#method-disable
	// https://chromedevtools.github.io/devtools-protocol/tot/Performance/#method-getMetrics
	// https://chromedevtools.github.io/devtools-protocol/tot/Performance/#method-enable
	// NOTE: test failing sporadically with TimeoutException
	// stable when run alone
	@SuppressWarnings("deprecation")
	@Test
	public void getMetricsTest() {

		chromeDevTools.send(disable());
		chromeDevTools.send(Performance
				.setTimeDomain(Performance.SetTimeDomainTimeDomain.THREADTICKS));

		// Selenium 4.0.0-alpha-6 change
		// method enable in class
		// org.openqa.selenium.devtools.performance.Performance cannot be applied to
		// given types;
		// required:
		// java.util.Optional<org.openqa.selenium.devtools.performance.Performance.EnableTimeDomain>
		// found: no arguments
		// reason: actual and formal argument lists differ in length
		chromeDevTools.send(enable(Optional.empty()));
		driver.get("https://www.wikipedia.org");
		List<Metric> metrics = chromeDevTools.send(getMetrics());
		Assert.assertFalse(metrics.isEmpty());
		assertThat(metrics, notNullValue());
		metrics.stream()
				.forEach(o -> System.err.println(o.getName() + " " + o.getValue()));
		chromeDevTools.send(disable());
		List<String> metricNames = metrics.stream().map(o -> o.getName())
				.collect(Collectors.toList());
		System.err.println("Verifying: " + metricNames);

		List<String> expectedMetricNames = Arrays
				.asList(new String[] { "Timestamp", "Documents", "Frames",
						"JSEventListeners", "LayoutObjects", "MediaKeySessions", "Nodes",
						"Resources", "DomContentLoaded", "NavigationStart" });
		// some keys
		expectedMetricNames.forEach(o -> assertThat("Verify: " + o,
				metricNames.indexOf(o), is(greaterThan(-1))));
		chromeDevTools.send(disable());
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

	@Ignore
	@Test
	// https://chromedevtools.github.io/devtools-protocol/1-2/Input/#method-dispatchKeyEvent
	public void zoomTest() {
		// Assert
		driver.get("https://ya.ru");
		// Act
		for (int cnt = 0; cnt != 3; cnt++) {
			chromeDevTools.send(Input.dispatchKeyEvent(DispatchKeyEventType.KEYDOWN,
					Optional.of(4), Optional.empty(), Optional.of("-"), Optional.empty(),
					Optional.empty(), Optional.empty(), Optional.empty(),
					Optional.empty(), Optional.empty(), Optional.empty(),
					Optional.empty(), Optional.empty(), Optional.empty(),
					Optional.empty()));

			chromeDevTools.send(Input.dispatchKeyEvent(DispatchKeyEventType.KEYUP,
					Optional.of(0), Optional.empty(), Optional.empty(), Optional.empty(),
					Optional.empty(), Optional.empty(), Optional.empty(),
					Optional.empty(), Optional.empty(), Optional.empty(),
					Optional.empty(), Optional.empty(), Optional.empty(),
					Optional.empty()));

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		}
	}
}
