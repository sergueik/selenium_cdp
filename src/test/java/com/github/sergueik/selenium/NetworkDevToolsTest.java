package com.github.sergueik.selenium;

/**
 * Copyright 2020-2024 Serguei Kouzmine
 */

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;

import java.io.UnsupportedEncodingException;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.codec.binary.Base64;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.chromium.ChromiumDriver;

import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.DevToolsException;
import org.openqa.selenium.devtools.HasDevTools;
import org.openqa.selenium.devtools.v125.network.Network;
import org.openqa.selenium.devtools.v125.network.model.DataReceived;
import org.openqa.selenium.devtools.v125.network.model.Headers;
import org.openqa.selenium.devtools.v125.network.model.RequestId;
import org.openqa.selenium.devtools.v125.network.model.RequestWillBeSent;
import org.openqa.selenium.devtools.v125.network.model.ResourceTiming;
import org.openqa.selenium.devtools.v125.network.model.Response;
import org.openqa.selenium.devtools.v125.network.model.ResponseReceived;

/**
 * Selected test scenarios for Selenium Chrome Developer Tools Selenium 4 bridge
 * https://chromedevtools.github.io/devtools-protocol/tot/Network/#method-setExtraHTTPHeaders
 * https://chromedevtools.github.io/devtools-protocol/tot/Network/#method-getResponseBody
 * https://chromedevtools.github.io/devtools-protocol/tot/Network#method-enable
 * https://chromedevtools.github.io/devtools-protocol/tot/Network/#event-dataReceived
 * https://chromedevtools.github.io/devtools-protocol/tot/Network/#method-setCacheDisabled
 * https://chromedevtools.github.io/devtools-protocol/tot/Console#method-enable
 * https://chromedevtools.github.io/devtools-protocol/tot/Log#method-enable
 * https://chromedevtools.github.io/devtools-protocol/tot/Network/#event-responseReceived
 * https://chromedevtools.github.io/devtools-protocol/tot/Network/#type-ResourceType
 * https://chromedevtools.github.io/devtools-protocol/tot/Network/#type-Response
 * https://chromedevtools.github.io/devtools-protocol/tot/Network/#type-ResourceTiming
 * 
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
	private static String url = "https://apache.org";

	private static Map<String, Object> headers = new HashMap<>();

	private static WebElement element = null;

	private static WebDriverWait wait;
	private static int flexibleWait = 60;
	private static int pollingInterval = 500;

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
		wait = new WebDriverWait(driver, Duration.ofSeconds(flexibleWait));
		wait.pollingEvery(Duration.ofMillis(pollingInterval));
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
		chromeDevTools.send(Network.clearBrowserCache());
		chromeDevTools.send(Network.setCacheDisabled(true));
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
				(RequestWillBeSent event) -> System.err.println(String.format(
						"request will be sent with extra header %s=%s", "customHeaderName",
						event.getRequest().getHeaders().get("customHeaderName"))));
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
		driver.get(url);
	}

	// https://stackoverflow.com/questions/6509628/how-to-get-http-response-code-using-selenium-webdriver
	// python has seleniumwire
	// https://github.com/wkeeling/selenium-wire#waiting-for-a-request
	// module that extends Selenium's Python bindings to give you the ability to
	// inspect requests made by the browser
	@Test
	public void test3() {
		chromeDevTools.addListener(Network.responseReceived(),
				(ResponseReceived event) -> {
					Response response = event.getResponse();
					System.err.println(String.format(
							"Network request of %s status is %s Headers: %s",
							response.getUrl(), response.getStatus(), response.getHeaders()));
				});

		driver.get("http://httpbin.org/basic-auth/guest/wrong_password");
		driver.getCurrentUrl();
		// 401 - Unauthorized
		driver.get("http://httpbin.org/status/403");
		// 403 - Forbidden
	}

	// based on:
	// https://software-testing.ru/library/testing/testing-tools/4066-selenium-4
	// (in Russian)
	@Test
	public void test5() {
		url = "https://www.wikipedia.org/";
		Map<String, Map<String, Object>> capturedRequests = new HashMap<>();
		chromeDevTools.send(
				Network.enable(Optional.empty(), Optional.empty(), Optional.empty()));
		chromeDevTools.addListener(Network.requestWillBeSent(),
				(RequestWillBeSent event) -> {
					capturedRequests.put(event.getRequest().getUrl(),
							event.getRequest().getHeaders().toJson());
				});
		driver.get(url);
		element = wait.until(ExpectedConditions.visibilityOfElementLocated(
				By.xpath("//*[contains(@title, 'Deutsch')]")));

		assertThat(element, notNullValue());
		assertThat(element.isDisplayed(), is(true));
		Utils.highlight(element);
		Utils.sleep(1000);
		element.click();
		Utils.sleep(1000);
		assertThat(capturedRequests.size(), greaterThan(1));
		capturedRequests.keySet().stream().forEach(System.err::println);
		assertThat(capturedRequests.keySet(),
				hasItems(new String[] { "https://www.wikipedia.org/",
						"https://www.wikipedia.org/static/favicon/wikipedia.ico" }));
		// this is what is shown in the browser address bar
		String script = "const currentUrl = window.location.href; console.log(currentUrl); return currentUrl;";
		String url2 = (String) driver.executeScript(script);
		assertThat(capturedRequests.containsKey(url2), is(true));

	}

	@Test
	public void test6() {
		chromeDevTools.addListener(Network.requestServedFromCache(),
				(RequestId event) -> {
					System.err.println(String.format(
							"Network request will be served fom cache: %s", event.toJson()));
				});

		driver.get("http://httpbin.org/basic-auth/guest/wrong_password");
		// 401 - Unauthorized
		driver.get("http://httpbin.org/status/403");
		// 403 - Forbidden
	}

	// https://github.com/SeleniumHQ/selenium/blob/trunk/common/devtools/chromium/v93/browser_protocol.pdl#L5763
	@Test
	public void test4() {
		final RequestId[] requestIdCaptures = new RequestId[1];
		chromeDevTools.addListener(Network.responseReceived(),
				(ResponseReceived event) -> {

					Response response = event.getResponse();
					// System.err.println(response.getStatus());
					if ((response.getStatus().toString().startsWith("4"))
							|| response.getStatus().compareTo(400) > 0) {
						System.err.println(response.getUrl()
								+ " has failed with status code " + response.getStatus());
					}

					// collect request id for some purpose
					// see also
					// https://github.com/SrinivasanTarget/selenium4CDPsamples/blob/master/src/test/java/DevToolsTest.java#L86
					requestIdCaptures[0] = event.getRequestId();
					if (cnt++ < 10)
						System.err
								.println(String.format("Network request %s response status: %s",
										event.getRequestId(), event.getResponse().getStatus()));
					try {
						Network.GetResponseBodyResponse responseBody = chromeDevTools
								.send(Network.getResponseBody(event.getRequestId()));
						String body = responseBody.getBody();
						if (responseBody.getBase64Encoded()) {
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
		driver.get(url);

	}

	// evaluate timing of requests made by the browser
	// https://github.com/Vipinvwarrier/selenium_cdp_profiler/blob/main/selenium_cdp_profiler/cdp/network_profiler.py
	@Test
	public void test7() {
		chromeDevTools.addListener(Network.responseReceived(),
				(ResponseReceived event) -> {
					Response response = event.getResponse();
					Optional<ResourceTiming> responseTiming = response.getTiming();
					if (responseTiming.isPresent()) {
						ResourceTiming timing = responseTiming.get();
						Number requestTime = timing.getRequestTime();
						// baseline in seconds
						Number sendStart = timing.getSendStart();
						// Started sending request
						Number receiveHeadersEnd = timing.getReceiveHeadersEnd();
						// finished receiving response headers
						System.err.println(String.format(
								"Network request of %s timing is %8.2f", response.getUrl(),
								receiveHeadersEnd.doubleValue() - sendStart.doubleValue()));
					} else {
						System.err.println(
								String.format("Network request of %s has no timing information",
										response.getUrl()));

					}

				});

		driver.get("https://fakeresponder.com?sleep=500");
		driver.get("https://fakeresponder.com?sleep=1500");
	}

}
