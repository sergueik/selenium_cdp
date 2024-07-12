package com.github.sergueik.selenium;

/**
 * Copyright 2021,2024 Serguei Kouzmine
 */

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.devtools.v126.network.Network;
import org.openqa.selenium.devtools.v126.network.model.BlockedReason;
import org.openqa.selenium.devtools.v126.network.model.Headers;
import org.openqa.selenium.devtools.v126.network.model.InterceptionStage;
import org.openqa.selenium.devtools.v126.network.model.LoadingFailed;
import org.openqa.selenium.devtools.v126.network.model.Request;
import org.openqa.selenium.devtools.v126.network.model.RequestId;
import org.openqa.selenium.devtools.v126.network.model.RequestIntercepted;
import org.openqa.selenium.devtools.v126.network.model.RequestPattern;
import org.openqa.selenium.devtools.v126.network.model.RequestWillBeSent;
import org.openqa.selenium.devtools.v126.network.model.ResourceType;
import org.openqa.selenium.devtools.v126.network.model.ResponseReceived;
import org.openqa.selenium.devtools.v126.page.Page;

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
 * 
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

public class FilterUrlDevToolsTest extends BaseDevToolsTest {

	@Before
	public void before() throws Exception {
		chromeDevTools.send(Network.enable(Optional.of(100000000), Optional.empty(), Optional.empty()));
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

	// see also:
	// https://github.com/adiohana/selenium-chrome-devtools-examples/blob/master/src/test/java/ChromeDevToolsTest.java
	@Test
	public void test1() {

		// Arrange
		chromeDevTools.send(Network.setBlockedURLs(
				ImmutableList.of("*.css", "*.png", "*.jpg", "*.gif", "*favicon.ico")));

		// verify that css jpg and png are blocked
		// see also:
		// https://rahulshettyacademy.com/blog/index.php/2021/11/04/selenium-4-key-feature-network-interception/
		// https://javadoc.io/static/org.seleniumhq.selenium/selenium-devtools-v120/4.18.1/org/openqa/selenium/devtools/v126/network/model/LoadingFailed.html
		chromeDevTools.addListener(Network.loadingFailed(),
				(LoadingFailed event) -> {
					//
					RequestId requestId = event.getRequestId();
					ResourceType resourceType = event.getType();
					if (resourceType.equals(ResourceType.STYLESHEET)
							|| resourceType.equals(ResourceType.IMAGE)
							|| resourceType.equals(ResourceType.OTHER)) {
						Optional<BlockedReason> blockedReason = event.getBlockedReason();
						assertThat(blockedReason.isPresent(), is(true));
						assertThat(blockedReason.get(), is(BlockedReason.INSPECTOR));
					} else {
						System.err.println(String.format("Also Blocked request %s event type: %s", requestId, resourceType));
					}
					System.err.println(String.format("Blocked request %s event type: %s", requestId, resourceType));
				});

		chromeDevTools.addListener(Network.requestWillBeSent(),
				(RequestWillBeSent event) -> {
			System.err.println("Request will be sent to get url: "
							+ event.getRequest().getUrl());

				});
		chromeDevTools.addListener(Network.responseReceived(),
				(ResponseReceived event) -> {
			System.err.println( "Response received with url: " + event.getResponse().getUrl());

				}); // Act
		// see also:
		// https://weblium.com/blog/21-bad-website-examples-of-2021/
		driver.get(baseURL);
		Utils.sleep(1000);
	}

	// see also:
	// https://github.com/adiohana/selenium-chrome-devtools-examples/blob/master/src/test/java/ChromeDevToolsTest.java#L81
	// see also:
	// https://rahulshettyacademy.com/blog/index.php/2021/11/04/selenium-4-key-feature-network-interception/
	// NOTE: need to print to read the whole document
	@SuppressWarnings("deprecation")
	@Ignore
	@Test
	public void test2() {
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

}
