package com.github.sergueik.selenium;

/**
 * Copyright 2021,2024 Serguei Kouzmine
 */

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.devtools.v140.network.Network;
import org.openqa.selenium.devtools.v140.network.model.BlockedReason;
import org.openqa.selenium.devtools.v140.network.model.Headers;
import org.openqa.selenium.devtools.v140.network.model.InterceptionStage;
import org.openqa.selenium.devtools.v140.network.model.LoadingFailed;
import org.openqa.selenium.devtools.v140.network.model.Request;
import org.openqa.selenium.devtools.v140.network.model.RequestId;
import org.openqa.selenium.devtools.v140.network.model.RequestIntercepted;
import org.openqa.selenium.devtools.v140.network.model.RequestPattern;
import org.openqa.selenium.devtools.v140.network.model.RequestWillBeSent;
import org.openqa.selenium.devtools.v140.network.model.ResourceType;
import org.openqa.selenium.devtools.v140.network.model.ResponseReceived;
import org.openqa.selenium.devtools.v140.page.Page;
import org.openqa.selenium.devtools.v140.network.model.AuthChallengeResponse;
import org.openqa.selenium.devtools.v140.network.model.AuthChallengeResponse.Response;

import com.google.common.collect.ImmutableList;

/**
 * Selected test scenarios for Selenium Chrome Developer Tools Selenium 4 bridge
 * https://chromedevtools.github.io/devtools-protocol/tot/Network/#method-
 * setBlockedURLs
 * https://chromedevtools.github.io/devtools-protocol/tot/Network/#event-
 * loadingFailed
 * https://chromedevtools.github.io/devtools-protocol/tot/Network/#event-
 * requestWillBeSent
 * https://chromedevtools.github.io/devtools-protocol/tot/Network/#event-
 * responseReceived
 * https://chromedevtools.github.io/devtools-protocol/tot/Network/#method-
 * continueInterceptedRequest
 * https://chromedevtools.github.io/devtools-protocol/tot/Network/#type-
 * RequestPattern
 * https://chromedevtools.github.io/devtools-protocol/tot/Network/#method-
 * setCacheDisabled
 * https://chromedevtools.github.io/devtools-protocol/tot/Network/#method-
 * clearBrowserCache
 * https://chromedevtools.github.io/devtools-protocol/tot/Network/#method-enable
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

public class FilterUrlDevToolsTest extends BaseDevToolsTest {

	@Before
	public void before() throws Exception {
		chromeDevTools.send(Network.enable(
				Optional.of(100000000),  // maxTotalBufferSize
				Optional.empty(), // maxResourceBufferSize
				Optional.empty(), // maxPostDataSize
				Optional.empty() // reportDirectSocketTraffic
				));
		chromeDevTools.send(Network.clearBrowserCache());
		chromeDevTools.send(Network.setCacheDisabled(true));
		baseURL = "http://arngren.net";
	}

	@After
	public void after() {

		chromeDevTools.send(Network.setBlockedURLs(new ArrayList<String>()));
		chromeDevTools.send(Network.disable());
		chromeDevTools.send(Network.setCacheDisabled(false));
		chromeDevTools.clearListeners();
	}

	private Object monitor = new Object();
	private final int count = 10;

	// see also:
	// https://github.com/adiohana/selenium-chrome-devtools-examples/blob/master/src/test/java/ChromeDevToolsTest.java
	@Test
	public void test1() {
		final Map<String, Map<String, String>> requests = new HashMap<>();
		// Arrange
		chromeDevTools
				.send(Network.setBlockedURLs(ImmutableList.of("*.css", "*.png", "*.jpg", "*.gif", "*favicon.ico")));

		// verify that css jpg and png are blocked
		// see also:
		// https://rahulshettyacademy.com/blog/index.php/2021/11/04/selenium-4-key-feature-network-interception/
		// https://javadoc.io/static/org.seleniumhq.selenium/selenium-devtools-v120/4.18.1/org/openqa/selenium/devtools/v126/network/model/LoadingFailed.html
		// see also:
		// https://weblium.com/blog/21-bad-website-examples-of-2021/
		chromeDevTools.addListener(Network.loadingFailed(), (LoadingFailed event) -> {
			//
			RequestId requestId = event.getRequestId();
			ResourceType resourceType = event.getType();
			if (resourceType.equals(ResourceType.STYLESHEET) || resourceType.equals(ResourceType.IMAGE)
					|| resourceType.equals(ResourceType.OTHER)) {
				Optional<BlockedReason> blockedReason = event.getBlockedReason();
				assertThat(blockedReason.isPresent(), is(true));
				assertThat(blockedReason.get(), is(BlockedReason.INSPECTOR));
			} else {
				System.err.println(String.format("Also Blocked request %s event type: %s", requestId, resourceType));
			}
			String key = requestId.toString();
			Map<String, String> details;
			synchronized (monitor) {
				if (requests.containsKey(key)) {
					details = requests.get(key);
				} else {
					details = new HashMap<>();
				}
				details.put("status", "blocked");
				requests.put(key, details);
			}
			System.err.println(String.format("Blocked request %s on event type: %s", requestId, resourceType));
		});

		chromeDevTools.addListener(Network.requestWillBeSent(), (RequestWillBeSent event) -> {
			Request request = event.getRequest();
			RequestId requestId = event.getRequestId();
			String url = request.getUrl();
			String key = requestId.toString();
			Map<String, String> details;

			synchronized (monitor) {
				if (requests.containsKey(key)) {
					details = requests.get(key);
				} else {
					details = new HashMap<>();
				}
				details.put("url", url);
				requests.put(requestId.toString(), details);
			}
			System.err.println(String.format("Request %s will be sent to get url: %s", requestId, url));

		});
		chromeDevTools.addListener(Network.responseReceived(), (ResponseReceived event) -> {
			System.err.println("Response received with url: " + event.getResponse().getUrl());

		}); // Act
		System.err.println(String.format("blocked urls: (only %d shown) ", count));
		driver.get(baseURL);
		Utils.sleep(1000);
		requests.keySet().stream().limit(count).forEach((String id) -> {
			final Map<String, String> details = requests.get(id);
			String url = details.get("url");
			String status = details.get("status");
			System.err.println(String.format("%s %s", url, status));

		});
		List<WebElement> elements = driver.findElements(By.tagName("img"));
		elements.stream().limit(count).forEach((WebElement element) -> {
			Utils.highlight(element);
			isImageBroken(element);
			Utils.sleep(100);
		});

	}

	// see also:
	// https://github.com/adiohana/selenium-chrome-devtools-examples/blob/master/src/test/java/ChromeDevToolsTest.java#L81
	// see also:
	// https://rahulshettyacademy.com/blog/index.php/2021/11/04/selenium-4-key-feature-network-interception/
	// NOTE: need to print to read the whole document
	@SuppressWarnings("deprecation")
	@Ignore
	@Test
	public void test3() {
		// TODO: java.lang.ClassCastException:
		// java.util.LinkedHashMap cannot be
		// cast to java.lang.Void
		chromeDevTools.addListener(Network.requestIntercepted(),
				(RequestIntercepted event) -> chromeDevTools
						.send(Network.continueInterceptedRequest(event.getInterceptionId(), Optional.empty(),
								Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
								Optional.of(new Headers(new HashMap<String, Object>())), Optional.empty())));

		// set request interception only for css requests
		RequestPattern requestPattern = new RequestPattern(Optional.of("*.gif"), Optional.of(ResourceType.IMAGE),
				Optional.of(InterceptionStage.HEADERSRECEIVED));
		chromeDevTools.send(Network.setRequestInterception(ImmutableList.of(requestPattern)));
		chromeDevTools
				.send(Page.navigate(baseURL, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()));
		Utils.sleep(1000);
	}

	private void isImageBroken(WebElement image) {
		if (image.getAttribute("naturalWidth").equals("0")) {
			System.err.println(image.getAttribute("src") + " is broken.");
		}
	}

	// see also:
	// https://github.com/adiohana/selenium-chrome-devtools-examples/blob/master/src/test/java/ChromeDevToolsTest.java#L81
	// see also:
	// https://rahulshettyacademy.com/blog/index.php/2021/11/04/selenium-4-key-feature-network-interception/
	// NOTE: need to print to read the whole document
	@Ignore
	@SuppressWarnings("deprecation")
	@Test
	public void test2() {
		// TODO: java.lang.ClassCastException:
		// java.util.LinkedHashMap cannot be
		// cast to java.lang.Void

		chromeDevTools.addListener(Network.requestIntercepted(),
				(RequestIntercepted event) -> chromeDevTools.send(Network.continueInterceptedRequest(
						event.getInterceptionId(), Optional.empty(), Optional.empty(), Optional.empty(),
						Optional.empty(), Optional.empty(), Optional.of(new Headers(new HashMap<String, Object>())),
						Optional.of(new AuthChallengeResponse(Response.DEFAULT, Optional.empty(), Optional.empty())))));
		// java.lang.NullPointerException: response is required
		// org.openqa.selenium.devtools.DevToolsException:
		// {"id":38,"error":{"code":-32602,"message":"authChallengeResponse not expected."},"sessionId":"B6AD0AAC5C8C4E26C8CCF82355E7D7A3"}

		// https://javadoc.io/doc/org.seleniumhq.selenium/selenium-devtools-v126/latest/org/openqa/selenium/devtools/v126/network/model/AuthChallengeResponse.html

		// set request interception only for css requests
		RequestPattern requestPattern = new RequestPattern(Optional.of("*.gif"), Optional.of(ResourceType.IMAGE),
				Optional.of(InterceptionStage.HEADERSRECEIVED));
		chromeDevTools.send(Network.setRequestInterception(ImmutableList.of(requestPattern)));
		chromeDevTools
				.send(Page.navigate(baseURL, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()));
		Utils.sleep(1000);
	}

	// incompatible types: inference variable T has incompatible bounds:
	// [ERROR] equality constraints:
	// org.openqa.selenium.devtools.v140.network.model.AuthChallengeResponse
	// [ERROR] lower bounds: java.lang.Object
	/*
	 * @Ignore
	 * 
	 * @SuppressWarnings("deprecation")
	 * 
	 * @Test public void test4() {
	 * 
	 * chromeDevTools .addListener(Network.requestIntercepted(),
	 * (RequestIntercepted event) ->
	 * chromeDevTools.send(Network.continueInterceptedRequest(
	 * event.getInterceptionId(), Optional.empty(), Optional.empty(),
	 * Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(new
	 * Headers(new HashMap<String, Object>())), Optional.of(new Object()))));
	 * 
	 * // set request interception only for css requests RequestPattern
	 * requestPattern = new RequestPattern(Optional.of("*.gif"),
	 * Optional.of(ResourceType.IMAGE),
	 * Optional.of(InterceptionStage.HEADERSRECEIVED));
	 * chromeDevTools.send(Network.setRequestInterception(ImmutableList.of(
	 * requestPattern))); chromeDevTools .send(Page.navigate(baseURL,
	 * Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()));
	 * Utils.sleep(1000); }
	 */
}
