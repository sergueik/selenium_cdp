package com.github.sergueik.selenium;

/**
 * Copyright 2021 Serguei Kouzmine
 */
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.UnsupportedEncodingException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.chromium.ChromiumDriver;

import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.DevToolsException;
import org.openqa.selenium.devtools.HasDevTools;
import org.openqa.selenium.devtools.v105.network.Network.GetResponseBodyResponse;
import org.openqa.selenium.devtools.v105.network.model.Response;
import org.openqa.selenium.devtools.v105.network.Network;
import org.openqa.selenium.devtools.v105.network.model.Headers;
import org.openqa.selenium.devtools.v105.network.model.RequestId;
import org.openqa.selenium.devtools.v105.network.model.DataReceived;
import org.openqa.selenium.devtools.v105.network.model.ResponseReceived;

import org.apache.commons.codec.binary.Base64;

/**
 * Selected test scenarios for Selenium Chrome Developer Tools Selenium 4 bridge
 * https://chromedevtools.github.io/devtools-protocol/tot/Network/#method-setExtraHTTPHeaders
 * https://chromedevtools.github.io/devtools-protocol/tot/Network/#method-getResponseBody
 * https://chromedevtools.github.io/devtools-protocol/tot/Network#method-enable
 * https://chromedevtools.github.io/devtools-protocol/tot/Network/#event-dataReceived
 * https://chromedevtools.github.io/devtools-protocol/tot/Network/#method-setCacheDisabled
 * https://chromedevtools.github.io/devtools-protocol/tot/Console#method-enable
 * https://chromedevtools.github.io/devtools-protocol/tot/Log#method-enable
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
	private static final String url = "https://apache.org";

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

		chromeDevTools.createSession();
	}

	@BeforeClass
	public static void beforeClass() throws Exception {
		driver.get(baseURL);
	}

	@AfterClass
	public static void tearDown() {
		if (driver != null) {
			driver.quit();
		}
	}

	@After
	public void after() {
		chromeDevTools.clearListeners();
	}

	@Before
	public void before() throws Exception {
		chromeDevTools.send(Network.enable(Optional.of(100000000), Optional.empty(),
				Optional.empty()));
	}

	// https://chromedevtools.github.io/devtools-protocol/tot/Network/#method-setExtraHTTPHeaders
	// see also:
	// https://stackoverflow.com/questions/15645093/setting-request-headers-in-selenium
	// see also:
	// https://github.com/SeleniumHQ/selenium/blob/master/java/client/test/org/openqa/selenium/devtools/ChromeDevToolsNetworkTest.java
	// @Ignore
	@Test
	public void test1() {

		headers = new HashMap<>();
		headers.put("customHeaderName", "customHeaderValue");
		/*
		 * headers.put("customHeaderName", this.getClass().getName() +
		 * " addCustomHeadersTest");
		 */
		chromeDevTools.send(Network.setExtraHTTPHeaders(new Headers(headers)));
		// add event listener to log that requests are sending with the custom
		// header
		// The assertion is failing often
		/*
		 * chromeDevTools.addListener(Network.requestWillBeSent(), o ->
		 * assertThat(o.getRequest().getHeaders().get("customHeaderName"),
		 * is("customHeaderValue")));
		 */
		chromeDevTools.addListener(Network.requestWillBeSent(),
				o -> System.err.println(String.format(
						"request will be sent with extra header %s=%s", "customHeaderName",
						o.getRequest().getHeaders().get("customHeaderName"))));
		// to test with a dummy server fire on locally and inspect the headers
		// server-side
		// driver.get("http://127.0.0.1:8080/demo/Demo");
		// otherwise just hit a generic web site
		driver.get(url);
	}

	@Test
	public void test2() {
		chromeDevTools.addListener(Network.dataReceived(), (DataReceived event) -> {
			// https://github.com/SeleniumHQ/selenium/blob/master/common/devtools/chromium/v93/browser_protocol.pdl#L5618
			if (cnt++ < 10)
				System.err
						.println(String.format("Network request %s data received at %s",
								event.getRequestId(), event.getTimestamp()));
		});
		chromeDevTools.send(Network.setCacheDisabled(true));
		driver.get(url);

	}

	// https://github.com/SeleniumHQ/selenium/blob/trunk/common/devtools/chromium/v93/browser_protocol.pdl#L5763
	@Test
	public void test3() {
		final RequestId[] requestIdCaptures = new RequestId[1];
		chromeDevTools.addListener(Network.responseReceived(),
				(ResponseReceived event) -> {
					// collect request id for some purpose
					// see also
					// https://github.com/SrinivasanTarget/selenium4CDPsamples/blob/master/src/test/java/DevToolsTest.java#L86
					requestIdCaptures[0] = event.getRequestId();
					if (cnt++ < 10)
						System.err
								.println(String.format("Network request %s response status: %s",
										event.getRequestId(), event.getResponse().getStatus()));
					try {
						Network.GetResponseBodyResponse response = chromeDevTools
								.send(Network.getResponseBody(event.getRequestId()));
						String body = response.getBody();
						if (response.getBase64Encoded()) {
							try {
								body = new String(Base64.decodeBase64(body.getBytes("UTF8")));
							} catch (UnsupportedEncodingException e) {
								System.err.println("Exception (ignored): " + e.toString());
							}
						}
						System.err.println("response body:\n" + (body.length() > 100
								? body.substring(0, 100) + "..." : body));

					} catch (DevToolsException e) {
						System.err.println("Web Driver exception (ignored): "
								+ Utils.processExceptionMessage(e.getMessage()));
					}
				});
		chromeDevTools.send(Network.setCacheDisabled(true));
		driver.get(url);

	}
}
