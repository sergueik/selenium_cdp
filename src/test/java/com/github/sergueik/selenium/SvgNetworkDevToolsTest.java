package com.github.sergueik.selenium;

/**
 * Copyright 2026 Serguei Kouzmine
 */

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;

import java.io.UnsupportedEncodingException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.codec.binary.Base64;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.devtools.DevToolsException;

import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.DevToolsException;
import org.openqa.selenium.devtools.HasDevTools;
import org.openqa.selenium.devtools.v153.network.Network;
import org.openqa.selenium.devtools.v153.network.model.DataReceived;
import org.openqa.selenium.devtools.v153.network.model.Headers;
import org.openqa.selenium.devtools.v153.network.model.RequestId;
import org.openqa.selenium.devtools.v153.network.model.RequestWillBeSent;
import org.openqa.selenium.devtools.v153.network.model.ResourceTiming;
import org.openqa.selenium.devtools.v153.network.model.ResourceType;
import org.openqa.selenium.devtools.v153.network.model.Response;
import org.openqa.selenium.devtools.v153.network.model.ResponseReceived;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.google.gson.Gson;

/**
 * Selected test scenarios for Selenium Chrome Developer Tools Selenium 4 bridge
 * see:
 * https://chromedevtools.github.io/devtools-protocol/#/Network.getResponseBody
 * https://chromedevtools.github.io/devtools-protocol/#/Network.disable
 * https://chromedevtools.github.io/devtools-protocol/#/Network.enable
 * https://chromedevtools.github.io/devtools-protocol/#/Network.clearBrowserCache
 * https://chromedevtools.github.io/devtools-protocol/#/Network.responseReceived
 * https://chromedevtools.github.io/devtools-protocol/#/Network.setCacheDisabled
 * 
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */
public class SvgNetworkDevToolsTest extends BaseDevToolsTest {
	private static Map<String, Object> headers = new HashMap<>();

	private static WebElement element = null;

	private static WebDriverWait wait;
	private static int flexibleWait = 60;
	private static int pollingInterval = 500;
	private static boolean getBody = false;
	private Map<String, Object> data = new HashMap<>();
	private final static String page = "svg_test2.html"; 

	// NOTE: multiple defined
	/*
	 * @BeforeClass public static void setUp() throws Exception { }
	 */
	@Test
	public void test1() {
		wait = new WebDriverWait(driver, Duration.ofSeconds(flexibleWait));
		wait.pollingEvery(Duration.ofMillis(pollingInterval));
		Utils.setDriver(driver);

		chromeDevTools = ((HasDevTools) driver).getDevTools();

		// @formatter:off
		chromeDevTools.send(Network.enable(Optional.of(100000000), // maxPostDataSize
				Optional.empty(), // enableDurableMessages
				Optional.empty(), // maxResourceBufferSize
				Optional.empty(), // maxTotalBufferSize
				Optional.empty() // reportDirectSocketTraffic
		));
		// @formatter:on
		chromeDevTools.send(Network.clearBrowserCache());
		chromeDevTools.send(Network.setCacheDisabled(true));

		chromeDevTools.addListener(Network.requestWillBeSent(), (RequestWillBeSent event) -> {
			System.err.println(
					String.format("Request type: %s url: \"%s\"", event.getType(), event.getRequest().getUrl()));
		});

		chromeDevTools.addListener(Network.responseReceived(), (ResponseReceived event) -> {
			Response response = event.getResponse();

			System.err.println(String.format("Response status: %s type: %s mime: %s url: \"%s\"", response.getStatus(),
					event.getType(), response.getMimeType(), response.getUrl()));

		});

		if (getBody) {
			chromeDevTools.addListener(Network.responseReceived(), (ResponseReceived event) -> {
				ResourceType responseReceivedType = event.getType();
				String body = null;
				data.clear();
				System.err.println("Response Received Type: " + responseReceivedType.name());

				Network.GetResponseBodyResponse responseBody = chromeDevTools
						.send(Network.getResponseBody(event.getRequestId()));
				if (responseBody.getBase64Encoded()) {
					try {
						body = new String(Base64.decodeBase64(responseBody.getBody().getBytes("UTF8")));
					} catch (UnsupportedEncodingException e) {
						System.err.println("Exception (ignored): " + e.toString());
					}
				} else {
					body = responseBody.getBody();
				}
				if (responseReceivedType.name().equalsIgnoreCase("FETCH")) {
					data = (Map<String, Object>) new Gson().fromJson(body, Map.class);
					assertThat(data, notNullValue());
					System.err.println("FETCH request response JSON keys: " + Arrays.asList(data.keySet()));
				}
				if (responseReceivedType.name().equalsIgnoreCase("SCRIPT")) {
					if (body.length() > 100) {
						System.err.println("SCRIPT request response: " + body.substring(0, 100) + "... " + "("
								+ body.length() + " chars)");
					} else {
						System.err.println("SCRIPT request response: " + body);
					}
				}
			});
		}
		driver.get(Utils.getPageContent(page));

		element = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("diagram")));
		Utils.sleep(1000);
	}

	@After
	public void afterTest() throws Exception {
		chromeDevTools.clearListeners();
		chromeDevTools.send(Network.disable());
	}

}
