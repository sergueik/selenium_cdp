package com.github.sergueik.selenium;

/**
 * Copyright 2023,2024 Serguei Kouzmine
 */


import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Copyright 2023 Serguei Kouzmine
 */

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.codec.binary.Base64;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.devtools.v138.network.Network;
import org.openqa.selenium.devtools.v138.network.model.ResourceType;
import org.openqa.selenium.devtools.v138.network.model.ResponseReceived;
import org.openqa.selenium.interactions.Actions;

import com.google.gson.Gson;

/**
 * Selected test scenarios for Selenium Chrome Developer Tools Selenium 4 bridge
 * see:
 * https://chromedevtools.github.io/devtools-protocol/tot/Network/#method-getResponseBody
 * https://chromedevtools.github.io/devtools-protocol/tot/Network#method-enable
 * https://chromedevtools.github.io/devtools-protocol/tot/Network/#event-responseReceived
 * 
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */
// see:
// https://github.com/Vipinvwarrier/selenium_cdp_profiler/blob/main/selenium_cdp_profiler/cdp/network_profiler.py

public class NetworkResponseBodyTest extends BaseDevToolsTest {

	public Actions actions;
	private final static String url = "https://en.wikipedia.org/wiki/XMLHttpRequest";
	private final int count = 5;

	@Before
	public void beforeTest() throws Exception {

		chromeDevTools.send(Network.enable(Optional.of(100000000), Optional.empty(),
				Optional.empty()));
		chromeDevTools.send(Network.clearBrowserCache());
		chromeDevTools.send(Network.setCacheDisabled(true));

	}

	@After
	public void afterTest() throws Exception {
		chromeDevTools.clearListeners();
		chromeDevTools.send(Network.disable());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void test() {
		// Arrange
		final Gson gson = new Gson();
		try {
			chromeDevTools.addListener(Network.responseReceived(),
					(ResponseReceived event) -> {
						ResourceType resourceType = event.getType();
						String body = null;
						Map<String, Object> data = new HashMap<>();
						System.err.println("Response type: " + resourceType.name());

						Network.GetResponseBodyResponse responseBody = chromeDevTools
								.send(Network.getResponseBody(event.getRequestId()));
						if (responseBody.getBase64Encoded()) {
							try {
								body = new String(Base64
										.decodeBase64(responseBody.getBody().getBytes("UTF8")));
							} catch (UnsupportedEncodingException e) {
								System.err.println("Exception (ignored): " + e.toString());
							}
						} else {
							body = responseBody.getBody();
						}
						if (resourceType.name().equalsIgnoreCase("FETCH")) {
							data = (Map<String, Object>) new Gson().fromJson(body, Map.class);
							assertThat(data, notNullValue());
							System.err.println("FETCH request response JSON keys: "
									+ Arrays.asList(data.keySet()));
						}
						if (resourceType.name().equalsIgnoreCase("SCRIPT")) {
							if (body.length() > 100) {
								System.err.println(
										"SCRIPT request response: " + body.substring(0, 100)
												+ "... " + "(" + body.length() + " chars)");
							} else {
								System.err.println("SCRIPT request response: " + body);
							}
						}
					});

			// Act
			// hover the links in the main wikipedia document
			driver.get(url);
			Utils.sleep(1000);
			List<WebElement> elements = driver.findElement(By.id("mw-content-text"))
					.findElements(By.tagName("a"));
			actions = new Actions(driver);
			elements.stream().limit(count).forEach((WebElement element) -> {
				actions.moveToElement(element).build().perform();
				Utils.sleep(1000);
			});
		} catch (WebDriverException e) {
			System.err.println("Web Driver exception (ignored): "
					+ Utils.processExceptionMessage(e.getMessage()));
		} catch (Exception e) {
			System.err.println("Exception: " + e.toString());
			throw (new RuntimeException(e));
		}
	}

}
