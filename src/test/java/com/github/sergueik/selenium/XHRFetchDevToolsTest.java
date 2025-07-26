package com.github.sergueik.selenium;

/**
 * Copyright 2021,2023,2024 Serguei Kouzmine
 */

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.codec.binary.Base64;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import org.openqa.selenium.devtools.DevToolsException;
import org.openqa.selenium.devtools.v134.fetch.Fetch;
import org.openqa.selenium.devtools.v134.fetch.model.HeaderEntry;
import org.openqa.selenium.devtools.v134.fetch.model.RequestPattern;
import org.openqa.selenium.devtools.v134.fetch.model.RequestPaused;
import org.openqa.selenium.devtools.v134.fetch.model.RequestStage;
import org.openqa.selenium.devtools.v134.network.model.ResourceType;

import com.google.gson.Gson;

/**
 * Selected test scenarios for Selenium Chrome Developer Tools Selenium 4 bridge
 * see:
 * https://chromedevtools.github.io/devtools-protocol/tot/Fetch/#method-enable
 * https://chromedevtools.github.io/devtools-protocol/tot/Fetch/#event-requestPaused
 * https://chromedevtools.github.io/devtools-protocol/tot/Fetch/#method-getResponseBody
 * https://chromedevtools.github.io/devtools-protocol/tot/Fetch/#method-continueResponse
 * https://chromedevtools.github.io/devtools-protocol/tot/Fetch/#method-disable
 * 
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */
// based on:
// https://github.com/rookieInTraining/selenium-cdp-examples/blob/main/src/test/java/com/rookieintraining/cdp/examples/Fetch.java
// see also:
// https://groups.google.com/g/selenium-users/c/OueDjaEqp2U
//
// https://automated-testing.info/t/rabota-s-trafikom-fetch-xhr-pri-pomoshhi-selenium-4-webdriver-primer-vnutri/25503
// (same code, discussion in Russian)
// (in c# 5.x)
public class XHRFetchDevToolsTest extends BaseDevToolsTest {

	public Actions actions;
	private final static String url = "https://en.wikipedia.org/wiki/XMLHttpRequest";
	private final int count = 5;

	@Before
	public void beforeTest() throws Exception {

		List<RequestPattern> reqPattern = new ArrayList<>();
		RequestPattern xhrReqPattern = new RequestPattern(Optional.of("*"),
				Optional.of(ResourceType.XHR), Optional.of(RequestStage.RESPONSE));

		reqPattern.add(xhrReqPattern);
		chromeDevTools
				.send(Fetch.enable(Optional.of(reqPattern), Optional.of(false)));
	}

	@After
	public void afterTest() throws Exception {
		chromeDevTools.clearListeners();
		chromeDevTools.send(Fetch.disable());
	}

	@Test
	public void test() {
		// Arrange
		final Gson gson = new Gson();
		try {
			chromeDevTools.addListener(Fetch.requestPaused(),
					(RequestPaused event) -> {
						try {

							List<HeaderEntry> headerEntries = event.getResponseHeaders()
									.isPresent() ? event.getResponseHeaders().get()
											: new ArrayList<>();

							List<String> headers = headerEntries.stream()
									.map((HeaderEntry entry) -> String.format("%s: %s",
											entry.getName(), entry.getValue()))
									.collect(Collectors.toList());

							System.err.println("in Fetch.requestPaused listener. id:"
									+ event.getRequestId().toString() + "\tURL: "
									+ (event.getRequest().getUrlFragment().isPresent()
											? event.getRequest().getUrlFragment().get() : "none")
									+ "\trequest headers: " + event.getRequest().getHeaders()
									+ "\response status: " + event.getResponseStatusCode().get()
									+ "\tresponse headers: "
									+ (event.getResponseHeaders().isPresent() ? headers : "none")
									+ "\tresource type: " + event.getResourceType());
							// always empty
							// Also possible to work with headers as a map
							// Map<String, Object> headersJsonMap = event.getRequest()
							// .getHeaders().toJson();
							event.getRequest().getPostData().ifPresent((String data) -> {
								System.err.println("Post Data:\n" + data + "\n");
							});

							Fetch.GetResponseBodyResponse response = chromeDevTools
									.send(Fetch.getResponseBody(event.getRequestId()));
							String body = null;
							if (response.getBase64Encoded()) {
								try {
									body = new String(
											Base64.decodeBase64(response.getBody().getBytes("UTF8")));
								} catch (UnsupportedEncodingException e) {
									System.err.println("Exception (ignored): " + e.toString());
								}
							} else {
								body = response.getBody();
							}
							System.err.println("response body:\n" + body);
							@SuppressWarnings("unchecked")
							Map<String, Object> payload = (Map<String, Object>) gson
									.fromJson(body, Map.class);
							System.err
									.println("response extract:\n" + payload.get("extract"));
							// this command is returning void
							chromeDevTools.send(Fetch.continueRequest(event.getRequestId(),
									Optional.empty(), Optional.empty(), Optional.empty(),
									Optional.empty(), Optional.empty()));
						} catch (DevToolsException e) {
							System.err.println("Web Driver exception (ignored): "
									+ Utils.processExceptionMessage(e.getMessage()));

							// org.openqa.selenium.devtools.DevToolsException:
							// {"id":6,"error":{"code":-32602,"message":"Invalid
							// InterceptionId."},"sessionId":"4515310FC6FFDECA0705C54441EFD84B"}
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
