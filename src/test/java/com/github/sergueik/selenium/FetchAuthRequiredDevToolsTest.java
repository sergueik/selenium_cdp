package com.github.sergueik.selenium;

/**
 * Copyright 2023,2024 Serguei Kouzmine
 */

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import org.openqa.selenium.devtools.v141.fetch.Fetch;
import org.openqa.selenium.devtools.v141.fetch.model.HeaderEntry;
import org.openqa.selenium.devtools.v141.fetch.model.AuthChallengeResponse;
import org.openqa.selenium.devtools.v141.fetch.model.AuthChallengeResponse.Response;

import org.openqa.selenium.WebDriverException;

/**
 * Selected test scenarios for Selenium Chrome Developer Tools Selenium 4 bridge
 * https://chromedevtools.github.io/devtools-protocol/tot/Fetch/#method-enable
 * https://chromedevtools.github.io/devtools-protocol/tot/Fetch/#method-disable
 * https://chromedevtools.github.io/devtools-protocol/tot/Fetch/#method-continueRequest
 * https://chromedevtools.github.io/devtools-protocol/tot/Fetch/#method-continueWithAuth
 * https://chromedevtools.github.io/devtools-protocol/tot/Fetch/#type-AuthChallengeResponse
 * https://chromedevtools.github.io/devtools-protocol/tot/Fetch/#type-HeaderEntry
 * https://chromedevtools.github.io/devtools-protocol/tot/Fetch/#type-RequestId
 *  
 * based on:
 * https://stackoverflow.com/questions/50834002/chrome-headless-browser-with-corporate-proxy-authetication-not-working/67321556#67321556
 * see also:
 * https://www.programsbuzz.com/article/selenium-intercept-requests
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

public class FetchAuthRequiredDevToolsTest extends BaseDevToolsTest {

	private static final String username = "user";
	private static final String password = "guest";

	private static String baseURL = String
			.format("http://httpbin.org/basic-auth/%s/%s", username, password);

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
		chromeDevTools.send(Fetch.disable());
	}

	@Before
	public void before() throws UnsupportedEncodingException {
		chromeDevTools.send(Fetch.enable(Optional.empty(), Optional.of(true)));
	}

	@Test
	public void test1() {
		// Fetch.continueResponse(requestId, responseCode, responsePhrase,
		// responseHeaders, binaryResponseHeaders)
		// @formatter:off
		chromeDevTools.addListener(Fetch.requestPaused(),
			requestPaused -> chromeDevTools.send(
				Fetch.continueRequest(
					requestPaused.getRequestId(), // requestId 
					Optional.empty(), // url 
					Optional.empty(), // method
					Optional.empty(), // postData
					Optional.empty(), // headers
					Optional.empty() // interceptResponse
				)
			)
		);

		// @formatter:on
		// @formatter:off
		chromeDevTools.addListener(Fetch.authRequired(),
			authRequired -> chromeDevTools.send(
				Fetch.continueWithAuth(
					authRequired.getRequestId(),
					new AuthChallengeResponse(
						Response.PROVIDECREDENTIALS,
						Optional.of(username), 
						Optional.of(password)
					)
				)
			)
		);
		// @formatter:on
		driver.get(baseURL);
		Utils.sleep(100);
		String pageSource = driver.getPageSource();
		assertThat(pageSource, containsString("\"authenticated\": true"));
		// System.err.println(pageSource);

	}

	@Ignore("hanging")
	@Test
	public void test3() {
		// Fetch.continueResponse(requestId, responseCode, responsePhrase,
		// responseHeaders, binaryResponseHeaders)
		// @formatter:off
		chromeDevTools.addListener(Fetch.requestPaused(),
			requestPaused ->
					{
						List<HeaderEntry> headers = new ArrayList<>();
						headers.add(new HeaderEntry(username, password));
						try {
							 chromeDevTools.send(
									 	Fetch.continueResponse(
										requestPaused.getRequestId(), // requestId 
										requestPaused.getResponseStatusCode(), // responseCode 
										requestPaused.getResponseStatusText(), // responsePhrase
										Optional.of(headers), // responseHeaders
										Optional.empty()
								));
						} catch (WebDriverException e) { 
							// Cannot override only status or headers, both should be provided
								System.err.println(e.getMessage());
						 }
					}
		);
		// @formatter:on
		driver.get(baseURL);
	}
}
