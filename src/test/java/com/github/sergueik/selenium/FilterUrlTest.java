package com.github.sergueik.selenium;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.openqa.selenium.devtools.v101.network.Network;
import org.openqa.selenium.devtools.v101.network.model.BlockedReason;
import org.openqa.selenium.devtools.v101.network.model.InterceptionStage;
import org.openqa.selenium.devtools.v101.network.model.LoadingFailed;
import org.openqa.selenium.devtools.v101.network.model.ResourceType;
import org.openqa.selenium.devtools.v101.network.model.ResponseReceived;
import org.openqa.selenium.devtools.v101.page.Page;
import org.openqa.selenium.devtools.v101.network.model.RequestIntercepted;
import org.openqa.selenium.devtools.v101.network.model.RequestPattern;
import org.openqa.selenium.devtools.v101.network.model.RequestWillBeSent;
import static org.openqa.selenium.devtools.v101.network.Network.continueInterceptedRequest;
import com.google.common.collect.ImmutableList;

/**
 * Selected test scenarios for Selenium Chrome Developer Tools Selenium 4 bridge
 * https://chromedevtools.github.io/devtools-protocol/tot/Network/#method-setBlockedURLs
 * https://chromedevtools.github.io/devtools-protocol/tot/Network/#event-loadingFailed
 * https://chromedevtools.github.io/devtools-protocol/tot/Network/#event-requestWillBeSent
 * https://chromedevtools.github.io/devtools-protocol/tot/Network/#event-responseReceived
 * https://chromedevtools.github.io/devtools-protocol/tot/Network/#method-continueInterceptedRequest
 * https://chromedevtools.github.io/devtools-protocol/tot/Network/#type-RequestPattern
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

public class FilterUrlTest extends BaseDevToolsTest {

	@Before
	public void before() throws Exception {
		chromeDevTools.send(Network.enable(Optional.of(100000000), Optional.empty(),
				Optional.empty()));
		baseURL = "http://arngren.net";
	}

	// see also:
	// https://github.com/adiohana/selenium-chrome-devtools-examples/blob/master/src/test/java/ChromeDevToolsTest.java
	@Test
	public void test1() {

		// Arrange
		chromeDevTools.send(Network.setBlockedURLs(
				ImmutableList.of("*.css", "*.png", "*.jpg", "*.gif", "*favicon.ico")));

		// verify that css jpg and png are blocked
		chromeDevTools.addListener(Network.loadingFailed(),
				(LoadingFailed event) -> {
					ResourceType resourceType = event.getType();
					if (resourceType.equals(ResourceType.STYLESHEET)
							|| resourceType.equals(ResourceType.IMAGE)
							|| resourceType.equals(ResourceType.OTHER)) {
						Optional<BlockedReason> blockedReason = event.getBlockedReason();
						assertThat(blockedReason.isPresent(), is(true));
						assertThat(blockedReason.get(), is(BlockedReason.INSPECTOR));
					} else {
						// TODO
					}
					System.err.println("Blocked event: " + event.getType());
				});

		chromeDevTools.addListener(Network.requestWillBeSent(),
				(RequestWillBeSent event) -> {
					System.err.println("Request will be sent to get url: "
							+ event.getRequest().getUrl());

				});
		chromeDevTools.addListener(Network.responseReceived(),
				(ResponseReceived event) -> {
					System.err.println(
							"Response received with url: " + event.getResponse().getUrl());

				}); // Act
		// see also:
		// https://weblium.com/blog/21-bad-website-examples-of-2021/
		driver.get(baseURL);

	}

	// see also:
	// https://github.com/adiohana/selenium-chrome-devtools-examples/blob/master/src/test/java/ChromeDevToolsTest.java#L81
	@SuppressWarnings("deprecation")
	@Test
	public void test2() {

		chromeDevTools.addListener(Network.requestIntercepted(),
				(RequestIntercepted event) -> chromeDevTools
						.send(Network.continueInterceptedRequest(event.getInterceptionId(),
								Optional.empty(), Optional.empty(), Optional.empty(),
								Optional.empty(), Optional.empty(), Optional.empty(),
								Optional.empty())));

		// set request interception only for css requests
		RequestPattern requestPattern = new RequestPattern(Optional.of("*.gif"),
				Optional.of(ResourceType.IMAGE),
				Optional.of(InterceptionStage.HEADERSRECEIVED));
		chromeDevTools
				.send(Network.setRequestInterception(ImmutableList.of(requestPattern)));
		chromeDevTools.send(Page.navigate(baseURL, Optional.empty(),
				Optional.empty(), Optional.empty(), Optional.empty()));
	}

	@After
	public void after() {
		Utils.sleep(1000);
		chromeDevTools.send(Network.setBlockedURLs(new ArrayList<String>()));
		chromeDevTools.send(Network.disable());
		chromeDevTools.clearListeners();
	}
}
