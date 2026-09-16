package com.github.sergueik.selenium;

/**
 * Copyright 2021,2024,2025,2026 Serguei Kouzmine
 */

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.devtools.v153.network.Network;
import org.openqa.selenium.devtools.v153.network.model.BlockedReason;
import org.openqa.selenium.devtools.v153.network.model.Headers;
import org.openqa.selenium.devtools.v153.network.model.LoadingFailed;
import org.openqa.selenium.devtools.v153.network.model.Request;
import org.openqa.selenium.devtools.v153.network.model.RequestId;
import org.openqa.selenium.devtools.v153.network.model.RequestWillBeSent;
import org.openqa.selenium.devtools.v153.network.model.BlockPattern;
import org.openqa.selenium.devtools.v153.network.model.ResourceType;
import org.openqa.selenium.devtools.v153.network.model.ResponseReceived;
import org.openqa.selenium.devtools.v153.page.Page;
import org.openqa.selenium.devtools.v153.network.model.AuthChallengeResponse;
import org.openqa.selenium.devtools.v153.network.model.AuthChallengeResponse.Response;
import com.google.common.collect.ImmutableList;

/**
 * Selected test scenarios for Selenium Chrome Developer Tools Selenium 4 bridge
 * https://chromedevtools.github.io/devtools-protocol/tot/Network/#method-setBlockedURLs
 * https://chromedevtools.github.io/devtools-protocol/tot/Network/#event-loadingFailed
 * https://chromedevtools.github.io/devtools-protocol/tot/Network/#event-requestWillBeSent
 * NOTE: gone
 * https://chromedevtools.github.io/devtools-protocol/tot/Network/#event-requestIntercepted
 * https://chromedevtools.github.io/devtools-protocol/tot/Network/#event-responseReceived
 * https://chromedevtools.github.io/devtools-protocol/tot/Network/#method-continueInterceptedRequest
 * https://chromedevtools.github.io/devtools-protocol/tot/Network/#type-RequestPattern
 * https://chromedevtools.github.io/devtools-protocol/tot/Network/#type-BlockPattern
 * https://chromedevtools.github.io/devtools-protocol/tot/Network/#method-setCacheDisabled
 * https://chromedevtools.github.io/devtools-protocol/tot/Network/#method-clearBrowserCache
 * https://chromedevtools.github.io/devtools-protocol/tot/Network/#method-enable
 *
 * see also [Intercepting and Faking Requests with Fetch](https://qaskills.sh/blog/selenium-cdp-add-script-evaluate-guide)
 *
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

public class FilterUrlDevToolsTest extends BaseDevToolsTest {

	private Object monitor = new Object();
	private final int count = 10;

	@Before
	public void before() throws Exception {
		// @formatter:off
		chromeDevTools.send(Network.enable(Optional.of(100000000), // maxTotalBufferSize
				Optional.empty(), // maxResourceBufferSize
				Optional.empty(), // maxPostDataSize
				Optional.empty(), // reportDirectSocketTraffic
				Optional.empty() // enableDurableMessages
		));
		// @formatter:on
		chromeDevTools.send(Network.clearBrowserCache());
		chromeDevTools.send(Network.setCacheDisabled(true));
		baseURL = "http://arngren.net";
	}

	@After
	public void after() {

		// @formatter:off
		chromeDevTools.send(Network.setBlockedURLs(Optional.of(new ArrayList<BlockPattern>()), // urlPatterns
				Optional.of(Collections.emptyList()) // urls
		));
		// @formatter:on
		chromeDevTools.send(Network.disable());
		chromeDevTools.send(Network.setCacheDisabled(false));
		chromeDevTools.clearListeners();
	}

	// see also:
	// https://github.com/adiohana/selenium-chrome-devtools-examples/blob/master/src/test/java/ChromeDevToolsTest.java
	// NOTE: there is CDP model drift vs. Javadoc mismatch
	@Test
	public void test1() {
		final Map<String, Map<String, String>> requests = new HashMap<>();
		List<BlockPattern> blockPatterns = Arrays
				// @formatter:off
				.asList(new String[] {
					"*.css",
					"*.png",
					"*.jpg",
					"*.gif",
					"*favicon.ico"
				}).stream()
				// @formatter:on
				.map((String oldUrlPattern) -> "*://*/" + oldUrlPattern)
				.map((String urlPattern) -> new BlockPattern(urlPattern, true))

				.collect(Collectors.toList());
		// @formatter:off
		chromeDevTools.send(Network.setBlockedURLs(
			Optional.of(blockPatterns), // urlPatterns
			Optional.of(Collections.emptyList()) // urls
		)
		// @formatter:on
		);

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
				assertThat(blockedReason, notNullValue());
				if (blockedReason.isPresent()) {
					assertThat(blockedReason.get(), notNullValue());
					System.err.println(String.format("Blocked Reason: %s", blockedReason.get()));
					assertThat(blockedReason.get(), is(BlockedReason.INSPECTOR));
				}
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
			reportImageIsNotLoaded(element);
			Utils.sleep(100);
		});

	}

	private void reportImageIsNotLoaded(WebElement image) {
		if (image.getAttribute("naturalWidth").equals("0")) {
			System.err.println(String.format("Image \"%s\" is not loaded.", image.getAttribute("src")));
		}
	}

}
